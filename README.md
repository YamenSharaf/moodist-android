<div align="center">
  <img src="/assets/banner.png" alt="Moodist Logo Banner" />
  <h2>Moodist for Android 🌲</h2>
  <p>A fork of <a href="https://github.com/remvze/moodist">Moodist</a>, packaged for Android.</p>
  <p><a href="https://github.com/YamenSharaf/moodist-android/releases/latest"><strong>Download the Latest APK from Releases</strong></a></p>
</div>

## About This Project

This repository contains the source code for **Moodist for Android**, a project that brings the wonderful ambient sound experience of the original [Moodist web app](https://github.com/remvze/moodist) by **@remvze** to Android devices. It uses the same core web application, packaged natively using the [Capacitor](https://capacitorjs.com/) framework.

## Features

1. 🎵 Over 75 ambient sounds.
1. 📝 Persistent sound selection.
1. ✈️ Sharing sound selections with others.
1. 🧰 Custom sound presets.
1. 🌙 Sleep timer for sounds.
1. 📓 Notepad for quick notes.
1. 🍅 Pomodoro timer.
1. ✅ Simple to-do list.
1. ⏯️ Media controls.
1. ⌨️ Keyboard shortcuts for everything.
1. 🥷 Privacy focused: no data collection.
1. 💰 Completely free and open-source.

## Building from Source

To compile the Android APK yourself, you'll need to have Node.js, a Java Development Kit (JDK), and the Android SDK installed.

### 1. Prerequisites

*   **Node.js**
*   **JDK 17** (for example, OpenJDK 17)
*   **Android SDK**

Before you begin, make sure the `JAVA_HOME` and `ANDROID_HOME` environment variables are correctly set. For example:
```sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=~/Android/Sdk
```

### 2. Build Commands

1.  **Install Dependencies:**
    Clone this repository and install all the required npm packages.
    ```sh
    npm install
    ```

2.  **Build and Sync Assets:**
    Build the web project and sync the assets to the Android project.
    ```sh
    npm run build
    npx cap sync
    ```

3.  **Assemble the APK:**
    Navigate into the `android` directory and run the Gradle build command.
    ```sh
    cd android
    ./gradlew assembleDebug
    ```

4.  **Locate the APK:**
    The final APK will be available at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Credits

*   This project is a fork of the original [Moodist](https://github.com/remvze/moodist), created by **@remvze**.
