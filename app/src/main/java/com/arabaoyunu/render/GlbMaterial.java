package com.arabaoyunu.render;

public final class GlbMaterial {
    public String name = "material";

    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float a = 1f;

    public float metallic = 0f;
    public float roughness = 0.65f;
    public float emissiveR = 0f;
    public float emissiveG = 0f;
    public float emissiveB = 0f;
    public float emissiveStrength = 1f;

    public int imageIndex = -1;
    public int metallicRoughnessImageIndex = -1;
    public int normalImageIndex = -1;
    public int emissiveImageIndex = -1;
    public int occlusionImageIndex = -1;

    public boolean alphaBlend;
    public boolean doubleSided;
    public String alphaMode = "OPAQUE";

    // ArabaOyunu_19 KHR material extension PBR-lite alanlari.
    public float clearcoatFactor = 0f;
    public float clearcoatRoughnessFactor = 0.35f;
    public float specularFactor = 1f;
    public float specularColorR = 1f;
    public float specularColorG = 1f;
    public float specularColorB = 1f;
    public float transmissionFactor = 0f;
    public float ior = 1.45f;

    // KHR_texture_transform hazirlik alanlari. Shader tam matrix uygulamasi sonraki asamada genisletilebilir.
    public float texOffsetU = 0f;
    public float texOffsetV = 0f;
    public float texScaleU = 1f;
    public float texScaleV = 1f;

    // MaterialKind sabitleri kullanilir.
    public int materialKind = MaterialKind.DEFAULT;
    public float specularBoost = 1f;
    public float brightnessBoost = 1f;
    public float clearcoatBoost = 0f;
    public float glassAlpha = 1f;
}
