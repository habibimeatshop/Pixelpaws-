# Pixel Paws — Android overlay pet

Prototipe game peliharaan pixel yang tampil di atas aplikasi lain. Dibuat native dengan Java dan Android Canvas, tanpa library pihak ketiga.

## Fitur V0.1

- Kucing pixel pastel transparan di atas aplikasi lain
- Ketuk untuk mengelus dan membuka menu mini
- Seret bebas untuk memindahkan pet
- Makan, bermain, tidur, level, XP, dan koin
- Hunger, happiness, dan energy tersimpan otomatis
- Foreground service dengan notifikasi Android
- Tombol tampilkan/sembunyikan di aplikasi utama

## Menjalankan

1. Buka folder ini dengan Android Studio (Ladybug atau lebih baru).
2. Tunggu Gradle Sync selesai.
3. Hubungkan HP Android, lalu pilih **Run app**.
4. Tekan **Tampilkan kucing** dan izinkan **Tampil di atas aplikasi lain**.
5. Kembali ke aplikasi lalu tekan **Tampilkan kucing** sekali lagi.

Min SDK 26 (Android 8), target SDK 35. Overlay tidak didukung di iPhone. Beberapa merek HP perlu mengizinkan autostart/battery unrestricted agar pet tidak dihentikan sistem.

## Build APK

Di Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

Proyek juga menyertakan GitHub Actions. Setelah source dimasukkan ke repository GitHub,
workflow **Build Pixel Paws APK** otomatis membangun APK dan menyediakan hasilnya sebagai
artifact `PixelPaws-v0.1-debug-apk` yang dapat diunduh dan dipasang.

Untuk distribusi publik, tambahkan adaptive icon, privacy policy, signing config, pengujian battery optimization, serta deklarasi penggunaan foreground service di Play Console.
