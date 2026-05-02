📱 ChargeGrid Android App

ChargeGrid is a native Android application built to interact with the ChargeGrid Electric Vehicle (EV) charging network.
This client provides a modern, reactive user interface for viewing charging stations, monitoring their real-time statuses, and managing the grid on the go.
🛠️ Tech Stack

    Language: Kotlin

    UI Toolkit: Jetpack Compose (100% Declarative UI)

    Architecture: MVI (Model-View-Intent) with ViewModels & StateFlow

    Networking: Retrofit & OkHttp

    Serialization: Kotlinx Serialization (with Custom Serializers for Time)

    Build Tool: Gradle (Kotlin DSL & Version Catalogs)

🚀 Features (MVP)

    Station Explorer: Browse a real-time list of all physical EV charging stations in the network.

    Status Management: Monitor and dynamically update the state of any station (AVAILABLE, IN_USE, MAINTENANCE) directly from the device.

    Grid Expansion: Register and create new charging stations through a native Compose form.

    Detail View: Access comprehensive data for each station, including hardware specs (kW), coordinates, and recent charging sessions.

    Error Handling: Resilient network architecture with graceful error states and retry mechanisms.

💻 Prerequisites

To build and run this project locally, you will need:

    Android Studio (Ladybug or latest stable version recommended).

    Android SDK (Min SDK 24, Target SDK 34+).

    A physical Android device or Emulator.

    A running instance of the ChargeGrid API (Local or VPS deployed).

🔑 Setup Configuration

For security reasons, the API URL is not tracked in version control. Before syncing Gradle, create or modify the local.properties file in the root directory of the project and add your backend URL:
Properties

# Add your Local IP or VPS Domain here
API_BASE_URL="http://YOUR_IP:8080/"
