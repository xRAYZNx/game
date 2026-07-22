package com.arabaoyunu.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.arabaoyunu.map.MapDefinition;
import com.arabaoyunu.util.GameLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ArabaOyunu_61_2: Büyük açık dünya GLB haritalarını araç renderer'ından ayırır.
 * - Teker/cam/araç semantiği uygulanmaz.
 * - Harita sadece seçili sürüş haritasıysa çizilir.
 * - Kaliteye göre texture ve parça bütçesi kullanır.
 * - Collision bu renderer'da değil, BaseMap proxy içinde kalır.
 */
public final class StaticMapRenderer {
    private static final String TAG = "StaticMapRenderer";

    private int program;
    private int aPosition;
    private int aNormal;
    private int aTexCoord;
    private int uMvp;
    private int uModel;
    private int uColor;
    private int uTexture;
    private int uHasTexture;
    private int uEmissive;
    private int uBrightness;
    private int uAlphaBoost;

    private GlbModel model;
    private MapDefinition definition;
    private StaticMapBatch[] batches;
    private int[] textureIds;
    private int whiteTexture;
    private boolean ready;
    private String status = "not_created";
    private int qualityLevel = CarVisualConfig.QUALITY_MEDIUM;
    private int maxTextureSize = 768;
    private int uploadedParts;
    private int skippedByBudget;
    private float mapScale = 1f;
    private float groundY;
    private float modelCenterX;
    private float modelCenterZ;
    private float modelMinY;
    private float renderDistance = 220f;

    private final float[] mapMatrix = new float[16];
    private final float[] partWorld = new float[16];
    private final float[] mvp = new float[16];

    public void create(Context context, MapDefinition definition, int quality) {
        this.definition = definition;
        this.qualityLevel = normalizeQuality(quality);
        this.maxTextureSize = textureCapForQuality(this.qualityLevel);
        this.renderDistance = renderDistanceForQuality(this.qualityLevel, definition == null ? 220f : definition.mapHalfSize);
        try {
            if (definition == null || definition.assetPath == null || definition.assetPath.length() == 0) {
                throw new IllegalArgumentException("Harita asset yolu boş");
            }
            createShader();
            whiteTexture = TextureCache.createWhiteTexture();
            model = GlbLoader.loadStatic(context, definition.assetPath);
            configureTransform(definition, model);
            applyUploadBudget();
            uploadTextures();
            uploadMeshes();
            buildBatches();
            model.releaseImageData();
            System.gc();
            ready = uploadedParts > 0 && batches != null && batches.length > 0;
            status = ready
                    ? "ready map=" + definition.displayName
                        + " parts=" + uploadedParts + "/" + model.parts.size()
                        + " skippedBudget=" + skippedByBudget
                        + " batches=" + batches.length
                        + " vertices=" + model.totalVertices
                        + " indices=" + model.totalIndices
                        + " scale=" + mapScale
                        + " texMax=" + maxTextureSize
                    : "failed_no_uploaded_parts";
            GameLog.i(TAG, "A61_5 static harita hazir: " + status);
        } catch (Throwable t) {
            ready = false;
            status = "failed: " + t.getMessage();
            GameLog.e(TAG, "A61_5 static harita yuklenemedi: " + status, t);
            dispose();
        }
    }

    public boolean isReady() {
        return ready && model != null && batches != null && batches.length > 0 && program != 0;
    }

    public String getStatus() {
        return status;
    }

    public boolean render(float[] viewProjection, float focusX, float focusZ, RenderStats stats) {
        if (!isReady() || viewProjection == null) return false;

        buildMapMatrix();
        GLES20.glUseProgram(program);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(uTexture, 0);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        int drawn = 0;
        for (int b = 0; b < batches.length; b++) {
            StaticMapBatch batch = batches[b];
            GlbMaterial mat = getMaterial(batch.materialIndex);
            int textureId = textureFor(mat);
            float hasTexture = textureId == whiteTexture ? 0f : 1f;

            if (mat.alphaBlend || mat.a < 0.995f) {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                GLES20.glDisable(GLES20.GL_BLEND);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform4f(uColor, mat.r, mat.g, mat.b, mat.a);
            GLES20.glUniform1f(uHasTexture, hasTexture);
            GLES20.glUniform3f(uEmissive, mat.emissiveR, mat.emissiveG, mat.emissiveB);
            GLES20.glUniform1f(uBrightness, Math.max(0.72f, mat.brightnessBoost));
            GLES20.glUniform1f(uAlphaBoost, mat.alphaBlend ? 0.86f : 1.0f);

            for (int i = 0; i < batch.partCount; i++) {
                int partIndex = batch.partIndices[i];
                if (partIndex < 0 || partIndex >= model.parts.size()) continue;
                GlbMeshPart part = model.parts.get(partIndex);
                if (part == null || part.skipped || !part.gpuReady || part.indexCount <= 0) continue;
                if (!isPartVisibleNearFocus(part, focusX, focusZ)) continue;

                Matrix.multiplyMM(partWorld, 0, mapMatrix, 0, part.localMatrix, 0);
                Matrix.multiplyMM(mvp, 0, viewProjection, 0, partWorld, 0);
                GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
                GLES20.glUniformMatrix4fv(uModel, 1, false, partWorld, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, part.vertexBufferId);
                GLES20.glEnableVertexAttribArray(aPosition);
                GLES20.glEnableVertexAttribArray(aNormal);
                GLES20.glEnableVertexAttribArray(aTexCoord);
                GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, GlbMeshPart.STRIDE_BYTES, 0);
                GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, GlbMeshPart.STRIDE_BYTES, 3 * 4);
                GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, GlbMeshPart.STRIDE_BYTES, 6 * 4);

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, part.indexBufferId);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, part.indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

                int glError = GLES20.glGetError();
                if (glError != GLES20.GL_NO_ERROR) {
                    part.skipped = true;
                    part.error = "A61_5 static draw GL error=" + glError;
                    continue;
                }
                drawn++;
                if (stats != null) {
                    stats.drawCalls++;
                    stats.renderedObjects++;
                }
            }
        }

        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aNormal);
        GLES20.glDisableVertexAttribArray(aTexCoord);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        return drawn > 0;
    }

    private boolean isPartVisibleNearFocus(GlbMeshPart part, float focusX, float focusZ) {
        if (qualityLevel == CarVisualConfig.QUALITY_ULTRA) return true;
        if (part == null || !part.hasWorldBounds()) return true;
        float cx = (part.worldCenterX() - modelCenterX) * mapScale;
        float cz = (part.worldCenterZ() - modelCenterZ) * mapScale;
        float dx = cx - focusX;
        float dz = cz - focusZ;
        float distSq = dx * dx + dz * dz;
        float sx = part.worldSizeX() * mapScale;
        float sz = part.worldSizeZ() * mapScale;
        float large = Math.max(sx, sz);
        float allowed = renderDistance + Math.min(90f, large * 0.75f);
        return distSq <= allowed * allowed;
    }

    private void configureTransform(MapDefinition def, GlbModel m) {
        mapScale = def == null ? 1f : Math.max(0.01f, def.visualScale);
        groundY = def == null ? 0f : def.groundY;
        modelCenterX = m == null ? 0f : m.centerX();
        modelCenterZ = m == null ? 0f : m.centerZ();
        modelMinY = m == null ? 0f : m.minY;
    }

    private void buildMapMatrix() {
        Matrix.setIdentityM(mapMatrix, 0);
        Matrix.translateM(mapMatrix, 0, 0f, groundY, 0f);
        Matrix.scaleM(mapMatrix, 0, mapScale, mapScale, mapScale);
        Matrix.translateM(mapMatrix, 0, -modelCenterX, -modelMinY, -modelCenterZ);
    }

    private void applyUploadBudget() {
        if (model == null || model.parts.size() == 0) return;
        int budget = partBudgetForQuality(qualityLevel, model.parts.size());
        if (budget >= model.parts.size()) return;

        List<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < model.parts.size(); i++) order.add(Integer.valueOf(i));
        Collections.sort(order, new Comparator<Integer>() {
            @Override public int compare(Integer a, Integer b) {
                float sb = importanceScore(model.parts.get(b.intValue()));
                float sa = importanceScore(model.parts.get(a.intValue()));
                return sb > sa ? 1 : (sb < sa ? -1 : 0);
            }
        });
        boolean[] keep = new boolean[model.parts.size()];
        for (int i = 0; i < budget && i < order.size(); i++) {
            keep[order.get(i).intValue()] = true;
        }
        skippedByBudget = 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null) continue;
            if (!keep[i]) {
                part.skipped = true;
                part.error = "A61_5 quality budget static map skip";
                part.vertices = null;
                part.indices = null;
                skippedByBudget++;
            }
        }
    }

    private float importanceScore(GlbMeshPart part) {
        if (part == null) return 0f;
        float sx = Math.max(0f, part.worldSizeX());
        float sy = Math.max(0f, part.worldSizeY());
        float sz = Math.max(0f, part.worldSizeZ());
        float area = sx * sz;
        float height = Math.max(0.25f, sy);
        return part.indexCount + area * 160f + height * 45f;
    }

    private void uploadTextures() {
        if (model == null) return;
        textureIds = new int[model.images.size()];
        int uploaded = 0;
        for (int i = 0; i < model.images.size(); i++) {
            textureIds[i] = TextureCache.createTextureFromImageBytes(model.images.get(i), maxTextureSize, qualityLevel != CarVisualConfig.QUALITY_LOW);
            if (textureIds[i] == 0) textureIds[i] = whiteTexture;
            else uploaded++;
        }
        GameLog.i(TAG, "A61_5 map texture upload " + uploaded + "/" + model.images.size() + " max=" + maxTextureSize);
    }

    private void uploadMeshes() {
        uploadedParts = 0;
        if (model == null) return;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped) continue;
            part.uploadToGpu();
            if (part.gpuReady) uploadedParts++;
        }
        if (uploadedParts == 0) throw new RuntimeException("Statik haritada GPU'ya yuklenen parca yok");
    }

    private void buildBatches() {
        if (model == null || model.parts.size() == 0) {
            batches = new StaticMapBatch[0];
            return;
        }
        int materialCount = Math.max(1, model.materials.size());
        ArrayList<ArrayList<Integer>> byMaterial = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < materialCount; i++) byMaterial.add(new ArrayList<Integer>());
        int[] indexCounts = new int[materialCount];
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped || !part.gpuReady) continue;
            int mi = part.materialIndex;
            if (mi < 0 || mi >= materialCount) mi = 0;
            byMaterial.get(mi).add(Integer.valueOf(i));
            indexCounts[mi] += Math.max(0, part.indexCount);
        }
        ArrayList<StaticMapBatch> result = new ArrayList<StaticMapBatch>();
        for (int mi = 0; mi < materialCount; mi++) {
            ArrayList<Integer> list = byMaterial.get(mi);
            if (list.size() == 0) continue;
            int[] indices = new int[list.size()];
            for (int j = 0; j < list.size(); j++) indices[j] = list.get(j).intValue();
            result.add(new StaticMapBatch(mi, indices, indices.length, indexCounts[mi]));
        }
        batches = result.toArray(new StaticMapBatch[result.size()]);
    }

    private int textureFor(GlbMaterial mat) {
        if (mat != null && mat.imageIndex >= 0 && textureIds != null && mat.imageIndex < textureIds.length && textureIds[mat.imageIndex] != 0) {
            return textureIds[mat.imageIndex];
        }
        if (mat != null && mat.emissiveImageIndex >= 0 && textureIds != null && mat.emissiveImageIndex < textureIds.length && textureIds[mat.emissiveImageIndex] != 0) {
            return textureIds[mat.emissiveImageIndex];
        }
        return whiteTexture;
    }

    private GlbMaterial getMaterial(int index) {
        if (model == null || model.materials.size() == 0) return new GlbMaterial();
        if (index < 0 || index >= model.materials.size()) return model.materials.get(0);
        return model.materials.get(index);
    }

    private int normalizeQuality(int quality) {
        if (quality < CarVisualConfig.QUALITY_LOW || quality > CarVisualConfig.QUALITY_ULTRA) return CarVisualConfig.QUALITY_MEDIUM;
        return quality;
    }

    private int textureCapForQuality(int quality) {
        if (quality == CarVisualConfig.QUALITY_LOW) return 512;
        if (quality == CarVisualConfig.QUALITY_MEDIUM) return 768;
        if (quality == CarVisualConfig.QUALITY_HIGH) return 1024;
        return 1536;
    }

    private int partBudgetForQuality(int quality, int total) {
        if (quality == CarVisualConfig.QUALITY_LOW) return Math.min(total, 1800);
        if (quality == CarVisualConfig.QUALITY_MEDIUM) return Math.min(total, 3800);
        if (quality == CarVisualConfig.QUALITY_HIGH) return Math.min(total, 6500);
        return total;
    }

    private float renderDistanceForQuality(int quality, float mapHalfSize) {
        if (quality == CarVisualConfig.QUALITY_LOW) return Math.min(mapHalfSize, 110f);
        if (quality == CarVisualConfig.QUALITY_MEDIUM) return Math.min(mapHalfSize, 155f);
        if (quality == CarVisualConfig.QUALITY_HIGH) return Math.min(mapHalfSize, 230f);
        return mapHalfSize * 3f;
    }

    public void dispose() {
        if (model != null) {
            for (int i = 0; i < model.parts.size(); i++) {
                GlbMeshPart part = model.parts.get(i);
                if (part != null) part.dispose();
            }
        }
        TextureCache.deleteTextures(textureIds);
        textureIds = null;
        if (whiteTexture != 0) {
            TextureCache.deleteTexture(whiteTexture);
            whiteTexture = 0;
        }
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        model = null;
        batches = null;
        ready = false;
    }

    private void createShader() {
        String vertex =
                "uniform mat4 uMVP;" +
                "uniform mat4 uModel;" +
                "attribute vec3 aPosition;" +
                "attribute vec3 aNormal;" +
                "attribute vec2 aTexCoord;" +
                "varying vec3 vNormal;" +
                "varying vec2 vTexCoord;" +
                "void main(){" +
                "  vNormal = normalize((uModel * vec4(aNormal, 0.0)).xyz);" +
                "  vTexCoord = aTexCoord;" +
                "  gl_Position = uMVP * vec4(aPosition, 1.0);" +
                "}";
        String fragment =
                "precision mediump float;" +
                "uniform vec4 uColor;" +
                "uniform sampler2D uTexture;" +
                "uniform float uHasTexture;" +
                "uniform vec3 uEmissive;" +
                "uniform float uBrightness;" +
                "uniform float uAlphaBoost;" +
                "varying vec3 vNormal;" +
                "varying vec2 vTexCoord;" +
                "void main(){" +
                "  vec3 n = normalize(vNormal);" +
                "  vec3 lightDir = normalize(vec3(-0.35, 0.82, 0.48));" +
                "  vec3 fillDir = normalize(vec3(0.45, 0.45, -0.40));" +
                "  float ndotl = max(dot(n, lightDir), 0.0);" +
                "  float fill = max(dot(n, fillDir), 0.0) * 0.28;" +
                "  vec4 tex = texture2D(uTexture, vTexCoord);" +
                "  vec4 base = mix(vec4(1.0), tex, uHasTexture) * uColor;" +
                "  vec3 ambient = base.rgb * 0.42;" +
                "  vec3 diffuse = base.rgb * (ndotl * 0.82 + fill);" +
                "  vec3 emissive = uEmissive * 1.25;" +
                "  gl_FragColor = vec4(min((ambient + diffuse) * uBrightness + emissive, vec3(1.0)), base.a * uAlphaBoost);" +
                "}";
        int vs = compile(GLES20.GL_VERTEX_SHADER, vertex);
        int fs = compile(GLES20.GL_FRAGMENT_SHADER, fragment);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);
        int[] link = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("StaticMap shader link hatasi: " + log);
        }
        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        aNormal = GLES20.glGetAttribLocation(program, "aNormal");
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
        uMvp = GLES20.glGetUniformLocation(program, "uMVP");
        uModel = GLES20.glGetUniformLocation(program, "uModel");
        uColor = GLES20.glGetUniformLocation(program, "uColor");
        uTexture = GLES20.glGetUniformLocation(program, "uTexture");
        uHasTexture = GLES20.glGetUniformLocation(program, "uHasTexture");
        uEmissive = GLES20.glGetUniformLocation(program, "uEmissive");
        uBrightness = GLES20.glGetUniformLocation(program, "uBrightness");
        uAlphaBoost = GLES20.glGetUniformLocation(program, "uAlphaBoost");
        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
    }

    private int compile(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("StaticMap shader compile hatasi: " + log);
        }
        return shader;
    }
}
