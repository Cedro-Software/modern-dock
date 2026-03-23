![GitHub contributors](https://img.shields.io/github/contributors/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub Repo stars](https://img.shields.io/github/stars/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/arthurdeka/cedro-modern-dock?style=for-the-badge)
![GitHub License](https://img.shields.io/github/license/arthurdeka/cedro-modern-dock?style=for-the-badge)
[![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/arthur-wa-rodrigues/)

![modern-dock-banner1](https://github.com/user-attachments/assets/77157afc-4db1-400b-8141-3e5bfb2ee7a9)

> ## How To Install
> 1. Go to [Releases](https://github.com/arthurdeka/cedro-modern-dock/releases):
> 2. Download the latest ```Setup-CedroSoftware-ModernDock_x64.exe``` file
> 3. Execute and Install
<br>

## ✨ Features

- **Quick Access:** Easily add and manage your favorite applications for quick launching.
- **Deep Customization:** Tailor the dock's appearance to your liking:
    - Icon size and spacing
    - Background color and transparency
    - Rounded corners for the dock's frame
    - Hover effect: A smooth zoom effect on icons when you mouse over them
<br>

![settings](https://github.com/user-attachments/assets/c62a8aaa-fa07-4a71-86bd-afdc5b2b627a)

<!-- BUILT WITH -->
## ⚙️ Built With

* ![JavaFX](https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white)
<br>

<!-- GETTING STARTED -->
## 👨‍🎨 How To Contribute

## Understand the project architecture:

The project now follows a lightweight layered architecture:

- `controller`: JavaFX controllers and UI event handling
- `application`: dock use cases, appearance rules, item actions and preview orchestration
- `domain`: ports/interfaces for persistence, icon resolution, program launch and native window queries
- `infrastructure`: Jackson persistence and Windows-specific adapters
- `model`: serializable dock data structures used by the application layer
- `view` and `resources`: popup rendering, FXML, CSS and static assets

`App.java` is responsible for composing these dependencies and injecting them into the JavaFX controllers.

<!-- PREREQUISITES -->
##  Prerequisites

Before you begin, ensure you have the following installed on your system:

- **An IDE** (IntelliJ IDEA recommended).
- **JDK 21**.
- **Apache Maven**.
- **Git** for cloning the repository.

<br>

##  How To Run Locally

1. Clone the repository
2. Before running, make sure to install all the dependencies
3. Run `App.java` to run the program 
4. Make desired changes
5. Commit your Changes
6. Open a Pull Request

<br>

##  How To `COMPILE` Locally

1. Clone the repository
2. Install all dependencies
3. Run `mvn clean package` on the terminal
4. The .exe file will be avaible at `target\dist\Cedro Modern Dock`

<br>
