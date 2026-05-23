# Asisten AI Pribadi

Android app pribadi dengan arsitektur host-agent mobile-first yang mirip arah AnyClaw:

- host lokal di HP
- dashboard chat / sessions / tools / providers
- local runtime atau local bridge stub
- dukung banyak provider AI dari awal

## Phase 1 yang sudah saya bentuk

- Kotlin native + Jetpack Compose
- clean architecture sederhana
- provider abstraction untuk `OpenAI` dan `Gemini`
- auth interface dengan 2 jalur:
  - official account login slot
  - fallback encrypted API key profile
- dashboard provider yang jelas:
  - connected / disconnected
  - auth type
  - model aktif
  - provider aktif
- session chat lokal
- tools / local bridge stub
- credential sensitif disimpan aman di encrypted storage

## Jujur soal login resmi

Phase 1 ini **tidak mengklaim** bisa login akun OpenAI atau Gemini secara resmi dari third-party Android app kalau memang jalur resminya belum ada atau belum dipasang.

Yang saya sediakan sekarang:

- interface official login tetap ada
- tapi gateway stub akan bilang terus terang kalau jalur itu belum tersedia
- fallback yang dipakai adalah encrypted API key profile

## Yang benar-benar runnable sekarang

- buka dashboard host agent di HP
- bikin session chat
- kirim prompt ke chat stub
- lihat tools / local bridge stub
- connect OpenAI via encrypted API key profile
- connect Gemini via encrypted API key profile
- pilih provider aktif

## Yang masih stub / mock

- verifikasi real credential ke endpoint OpenAI
- verifikasi real credential ke endpoint Gemini
- streaming response real
- official account login
- tool execution native yang benar-benar penuh seperti AnyClaw produksi

## Struktur

```text
app/src/main/java/com/aistudio/aiagent/pxtmre
├── data
│   ├── local
│   ├── secure
│   ├── service
│   └── DefaultAgentRepository.kt
├── domain
│   ├── model
│   ├── repository
│   └── service
├── presentation
│   ├── ui
│   └── AgentViewModel.kt
└── MainActivity.kt
```

## Cara build

1. Buka folder project ini di Android Studio.
2. Pastikan JDK 17 aktif.
3. Sync Gradle.
4. Run ke device atau emulator.

## Asumsi

- target awal adalah app host-agent pribadi, bukan clone full AnyClaw production
- integrasi provider real bisa ditambah di Phase 2
- requirement utama Phase 1 adalah fondasi jujur, runnable, dan aman
