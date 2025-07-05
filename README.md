# StepperFX
##### Implementation of the Stepper algorithm using the JavaFX framework  

First large-scale project using JavaFX. 
Enables users to take input either through text or with an input .txt file.  
Users can select an arbitrary number of worker threads to do the operations.  
This program supports both normal and enhanced (version 2) processes.  
For Java 21+

### Project Structure

FXML files are stored in `src/resources/views`.
Each FXML file represents the layout of one of the app's screens.  


Java source code is inside the `src/java/stepperfx` package.  
- The main class is called `MainApplication`. To export the app as a JAR, select `Launcher` as the main class.
- `StepperFields` contains the shared state of the application.  
- The `administration` package contains the `IntegratedController`, the base class for GUI controllers.
The package also contains `ScreenManager`, which controllers use to change the screen.  
- The `controllers` package contains the GUI controllers, one per screen.  
- The `threading` package contains worker thread classes, which the app uses to process inputs.
A `ProcessService`, stored in a `StepperFields` instance, deploys a `ProcessTask`, which in turn
deploys `ProcessSubtaskDiacritics` and `ProcessSubtaskMain` instances. The final result is retrieved
by the `ResultsController` by using a value property listener set on the app's `ProcessService`.

Anyone working on this project must follow the rules stated in rules.txt.
Failure to obey the rules means I will hunt you down and [REDACTED].

