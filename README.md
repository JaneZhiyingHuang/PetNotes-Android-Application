# PetNotes 🐱🐕

## 📖 Overview

PetNotes helps you manage pet profiles, write daily notes, track weight trends, and monitor abnormal behaviors—all in one simple app. Stay organized, catch health issues early, and give your pets the care they deserve with PetNotes!

## ⚡Technologies

- [`Kotlin`](https://kotlinlang.org/)
- [`Firebase`](https://firebase.google.com/)
- [`Google Map API`](https://developers.google.com/maps)
- [`Firebase Storage`](https://firebase.google.com/docs/storage)
- [`kizitonwose/Calendar`](https://github.com/kizitonwose/Calendar)
- [`ehsannarmani/ComposeCharts`](https://github.com/ehsannarmani/ComposeCharts)


## 🧑‍🤝‍🧑 Team Members

- [Eaint Mon](https://github.com/SteffiEaint) : Authentication Manage | FrontEnd Development
- [Xuanyu Liu](https://github.com/xuanyu2003) : Firesbase Manage | FrontEnd Development
- [Zhiying Huang](https://github.com/JaneZhiyingHuang) : UI/UX Design | FrontEnd Development  


## 🌟 Features

- **🔐 User Authentication**: Users can sign up and log in securely to manage their pet data.
- **🐾 Pet Profiles**: Add and manage detailed profiles for each of your pets.
- **📝 Daily Notes**: Record daily activities, habits, or health updates for your pets.
- **📊 Weight Tracking**: Log and view weight trends over time in a visual chart.
- **🚨 Behavior Monitoring**: Track and get alerts for unusual or abnormal behaviors.
- **📷 Media Attachments**: Attach photos or documents to your notes for richer records.
- **🗺️ Pet Map**: Discover recommended pet products and clinics.
- **🎨 Responsive Design**: A clean and user-friendly interface.
  
## 🏗️ Application Architecture
- **⚛️ FrontEnd:**: Developed using Kotlin and Jetpack Compose, providing a modern, declarative UI with a clean and responsive design across all Android devices. The app follows MVVM architecture for clear separation of concerns.
- **⚙️ BackEnd**: Backend functionalities are handled via Firebase, including Authentication, Firestore Database, and Cloud Storage. This serverless setup ensures scalability and real-time data syncing.
- **🛢 Database**: Uses Cloud Firestore, a NoSQL cloud database, to efficiently store and retrieve structured pet data such as profiles, daily notes, weight records, and behavioral logs.

## 🎨 UI Design
The figma link for UI wireframe and pages layout:
https://www.figma.com/design/zuWRlwQW2uoDNhvvf1WNvB/PetNotes?node-id=0-1&t=NIBf71VJDZlu2iy7-1

## 🚦 Running the Project

To run the **PetNotes** app on your local Android Studio environment, follow these steps:

1. 🧬 **Clone the Repository**  
    Open a terminal and run:  
    ```sh
    git clone <https://github.com/JaneZhiyingHuang/MDP-Group3.git>
    ```

2. 📂 **Open in Android Studio**  
    Launch **Android Studio**, then select **"Open an existing project"** and navigate to the cloned folder.

3. 📦 **Sync Gradle & Install Dependencies**  
    Android Studio will automatically sync the project. If not, go to:  
    `File > Sync Project with Gradle Files`

4. 🔐 **Set Up Firebase**  
    - Go to [Firebase Console](https://console.firebase.google.com/).  
    - Create a new project and register your app with the package name.  
    - Download the `google-services.json` file and place it inside the `app/` directory.
    - Ensure the following is in your `build.gradle` files:

      **In project-level `build.gradle`:**
      ```kotlin
      classpath 'com.google.gms:google-services:4.3.15'
      ```

      **In app-level `build.gradle`:**
      ```kotlin
      apply plugin: 'com.google.gms.google-services'
      ```

5. 📱 **Run the App on Emulator or Device**  
    - Connect your Android device via USB or start an emulator.  
    - Click the **Run** button ▶️ in Android Studio or press `Shift + F10`.

6. ✅ **Enjoy PetNotes**  
    Once the app builds and launches, you’ll be able to sign up, create pet profiles, add notes, and more!

## 🎥 Demo/Slides
- **demo**: https://youtu.be/S842Rq5kbnc
- **slides**: https://docs.google.com/presentation/d/11yM-FC9KQFOLO1MwBze8ioIcBvwnqjp9xz9aP8RQKdw/edit?usp=sharing
## 📝 License
This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for more information.
