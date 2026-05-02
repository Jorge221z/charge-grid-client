# 📱 ChargeGrid Android App

ChargeGrid is a native Android application designed to interact with the **ChargeGrid Electric Vehicle (EV)** charging network. This client provides a modern, reactive user interface for viewing charging stations, monitoring real-time statuses, and managing the charging grid on the go.

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (100% Declarative UI)
*   **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
*   **Reactive Flow:** Kotlin Coroutines & `StateFlow`
*   **Networking:** Retrofit & OkHttp
*   **Serialization:** Kotlinx Serialization (with Custom Serializers for `LocalDateTime`)
*   **Build Tool:** Gradle (Kotlin DSL & Version Catalogs)

## 🚀 Features (MVP)

*   **Station Explorer:** Browse a real-time list of all physical EV charging stations in the network.
*   **Status Management:** Monitor and dynamically update the state of any station (`AVAILABLE`, `IN_USE`, `MAINTENANCE`) directly from the device.
*   **Grid Expansion:** Register and create new charging stations through a native Compose form with validation.
*   **Detail View:** Access comprehensive data for each station, including power specs (kW), coordinates, and recent charging sessions history.
*   **Charging Sessions:** Start and stop charging sessions directly from the station detail view.
*   **Error Handling:** Resilient network architecture with graceful error states and retry mechanisms.

## 💻 Prerequisites

To build and run this project locally, you will need:

*   **Android Studio** (Ladybug | 2024.2.1 or latest stable version recommended).
*   **Android SDK** (Min SDK 26, Target SDK 36).
*   **Physical Android device or Emulator.**
*   **ChargeGrid API:** A running instance of the [ChargeGrid Backend](https://github.com/your-repo/chargegrid-backend).

## 🔑 Setup & Configuration

For security reasons, the API URL is not tracked in version control. Before syncing Gradle, create or modify the `local.properties` file in the root directory of the project and add your backend URL:

```properties
# Add your Local IP or VPS Domain here
# For Android Emulator, use http://10.0.2.2:8080/ to access your host's localhost
API_BASE_URL="http://10.0.2.2:8080/"
```

## 🏗️ Architecture Overview

The project follows the **Clean Architecture** principles and the **MVVM** pattern:

1.  **UI (Compose):** Screens and Components in `com.jorge.chargegridapp.ui`.
2.  **ViewModel:** Manages UI state and business logic using `StateFlow`.
3.  **Repository:** Orchestrates data flow between the API and the ViewModels.
4.  **Network (Retrofit):** Defines API interfaces and DTOs.

### Directory Structure

```text
app/src/main/java/com/jorge/chargegridapp/
├── core/            # Networking, UI Theme, and Utils
├── station/         # Station module (Repository, ViewModel, API, DTOs)
├── chargesession/   # Session module (Repository, ViewModel, API, DTOs)
└── ui/              # Main Screens and Compose Components
```

---
*Developed as part of the ChargeGrid Ecosystem.*
