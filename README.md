# Family Messenger - Android Messaging App

A personal Android messaging application designed for family communication. This app provides real-time messaging capabilities with a clean, user-friendly interface.

## Features

- **User Authentication**: Secure email/password authentication
- **Real-time Messaging**: Instant message delivery using Firebase Firestore
- **Family Chat List**: View all your conversations in one place
- **One-on-One Chats**: Private conversations with family members
- **Online Status**: See when family members are online
- **Push Notifications**: Get notified of new messages (FCM ready)
- **Local Caching**: Messages stored locally using Room database
- **Material Design**: Modern, clean UI following Material Design guidelines

## Technology Stack

- **Language**: Kotlin
- **Architecture**: MVVM pattern
- **Database**:
  - Firebase Firestore (cloud storage)
  - Room (local caching)
- **Authentication**: Firebase Authentication
- **Messaging**: Firebase Cloud Messaging (FCM)
- **UI Framework**: Material Design Components
- **Concurrency**: Kotlin Coroutines
- **Build System**: Gradle

## Project Structure

```
app/
├── src/main/
│   ├── java/com/family/messenger/
│   │   ├── adapters/           # RecyclerView adapters
│   │   │   ├── ChatsAdapter.kt
│   │   │   ├── MessagesAdapter.kt
│   │   │   └── UsersAdapter.kt
│   │   ├── data/               # Data layer
│   │   │   ├── AppDatabase.kt
│   │   │   ├── ChatDao.kt
│   │   │   ├── Converters.kt
│   │   │   ├── FirebaseRepository.kt
│   │   │   └── MessageDao.kt
│   │   ├── models/             # Data models
│   │   │   ├── Chat.kt
│   │   │   ├── Message.kt
│   │   │   └── User.kt
│   │   ├── ui/                 # UI activities
│   │   │   ├── ChatActivity.kt
│   │   │   ├── LoginActivity.kt
│   │   │   ├── MainActivity.kt
│   │   │   └── RegisterActivity.kt
│   │   └── utils/              # Utilities
│   │       └── MessagingService.kt
│   └── res/                    # Resources
│       ├── layout/             # XML layouts
│       ├── values/             # Strings, colors, themes
│       └── menu/               # Menu definitions
```

## Setup Instructions

### Prerequisites

1. **Android Studio**: Arctic Fox or newer
2. **Java Development Kit**: JDK 17
3. **Android SDK**: API level 24 or higher
4. **Firebase Account**: Google account for Firebase services

### Firebase Setup

1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project" and follow the setup wizard
   - Enter project name (e.g., "Family Messenger")

2. **Add Android App to Firebase**:
   - In Firebase Console, click "Add app" and select Android
   - Package name: `com.family.messenger`
   - Download `google-services.json`
   - Replace the placeholder `google-services.json` in `app/` directory

3. **Enable Firebase Services**:
   - **Authentication**:
     - Go to Authentication → Sign-in method
     - Enable "Email/Password" provider

   - **Firestore Database**:
     - Go to Firestore Database
     - Click "Create database"
     - Start in test mode (or production mode with security rules)
     - Choose a location

   - **Cloud Messaging** (Optional):
     - Already enabled by default
     - For advanced notifications, configure FCM in project settings

4. **Firestore Security Rules** (Recommended):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Users collection
       match /users/{userId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && request.auth.uid == userId;
       }

       // Chats collection
       match /chats/{chatId} {
         allow read, write: if request.auth != null &&
           request.auth.uid in resource.data.participants;
       }

       // Messages collection
       match /messages/{messageId} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

### Building the App

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd aMess
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Sync Gradle**:
   - Android Studio will automatically prompt to sync
   - Click "Sync Now" or File → Sync Project with Gradle Files

4. **Run the app**:
   - Connect an Android device or start an emulator (API 24+)
   - Click the "Run" button or press Shift+F10

### Configuration

#### Gradle Wrapper

The project uses Gradle Wrapper. To download and use it:

```bash
# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build the project
./gradlew build

# Install on device
./gradlew installDebug
```

#### Minimum Requirements

- **minSdk**: 24 (Android 7.0 Nougat)
- **targetSdk**: 34 (Android 14)
- **compileSdk**: 34

## Usage Guide

### First Time Setup

1. **Launch the app**
2. **Register a new account**:
   - Tap "Don't have an account? Register"
   - Enter display name, email, and password
   - Tap "Register"

3. **Login**:
   - Enter your registered email and password
   - Tap "Login"

### Starting a Conversation

1. **Tap the "+" button** (Floating Action Button)
2. **Select a family member** from the list
3. **Start chatting**

### Sending Messages

1. Open a chat
2. Type your message in the input field
3. Tap the send button
4. Messages appear in real-time

## Features in Detail

### Authentication
- Secure email/password authentication via Firebase
- Session persistence (stay logged in)
- Logout functionality

### Real-time Messaging
- Messages sync instantly across devices
- Read/unread message indicators
- Timestamp for each message
- Message history preserved in cloud

### Chat Management
- View all active conversations
- Last message preview
- Time indicators
- Unread message badges

### User Interface
- Clean Material Design interface
- Intuitive navigation
- Smooth animations
- Responsive layouts

## Troubleshooting

### Build Issues

**Problem**: Gradle sync fails
- **Solution**: Check internet connection, invalidate caches (File → Invalidate Caches / Restart)

**Problem**: Firebase dependencies not found
- **Solution**: Ensure `google-services.json` is in the `app/` directory

### Runtime Issues

**Problem**: Login/Register fails
- **Solution**: Check Firebase Authentication is enabled and internet connection is active

**Problem**: Messages not sending
- **Solution**: Verify Firestore is properly configured and security rules allow access

**Problem**: App crashes on startup
- **Solution**: Check Logcat for errors, verify all Firebase services are enabled

## Future Enhancements

Potential features for future versions:

- [ ] Group chat support
- [ ] Image and file sharing
- [ ] Voice messages
- [ ] Video calling
- [ ] Message search
- [ ] Custom themes
- [ ] Message reactions
- [ ] Typing indicators
- [ ] Message encryption
- [ ] Backup and restore

## Dependencies

Key dependencies used in this project:

- Firebase BOM: 32.7.0
- Firebase Auth, Firestore, Messaging
- Room: 2.6.1
- Material Components: 1.11.0
- Kotlin Coroutines: 1.7.3
- Lifecycle Components: 2.7.0
- Gson: 2.10.1

See `app/build.gradle` for complete dependency list.

## Security Considerations

- Passwords are never stored locally
- All authentication handled by Firebase
- Firestore security rules restrict unauthorized access
- HTTPS used for all network communication
- Local database encrypted (Room supports SQLCipher if needed)

## Contributing

This is a personal family messaging app. If you'd like to use it:

1. Fork the repository
2. Set up your own Firebase project
3. Update the package name in all files
4. Configure your Firebase credentials

## License

This project is created for personal/family use. Feel free to use and modify for your own family communication needs.

## Support

For issues or questions:
- Check the Troubleshooting section
- Review Firebase documentation
- Check Android Developer documentation

## Credits

Built with:
- Kotlin
- Firebase
- Android Jetpack
- Material Design

---

**Note**: Remember to keep your `google-services.json` file secure and never commit it to public repositories with production credentials.
