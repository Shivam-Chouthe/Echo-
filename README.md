# Echo - AI-Powered Memory Assistant 🌀

**Echo** is a high-context memory assistant designed for Android. It transforms the "Share to Save" habit into an "Echo to Remember" workflow. Instead of content dying in a bookmark folder, Echo uses on-device intelligence to categorize, extract meaning, and "echo" it back to you exactly when it matters.

## 🚀 Key Features

### 1. Zero-Friction Capture
*   **Share Sheet Integration:** Instantly capture any text or link from any app.
*   **Intelligent OCR:** Snap a photo or share a screenshot; Echo extracts text locally using ML Kit.
*   **Ambient Pulse UI:** A non-intrusive capture overlay that lets you get back to your flow immediately.

### 2. Hybrid Intelligence Layer
*   **Rule-Based Engine:** Fast, local classification for Tasks, Events, and Recipes.
*   **AI Refinement:** (On-Device) Leverages LLMs to refine titles, summarize intents, and understand context without your data leaving the phone.
*   **Entity Extraction:** Automatically detects dates, locations, and action items.

### 3. Smart Notifications ("Echoes")
*   **Date-Aware Reminders:** Echoes are automatically scheduled based on dates found in the content (e.g., "Deadline tomorrow" schedules a notification).
*   **Contextual Actions:** One-tap actions from the Detail screen:
    *   **Events:** Add to Calendar.
    *   **Places:** Open in Google Maps.
    *   **Recipes/Topics:** Instant Web Search.
    *   **Tools:** Direct link opening.

### 4. Office Kit Sync
*   **Laptop Dashboard:** One-click sync that generates a portable HTML dashboard of your Echoes, ready to be viewed on your PC or Mac.

## 🛠️ Tech Stack

*   **UI:** Jetpack Compose (Material 3)
*   **Language:** Kotlin
*   **Database:** Room Persistence Library
*   **ML/AI:** 
    *   Google ML Kit (Text Recognition)
    *   MediaPipe LLM Inference (On-Device GenAI)
*   **Background:** AlarmManager for precise "Echo" scheduling.
*   **Architecture:** Clean Architecture with ViewModel and Repository patterns.

## 📂 Project Structure

*   `capture/`: Handles Share Intent and OCR processing.
*   `ocr/`: The intelligence core (Categorizer, EntityExtractor, AI Processor).
*   `data/`: Room database and EchoItem models.
*   `notifications/`: Alarm scheduling and notification management.
*   `sync/`: Laptop Dashboard generation logic.
*   `ui/`: Modern Compose-based dashboard and detail screens.

---

## 🏗️ Getting Started

1.  **Build:** Open in Android Studio (Koala/Ladybug+) and sync Gradle.
2.  **Deploy:** Target an Android device (API 26+). 
    *   *Note: On-device AI features optimized for high-end NPU/GPU devices.*
3.  **Usage:** Find something interesting in Chrome, Twitter, or your Gallery, and hit **Share > Echo**.

---
*Developed for the Android Hackathon 2024.*
