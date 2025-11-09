# StepperFX
Implementation of the Stepper algorithm using the JavaFX framework

First large-scale project using JavaFX.
Enables users to take input directly in the app or through a chosen .txt file.  
Users can select an arbitrary number of worker threads to do the operations.  
This program supports both normal and enhanced (version 2) processes.

For Java 21+

By using, viewing, or contributing to this project, you agree to follow the rules listed in the [rules document](rules.md).


### Table of Contents
- [IDE Configuration Instructions](#ide-configuration-instructions)
- [Git Contribution Rules](#git-contribution-rules)
- [Project Structure](#project-structure)
  - [FXMLs and Stylesheets](#fxml-and-css-stylesheets)
  - [Images](#images)
  - [Java Code](#code)
- [Final Notes](#final-notes)


## IDE Configuration Instructions
Use the following directory configuration:
- Sources root: `src/main/java`
- Resources root: `src/main/resources`
- Test Sources root: `src/test/java`

To change the directory configuration in IntelliJ, right-click on a directory, then go to "Mark Directory As".  
These settings are known to work for the IntelliJ IDE. I have never used another IDE before.

The main class is `src/main/java/stepperfx/Launcher`. If not exporting to a JAR, `src/main/java/stepperfx/MainApplication` also works as the main class.


## Run Instructions

Use `src/main/java/stepperfx/Launcher` (or `src/main/java/stepperfx/MainApplication`, if not exporting to a JAR)
as the main class.

If using Java 24+, run with the VM option `--enable-native-access=javafx.graphics`.

## Git Contribution Rules
Make all changes and updates on a *new* Git branch. When the changes are completed and *thoroughly tested*, make a pull request to "main".

Any version branches (i.e. "v0.7.0") should *never* be deleted!  
Do not push to "main" without my explicit permission!



## Project Structure

##### FXML and CSS Stylesheets
FXML files are stored in `src/main/resources/views`.
- Each FXML file represents the layout of one of the app's screens.
- The directory also contains CSS stylesheets: `login-styles.css`, for the login and login rejection screens,
  and `main-styles.css`, for all other screens (and dialogs).

##### Images
Images are stored in `src/main/resources/images`.
- Any image that you don't want pushed to the GitHub
  should be put in another directory inside `src/main/resources/`, then added to your `.gitignore` file.
  As an example, I have images in `src/main/resources/images_gitignored`, a directory added to my `.gitignore`.  
  Note: To use the images, you need to change the paths in the `src/main/java/stepperfx/integration/StyledDialogs` class.

##### Code
All Java source code is inside the `src/main/java/stepperfx` package.
- The **main class** is called `MainApplication`. To export the app as a JAR, select `Launcher` as the main class.

- The `controllers` package contains the **GUI controllers**. Each controller class is responsible for one screen.

- The `integration` package holds functionality **shared throughout the app**. It contains 4 classes and 1 enum:
  - `IntegratedController`, the **base class for GUI controllers**. It allows controllers to change the screen and access shared variables.
  - `ScreenManager`, which controllers use to **change the screen**.
  - `ScreenName` (the enum), a set of **possible screen names**.
  - `StepperFields`, containing the **shared state** of the application. The class contains constants, encapsulated variables, and a shared Service for multithreaded operations.
  - `StyledDialogs`, with static methods to **show dialogs**. It takes its styles from the `dialog` style class inside `src/main/resources/views/main-styles.css`.

- The `threading` package contains worker thread classes, which the app uses to **process inputs**.
  - `ProcessService`, where an instance is stored in a `StepperFields` instance, deploys a `ProcessTask`.
  - `ProcessTask` processes the input given to it by a `ProcessService`. To help it process input, a Task assigns work to ProcessSubtask instances.
  - `ProcessSubtaskDiacritics` and `ProcessSubtaskMain` parse a small piece of their given inputs.


## Final Notes

The GUI is designed to be inconspicuous. All screens except for the login screen use a color that imitates "Go Away Green",
allowing the GUI to avoid calling attention to itself. The color choice makes viewing by unauthorized users unlikely.
The default text color is hard to see, so curious onlookers have a harder time reading the text.

Anyone working on this project must follow the rules stated in the [rules document](rules.md).
I reserve the rights to change, update, nullify, or interpret the rules at any time.  
Failure to obey the rules means I will hunt you down and [DATA EXPUNGED].

DO NOT PUSH TO THE GITHUB "main" BRANCH WITHOUT EXPLICIT PERMISSION FROM ME... OR ELSE.
(YOU DON'T WANT TO KNOW WHAT WILL HAPPEN. TRUST ME.)

[Back to table of contents](#table-of-contents)