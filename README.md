# StepperFX
###### Implementation of the Stepper algorithm using the JavaFX framework  

First large-scale project using JavaFX. 
Enables users to take input directly in the app or through a chosen .txt file.  
Users can select an arbitrary number of worker threads to do the operations.  
This program supports both normal and enhanced (version 2) processes.  

For Java 21+



## IDE Configuration Instructions
Use the following settings:
- Main class: `src/java/stepperfx/Launcher`. If not exporting to a JAR, `src/java/stepperfx/MainApplication` also works as the main class.
- Sources root: `src/java`
- Resources root: `src/resources`
- Test Sources root: `src/tests`  

These settings are known to work for the IntelliJ IDE.


## Contribution Rules
Make all changes and updates on a *new* Git branch. When the changes are completed and *thoroughly tested*, make a pull request to "main".

Any version branches (i.e. "v0.7.0") should **never** be deleted!  
Do not push to "main" without my explicit permission!



## Project Structure
FXML files are stored in `src/resources/views`.
Each FXML file represents the layout of one of the app's screens.  

Java source code is inside the `src/java/stepperfx` package.
- The main class is called `MainApplication`. To export the app as a JAR, select `Launcher` as the main class.
- `StepperFields` contains the shared state of the application. The class contains constants, encapsulated variables, and a shared Service for mulththeaded operations.


- The `controllers` package contains the GUI controllers. Each controller class is responsible for one screen.
Also inside the package is the `Dialogs` class, which contains static methods for displaying popup windows.


- The `screen_management` package contains the `IntegratedController`, the base class for GUI controllers.
The package also contains `ScreenManager`, which controllers use to change the screen.


- The `threading` package contains worker thread classes, which the app uses to process inputs.
A `ProcessService`, stored in a `StepperFields` instance, deploys a `ProcessTask`, which in turn
deploys `ProcessSubtaskDiacritics` and `ProcessSubtaskMain` instances. The final result is retrieved
by the `ResultsController`. To get the result, the controller uses a value property listener set on the app's `ProcessService`.


## Final Notes

Anyone working on this project must follow the rules stated in `rules.txt`. I reserve the rights to change, update, or interpret the rules at any time.
Failure to obey the rules means I will hunt you down and [REDACTED].  

DO NOT PUSH TO THE GITHUB "main" BRANCH WITHOUT EXPLICIT PERMISSION FROM ME... OR ELSE. (YOU DON'T WANT TO KNOW WHAT WILL HAPPEN. TRUST ME.)