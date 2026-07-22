package com.arabaoyunu.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.arabaoyunu.util.GameLog;

public final class ModelRenderer {

    private static final String TAG = "ModelRenderer";

    private int program;
    private int aPosition;
    private int aNormal;
    private int aTexCoord;
    private int uMvp;
    private int uModel;
    private int uColor;
    private int uTexture;
    private int uHasTexture;
    private int uMetallic;
    private int uRoughness;
    private int uEmissive;
    private int uMaterialKind;
    private int uSpecularBoost;
    private int uBrightnessBoost;
    private int uClearcoatBoost;
    private int uEmissiveStrength;
    private int uGlassAlpha;
    private int uLightState;
    private int uPaintTint;
    private int uRimTint;
    private int uGlassTint;
    private int uFrontLightTint;
    private int uRearLightTint;
    private int uPaintStyle;
    private int uDamageState;

    private GlbModel model;
    private CarVisualConfig config;
    private VehicleVisualTransform visualTransform;
    private int[] textureIds;
    private int whiteTexture;
    private boolean ready;
    private boolean staticSceneMode;
    private String status = "not_created";

    private final float[] carMatrix = new float[16];
    private final float[] partWorld = new float[16];
    private final float[] animatedLocal = new float[16];
    private final float[] mvp = new float[16];

    private float wheelSpinRad;
    private int paintPreset;
    private int rimPreset;
    private int paintFinishPreset;
    private int wrapPreset;
    private int stickerPreset;
    private int rimStylePreset;
    private int rimColorPreset;
    private int mirrorColorPreset;
    private int glassTintPreset;
    private int headlightColorPreset;
    private int tailLightPreset;
    private float damageHealth = 1f;
    private float damageMotor;
    private float damageTire;
    private float damageGlass;
    private float damageBody;
    private final VehicleLightState fallbackLightState = new VehicleLightState();



    public void create(Context context, CarVisualConfig config) {
        createInternal(context, config, false);
    }

    /**
     * A64.4: Garajdaki gerçek 3D showroom araç gibi sanitize edilmez.
     * Showroom bir çevre/scene asset'i olduğu için GlbLoader.loadStatic() ile
     * yüklenir; böylece "showroom/studio/platform" isimli gerçek parçalar
     * yanlışlıkla araç dışı prop sanılıp gizlenmez.
     */
    public void createStatic(Context context, CarVisualConfig config) {
        createInternal(context, config, true);
    }

    private void createInternal(Context context, CarVisualConfig config, boolean staticScene) {
        this.config = config;
        this.staticSceneMode = staticScene;
        try {
            createShader();
            whiteTexture = TextureCache.createWhiteTexture();
            model = staticScene ? GlbLoader.loadStatic(context, config.assetPath) : GlbLoader.load(context, config.assetPath);
            visualTransform = VehicleEntityInitializer.initialize(model, config);
            uploadTextures();
            uploadMeshes();
            model.releaseImageData();
            System.gc();
            ready = true;
            GameLog.i(TAG, "Wheel semantic test: " + wheelDebugSummary());
            GameLog.i(TAG, "Vehicle semantic material test: " + semanticDebugSummary());
            int wheelParts = countWheelParts();
            status = (staticScene ? "static_scene_ready parts=" : "ready parts=") + model.parts.size()
                    + " wheelParts=" + wheelParts
                    + " " + wheelDebugSummary()
                    + " vertices=" + model.totalVertices
                    + " indices=" + model.totalIndices
                    + " scale=" + visualTransform.scale
                    + " quality=" + config.qualityName
                    + " texMax=" + config.maxTextureSize;
            GameLog.i(TAG, (staticScene ? "GLB statik showroom hazir: " : "GLB arac hazir: ") + status);
        } catch (Throwable t) {
            ready = false;
            model = null;
            visualTransform = null;
            status = "failed: " + t.getMessage();
            GameLog.e(TAG, (staticScene ? "GLB showroom yuklenemedi; temiz fallback aktif. " : "GLB arac yuklenemedi; fallback govde aktif kalacak. ") + status, t);
        }
    }

    public String getStatus() {
        return status;
    }

    public boolean isReady() {
        return ready && model != null && model.parts.size() > 0 && visualTransform != null;
    }

    /**
     * A64.5: Garaj QA için modelin araç gibi mi yoksa statik showroom gibi mi
     * çalıştığını kısa ve güvenli şekilde raporlar. Kullanıcıya debug ekranı
     * basmak yerine rapor/log seviyesinde tutulur.
     */
    public String qaSummary() {
        if (!isReady()) return status == null ? "not_ready" : status;
        int body = 0;
        int wheels = 0;
        int glass = 0;
        int lights = 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped) continue;
            GlbMaterial mat = getMaterial(part.materialIndex);
            if (part.isWheel()) wheels++;
            else if (mat != null && mat.materialKind == MaterialKind.GLASS) glass++;
            else if (mat != null && isLightKind(mat.materialKind)) lights++;
            else body++;
        }
        return (staticSceneMode ? "static" : "vehicle")
                + " parts=" + model.parts.size()
                + " body=" + body
                + " wheel=" + wheels
                + " glass=" + glass
                + " light=" + lights
                + " scale=" + (visualTransform == null ? 0f : visualTransform.scale)
                + " status=" + status;
    }

    public void setCustomization(int paintPreset, int rimPreset) {
        setCustomization(paintPreset, rimPreset, 0, 0, 0, rimPreset, 0, 0, 0, 0, 0);
    }

    public void setCustomization(
            int paintPreset,
            int rimPreset,
            int paintFinishPreset,
            int wrapPreset,
            int stickerPreset,
            int rimStylePreset,
            int rimColorPreset,
            int mirrorColorPreset,
            int glassTintPreset,
            int headlightColorPreset,
            int tailLightPreset) {
        this.paintPreset = Math.max(0, Math.min(9, paintPreset));
        this.rimPreset = Math.max(0, Math.min(5, rimPreset));
        this.paintFinishPreset = Math.max(0, Math.min(2, paintFinishPreset));
        this.wrapPreset = Math.max(0, Math.min(5, wrapPreset));
        this.stickerPreset = Math.max(0, Math.min(6, stickerPreset));
        this.rimStylePreset = Math.max(0, Math.min(5, rimStylePreset));
        this.rimColorPreset = Math.max(0, Math.min(5, rimColorPreset));
        this.mirrorColorPreset = Math.max(0, Math.min(5, mirrorColorPreset));
        this.glassTintPreset = Math.max(0, Math.min(5, glassTintPreset));
        this.headlightColorPreset = Math.max(0, Math.min(4, headlightColorPreset));
        this.tailLightPreset = Math.max(0, Math.min(4, tailLightPreset));
    }

    public void setDamageVisual(float health, float motor, float tire, float glass, float body) {
        this.damageHealth = clamp(health, 0f, 1f);
        this.damageMotor = clamp(motor, 0f, 1f);
        this.damageTire = clamp(tire, 0f, 1f);
        this.damageGlass = clamp(glass, 0f, 1f);
        this.damageBody = clamp(body, 0f, 1f);
    }



    private String wheelDebugSummary() {
        int fl = 0;
        int fr = 0;
        int rl = 0;
        int rr = 0;
        if (model != null) {
            for (int i = 0; i < model.parts.size(); i++) {
                GlbMeshPart part = model.parts.get(i);
                if (part == null || part.semanticName == null) continue;
                if ("wheel_fl".equals(part.semanticName)) fl++;
                else if ("wheel_fr".equals(part.semanticName)) fr++;
                else if ("wheel_rl".equals(part.semanticName)) rl++;
                else if ("wheel_rr".equals(part.semanticName)) rr++;
            }
        }
        return "FL=" + fl + " FR=" + fr + " RL=" + rl + " RR=" + rr;
    }


    private String semanticDebugSummary() {
        int paint = 0;
        int glass = 0;
        int front = 0;
        int rear = 0;
        int brake = 0;
        int reverse = 0;
        int signal = 0;
        if (model != null) {
            for (int i = 0; i < model.parts.size(); i++) {
                GlbMeshPart part = model.parts.get(i);
                if (part == null) continue;
                GlbMaterial mat = getMaterial(part.materialIndex);
                if (mat == null) continue;
                if (mat.materialKind == MaterialKind.BODY_PAINT) paint++;
                else if (mat.materialKind == MaterialKind.GLASS) glass++;
                else if (mat.materialKind == MaterialKind.LIGHT_FRONT) front++;
                else if (mat.materialKind == MaterialKind.LIGHT_REAR) rear++;
                else if (mat.materialKind == MaterialKind.BRAKE_LIGHT) brake++;
                else if (mat.materialKind == MaterialKind.REVERSE_LIGHT) reverse++;
                else if (mat.materialKind == MaterialKind.TURN_SIGNAL) signal++;
            }
        }
        return "Paint=" + paint + " Glass=" + glass + " FrontLight=" + front
                + " RearLight=" + rear + " Brake=" + brake + " Reverse=" + reverse + " Signal=" + signal;
    }

    private int countWheelParts() {
        int count = 0;
        if (model != null) {
            for (int i = 0; i < model.parts.size(); i++) {
                GlbMeshPart part = model.parts.get(i);
                if (part != null && part.isWheel()) count++;
            }
        }
        return count;
    }


    /**
     * @return En az bir GLB parca cizildiyse true. Aksi halde GameRenderer fallback araci cizer.
     */
    public boolean renderCar(float[] viewProjection, float x, float y, float z, float pitch, float yaw, float roll, float forwardSpeed, float steerInput, float dt, VehicleLightState lightState, RenderStats stats) {
        if (!isReady()) return false;

        stabilizeVisualTransform(x, y, z, pitch, yaw, roll);
        updateWheelSpin(forwardSpeed, dt);
        if (lightState == null) {
            lightState = fallbackLightState;
        }

        Matrix.setIdentityM(carMatrix, 0);
        Matrix.translateM(carMatrix, 0,
                visualTransform.smoothedX,
                visualTransform.smoothedY + visualTransform.yOffset,
                visualTransform.smoothedZ);
        Matrix.rotateM(carMatrix, 0, (float) Math.toDegrees(visualTransform.smoothedYaw) + config.yawOffsetDeg, 0f, 1f, 0f);
        Matrix.rotateM(carMatrix, 0,
                ((float) Math.toDegrees(visualTransform.smoothedPitch) * config.pitchRollMultiplier) + config.pitchOffsetDeg,
                1f, 0f, 0f);
        Matrix.rotateM(carMatrix, 0,
                ((float) Math.toDegrees(visualTransform.smoothedRoll) * config.pitchRollMultiplier) + config.rollOffsetDeg,
                0f, 0f, 1f);
        Matrix.scaleM(carMatrix, 0, visualTransform.scale, visualTransform.scale, visualTransform.scale);
        Matrix.translateM(carMatrix, 0,
                -visualTransform.localCenterX,
                -visualTransform.localBaseY,
                -visualTransform.localCenterZ);

        GLES20.glUseProgram(program);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(uTexture, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        int drawn = 0;
        int bodyDrawn = 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped || !part.gpuReady || part.indexCount <= 0 || part.vertexBufferId == 0 || part.indexBufferId == 0) {
                continue;
            }

            GlbMaterial mat = getMaterial(part.materialIndex);

            int textureId = whiteTexture;
            float hasTexture = 0f;
            if (mat.imageIndex >= 0 && textureIds != null && mat.imageIndex < textureIds.length && textureIds[mat.imageIndex] != 0) {
                textureId = textureIds[mat.imageIndex];
                hasTexture = 1f;
            } else if (mat.emissiveImageIndex >= 0 && isLightKind(mat.materialKind)
                    && textureIds != null && mat.emissiveImageIndex < textureIds.length
                    && textureIds[mat.emissiveImageIndex] != 0) {
                textureId = textureIds[mat.emissiveImageIndex];
                hasTexture = 1f;
            }

            if (mat.alphaBlend || mat.a < 0.995f) {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                GLES20.glDisable(GLES20.GL_BLEND);
            }

            buildAnimatedPartMatrix(part, steerInput);
            Matrix.multiplyMM(partWorld, 0, carMatrix, 0, animatedLocal, 0);
            Matrix.multiplyMM(mvp, 0, viewProjection, 0, partWorld, 0);

            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniformMatrix4fv(uModel, 1, false, partWorld, 0);
            GLES20.glUniform4f(uColor, mat.r, mat.g, mat.b, mat.a);
            GLES20.glUniform1f(uHasTexture, hasTexture);
            GLES20.glUniform1f(uMetallic, mat.metallic);
            GLES20.glUniform1f(uRoughness, mat.roughness);
            GLES20.glUniform3f(uEmissive, mat.emissiveR, mat.emissiveG, mat.emissiveB);
            GLES20.glUniform1f(uMaterialKind, (float) mat.materialKind);
            GLES20.glUniform1f(uSpecularBoost, mat.specularBoost * Math.max(0.25f, mat.specularFactor));
            float brightness = mat.brightnessBoost;
            if (mat.materialKind == MaterialKind.BODY_PAINT) brightness *= config.bodyPaintBoost;
            if (mat.materialKind == MaterialKind.GLASS) brightness *= config.glassBoost;
            if (isLightKind(mat.materialKind)) brightness *= config.emissiveBoost;
            GLES20.glUniform1f(uBrightnessBoost, brightness);
            GLES20.glUniform1f(uClearcoatBoost, mat.clearcoatBoost + mat.clearcoatFactor);
            GLES20.glUniform1f(uEmissiveStrength, mat.emissiveStrength);
            GLES20.glUniform1f(uGlassAlpha, mat.glassAlpha);
            GLES20.glUniform4f(uLightState,
                    lightState.headlightsOn ? 1f : 0f,
                    lightState.brakeOn ? 1f : 0f,
                    lightState.reverseOn ? 1f : 0f,
                    (lightState.hazardOn || lightState.leftSignalOn || lightState.rightSignalOn) ? lightState.blinkValue() : 0f);
            setTintUniform(uPaintTint, combinedPaintPreset(), true);
            setTintUniform(uRimTint, combinedRimPreset(), false);
            setGlassTintUniform();
            setLightTintUniform(uFrontLightTint, headlightColorPreset, true);
            setLightTintUniform(uRearLightTint, tailLightPreset, false);
            setPaintStyleUniform();
            GLES20.glUniform4f(uDamageState, damageHealth, damageMotor, damageGlass, damageBody);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

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
                part.error = "draw GL error=" + glError;
                GameLog.e(TAG, "Parca cizilemedi ve atlandi: " + part.name + " " + part.error, null);
                continue;
            }

            drawn++;
            // A64.4: Bazı DLC GLB dosyalarında gövde parçaları isim/materyal nedeniyle
            // scene_prop olarak işaretlenebiliyor. Render boş sayılmasın diye gövde
            // tespiti artık scene_prop bayrağına değil; wheel/glass/light dışı gerçek
            // mesh çizilip çizilmediğine bakar.
            if (!part.isWheel() && mat.materialKind != MaterialKind.GLASS && !isLightKind(mat.materialKind)) {
                bodyDrawn++;
            }
            if (stats != null) {
                stats.drawCalls++;
                stats.renderedObjects++;
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

        if (drawn <= 0) {
            status = "no_drawn_parts_fallback";
            return false;
        }
        if (!staticSceneMode && bodyDrawn <= 0) {
            status = "wheel_or_glass_only_fallback drawn=" + drawn;
            return false;
        }
        if (staticSceneMode && drawn > 0) {
            status = "static_scene_drawn parts=" + drawn;
        }
        return true;
    }



    private boolean isLightKind(int kind) {
        return kind == MaterialKind.LIGHT_FRONT
                || kind == MaterialKind.LIGHT_REAR
                || kind == MaterialKind.BRAKE_LIGHT
                || kind == MaterialKind.TURN_SIGNAL
                || kind == MaterialKind.REVERSE_LIGHT;
    }

    private void updateWheelSpin(float forwardSpeed, float dt) {
        if (dt <= 0f) return;
        if (dt > 0.05f) dt = 0.05f;
        // ArabaOyunu_25: teker dönüşü daha görünür ve canlı.
        // Yaklaşık teker yarıçapı 0.34m; küçük GLB parçalarda spin hissi zayıf kalmasın diye
        // çok hafif görsel hız çarpanı kullanılır.
        wheelSpinRad += (forwardSpeed / 0.34f) * dt * 1.18f;
        if (wheelSpinRad > Math.PI * 2f || wheelSpinRad < -Math.PI * 2f) {
            wheelSpinRad = wheelSpinRad % ((float) (Math.PI * 2.0));
        }
    }

    private void buildAnimatedPartMatrix(GlbMeshPart part, float steerInput) {
        System.arraycopy(part.localMatrix, 0, animatedLocal, 0, 16);

        if (part == null) {
            return;
        }
        GlbMaterial safeMat = getMaterial(part.materialIndex);
        if (safeMat != null && safeMat.materialKind == MaterialKind.GLASS) {
            // ArabaOyunu_48: Cam/window/windshield parçaları asla teker gibi dönmez.
            return;
        }
        if (!part.isWheel()) {
            return;
        }

        // ArabaOyunu_16:
        // Grup pivot sistemi kaldirildi; video testinde tekerleri govdeden kopariyordu.
        // Guvenli mod: Her GLB teker parcasi kendi local merkezinde doner,
        // boylece lastik/jant arac govdesinden ayrilmaz.
        float cx = part.centerX();
        float cy = part.centerY();
        float cz = part.centerZ();

        Matrix.translateM(animatedLocal, 0, cx, cy, cz);

        if (part.isFrontWheel()) {
            // ArabaOyunu_48 kesin kural:
            // Sadece ön sol ve ön sağ teker direksiyon açısı alır.
            // Arka sol / arka sağ tekerler asla sağ-sol direksiyon dönüşü almaz.
            float steerDeg = clamp(steerInput, -1f, 1f) * 38f;
            Matrix.rotateM(animatedLocal, 0, steerDeg, 0f, 1f, 0f);
        }

        // Spin guvenli tutulur; sonradan fake/mavi teker eklenmez.
        // Teker sınıflandırması yakalanamayan generic "wheel" parçalarında da sadece spin yapılır.
        Matrix.rotateM(animatedLocal, 0, (float) Math.toDegrees(wheelSpinRad), 1f, 0f, 0f);

        Matrix.translateM(animatedLocal, 0, -cx, -cy, -cz);
    }


    private int combinedPaintPreset() {
        int p = paintPreset;
        if (wrapPreset > 0) p = wrapPreset;
        if (stickerPreset > 0 && p == 0) p = stickerPreset % 6;
        if (mirrorColorPreset > 0 && p == 0) p = mirrorColorPreset;
        return Math.max(0, Math.min(5, p));
    }

    private int combinedRimPreset() {
        int p = rimColorPreset > 0 ? rimColorPreset : rimPreset;
        if (rimStylePreset > 0 && p == 0) p = rimStylePreset;
        return Math.max(0, Math.min(5, p));
    }

    private void setPaintStyleUniform() {
        float matte = paintFinishPreset == 1 ? 1f : 0f;
        float metallic = paintFinishPreset == 2 ? 1f : 0f;
        float wrap = wrapPreset > 0 ? 0.40f : 0f;
        float decal = stickerPreset > 0 ? 0.22f : 0f;
        GLES20.glUniform4f(uPaintStyle, matte, metallic, Math.max(wrap, decal), 0f);
    }

    private void setGlassTintUniform() {
        float r = 0.02f, g = 0.04f, b = 0.05f, a = 0f;
        if (glassTintPreset == 1) { r = 0.04f; g = 0.08f; b = 0.10f; a = 0.18f; }
        else if (glassTintPreset == 2) { r = 0.03f; g = 0.05f; b = 0.07f; a = 0.32f; }
        else if (glassTintPreset == 3) { r = 0.01f; g = 0.015f; b = 0.02f; a = 0.46f; }
        else if (glassTintPreset == 4) { r = 0.0f; g = 0.0f; b = 0.0f; a = 0.58f; }
        else if (glassTintPreset == 5) { r = 0.03f; g = 0.12f; b = 0.22f; a = 0.42f; }
        GLES20.glUniform4f(uGlassTint, r, g, b, a);
    }

    private void setLightTintUniform(int uniform, int preset, boolean front) {
        float r = front ? 0.92f : 1.0f;
        float g = front ? 0.96f : 0.05f;
        float b = front ? 1.0f : 0.02f;
        float a = 0f;
        if (front) {
            if (preset == 1) { r = 0.92f; g = 0.96f; b = 1.0f; a = 0.35f; }
            else if (preset == 2) { r = 0.40f; g = 0.72f; b = 1.0f; a = 0.45f; }
            else if (preset == 3) { r = 1.0f; g = 0.86f; b = 0.42f; a = 0.40f; }
            else if (preset == 4) { r = 0.86f; g = 0.94f; b = 1.0f; a = 0.52f; }
        } else {
            if (preset == 1) { r = 1.0f; g = 0.04f; b = 0.02f; a = 0.30f; }
            else if (preset == 2) { r = 0.40f; g = 0.02f; b = 0.02f; a = 0.45f; }
            else if (preset == 3) { r = 1.0f; g = 0.12f; b = 0.04f; a = 0.50f; }
            else if (preset == 4) { r = 1.0f; g = 0.02f; b = 0.015f; a = 0.55f; }
        }
        GLES20.glUniform4f(uniform, r, g, b, a);
    }

    private void setTintUniform(int uniform, int preset, boolean paint) {
        float r = 1f, g = 1f, b = 1f, a = 0f;
        if (paint) {
            if (preset == 0) { r = 1.0f; g = 0.06f; b = 0.03f; a = 0.42f; }      // Kırmızı
            else if (preset == 1) { r = 0.05f; g = 0.22f; b = 1.0f; a = 0.42f; } // Mavi
            else if (preset == 2) { r = 0.01f; g = 0.012f; b = 0.015f; a = 0.56f; } // Siyah
            else if (preset == 3) { r = 0.92f; g = 0.96f; b = 1.0f; a = 0.26f; } // Beyaz
            else if (preset == 4) { r = 0.44f; g = 0.48f; b = 0.52f; a = 0.46f; } // Gri
            else if (preset == 5) { r = 1.0f; g = 0.74f; b = 0.08f; a = 0.44f; } // Sarı
            else if (preset == 6) { r = 0.05f; g = 0.86f; b = 0.28f; a = 0.42f; } // Yeşil
            else if (preset == 7) { r = 0.56f; g = 0.26f; b = 1.0f; a = 0.48f; } // Mor
            else if (preset == 8) { r = 1.0f; g = 0.36f; b = 0.04f; a = 0.46f; } // Turuncu
            else if (preset == 9) { r = 0.0f; g = 0.0f; b = 0.0f; a = 0.64f; } // Mat siyah
        } else {
            if (preset == 1) { r = 0.08f; g = 0.08f; b = 0.09f; a = 0.42f; }
            else if (preset == 2) { r = 0.02f; g = 0.55f; b = 1.0f; a = 0.35f; }
            else if (preset == 3) { r = 1.0f; g = 0.62f; b = 0.06f; a = 0.42f; }
            else if (preset == 4) { r = 0.9f; g = 0.95f; b = 1.0f; a = 0.20f; }
            else if (preset == 5) { r = 1.0f; g = 0.05f; b = 0.03f; a = 0.40f; }
        }
        GLES20.glUniform4f(uniform, r, g, b, a);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void stabilizeVisualTransform(float x, float y, float z, float pitch, float yaw, float roll) {
        if (visualTransform == null) return;

        if (!visualTransform.initialized) {
            visualTransform.smoothedX = x;
            visualTransform.smoothedY = y;
            visualTransform.smoothedZ = z;
            visualTransform.smoothedYaw = yaw;
            visualTransform.smoothedPitch = pitch;
            visualTransform.smoothedRoll = roll;
            visualTransform.initialized = true;
            return;
        }

        float dx = x - visualTransform.smoothedX;
        float dy = y - visualTransform.smoothedY;
        float dz = z - visualTransform.smoothedZ;
        float distSq = dx * dx + dy * dy + dz * dz;
        if (distSq > 100f) {
            visualTransform.smoothedX = x;
            visualTransform.smoothedY = y;
            visualTransform.smoothedZ = z;
            visualTransform.smoothedYaw = yaw;
            visualTransform.smoothedPitch = pitch;
            visualTransform.smoothedRoll = roll;
            return;
        }

        visualTransform.smoothedX = lerp(visualTransform.smoothedX, x, 0.55f);
        visualTransform.smoothedZ = lerp(visualTransform.smoothedZ, z, 0.55f);

        float yDelta = Math.abs(y - visualTransform.smoothedY);
        if (yDelta > config.visualYSnapThreshold) {
            visualTransform.smoothedY = lerp(visualTransform.smoothedY, y, 0.14f);
        }

        visualTransform.smoothedYaw = lerpAngle(visualTransform.smoothedYaw, yaw, 0.40f);
        visualTransform.smoothedPitch = lerp(visualTransform.smoothedPitch, pitch, 0.16f);
        visualTransform.smoothedRoll = lerp(visualTransform.smoothedRoll, roll, 0.16f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float lerpAngle(float a, float b, float t) {
        float delta = b - a;
        while (delta > Math.PI) delta -= (float) (Math.PI * 2.0);
        while (delta < -Math.PI) delta += (float) (Math.PI * 2.0);
        return a + delta * t;
    }

    public void dispose() {
        if (model != null) {
            for (int i = 0; i < model.parts.size(); i++) {
                model.parts.get(i).dispose();
            }
        }
        if (textureIds != null) {
            TextureCache.deleteTextures(textureIds);
            textureIds = null;
        }
        if (whiteTexture != 0) {
            TextureCache.deleteTexture(whiteTexture);
            whiteTexture = 0;
        }
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
    }

    private GlbMaterial getMaterial(int index) {
        if (model.materials.size() == 0) {
            return new GlbMaterial();
        }
        if (index < 0 || index >= model.materials.size()) {
            return model.materials.get(0);
        }
        return model.materials.get(index);
    }

    private void uploadMeshes() {
        int uploaded = 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            part.uploadToGpu();
            if (part.gpuReady) uploaded++;
        }
        if (uploaded == 0) {
            throw new RuntimeException("Hicbir GLB mesh GPU'ya yuklenemedi");
        }
        GameLog.i(TAG, "GPU mesh upload tamam: " + uploaded + "/" + model.parts.size());
    }

    private void uploadTextures() {
        textureIds = new int[model.images.size()];
        int uploaded = 0;
        for (int i = 0; i < model.images.size(); i++) {
            textureIds[i] = TextureCache.createTextureFromImageBytes(model.images.get(i), config.maxTextureSize);
            if (textureIds[i] == 0) {
                textureIds[i] = whiteTexture;
                GameLog.i(TAG, "Texture yuklenemedi, white fallback kullanildi index=" + i);
            } else {
                uploaded++;
            }
        }
        GameLog.i(TAG, "Texture upload tamam: " + uploaded + "/" + model.images.size() + " max=" + config.maxTextureSize);
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
                "uniform float uMetallic;" +
                "uniform float uRoughness;" +
                "uniform vec3 uEmissive;" +
                "uniform float uMaterialKind;" +
                "uniform float uSpecularBoost;" +
                "uniform float uBrightnessBoost;" +
                "uniform float uClearcoatBoost;" +
                "uniform float uEmissiveStrength;" +
                "uniform float uGlassAlpha;" +
                "uniform vec4 uLightState;" +
                "uniform vec4 uPaintTint;" +
                "uniform vec4 uRimTint;" +
                "uniform vec4 uGlassTint;" +
                "uniform vec4 uFrontLightTint;" +
                "uniform vec4 uRearLightTint;" +
                "uniform vec4 uPaintStyle;" +
                "uniform vec4 uDamageState;" +
                "varying vec3 vNormal;" +
                "varying vec2 vTexCoord;" +
                "void main(){" +
                "  vec3 n = normalize(vNormal);" +
                "  vec3 lightDir = normalize(vec3(-0.42, 0.86, 0.52));" +
                "  vec3 fillDir = normalize(vec3(0.55, 0.45, -0.30));" +
                "  vec3 rimDir = normalize(vec3(-0.25, 0.28, -0.95));" +
                "  vec3 viewDir = normalize(vec3(0.0, 0.35, 1.0));" +
                "  vec3 halfDir = normalize(lightDir + viewDir);" +
                "  float ndotl = max(dot(n, lightDir), 0.0);" +
                "  float fill = max(dot(n, fillDir), 0.0) * 0.24;" +
                "  float rim = pow(max(dot(n, rimDir), 0.0), 3.0) * 0.14;" +
                "  float rough = clamp(uRoughness, 0.035, 1.0);" +
                "  float metal = clamp(uMetallic, 0.0, 1.0);" +
                "  float specPower = mix(170.0, 20.0, rough);" +
                "  float spec = pow(max(dot(n, halfDir), 0.0), specPower) * mix(0.16, 1.05, metal);" +
                "  spec += pow(max(dot(n, halfDir), 0.0), 210.0) * clamp(uClearcoatBoost, 0.0, 2.0) * 0.65;" +
                "  vec4 tex = texture2D(uTexture, vTexCoord);" +
                "  vec4 base = mix(vec4(1.0), tex, uHasTexture) * uColor;" +
                "  float alpha = base.a;" +

                // BODY_PAINT
                "  if (uMaterialKind > 0.5 && uMaterialKind < 1.5){" +
                "    spec += pow(max(dot(n, halfDir), 0.0), 240.0) * 0.62;" +
                "    base.rgb = max(base.rgb, vec3(0.018));" +
                "    base.rgb = mix(base.rgb, uPaintTint.rgb, clamp(uPaintTint.a, 0.0, 0.70));" +
                "    base.rgb = mix(base.rgb, vec3(0.02, 0.02, 0.022), clamp(uPaintStyle.z, 0.0, 0.35));" +
                "    spec *= mix(1.0, 0.32, clamp(uPaintStyle.x, 0.0, 1.0));" +
                "    spec *= mix(1.0, 1.75, clamp(uPaintStyle.y, 0.0, 1.0));" +
                "    base.rgb = mix(base.rgb, vec3(0.08, 0.065, 0.055), clamp(uDamageState.w, 0.0, 0.75));" +
                "    spec *= (1.0 - clamp(uDamageState.w, 0.0, 0.70) * 0.55);" +
                "  }" +

                // GLASS
                "  if (uMaterialKind > 1.5 && uMaterialKind < 2.5){" +
                "    base.rgb = mix(base.rgb, vec3(0.025, 0.055, 0.065), 0.30);" +
                "    base.rgb = mix(base.rgb, uGlassTint.rgb, clamp(uGlassTint.a, 0.0, 0.72));" +
                "    spec *= 1.85;" +
                "    alpha = min(alpha, clamp(uGlassAlpha - uGlassTint.a * 0.18, 0.22, 0.88));" +
                "    base.rgb = mix(base.rgb, vec3(0.78, 0.90, 1.0), clamp(uDamageState.z, 0.0, 0.75));" +
                "    spec *= (1.0 + clamp(uDamageState.z, 0.0, 1.0) * 0.65);" +
                "  }" +

                // CHROME
                "  if (uMaterialKind > 2.5 && uMaterialKind < 3.5){" +
                "    base.rgb = mix(base.rgb, vec3(0.88, 0.90, 0.92), 0.22);" +
                "    base.rgb = mix(base.rgb, uRimTint.rgb, clamp(uRimTint.a, 0.0, 0.58));" +
                "    spec *= 2.15;" +
                "  }" +

                // RUBBER / CARBON / INTERIOR
                "  if (uMaterialKind > 3.5 && uMaterialKind < 4.5){" +
                "    base.rgb *= 0.72;" +
                "    spec *= 0.45;" +
                "  }" +
                "  if (uMaterialKind > 9.5 && uMaterialKind < 10.5){" +
                "    base.rgb *= 0.82;" +
                "    spec *= 0.80;" +
                "    rim += 0.05;" +
                "  }" +
                "  if (uMaterialKind > 10.5 && uMaterialKind < 11.5){" +
                "    base.rgb *= 0.92;" +
                "    spec *= 0.62;" +
                "  }" +

                "  vec3 ambient = base.rgb * 0.40;" +
                "  vec3 diffuse = base.rgb * (ndotl * 0.84 + fill + rim);" +
                "  vec3 proceduralEmissive = uEmissive * uEmissiveStrength;" +

                // LIGHT_FRONT
                "  if (uMaterialKind > 4.5 && uMaterialKind < 5.5){" +
                "    base.rgb = mix(base.rgb, uFrontLightTint.rgb, clamp(uFrontLightTint.a, 0.0, 0.65));" +
                "    proceduralEmissive += base.rgb * (0.30 + uLightState.x * 1.75) * uEmissiveStrength;" +
                "    spec *= 1.60;" +
                "  }" +
                // LIGHT_REAR
                "  if (uMaterialKind > 5.5 && uMaterialKind < 6.5){" +
                "    base.rgb = mix(base.rgb, uRearLightTint.rgb, clamp(uRearLightTint.a, 0.0, 0.65));" +
                "    proceduralEmissive += base.rgb * (0.20 + uLightState.x * 0.35) * uEmissiveStrength;" +
                "  }" +
                // BRAKE_LIGHT
                "  if (uMaterialKind > 6.5 && uMaterialKind < 7.5){" +
                "    base.rgb = mix(base.rgb, uRearLightTint.rgb, clamp(uRearLightTint.a, 0.0, 0.65));" +
                "    proceduralEmissive += base.rgb * (0.25 + uLightState.y * 2.20) * uEmissiveStrength;" +
                "  }" +
                // TURN_SIGNAL
                "  if (uMaterialKind > 7.5 && uMaterialKind < 8.5){" +
                "    proceduralEmissive += vec3(1.0, 0.45, 0.02) * (0.15 + uLightState.w * 2.20) * uEmissiveStrength;" +
                "  }" +
                // REVERSE_LIGHT
                "  if (uMaterialKind > 8.5 && uMaterialKind < 9.5){" +
                "    proceduralEmissive += vec3(0.85, 0.92, 1.0) * (0.10 + uLightState.z * 1.90) * uEmissiveStrength;" +
                "    spec *= 1.45;" +
                "  }" +

                "  vec3 finalColor = (ambient + diffuse) * uBrightnessBoost + vec3(spec) * uSpecularBoost + proceduralEmissive;" +
                "  gl_FragColor = vec4(min(finalColor, vec3(1.0)), alpha);" +
                "}";

        int vs = compile(GLES20.GL_VERTEX_SHADER, vertex);
        int fs = compile(GLES20.GL_FRAGMENT_SHADER, fragment);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);

        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Model shader link hatasi: " + log);
        }

        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        aNormal = GLES20.glGetAttribLocation(program, "aNormal");
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
        uMvp = GLES20.glGetUniformLocation(program, "uMVP");
        uModel = GLES20.glGetUniformLocation(program, "uModel");
        uColor = GLES20.glGetUniformLocation(program, "uColor");
        uTexture = GLES20.glGetUniformLocation(program, "uTexture");
        uHasTexture = GLES20.glGetUniformLocation(program, "uHasTexture");
        uMetallic = GLES20.glGetUniformLocation(program, "uMetallic");
        uRoughness = GLES20.glGetUniformLocation(program, "uRoughness");
        uEmissive = GLES20.glGetUniformLocation(program, "uEmissive");
        uMaterialKind = GLES20.glGetUniformLocation(program, "uMaterialKind");
        uSpecularBoost = GLES20.glGetUniformLocation(program, "uSpecularBoost");
        uBrightnessBoost = GLES20.glGetUniformLocation(program, "uBrightnessBoost");
        uClearcoatBoost = GLES20.glGetUniformLocation(program, "uClearcoatBoost");
        uEmissiveStrength = GLES20.glGetUniformLocation(program, "uEmissiveStrength");
        uGlassAlpha = GLES20.glGetUniformLocation(program, "uGlassAlpha");
        uLightState = GLES20.glGetUniformLocation(program, "uLightState");
        uPaintTint = GLES20.glGetUniformLocation(program, "uPaintTint");
        uRimTint = GLES20.glGetUniformLocation(program, "uRimTint");
        uGlassTint = GLES20.glGetUniformLocation(program, "uGlassTint");
        uFrontLightTint = GLES20.glGetUniformLocation(program, "uFrontLightTint");
        uRearLightTint = GLES20.glGetUniformLocation(program, "uRearLightTint");
        uPaintStyle = GLES20.glGetUniformLocation(program, "uPaintStyle");
        uDamageState = GLES20.glGetUniformLocation(program, "uDamageState");

        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
    }

    private int compile(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Model shader compile hatasi: " + log);
        }
        return shader;
    }
}
