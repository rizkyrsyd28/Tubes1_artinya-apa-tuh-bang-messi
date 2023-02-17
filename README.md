# Galaxio

## Description
Galaxio sendiri merupakan chalenge yang diadakan oleh Entelect. Galaxio merupakan permainan pesawat ruang angkasa. Pada permainan ini terdapat beberapa objek seperti food, superfood, torpedo, supernova, gas cloud, asteroid, dan player. Setiap objek memiliki efek yang berbeda-beda kepada player. Tujuan dari permainan ini adalah siapa yang bertahan sampai akhir dan memiliki skor tertinggi akan menjadi pemenangnya.

Pada bot yang telah kami rancang, kami menggunakan algoritma greedy untuk memenangkan permainan ini. Pertama, bot akan bertahan ketika ada serangan seperti teleport, supernova, torpedo, dan kejaran lawan. Ketika kondisi bertahan sudah tidak terpenuhi, maka bot akan mencari makan. Dan setelah cukup amunisi dan makanannya, maka bot akan menyerang dengan teleport, torpedo, dan supernova.



## Requirement
* NET Core 3.1
* JDK 1.8
* Apache Maven/VS Code/Intellij idea (untuk build jar)
* Clone repository ini
* Download starter-pack di https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2

## Build and Run
* Download starter-pack di https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2
* Buat file run.bat
* Ubah konfigurasi permainan sesuai keinginan (jumlah bot, dll) pada file appsettings.json pada runner-publish dan engine-publish
* Clone repository ini
* Build dengan maven akan menghasilkan folder target dan file .jar
* Isi file run.bat dengan
```
@echo off
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../reference-bot-publish/
timeout /t 3
start "" dotnet referenceBot.dll
timeout /t 3
start "" dotnet referenceBot.dll
timeout /t 3
start "" java -jar [path file jar]
timeout /t 3
start "" java -jar [path file jar]
cd ../

pause
```
* Jalankan file run.bat

## Useful Links
* Repo Utama: https://github.com/EntelectChallenge/2021-Galaxio
* Rules: https://github.com/EntelectChallenge/2021-Galaxio/blob/develop/game-engine/game-rules.md#torpedo-salvo
* Starter-pack: https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2

## Video
* https://youtu.be/9bT1n0zgd08

## Authors
* Mutawally Nawwar (1352165)
* Vieri Fajar Firdaus (13521099)
* Rizky Abdillah Rasyid (13521109)