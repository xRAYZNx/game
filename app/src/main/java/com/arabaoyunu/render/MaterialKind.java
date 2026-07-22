package com.arabaoyunu.render;

/**
 * ArabaOyunu_19: Arac odakli material siniflandirmasi.
 * Shader tarafinda int/float uniform olarak kullanilir.
 */
public final class MaterialKind {
    public static final int DEFAULT = 0;
    public static final int BODY_PAINT = 1;
    public static final int GLASS = 2;
    public static final int CHROME = 3;
    public static final int RUBBER = 4;
    public static final int LIGHT_FRONT = 5;
    public static final int LIGHT_REAR = 6;
    public static final int BRAKE_LIGHT = 7;
    public static final int TURN_SIGNAL = 8;
    public static final int REVERSE_LIGHT = 9;
    public static final int CARBON = 10;
    public static final int INTERIOR = 11;

    private MaterialKind() {}
}
