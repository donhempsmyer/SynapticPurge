# Synaptic Purge

A focused Android application for capturing audio recordings and generating automated transcriptions, summaries, and actionable lists using the **Gemini Developer** API. Built for low-friction “thought dumping” and cognitive offloading.

---

## ✨ MVP Features

- **Full-Bleed Recording UI**  
  Minimal, immersive recording overlay with “breathing” visual feedback.

- **Automated Transcription**  
  Uses **Gemini 2.5 Flash** to convert recorded audio into structured, punctuated notes.

- **Local Persistence**  
  Recordings and transcriptions are managed locally (Room) for offline access and efficient retrieval.

- **Material 3 Design**  
  Built with modern Jetpack Compose components and smooth state-driven animations.

- **Audio Playback**  
  Read your transcriptions or play your recordings.

---

## ✨✨ Beta Features

- **Summarization and Organization**  
  Gemini summarizes transcriptions and outputs them into organized notes, tasks and plans.
  View your summaries in an organized page seperated by Dates.

- **Settings**  
  Usefull settings for UI themes and managing your files.

---

## 🧱 Tech Stack

- **UI:** Jetpack Compose (Material 3)
- **Database:** Room Persistence Library
- **AI:** Gemini via Firebase AI Logic (Google AI / Firebase AI integration)
- **Audio:** `MediaRecorder`
- **Language:** Kotlin + Coroutines / Flow (StateFlow)
- **Dependency Injection** Hilt

---

## 🏗️ Architecture

The project follows recommended Android architecture guidelines (MVVM):

- **UI Layer:**  
  `PurgeRoute` and `PurgeScreen` (Compose-based, reactive UI)

- **State Management:**  
  `PurgeViewModel` using **StateFlow** to manage recording state and data streams

- **Data Layer:**  
  `RecordingsDao` for SQLite operations (file paths + transcription results)

---

## ⚙️ Setup / Configuration

To run this project locally:

1. **Clone Repository into an Android Studio project**
   - Ensure all dependencies are resolved
  
2. **Create/Register the app in Firebase**
   - Add an Android app entry in the Firebase Console (use your app’s package name).

3. **Enable required Firebase services**
   - Enable **Firebase AI Logic / Gemini Developer API** (for Gemini)
   - Enable **Anonymous Authentication**

4. **Add `google-services.json`**
   - Download `google-services.json` from Firebase
   - Place it in:
     
     app/google-services.json
     

5. **Sync & Run**
   - Sync Gradle
   - Run on a device/emulator (mic permission required)

---

## 🔒 Permissions

- `RECORD_AUDIO` — required for capturing audio input
- `INTERNET` — required for Gemini API call


---

## 📌 Notes

- Audio files are stored locally and can optionally be auto-deleted after successful transcription (depending on user settings / implementation).
- Transcription quality depends on input clarity and background noise.

---
