# StepperFX
Implementation of the Stepper algorithm using the JavaFX framework  

First large-scale project using JavaFX. 
Enables users to take input directly in the app or through a chosen .txt file.  
Users can select an arbitrary number of worker threads to do the operations.  
This program supports both normal and enhanced (version 2) processes.  

For Java 21+



## IDE Configuration Instructions
Use the following directory configuration:
- Sources root: `src/java`
- Resources root: `src/resources`
- Test Sources root: `src/tests`  

To change the directory configuration in IntelliJ, right-click on a directory, then go to "Mark Directory As".  
These settings are known to work for the IntelliJ IDE. I have never used another IDE before.

The main class is `src/java/stepperfx/Launcher`. If not exporting to a JAR, `src/java/stepperfx/MainApplication` also works as the main class.



## Contribution Rules
Make all changes and updates on a *new* Git branch. When the changes are completed and *thoroughly tested*, make a pull request to "main".

Any version branches (i.e. "v0.7.0") should **never** be deleted!  
Do not push to "main" without my explicit permission!



## Project Structure
FXML files are stored in `src/resources/views`.
Each FXML file represents the layout of one of the app's screens.  
The directory also contains CSS stylesheets: `login-styles.css`, for the login and login rejection screens,
and `main-styles.css`, for all other screens (and dialogs).


Java source code is inside the `src/java/stepperfx` package.
- The main class is called `MainApplication`. To export the app as a JAR, select `Launcher` as the main class.

- The `controllers` package contains the GUI controllers.  
Each controller class is responsible for one screen.


- The `integration` package contains 4 classes:
  - `IntegratedController`, the base class for GUI controllers. It allows controllers to change the screen and access shared variables.
  - `ScreenManager`, which controllers use to change the screen.  
  - `StepperFields`, containing the shared state of the application. The class contains constants, encapsulated variables, and a shared Service for multithreaded operations.
  - `StyledDialogs`, with static methods to show dialogs. It takes its styles from the `dialog` style class inside `resources/views/main-styles.css`.


- The `threading` package contains worker thread classes, which the app uses to process inputs.
  - `ProcessService`, where an instance is stored in a `StepperFields` instance, deploys a `ProcessTask`.
  - `ProcessTask` processes the input given to it by a `ProcessService`. To help it process input, a Task assigns work to `ProcessSubtaskDiacritics` and `ProcessSubtaskMain` instances.
  - `ProcessSubtaskDiacritics` and `ProcessSubtaskMain` parse a small piece of their given inputs.


## Final Notes

Anyone working on this project must follow the rules stated in `rules.txt`. I reserve the rights to change, update, or interpret the rules at any time.
Failure to obey the rules means I will hunt you down and [REDACTED].  

DO NOT PUSH TO THE GITHUB "main" BRANCH WITHOUT EXPLICIT PERMISSION FROM ME... OR ELSE. (YOU DON'T WANT TO KNOW WHAT WILL HAPPEN. TRUST ME.)