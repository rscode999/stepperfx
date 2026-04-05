package com.rscode.stepperfx;

import com.rscode.stepperfx.integration.ScreenControl;
import com.rscode.stepperfx.integration.StepperFields;
import javafx.application.Application;
import javafx.stage.Stage;
import com.rscode.stepperfx.integration.ScreenName;

/**
 * Responsible for configuring and displaying the app.<br>
 * For Java 21
 */
final public class MainApplication extends Application {

    /**
     * Serves as the entry point for the application.
     * @param initialStage the stage at the application's start
     * @throws Exception if any exception occurs during application launch
     */
    @Override
    public void start(Stage initialStage) throws Exception {

        StepperFields.assertConstantPreconditions();

        ScreenControl.setApplicationStage(initialStage);

        //Set the screens
        ScreenControl.addScreen(ScreenName.LOGIN, "/com/rscode/stepperfx/views/login-view.fxml");
        ScreenControl.addScreen(ScreenName.LOGIN_REJECT, "/com/rscode/stepperfx/views/login-reject-view.fxml");
        ScreenControl.addScreen(ScreenName.INPUT, "/com/rscode/stepperfx/views/input-view.fxml");
        ScreenControl.addScreen(ScreenName.LOADING, "/com/rscode/stepperfx/views/loading-view.fxml");
        ScreenControl.addScreen(ScreenName.SETTINGS, "/com/rscode/stepperfx/views/settings-view.fxml");
        ScreenControl.addScreen(ScreenName.RESULTS, "/com/rscode/stepperfx/views/results-view.fxml");

        //Add alternate styles for input, settings, results screens
        ScreenControl.addAlternateStylesheet(ScreenName.INPUT, "/com/rscode/stepperfx/views/high-contrast-main-styles.css");
        ScreenControl.addAlternateStylesheet(ScreenName.SETTINGS, "/com/rscode/stepperfx/views/high-contrast-main-styles.css");
        ScreenControl.addAlternateStylesheet(ScreenName.RESULTS, "/com/rscode/stepperfx/views/high-contrast-main-styles.css");

        ScreenControl.finishLoading();

        initialStage.setTitle("StepperFX");

        //Show the login screen
        ScreenControl.showScreen(ScreenName.LOGIN);
        initialStage.show();
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    //MAIN


    /**
     * Launches the app.
     * @param args Java Virtual Machine arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}