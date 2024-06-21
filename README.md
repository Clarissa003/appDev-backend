# appDev-backend

## Overview
This repository contains the backend code for the appDev project which is a mental health focused android applicaation
, focusing on habit tracking and social interaction features. The backend is implemented using Kotlin and Java, providing a robust API for managing user data, habits, moods, and more.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Backup and Data Extraction](#backup-and-data-extraction)
- [Contributing](#contributing)


## Features
- User authentication and authorization
- Habit tracking functionalities
- Social features for adding and managing friends
- Guided and unguided journal activities
- Mood tracking functionalities
- Media player for meditation, study and sleep sounds
- Integration with HuggingFace and OpenAI for AI capabilities

## Prerequisites
- Android Studio
- Kotlin 1.4 or higher
- Java JDK 8 or higher

## Installation

1. **Clone the repository**:
    ```sh
    git clone https://github.com/Clarissa003/appDev-backend.git
    cd appDev-backend
    ```

2. **Open in Android Studio**:
   Open the project in Android Studio.

3. **Install dependencies**:
   Android Studio will prompt you to install any missing dependencies or plugins required for the project.

## Configuration
- **API Keys**: Make sure to configure your API keys and endpoints in the relevant files, such as `HuggingFaceInterface.kt` and `OpenAIModels.kt`.
- **Properties Files**: Ensure `config.properties` is properly set up with the necessary configurations for your environment.

## Running the Application
To run the application, use the Android Studio run configuration to build and deploy the app on an emulator or physical device. To test the functionalities work as expected please sign-in into the application with *a valid* email account.

## API Documentation
API documentation for external integrations can be found in the relevant service files, such as `HuggingFaceInterface.kt` and `OpenAIModels.kt`.

## Project Structure
- **Activities**: Contains Kotlin files for various user interface activities (e.g., login, signup, profile, habit tracking, mood tracking, guided and unguided journaling).

- **Adapters**: Contains adapter classes for displaying lists of data (e.g., habits, moods, friends).

- **Data Models**: Contains data classes representing various entities (e.g., Habit, Mood, Friend).

- **Services and Interfaces**: Contains files for API integrations and service configurations (e.g., HuggingFace, OpenAI, Retrofit).

- **Configuration and Build**: Contains Gradle build files, properties files, and rules for data backup and extraction.

## Backup and Data Extraction
- **Backup Rules**: Defined in `backup_rules.xml` to manage what data should be backed up.
- **Data Extraction Rules**: Defined in `data_extraction_rules.xml` to manage what data should be extracted and backed up during cloud backups.

## Contributing
We welcome contributions! Please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make your changes and commit them (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Create a new Pull Request.

---

Thank you for using the appDev-backend project!
