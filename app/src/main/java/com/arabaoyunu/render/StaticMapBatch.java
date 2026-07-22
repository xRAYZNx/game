package com.arabaoyunu.render;

/**
 * ArabaOyunu_61_2: Statik GLB haritalar araç gibi işlenmez. Bu küçük sınıf,
 * aynı materyal altında çizilecek parça grubunun istatistiğini tutar. Meshleri
 * tek büyük buffera zorla birleştirmeyiz; büyük haritalarda runtime bellek patlaması
 * yaşamamak için güvenli materyal-gruplu draw akışı kullanılır.
 */
public final class StaticMapBatch {
    public final int materialIndex;
    public final int[] partIndices;
    public final int partCount;
    public final int indexCount;

    public StaticMapBatch(int materialIndex, int[] partIndices, int partCount, int indexCount) {
        this.materialIndex = materialIndex;
        this.partIndices = partIndices == null ? new int[0] : partIndices;
        this.partCount = Math.max(0, partCount);
        this.indexCount = Math.max(0, indexCount);
    }
}
