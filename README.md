ENGLISH  -->
# 📱 Argue - AI Debate Partner

Argue is a mobile application designed for debate practice, where a user presents an idea and an AI (Google Gemini) responds with counter-arguments. 
The goal is to better equip users for real-world discussions by preparing them for potential opposing viewpoints.

## ✨ Features

- **AI Opponent:** Real-time debate powered by Google's powerful Gemini 2.5 Flash model.
- **Expert Contrarian:** The AI challenges the user by taking the opposite stance on every argument without exception.
- **Dual-Mode Experience:**
    - **Text Mode:** A classic, text-based chat interface.
    - **Voice Mode:** A fully voice-driven debate experience using the device's native Speech-to-Text (STT) and Text-to-Speech (TTS) engines, complete with a live transcript.
- **Rule-Based Debates:** Each session is limited to 10 turns, after which the AI gives a final "verdict" on whether it was convinced by the user's arguments.
- **Modern UI:** A sleek, edge-to-edge design featuring gradient backgrounds and stylish chat bubbles.
- **Dynamic Controls:** User-friendly controls to start a new chat and switch between text and voice modes.

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Platform:** Android Native (XML Views)
- **Artificial Intelligence:** Google Gemini API (via Google AI Client SDK for Android)
- **Asynchronous Programming:** Kotlin Coroutines & `lifecycleScope`
- **UI Components:** `RecyclerView`, `ConstraintLayout`, `Toolbar`, Material Design Components
- **Audio Technologies:**
    - **Text-to-Speech (TTS):** Android's native TTS Engine.
    - **Speech-to-Text (STT):** Android's native `SpeechRecognizer` service.
- **Security:** The API key is securely stored using `local.properties` and `BuildConfig`.

## ⚙️ Setup and Installation

To run this project on your local machine:

1.  Clone this repository: `git clone https://github.com/YOUR-USERNAME/ArgueApp.git`
2.  Open the project in Android Studio.
3.  Create a file named `local.properties` in the root directory of the project.
4.  Add your Gemini API key, obtained from Google AI Studio, to this file in the following format:
    ```properties
    GEMINI_API_KEY="YOUR_API_KEY_HERE"
    ```
5.  Sync the project with Gradle files and run the application.

## 🔮 Future Plans

- [ ] **Chat History:** Save and list past conversations (using the Room database).
- [ ] **AI Personalities:** Selectable debater profiles, such as calmer, more aggressive, or more sarcastic.
- [ ] **User Profiles:** Track statistics like win/loss ratios.

---
This project was built from scratch with a 20-day target.

TÜRKÇE -->
# 📱 Argue - Yapay Zeka Tartışma Partneri

Argue, kullanıcıların bir fikir sunduğu ve yapay zekanın (Google Gemini) bu fikre her zaman karşıt argümanlarla cevap verdiği, tartışma pratiği yapmaya yönelik bir mobil uygulamadır.
Amacı, kullanıcıları potansiyel karşı argümanlara hazırlayarak daha donanımlı ve ikna kabiliyeti yüksek bireyler haline getirmektir.

## ✨ Özellikler

- **Yapay Zeka Rakip:** Google'ın güçlü Gemini 2.5 Flash modeli ile gerçek zamanlı tartışma imkanı.
- **Karşıt Görüş Uzmanı:** AI, sunulan her argümana istisnasız olarak karşı çıkarak kullanıcıyı zorlar.
- **Çift Modlu Deneyim:**
    - **Metin Modu:** Klasik yazılı sohbet arayüzü.
    - **Sesli Mod:** Cihazın kendi ses tanıma ve sesli okuma (TTS) motorlarını kullanarak tamamen sesli bir tartışma deneyimi ve konuşmaların canlı deşifresi.
- **Kurallı Tartışma:** Her tartışma seansı, 10 tur ile sınırlıdır ve sonunda yapay zeka, kullanıcının argümanları tarafından ikna olup olmadığına dair nihai bir "karar" verir.
- **Modern Arayüz:** Gradyan arka planlar ve şık sohbet balonları ile modern, uçtan uca (edge-to-edge) bir tasarım.
- **Dinamik Kontroller:** Sohbeti sıfırlama, sesli moda geçme gibi kullanıcı dostu kontroller.

## 🛠️ Kullanılan Teknolojiler

- **Dil:** Kotlin
- **Platform:** Android (XML Views)
- **Yapay Zeka:** Google Gemini API (Google AI Client SDK for Android aracılığıyla)
- **Asenkron Programlama:** Kotlin Coroutines & `lifecycleScope`
- **Arayüz (UI):** `RecyclerView`, `ConstraintLayout`, `Toolbar`, Material Design Components
- **Ses Teknolojileri:**
    - **Text-to-Speech (TTS):** Android'in dahili metin okuma motoru.
    - **Speech-to-Text (STT):** Android'in dahili `SpeechRecognizer` hizmeti.
- **Güvenlik:** API anahtarı, `local.properties` ve `BuildConfig` kullanılarak güvenli bir şekilde saklanmaktadır.

## ⚙️ Kurulum ve Çalıştırma

Bu projeyi yerel makinenizde çalıştırmak için:

1.  Bu depoyu klonlayın: `git clone https://github.com/KULLANICI-ADIN/ArgueApp.git`
2.  Projeyi Android Studio'da açın.
3.  Projenin ana dizininde `local.properties` adında bir dosya oluşturun.
4.  Google AI Studio'dan aldığınız Gemini API anahtarınızı bu dosyanın içine aşağıdaki formatta ekleyin:
    ```properties
    GEMINI_API_KEY="SENIN_API_ANAHTARIN"
    ```
5.  Projeyi senkronize edin ve çalıştırın.

## 🔮 Gelecek Planları

- [ ] **Geçmiş Sohbetler:** Tamamlanan tartışmaların kaydedilmesi ve listelenmesi (Room veritabanı ile).
- [ ] **Farklı AI Kişilikleri:** Daha sakin, daha agresif veya daha alaycı gibi seçilebilir tartışmacı profilleri.
- [ ] **Kullanıcı Profilleri:** Kazanma/kaybetme oranı gibi istatistiklerin tutulması.

---
Bu proje, 20 günlük bir hedefle sıfırdan oluşturulmuştur.
