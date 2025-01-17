
---

# TaskAce: Android Task Scheduling Application  

**TaskAce** is a powerful, feature-rich Android application designed to streamline task scheduling and management. Developed using **Kotlin** and **XML**, the app prioritizes ease of use, collaboration, and flexibility, making it the perfect solution for individuals and teams aiming to boost productivity.

---

## Features  

### 🌟 Core Functionality  
- **User Authentication**:  
  - Secure sign-in and login powered by **Firebase Authentication**.  
  - Collaborate with other users on shared tasks effortlessly.  

- **Dashboard with Interactive UI**:  
  - Displays **Progressive Tasks** (tasks containing sub-tasks).  
  - Displays **Daily/Short-term Tasks** for quick scheduling.  

- **Task Management**:  
  - Create tasks with attachments, URLs, and alarms.  
  - Assign **time intervals** and set priorities for tasks and sub-tasks.  
  - **No overlapping tasks**: Ensures time conflicts are avoided.  

- **Sub-Tasks**:  
  - Manage detailed sub-tasks under Progressive Tasks, with customizable priority levels.  

- **Attachments and Storage**:  
  - Upload and store attachments securely in **Firebase Storage**.  

- **Task Customization**:  
  - Edit tasks anytime after creation.  
  - Interactive "Create Task" section to simplify task entry.  

---

## Technology Stack  

- **Frontend**:  
  - **Kotlin**: Handles the app’s core functionality and logic.  
  - **XML**: Used for designing the user interface.  

- **Backend Services**:  
  - **Firebase Authentication**: Handles user login and sign-up.  
  - **Firebase Storage**: Stores user-uploaded attachments.  

- **Database**:  
  - **Firestore**: Used for storing task data, including task details, user collaborations, priorities, and sub-task information.  

- **Notifications**:  
  - Alarm and time-based reminders implemented for task notifications.

---

## Installation and Setup  

### Prerequisites  
- **Android Studio**: Download and install from [Android Studio](https://developer.android.com/studio).  
- **Firebase Project**:  
  1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).  
  2. Enable Firebase Authentication (email/password).  
  3. Enable Firebase Storage.  
  4. Add the `google-services.json` file to the `app/` directory of your project.  

### Steps  

1. **Clone the Repository**:  
   ```bash
   git clone https://github.com/yourusername/taskace.git
   cd taskace
   ```

2. **Open in Android Studio**:  
   - Open the project in Android Studio.  
   - Sync Gradle to resolve dependencies.  

3. **Configure Firebase**:  
   - Add the Firebase configuration (`google-services.json`).  
   - Ensure Firebase Authentication and Storage rules are set up.  

4. **Run the Application**:  
   - Connect an Android device or use an emulator.  
   - Build and run the app from Android Studio.  

---

## How to Use  

1. **User Authentication**:  
   - Sign in or log in using email/password.  

2. **Dashboard**:  
   - View **Progressive Tasks** and **Daily/Short-term Tasks**.  

3. **Creating a Task**:  
   - Use the "Create Task" section to add a task or Progressive Task.  
   - Add attachments, URLs, alarms, and priority levels.  
   - Assign sub-tasks for Progressive Tasks with customizable priorities.  

4. **Managing Tasks**:  
   - View, edit, and delete tasks directly from the dashboard.  
   - Avoid overlapping tasks through built-in validations.  

5. **Notifications**:  
   - Set alarms for tasks and receive timely reminders.  

---

## Future Enhancements  

- **AI-powered Task Suggestions**: Use machine learning to suggest task priorities and deadlines.  
- **Integration with Calendar Apps**: Sync tasks with external calendars (Google Calendar, Outlook, etc.).  
- **Offline Mode**: Enable task creation and management offline with auto-sync when back online.  
- **Dark Mode**: Add support for a dark theme.  
- **Collaboration Tools**: Introduce real-time task updates and chat functionality for enhanced teamwork.  

---

## Contact  

For questions or support, contact **[aswajith707@gmail.com](mailto:aswajith707@gmail.com)**.  

--- 
