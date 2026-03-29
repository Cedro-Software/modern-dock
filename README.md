![GitHub contributors](https://img.shields.io/github/contributors/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub Repo stars](https://img.shields.io/github/stars/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub License](https://img.shields.io/github/license/arthurdeka/cedro-modern-dock?style=for-the-badge)
![X (formerly Twitter) Follow](https://img.shields.io/twitter/follow/TeiuAlligator?style=for-the-badge)


<img width="769" height="163" alt="img1" src="https://github.com/user-attachments/assets/3a7cf5f1-a203-4d7b-aa9f-6e0904031eb5" />
<img width="785" height="270" alt="img3" src="https://github.com/user-attachments/assets/1489345d-4ddc-4482-a074-ca676da8eb28" />



> Follow @TeiuAlligator on X to stay tuned for new features and updates!


> ## How To Install
> 1. Go to [Releases](https://github.com/arthurdeka/cedro-modern-dock/releases):
> 2. Download the latest `CedroSetup.exe` file
> 3. Execute and install
<br>

## Features

- **Quick Launch Shortcuts:** Add your favorite `.exe` files to the dock for fast access.
- **Folder Shortcuts:** Pin folders alongside apps and open them directly from the dock.
- **Built-in Windows Modules:** Add native Windows shortcuts such as **This PC**, **Recycle Bin**, **Control Panel**, and **Settings**.
- **Appearance Customization:** Adjust icon size, spacing, background color, transparency, and dock corner rounding.
- **Flexible Positioning:** Choose between a static anchored layout with per-edge spacing or a dynamic draggable dock.
- **Running App Indicators:** Program shortcuts show a live indicator when matching windows are currently open.
- **Window Preview Popup:** Hovering a running app can show its open windows, and clicking a preview brings that window to the front.
- **Desktop-Friendly Behavior:** Includes system tray integration for quick access to settings.
<br>

## Supported Languages

- 🇺🇸  English
- 🇧🇷 Portuguese (Brazil)
- 🇪🇸 Spanish
- 🇫🇷 French
- 🇩🇪 German
- 🇯🇵 Japanese
- 🇨🇳 Chinese (Simplified)
- 🇹🇼 Chinese (Traditional)
- 🇮🇳 Hindi
- 🇸🇦 Arabic
- 🇧🇩 Bengali
- 🇷🇺 Russian
- 🇵🇰 Urdu
- 🇮🇩 Indonesian
- 🇳🇬 Nigerian Pidgin
- 🇮🇳 Marathi
- 🇮🇳 Telugu
- 🇹🇷 Turkish
- 🇮🇳 Tamil
- 🇭🇰 Cantonese
- 🇻🇳 Vietnamese
<br>

<!-- BUILT WITH -->
## Built With

* ![JavaFX](https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white)
<br>

<!-- GETTING STARTED -->
## How To Contribute
> Only for those who wish to contribute in the project's coding, none of this is required for you to do if you [just want to download it](https://github.com/arthurdeka/cedro-modern-dock/releases)
### Understand the project architecture:

The project now follows a lightweight layered architecture:

- `controller`: JavaFX controllers and UI event handling
- `application`: dock use cases, appearance rules, item actions and preview orchestration
- `domain`: ports/interfaces for persistence, icon resolution, program launch and native window queries
- `infrastructure`: Jackson persistence and Windows-specific adapters
- `model`: serializable dock data structures used by the application layer
- `view` and `resources`: popup rendering, FXML, CSS and static assets

`App.java` is responsible for composing these dependencies and injecting them into the JavaFX controllers.

<!-- PREREQUISITES -->
### Prerequisites

Before you begin, ensure you have the following installed on your system: (required only for development)

- **An IDE** (IntelliJ IDEA recommended).
- **JDK 21**.
- **Apache Maven**.
- **Git** for cloning the repository.

<br>

### How To Run Locally

1. Clone the repository
2. Before running, make sure to install all the dependencies
3. Run `App.java` to run the program
4. Make desired changes
5. Commit your changes
6. Open a pull request

<br>

### How To `COMPILE` Locally

1. Clone the repository
2. Install all dependencies
3. Run `mvn clean package` on the terminal
4. The `.exe` file will be available at `target\dist\Cedro Modern Dock`

<br>
