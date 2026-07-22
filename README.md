# ArabaOyunu

Android/Java ve OpenGL ES tabanlı çevrimdışı araç oyunu projesidir. Derleme
zinciri Java 17, Gradle 8.2.1 ve Android Gradle Plugin 8.2.2 kullanır.

## Derleme

- Java 17 kurulu olmalıdır.
- Android SDK Platform 28 ve Build Tools 34.0.0 kurulu olmalıdır.
- Proje kökünde `./gradlew assembleDebug` komutunu çalıştırın.

APK çıktısı `app/build/outputs/apk/debug/app-debug.apk` yolunda oluşur.

## GitHub Actions

`.github/workflows/build_apk.yml` workflow'u `main` veya `master` branch'ine
push yapıldığında otomatik çalışır. Java 17 ve Android SDK'yı kurar,
`./gradlew assembleDebug` komutunu çalıştırır ve oluşan APK'yı
`ArabaOyunu-debug-apk` adlı indirilebilir artifact olarak 30 gün saklar.

Workflow GitHub arayüzündeki **Actions** sekmesinden manuel olarak da
çalıştırılabilir.

## Optimize araç modeli

Projede tek üretim araç modeli bulunur:

`app/src/main/assets/models/car_main.glb`

Araç kataloğundaki mevcut kimlikler, kayıtlar, ekonomi ve ilerleme sistemi
geriye uyumluluk için korunmuştur. Bütün katalog girişleri merkezi olarak bu
tek doğrulanmış modele yönlendirilir.

Showroom ortamı araç modeli değildir ve ayrı statik sahne olarak korunur:

`app/src/main/assets/models/showroom/scifi_tron_studio__baked.glb`

## Ana yapı

- `app/src/main/java/com/arabaoyunu/`: oyun kaynak kodu
- `app/src/main/assets/audio/`: kullanılan ses varlıkları
- `app/src/main/assets/models/`: tek araç modeli ve showroom sahnesi
- `app/src/main/res/`: Android kaynakları ve kullanılan yükleme videosu
- `app/build.gradle`: Android uygulama modülü
- `.github/workflows/build_apk.yml`: otomatik APK derleme workflow'u
- `gradlew`, `gradlew.bat`, `gradle/wrapper/`: Gradle Wrapper dosyaları
