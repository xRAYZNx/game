package com.arabaoyunu.render;

import android.content.Context;
import android.opengl.Matrix;

import com.arabaoyunu.util.GameLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

public final class GlbLoader {

    private static final String TAG = "GlbLoader";
    private static final int GLB_MAGIC = 0x46546C67;
    private static final int CHUNK_JSON = 0x4E4F534A;
    private static final int CHUNK_BIN = 0x004E4942;

    private GlbLoader() {}

    public static GlbModel load(Context context, String assetPath) throws Exception {
        return loadInternal(context, assetPath, true);
    }

    /**
     * A61_2: Araç olmayan büyük statik haritalar için GLB yükleme.
     * Araç sanitize/wheel/cam filtreleri uygulanmaz; yol, bina, zemin ve dekor parçaları korunur.
     */
    public static GlbModel loadStatic(Context context, String assetPath) throws Exception {
        return loadInternal(context, assetPath, false);
    }

    private static GlbModel loadInternal(Context context, String assetPath, boolean sanitizeAsVehicle) throws Exception {
        byte[] fileBytes = readAsset(context, assetPath);
        ByteBuffer file = ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN);

        int magic = file.getInt();
        int version = file.getInt();
        int totalLength = file.getInt();
        if (magic != GLB_MAGIC || version != 2 || totalLength > fileBytes.length) {
            throw new IllegalArgumentException("Gecersiz GLB dosyasi: " + assetPath);
        }

        String jsonText = null;
        byte[] binChunk = null;

        while (file.position() + 8 <= totalLength) {
            int chunkLength = file.getInt();
            int chunkType = file.getInt();
            if (chunkLength < 0 || file.position() + chunkLength > fileBytes.length) {
                throw new IllegalArgumentException("GLB chunk uzunlugu bozuk: " + assetPath);
            }
            byte[] chunk = new byte[chunkLength];
            file.get(chunk);
            if (chunkType == CHUNK_JSON) {
                jsonText = new String(chunk, "UTF-8").trim();
            } else if (chunkType == CHUNK_BIN) {
                binChunk = chunk;
            }
        }

        if (jsonText == null || binChunk == null) {
            throw new IllegalArgumentException("GLB JSON/BIN chunk eksik: " + assetPath);
        }

        JSONObject root = new JSONObject(jsonText);
        GlbModel model = new GlbModel();
        model.sourceAsset = assetPath;

        JSONArray bufferViews = root.optJSONArray("bufferViews");
        JSONArray accessors = root.optJSONArray("accessors");
        JSONArray textures = root.optJSONArray("textures");
        JSONArray images = root.optJSONArray("images");
        JSONArray materials = root.optJSONArray("materials");
        JSONArray meshes = root.optJSONArray("meshes");
        JSONArray nodes = root.optJSONArray("nodes");
        JSONArray scenes = root.optJSONArray("scenes");

        if (bufferViews == null || accessors == null || meshes == null || nodes == null || scenes == null) {
            throw new IllegalArgumentException("GLB ana bolumleri eksik: " + assetPath);
        }

        loadImages(model, images, bufferViews, binChunk);
        loadMaterials(model, materials, textures);

        if (model.materials.size() == 0) {
            model.materials.add(new GlbMaterial());
        }

        int sceneIndex = root.optInt("scene", 0);
        if (sceneIndex < 0 || sceneIndex >= scenes.length()) sceneIndex = 0;
        JSONObject scene = scenes.getJSONObject(sceneIndex);
        JSONArray rootNodes = scene.optJSONArray("nodes");
        if (rootNodes == null) {
            throw new IllegalArgumentException("GLB sahnesinde root node yok: " + assetPath);
        }

        Set<Integer> visited = new HashSet<Integer>();
        float[] identity = new float[16];
        Matrix.setIdentityM(identity, 0);
        for (int i = 0; i < rootNodes.length(); i++) {
            traverseNode(rootNodes.getInt(i), identity, "", nodes, meshes, accessors, bufferViews, binChunk, model, visited);
        }

        if (sanitizeAsVehicle) {
            sanitizeVehicleModel(model);
        } else {
            rebuildModelBoundsFromVisibleParts(model);
            GameLog.i(TAG, "A61_2 static GLB sanitize bypass asset=" + model.sourceAsset
                    + " parts=" + model.parts.size()
                    + " bounds=" + model.minX + "," + model.minY + "," + model.minZ
                    + " -> " + model.maxX + "," + model.maxY + "," + model.maxZ);
        }

        if (model.parts.size() == 0) {
            throw new IllegalArgumentException("GLB icinde cizilebilir mesh bulunamadi: " + assetPath);
        }

        GameLog.i(TAG, "GLB okundu parts=" + model.parts.size()
                + " bounds=" + model.minX + "," + model.minY + "," + model.minZ
                + " -> " + model.maxX + "," + model.maxY + "," + model.maxZ);
        return model;
    }

    private static void loadImages(GlbModel model, JSONArray images, JSONArray bufferViews, byte[] binChunk) throws Exception {
        if (images == null) return;
        for (int i = 0; i < images.length(); i++) {
            JSONObject image = images.getJSONObject(i);
            int bvIndex = image.optInt("bufferView", -1);
            if (bvIndex < 0 || bvIndex >= bufferViews.length()) {
                model.images.add(null);
                continue;
            }
            JSONObject bv = bufferViews.getJSONObject(bvIndex);
            int offset = bv.optInt("byteOffset", 0);
            int length = bv.getInt("byteLength");
            if (offset < 0 || length <= 0 || offset + length > binChunk.length) {
                model.images.add(null);
                continue;
            }
            byte[] data = new byte[length];
            System.arraycopy(binChunk, offset, data, 0, length);
            model.images.add(data);
        }
    }

    private static void loadMaterials(GlbModel model, JSONArray materials, JSONArray textures) throws Exception {
        if (materials == null) return;

        for (int i = 0; i < materials.length(); i++) {
            JSONObject src = materials.getJSONObject(i);
            GlbMaterial mat = new GlbMaterial();
            mat.name = src.optString("name", "material_" + i);
            mat.doubleSided = src.optBoolean("doubleSided", false);
            String alphaMode = src.optString("alphaMode", "OPAQUE");
            mat.alphaMode = alphaMode;
            mat.alphaBlend = "BLEND".equals(alphaMode) || "MASK".equals(alphaMode);

            JSONObject pbr = src.optJSONObject("pbrMetallicRoughness");
            if (pbr != null) {
                JSONArray color = pbr.optJSONArray("baseColorFactor");
                if (color != null && color.length() >= 4) {
                    mat.r = (float) color.optDouble(0, 1.0);
                    mat.g = (float) color.optDouble(1, 1.0);
                    mat.b = (float) color.optDouble(2, 1.0);
                    mat.a = (float) color.optDouble(3, 1.0);
                }
                mat.metallic = (float) pbr.optDouble("metallicFactor", 0.0);
                mat.roughness = (float) pbr.optDouble("roughnessFactor", 0.65);
                mat.imageIndex = textureSource(pbr.optJSONObject("baseColorTexture"), textures);
                mat.metallicRoughnessImageIndex = textureSource(pbr.optJSONObject("metallicRoughnessTexture"), textures);
            }

            JSONArray emissive = src.optJSONArray("emissiveFactor");
            if (emissive != null && emissive.length() >= 3) {
                mat.emissiveR = (float) emissive.optDouble(0, 0.0);
                mat.emissiveG = (float) emissive.optDouble(1, 0.0);
                mat.emissiveB = (float) emissive.optDouble(2, 0.0);
            }
            mat.normalImageIndex = textureSource(src.optJSONObject("normalTexture"), textures);
            mat.emissiveImageIndex = textureSource(src.optJSONObject("emissiveTexture"), textures);
            mat.occlusionImageIndex = textureSource(src.optJSONObject("occlusionTexture"), textures);

            readTextureTransform(mat, pbr == null ? null : pbr.optJSONObject("baseColorTexture"));
            readMaterialExtensions(mat, src.optJSONObject("extensions"));

            tuneMaterial(mat);
            model.materials.add(mat);
        }
    }


    private static void readTextureTransform(GlbMaterial mat, JSONObject textureInfo) {
        if (mat == null || textureInfo == null) return;
        JSONObject ext = textureInfo.optJSONObject("extensions");
        if (ext == null) return;
        JSONObject transform = ext.optJSONObject("KHR_texture_transform");
        if (transform == null) return;

        JSONArray offset = transform.optJSONArray("offset");
        if (offset != null && offset.length() >= 2) {
            mat.texOffsetU = (float) offset.optDouble(0, 0.0);
            mat.texOffsetV = (float) offset.optDouble(1, 0.0);
        }

        JSONArray scale = transform.optJSONArray("scale");
        if (scale != null && scale.length() >= 2) {
            mat.texScaleU = (float) scale.optDouble(0, 1.0);
            mat.texScaleV = (float) scale.optDouble(1, 1.0);
        }
    }

    private static void readMaterialExtensions(GlbMaterial mat, JSONObject extensions) {
        if (mat == null || extensions == null) return;

        JSONObject emissiveStrength = extensions.optJSONObject("KHR_materials_emissive_strength");
        if (emissiveStrength != null) {
            mat.emissiveStrength = (float) emissiveStrength.optDouble("emissiveStrength", mat.emissiveStrength);
        }

        JSONObject clearcoat = extensions.optJSONObject("KHR_materials_clearcoat");
        if (clearcoat != null) {
            mat.clearcoatFactor = (float) clearcoat.optDouble("clearcoatFactor", mat.clearcoatFactor);
            mat.clearcoatRoughnessFactor = (float) clearcoat.optDouble("clearcoatRoughnessFactor", mat.clearcoatRoughnessFactor);
        }

        JSONObject specular = extensions.optJSONObject("KHR_materials_specular");
        if (specular != null) {
            mat.specularFactor = (float) specular.optDouble("specularFactor", mat.specularFactor);
            JSONArray color = specular.optJSONArray("specularColorFactor");
            if (color != null && color.length() >= 3) {
                mat.specularColorR = (float) color.optDouble(0, 1.0);
                mat.specularColorG = (float) color.optDouble(1, 1.0);
                mat.specularColorB = (float) color.optDouble(2, 1.0);
            }
        }

        JSONObject transmission = extensions.optJSONObject("KHR_materials_transmission");
        if (transmission != null) {
            mat.transmissionFactor = (float) transmission.optDouble("transmissionFactor", mat.transmissionFactor);
            if (mat.transmissionFactor > 0.01f) {
                mat.alphaBlend = true;
                mat.a = Math.min(mat.a, 0.68f);
            }
        }

        JSONObject ior = extensions.optJSONObject("KHR_materials_ior");
        if (ior != null) {
            mat.ior = (float) ior.optDouble("ior", mat.ior);
        }
    }

    private static void tuneMaterial(GlbMaterial mat) {
        String n = mat.name == null ? "" : mat.name.toLowerCase();

        boolean isReverse = n.contains("reverse") || n.contains("backup") || n.contains("back_up") || n.contains("white_light_rear");
        boolean isBrake = n.contains("brake") || n.contains("stop") || n.contains("brakelight") || n.contains("brake_light");
        boolean isTurn = n.contains("indicator") || n.contains("signal") || n.contains("turn") || n.contains("blinker") || n.contains("orange_glass") || n.contains("amber");
        boolean isRearLight = n.contains("tail") || n.contains("rear_light") || n.contains("taillight") || n.contains("tail_light") || n.contains("rear lamp") || n.contains("red_lamp") || n.contains("red_glass");
        boolean isFrontLight = n.contains("headlight") || n.contains("head_light") || n.contains("lowbeam") || n.contains("highbeam") || n.contains("running_lights") || n.contains("front_lamp") || n.contains("front_light");
        boolean isGlass = n.contains("glass") || n.contains("window") || n.contains("windshield") || n.contains("windscreen") || mat.transmissionFactor > 0.02f;
        boolean isPaint = n.contains("carpaint") || n.contains("car_paint") || n.contains("body_paint") || n.contains("paint") || n.contains("chassis") || n.contains("body");
        boolean isChrome = n.contains("chrome") || n.contains("metal") || n.contains("gold") || n.contains("silver") || n.contains("rim_main") || n.contains("rim_no");
        boolean isTire = n.contains("tire") || n.contains("tyre") || n.contains("rubber");
        boolean isCarbon = n.contains("carbon");
        boolean isInterior = n.contains("interior") || n.contains("seat") || n.contains("dashboard") || n.contains("leather");

        if (isReverse) {
            mat.materialKind = MaterialKind.REVERSE_LIGHT;
            mat.emissiveStrength = Math.max(mat.emissiveStrength, 1.35f);
            mat.specularBoost = 1.22f;
            mat.brightnessBoost = 1.08f;
            return;
        }

        if (isTurn) {
            mat.materialKind = MaterialKind.TURN_SIGNAL;
            mat.emissiveStrength = Math.max(mat.emissiveStrength, 1.35f);
            mat.specularBoost = 1.15f;
            mat.brightnessBoost = 1.05f;
            return;
        }

        if (isBrake) {
            mat.materialKind = MaterialKind.BRAKE_LIGHT;
            mat.emissiveStrength = Math.max(mat.emissiveStrength, 1.45f);
            mat.specularBoost = 1.20f;
            mat.brightnessBoost = 1.08f;
            return;
        }

        if (isRearLight) {
            mat.materialKind = MaterialKind.LIGHT_REAR;
            mat.emissiveStrength = Math.max(mat.emissiveStrength, 1.20f);
            mat.specularBoost = 1.18f;
            mat.brightnessBoost = 1.08f;
            return;
        }

        if (isFrontLight) {
            mat.materialKind = MaterialKind.LIGHT_FRONT;
            mat.emissiveStrength = Math.max(mat.emissiveStrength, 1.28f);
            mat.specularBoost = 1.25f;
            mat.brightnessBoost = 1.10f;
            mat.alphaBlend = mat.alphaBlend || isGlass;
            return;
        }

        if (isGlass) {
            mat.materialKind = MaterialKind.GLASS;
            mat.glassAlpha = Math.max(0.32f, Math.min(0.82f, mat.a));
            mat.a = mat.glassAlpha;
            mat.roughness = Math.min(mat.roughness, 0.22f);
            mat.specularBoost = Math.max(mat.specularBoost, 1.45f * mat.specularFactor);
            mat.clearcoatBoost = Math.max(mat.clearcoatBoost, 0.35f + mat.transmissionFactor);
            mat.brightnessBoost = 1.03f;
            mat.alphaBlend = true;
            return;
        }

        if (isPaint) {
            // ArabaOyunu_19: artik boyayi zorla kirmiziya cevirmiyoruz.
            // GLB dosyasinin kendi baseColorTexture/baseColorFactor rengi korunur.
            mat.materialKind = MaterialKind.BODY_PAINT;
            mat.metallic = Math.max(mat.metallic, 0.25f);
            mat.roughness = Math.min(mat.roughness, 0.32f);
            mat.specularBoost = Math.max(mat.specularBoost, 1.45f * mat.specularFactor);
            mat.clearcoatBoost = Math.max(mat.clearcoatBoost, 0.45f + mat.clearcoatFactor * 0.85f);
            mat.brightnessBoost = Math.max(mat.brightnessBoost, 1.08f);
            mat.alphaBlend = false;
            mat.a = 1f;
            return;
        }

        if (isChrome) {
            mat.materialKind = MaterialKind.CHROME;
            mat.metallic = Math.max(mat.metallic, 0.70f);
            mat.roughness = Math.min(mat.roughness, 0.24f);
            mat.specularBoost = Math.max(mat.specularBoost, 1.75f * mat.specularFactor);
            mat.clearcoatBoost = Math.max(mat.clearcoatBoost, 0.40f);
            mat.brightnessBoost = 1.12f;
            return;
        }

        if (isCarbon) {
            mat.materialKind = MaterialKind.CARBON;
            mat.roughness = Math.max(mat.roughness, 0.38f);
            mat.specularBoost = 1.05f;
            mat.brightnessBoost = 1.12f;
            return;
        }

        if (isTire) {
            mat.materialKind = MaterialKind.RUBBER;
            mat.roughness = Math.max(mat.roughness, 0.62f);
            mat.specularBoost = 0.55f;
            mat.brightnessBoost = 0.92f;
            return;
        }

        if (isInterior) {
            mat.materialKind = MaterialKind.INTERIOR;
            mat.roughness = Math.max(mat.roughness, 0.48f);
            mat.specularBoost = 0.85f;
            mat.brightnessBoost = 0.98f;
        }
    }

    private static int textureSource(JSONObject textureInfo, JSONArray textures) throws Exception {
        if (textureInfo == null || textures == null) return -1;
        int textureIndex = textureInfo.optInt("index", -1);
        if (textureIndex < 0 || textureIndex >= textures.length()) return -1;
        JSONObject texture = textures.getJSONObject(textureIndex);
        return texture.optInt("source", -1);
    }

    private static void traverseNode(
            int nodeIndex,
            float[] parentMatrix,
            String parentPath,
            JSONArray nodes,
            JSONArray meshes,
            JSONArray accessors,
            JSONArray bufferViews,
            byte[] binChunk,
            GlbModel model,
            Set<Integer> visited
    ) throws Exception {
        if (nodeIndex < 0 || nodeIndex >= nodes.length()) return;
        if (visited.contains(nodeIndex)) return;
        visited.add(nodeIndex);

        JSONObject node = nodes.getJSONObject(nodeIndex);
        String nodeName = node.optString("name", "node_" + nodeIndex);
        String pathName = parentPath == null || parentPath.length() == 0 ? nodeName : parentPath + "_" + nodeName;

        float[] local = readNodeMatrix(node);
        float[] global = new float[16];
        Matrix.multiplyMM(global, 0, parentMatrix, 0, local, 0);

        int meshIndex = node.optInt("mesh", -1);
        if (meshIndex >= 0 && meshIndex < meshes.length()) {
            JSONObject mesh = meshes.getJSONObject(meshIndex);
            String meshName = mesh.optString("name", "mesh_" + meshIndex);
            JSONArray primitives = mesh.optJSONArray("primitives");
            if (primitives != null) {
                for (int p = 0; p < primitives.length(); p++) {
                    try {
                        JSONObject primitive = primitives.getJSONObject(p);
                        int materialIndex = primitive.optInt("material", 0);
                        String materialName = materialIndex >= 0 && materialIndex < model.materials.size()
                                ? model.materials.get(materialIndex).name
                                : "";
                        // A66.2: Semantik tespitinde tüm parent path kullanmak bazı DLC araçlarda
                        // "Wheel1A" gibi üst node adını bütün gövde parçalarına bulaştırıyordu.
                        // Bu da gövdenin teker sanılıp yalnız teker/cam görünmesine sebep olabiliyordu.
                        // Bu yüzden sınıflandırma artık mevcut node + mesh + material adıyla yapılır;
                        // gerçek tekerler yine kendi node/mesh/material adından veya rubber/konum
                        // refine aşamasından yakalanır.
                        String semanticSearch = nodeName + "_" + meshName + "_" + materialName;
                        GlbMeshPart part = loadPrimitive(nodeName, meshName + "_" + p, semanticSearch, primitive, accessors, bufferViews, binChunk, global);
                        model.totalVertices += part.vertexCount;
                        model.totalIndices += part.indexCount;
                        includeTransformedBounds(model, part);
                        model.parts.add(part);
                    } catch (Throwable t) {
                        GameLog.e(TAG, "Mesh atlandi: " + pathName + "/" + meshName + " sebep=" + t.getMessage(), t);
                    }
                }
            }
        }

        JSONArray children = node.optJSONArray("children");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                traverseNode(children.getInt(i), global, pathName, nodes, meshes, accessors, bufferViews, binChunk, model, visited);
            }
        }
    }

    private static GlbMeshPart loadPrimitive(
            String nodeName,
            String name,
            String semanticSearch,
            JSONObject primitive,
            JSONArray accessors,
            JSONArray bufferViews,
            byte[] binChunk,
            float[] nodeMatrix
    ) throws Exception {
        int mode = primitive.optInt("mode", 4);
        if (mode != 4) {
            throw new IllegalArgumentException("Sadece TRIANGLES mode destekleniyor: " + name + " mode=" + mode);
        }

        JSONObject attrs = primitive.getJSONObject("attributes");
        int positionAccessor = attrs.getInt("POSITION");
        int normalAccessor = attrs.optInt("NORMAL", -1);
        int uvAccessor = attrs.optInt("TEXCOORD_0", -1);
        int indexAccessor = primitive.optInt("indices", -1);

        if (indexAccessor < 0) {
            throw new IllegalArgumentException("Indekssiz mesh su an desteklenmiyor: " + name);
        }

        AccessorReader posReader = new AccessorReader(accessors.getJSONObject(positionAccessor), bufferViews, binChunk);
        AccessorReader normalReader = normalAccessor >= 0 ? new AccessorReader(accessors.getJSONObject(normalAccessor), bufferViews, binChunk) : null;
        AccessorReader uvReader = uvAccessor >= 0 ? new AccessorReader(accessors.getJSONObject(uvAccessor), bufferViews, binChunk) : null;
        AccessorReader indexReader = new AccessorReader(accessors.getJSONObject(indexAccessor), bufferViews, binChunk);

        int vertexCount = posReader.count;
        if (vertexCount <= 0 || vertexCount > 65535) {
            throw new IllegalArgumentException("Mesh vertex sayisi destek disi: " + name + " count=" + vertexCount);
        }

        float[] vertices = new float[vertexCount * GlbMeshPart.FLOATS_PER_VERTEX];
        GlbMeshPart part = new GlbMeshPart();
        part.name = name;
        part.nodeName = semanticSearch == null ? nodeName : semanticSearch;
        part.semanticName = semanticName(semanticSearch == null ? (nodeName + "_" + name) : semanticSearch);

        for (int i = 0; i < vertexCount; i++) {
            int o = i * GlbMeshPart.FLOATS_PER_VERTEX;
            float px = posReader.readFloat(i, 0);
            float py = posReader.readFloat(i, 1);
            float pz = posReader.readFloat(i, 2);
            vertices[o] = px;
            vertices[o + 1] = py;
            vertices[o + 2] = pz;
            part.includeLocal(px, py, pz);

            if (normalReader != null) {
                vertices[o + 3] = normalReader.readFloat(i, 0);
                vertices[o + 4] = normalReader.readFloat(i, 1);
                vertices[o + 5] = normalReader.readFloat(i, 2);
            } else {
                vertices[o + 3] = 0f;
                vertices[o + 4] = 1f;
                vertices[o + 5] = 0f;
            }

            if (uvReader != null) {
                vertices[o + 6] = uvReader.readFloat(i, 0);
                vertices[o + 7] = 1f - uvReader.readFloat(i, 1);
            } else {
                vertices[o + 6] = 0f;
                vertices[o + 7] = 0f;
            }
        }

        int indexCount = indexReader.count;
        short[] indices = new short[indexCount];
        for (int i = 0; i < indexCount; i++) {
            int value = indexReader.readIndex(i);
            if (value < 0 || value >= vertexCount || value > 65535) {
                throw new IllegalArgumentException("Index aralik disi: " + name + " = " + value + " vertexCount=" + vertexCount);
            }
            indices[i] = (short) (value & 0xFFFF);
        }

        part.vertices = vertices;
        part.indices = indices;
        part.vertexCount = vertexCount;
        part.indexCount = indexCount;
        part.materialIndex = primitive.optInt("material", 0);
        System.arraycopy(nodeMatrix, 0, part.localMatrix, 0, 16);
        return part;
    }

    private static void includeTransformedBounds(GlbModel model, GlbMeshPart part) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        float[] xs = new float[] {part.minX, part.maxX};
        float[] ys = new float[] {part.minY, part.maxY};
        float[] zs = new float[] {part.minZ, part.maxZ};
        for (int xi = 0; xi < 2; xi++) {
            for (int yi = 0; yi < 2; yi++) {
                for (int zi = 0; zi < 2; zi++) {
                    float[] p = transform(part.localMatrix, xs[xi], ys[yi], zs[zi]);
                    if (p[0] < minX) minX = p[0];
                    if (p[1] < minY) minY = p[1];
                    if (p[2] < minZ) minZ = p[2];
                    if (p[0] > maxX) maxX = p[0];
                    if (p[1] > maxY) maxY = p[1];
                    if (p[2] > maxZ) maxZ = p[2];
                }
            }
        }
        part.worldMinX = minX;
        part.worldMinY = minY;
        part.worldMinZ = minZ;
        part.worldMaxX = maxX;
        part.worldMaxY = maxY;
        part.worldMaxZ = maxZ;
        model.includeBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void sanitizeVehicleModel(GlbModel model) {
        if (model == null || model.parts.size() == 0) return;

        float allSizeX = Math.max(0.0001f, model.sizeX());
        float allSizeY = Math.max(0.0001f, model.sizeY());
        float allSizeZ = Math.max(0.0001f, model.sizeZ());
        float allMinY = model.minY;

        int skippedProps = 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null) continue;
            GlbMaterial mat = part.materialIndex >= 0 && part.materialIndex < model.materials.size()
                    ? model.materials.get(part.materialIndex)
                    : null;

            if (mat != null && mat.materialKind == MaterialKind.RUBBER && !part.isWheel() && !isScenePropName(part.nodeName)) {
                float sx = part.worldSizeX();
                float sy = part.worldSizeY();
                float sz = part.worldSizeZ();
                boolean compactRubber = sx <= allSizeX * 0.38f && sz <= allSizeZ * 0.38f && sy <= allSizeY * 0.55f;
                if (compactRubber) part.semanticName = "wheel";
            }

            // A64.3: Birçok DLC GLB dosyasında tüm araç parçalarının nodeName'i "Scene"/"Root" geliyor.
            // Eski filtre nodeName üzerinden çalıştığı için gövdeyi sahne objesi sanıp gizleyebiliyordu.
            // Artık generic nodeName prop sebebi değildir; yalnızca mesh/mat adı ve düz zemin geometrisi filtrelenir.
            boolean propByName = isScenePropName(part.name) || (mat != null && isScenePropName(mat.name));
            boolean flatLargeBase = isFlatLargeBase(part, allSizeX, allSizeY, allSizeZ, allMinY);
            boolean neverVehicle = propByName || flatLargeBase;

            if (neverVehicle && !part.isWheel() && !"glass".equals(part.semanticName)) {
                part.skipped = true;
                part.semanticName = "scene_prop";
                part.error = "A51 scene prop filtered";
                skippedProps++;
            }
        }

        rebuildModelBoundsFromVisibleParts(model);
        // A64.5: Eğer filtre sonrası gövde çok zayıf kaldıysa DLC araç garajda boş/yarım görünür.
        // Bu durumda scene_prop olarak işaretlenen düz zemin olmayan parçalar geri açılır.
        // Ama flatLargeBase zemin/platform parçaları yine geri alınmaz.
        if (skippedProps > 0 && visibleNonWheelVehicleParts(model) <= 1) {
            for (int i = 0; i < model.parts.size(); i++) {
                GlbMeshPart part = model.parts.get(i);
                if (part != null && part.isSceneProp() && !isFlatLargeBase(part, allSizeX, allSizeY, allSizeZ, allMinY)) {
                    part.skipped = false;
                    part.error = null;
                    part.semanticName = semanticName((part.nodeName == null ? "" : part.nodeName) + "_" + (part.name == null ? "" : part.name));
                }
            }
            rebuildModelBoundsFromVisibleParts(model);
        }
        refineWheelSemanticsByPosition(model);

        if (!model.hasBounds()) {
            for (int i = 0; i < model.parts.size(); i++) {
                GlbMeshPart part = model.parts.get(i);
                if (part != null && part.isSceneProp()) {
                    part.skipped = false;
                    part.error = null;
                }
            }
            rebuildModelBoundsFromVisibleParts(model);
        }

        int wheels = 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part != null && !part.skipped && part.isWheel()) wheels++;
        }

        GameLog.i(TAG, "A64.5 vehicle sanitize asset=" + model.sourceAsset
                + " skippedSceneProps=" + skippedProps
                + " visibleBodyParts=" + visibleNonWheelVehicleParts(model)
                + " visibleBounds=" + model.minX + "," + model.minY + "," + model.minZ
                + " -> " + model.maxX + "," + model.maxY + "," + model.maxZ
                + " wheels=" + wheels);
    }


    private static int visibleNonWheelVehicleParts(GlbModel model) {
        int count = 0;
        if (model == null) return 0;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped || !part.hasWorldBounds()) continue;
            if (part.isWheel() || "glass".equals(part.semanticName) || part.isSceneProp()) continue;
            count++;
        }
        return count;
    }

    private static boolean isScenePropName(String raw) {
        if (raw == null) return false;
        String v = raw.toLowerCase();
        return containsAny(v,
                "floor", "ground", "plane", "base", "stand", "platform",
                "carpet", "rug", "showroom", "display", "environment",
                "env_", "backdrop", "shadow", "studio", "turntable",
                "road", "asphalt", "grid", "parking", "garage_floor",
                "hali", "halı", "zemin");
    }

    private static boolean isFlatLargeBase(GlbMeshPart part, float allSizeX, float allSizeY, float allSizeZ, float allMinY) {
        if (part == null || !part.hasWorldBounds()) return false;
        float sx = part.worldSizeX();
        float sy = part.worldSizeY();
        float sz = part.worldSizeZ();
        float cy = part.worldCenterY();
        boolean veryFlat = sy <= Math.max(0.015f, allSizeY * 0.050f);
        boolean veryWide = sx >= allSizeX * 0.58f && sz >= allSizeZ * 0.58f;
        boolean low = cy <= allMinY + Math.max(0.030f, allSizeY * 0.090f);
        return veryFlat && veryWide && low;
    }

    private static void rebuildModelBoundsFromVisibleParts(GlbModel model) {
        model.clearBounds();
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped || !part.hasWorldBounds()) continue;
            model.includeBounds(part.worldMinX, part.worldMinY, part.worldMinZ, part.worldMaxX, part.worldMaxY, part.worldMaxZ);
        }
    }

    private static void refineWheelSemanticsByPosition(GlbModel model) {
        if (model == null || !model.hasBounds()) return;
        float cx = model.centerX();
        float cz = model.centerZ();
        boolean frontPositiveZ = detectFrontPositiveZ(model, cz);

        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped || !part.hasWorldBounds()) continue;
            if (!part.isWheel()) continue;
            // ArabaOyunu_52: İsimden gelen hatalı FL/FR/RL/RR değerleri bazı GLB araçlarda
            // arka tekerlere de direksiyon açısı verdiriyordu. Bu yüzden tüm wheel parçaları
            // görünür araç merkezine göre yeniden sınıflandırılır.
            boolean right = part.worldCenterX() >= cx;
            boolean front = frontPositiveZ ? part.worldCenterZ() >= cz : part.worldCenterZ() <= cz;
            if (front && right) part.semanticName = "wheel_fr";
            else if (front) part.semanticName = "wheel_fl";
            else if (right) part.semanticName = "wheel_rr";
            else part.semanticName = "wheel_rl";
        }
    }

    private static boolean detectFrontPositiveZ(GlbModel model, float centerZ) {
        float frontSum = 0f;
        int frontCount = 0;
        float rearSum = 0f;
        int rearCount = 0;
        if (model == null) return true;
        for (int i = 0; i < model.parts.size(); i++) {
            GlbMeshPart part = model.parts.get(i);
            if (part == null || part.skipped || !part.hasWorldBounds()) continue;
            GlbMaterial mat = part.materialIndex >= 0 && part.materialIndex < model.materials.size()
                    ? model.materials.get(part.materialIndex)
                    : null;
            if (mat == null) continue;
            if (mat.materialKind == MaterialKind.LIGHT_FRONT || "front_light".equals(part.semanticName)) {
                frontSum += part.worldCenterZ();
                frontCount++;
            } else if (mat.materialKind == MaterialKind.LIGHT_REAR
                    || mat.materialKind == MaterialKind.BRAKE_LIGHT
                    || "rear_light".equals(part.semanticName)
                    || "brake_light".equals(part.semanticName)) {
                rearSum += part.worldCenterZ();
                rearCount++;
            }
        }
        if (frontCount > 0 && rearCount > 0) {
            return (frontSum / frontCount) >= (rearSum / rearCount);
        }
        if (frontCount > 0) return (frontSum / frontCount) >= centerZ;
        if (rearCount > 0) return (rearSum / rearCount) < centerZ;
        return true;
    }

    private static float[] transform(float[] m, float x, float y, float z) {
        return new float[] {
                m[0] * x + m[4] * y + m[8] * z + m[12],
                m[1] * x + m[5] * y + m[9] * z + m[13],
                m[2] * x + m[6] * y + m[10] * z + m[14]
        };
    }

    private static String semanticName(String value) {
        if (value == null) return "part";
        String v = value.toLowerCase();

        // ArabaOyunu_48:
        // Cam/window/windshield meshleri asla teker gibi animasyon almaz.
        // Bazı GLB'lerde cam materyal adı path'e girdiği için wheel/rim kelimeleriyle karışabiliyordu.
        boolean glassFamily = v.contains("glass")
                || v.contains("window")
                || v.contains("windshield")
                || v.contains("windscreen")
                || v.contains("clear_glass");
        if (glassFamily) return "glass";

        // Direksiyon simidi "steering wheel" olarak gelebilir; yol tekeri ile karismasin.
        boolean steeringWheel = v.contains("steeringwheel") || v.contains("steering_wheel") || v.contains("steering wheel");

        // ArabaOyunu_20/25: teker yonu icin once en acik patternler okunur.
        // Parent path icindeki baska LF/RF kelimeleri yanlis eslesme yapmasin diye
        // wheel2a_rf, 3dwheel_front_r gibi patternler generic _lf/_rf'ten once gelir.
        if (!steeringWheel) {
            if (containsAny(v, "wheel2a_rf", "wheel_rf", "wheel_fr", "wheelfr", "3dwheel_front_r", "front_r", "front r", "frontright", "front-right", "front_right", "right_front", "right front", "rf_wheel", "tire_rf", "tyre_rf", "rim_rf", "fr_wheel", "tire_fr", "tyre_fr", "rim_fr")) return "wheel_fr";
            if (containsAny(v, "wheel2a_lf", "wheel_lf", "wheel_fl", "wheelfl", "3dwheel_front_l", "front_l", "front l", "frontleft", "front-left", "front_left", "left_front", "left front", "lf_wheel", "tire_lf", "tyre_lf", "rim_lf", "fl_wheel", "tire_fl", "tyre_fl", "rim_fl")) return "wheel_fl";
            if (containsAny(v, "wheel2a_rr", "wheel_rr", "wheelrr", "3dwheel_rear_r", "rear_r", "rear r", "rearright", "rear-right", "back_r", "back r", "right_rear", "right rear", "rr_wheel", "tire_rr", "tyre_rr", "rim_rr")) return "wheel_rr";
            if (containsAny(v, "wheel2a_lr", "wheel2a_rl", "wheel_lr", "wheel_rl", "wheelrl", "3dwheel_rear_l", "rear_l", "rear l", "rearleft", "rear-left", "back_l", "back l", "left_rear", "left rear", "lr_wheel", "rl_wheel", "tire_lr", "tyre_lr", "rim_lr", "tire_rl", "tyre_rl", "rim_rl")) return "wheel_rl";
        }

        boolean wheelFamily =
                !steeringWheel && !glassFamily && (
                v.contains("wheel") ||
                v.contains("tyre") ||
                v.contains("tire") ||
                v.contains("rim") ||
                v.contains("geo_tyre") ||
                v.contains("ext_wheel") ||
                v.contains("tread") ||
                v.contains("alloy"));

        if (wheelFamily) {
            // Son guvenli fallback: sadece teker ailesi oldugu kesinlesince kisa ekleri kullan.
            if (containsAny(v, "_rf", " rf", "-rf", ".rf")) return "wheel_fr";
            if (containsAny(v, "_lf", " lf", "-lf", ".lf")) return "wheel_fl";
            if (containsAny(v, "_rr", " rr", "-rr", ".rr")) return "wheel_rr";
            if (containsAny(v, "_lr", " lr", "-lr", ".lr", "_rl", " rl", "-rl", ".rl")) return "wheel_rl";
            return "wheel";
        }

        // ArabaOyunu_25: isik semantikleri daha genis yakalanir.
        if (containsAny(v, "reverse", "backup", "back_up", "white_light_rear")) return "reverse_light";
        if (containsAny(v, "brake", "stop", "brakelight", "brake_light")) return "brake_light";
        if (containsAny(v, "indicator", "signal", "turn", "blinker", "orange_glass", "amber")) return "turn_signal";
        if (containsAny(v, "tail", "rear_light", "taillight", "tail_light", "red_glass", "rear lamp", "rear_lamp")) return "rear_light";
        if (containsAny(v, "headlight", "head_light", "lowbeam", "highbeam", "running_lights", "front_lamp", "front light", "front_light", "clear_glass")) return "front_light";

        if (v.contains("glass") || v.contains("window") || v.contains("windshield") || v.contains("windscreen")) return "glass";
        if (v.contains("carpaint") || v.contains("paint") || v.contains("body") || v.contains("chassis") || v.contains("ext_")) return "paint";
        if (v.contains("door_l")) return "door_l";
        if (v.contains("door_r")) return "door_r";
        if (v.contains("hood") || v.contains("bonnet")) return "hood";
        if (v.contains("steer")) return "steering_wheel";
        return "part";
    }

    private static boolean containsAny(String value, String... patterns) {
        if (value == null || patterns == null) return false;
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i] != null && value.contains(patterns[i])) return true;
        }
        return false;
    }

    private static float[] readNodeMatrix(JSONObject node) {
        float[] out = new float[16];
        Matrix.setIdentityM(out, 0);

        JSONArray matrix = node.optJSONArray("matrix");
        if (matrix != null && matrix.length() >= 16) {
            for (int i = 0; i < 16; i++) {
                out[i] = (float) matrix.optDouble(i, i % 5 == 0 ? 1.0 : 0.0);
            }
            return out;
        }

        JSONArray t = node.optJSONArray("translation");
        if (t != null && t.length() >= 3) {
            Matrix.translateM(out, 0,
                    (float) t.optDouble(0, 0.0),
                    (float) t.optDouble(1, 0.0),
                    (float) t.optDouble(2, 0.0));
        }

        JSONArray r = node.optJSONArray("rotation");
        if (r != null && r.length() >= 4) {
            float[] rot = quaternionToMatrix(
                    (float) r.optDouble(0, 0.0),
                    (float) r.optDouble(1, 0.0),
                    (float) r.optDouble(2, 0.0),
                    (float) r.optDouble(3, 1.0));
            float[] tmp = new float[16];
            Matrix.multiplyMM(tmp, 0, out, 0, rot, 0);
            out = tmp;
        }

        JSONArray s = node.optJSONArray("scale");
        if (s != null && s.length() >= 3) {
            Matrix.scaleM(out, 0,
                    (float) s.optDouble(0, 1.0),
                    (float) s.optDouble(1, 1.0),
                    (float) s.optDouble(2, 1.0));
        }

        return out;
    }

    private static float[] quaternionToMatrix(float x, float y, float z, float w) {
        float[] m = new float[16];
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float xy = x * y;
        float xz = x * z;
        float yz = y * z;
        float wx = w * x;
        float wy = w * y;
        float wz = w * z;

        m[0] = 1f - 2f * (yy + zz);
        m[1] = 2f * (xy + wz);
        m[2] = 2f * (xz - wy);
        m[3] = 0f;

        m[4] = 2f * (xy - wz);
        m[5] = 1f - 2f * (xx + zz);
        m[6] = 2f * (yz + wx);
        m[7] = 0f;

        m[8] = 2f * (xz + wy);
        m[9] = 2f * (yz - wx);
        m[10] = 1f - 2f * (xx + yy);
        m[11] = 0f;

        m[12] = 0f;
        m[13] = 0f;
        m[14] = 0f;
        m[15] = 1f;
        return m;
    }

    private static byte[] readAsset(Context context, String assetPath) throws Exception {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        try {
            in = context.getAssets().open(assetPath);
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static final class AccessorReader {
        private final ByteBuffer data;
        private final int baseOffset;
        private final int stride;
        private final int componentType;
        private final int componentCount;
        private final int componentSize;
        private final boolean normalized;
        public final int count;

        AccessorReader(JSONObject accessor, JSONArray bufferViews, byte[] binChunk) throws Exception {
            int bufferViewIndex = accessor.optInt("bufferView", -1);
            if (bufferViewIndex < 0) {
                throw new IllegalArgumentException("Accessor bufferView eksik");
            }
            JSONObject bv = bufferViews.getJSONObject(bufferViewIndex);
            int viewOffset = bv.optInt("byteOffset", 0);
            int accessorOffset = accessor.optInt("byteOffset", 0);
            baseOffset = viewOffset + accessorOffset;
            componentType = accessor.getInt("componentType");
            componentCount = componentsForType(accessor.getString("type"));
            componentSize = sizeForComponentType(componentType);
            count = accessor.getInt("count");
            normalized = accessor.optBoolean("normalized", false);
            stride = bv.optInt("byteStride", componentCount * componentSize);
            data = ByteBuffer.wrap(binChunk).order(ByteOrder.LITTLE_ENDIAN);
        }

        float readFloat(int index, int component) {
            int offset = baseOffset + index * stride + component * componentSize;
            if (componentType == 5126) {
                return data.getFloat(offset);
            }
            if (componentType == 5123) {
                int value = data.getShort(offset) & 0xFFFF;
                return normalized ? value / 65535f : value;
            }
            if (componentType == 5122) {
                short value = data.getShort(offset);
                return normalized ? Math.max(-1f, value / 32767f) : value;
            }
            if (componentType == 5125) {
                long value = data.getInt(offset) & 0xFFFFFFFFL;
                return normalized ? value / 4294967295f : (float) value;
            }
            if (componentType == 5121) {
                int value = data.get(offset) & 0xFF;
                return normalized ? value / 255f : value;
            }
            if (componentType == 5120) {
                byte value = data.get(offset);
                return normalized ? Math.max(-1f, value / 127f) : value;
            }
            return 0f;
        }

        int readIndex(int index) {
            int offset = baseOffset + index * stride;
            if (componentType == 5121) {
                return data.get(offset) & 0xFF;
            }
            if (componentType == 5123) {
                return data.getShort(offset) & 0xFFFF;
            }
            if (componentType == 5125) {
                long value = data.getInt(offset) & 0xFFFFFFFFL;
                if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
                return (int) value;
            }
            return 0;
        }

        private static int componentsForType(String type) {
            if ("SCALAR".equals(type)) return 1;
            if ("VEC2".equals(type)) return 2;
            if ("VEC3".equals(type)) return 3;
            if ("VEC4".equals(type)) return 4;
            if ("MAT4".equals(type)) return 16;
            return 1;
        }

        private static int sizeForComponentType(int type) {
            if (type == 5120 || type == 5121) return 1;
            if (type == 5122 || type == 5123) return 2;
            if (type == 5125 || type == 5126) return 4;
            return 4;
        }
    }
}
