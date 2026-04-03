package com.rscode.stepperfx;

import javafx.application.Application;
import javafx.stage.Stage;
import com.rscode.stepperfx.integration.ScreenName;
import com.rscode.stepperfx.integration.ScreenManager;
import com.rscode.stepperfx.integration.StepperFields;

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
        ScreenManager manager = new ScreenManager(initialStage);

        //Set the screens
        manager.addScreen(ScreenName.LOGIN, "/com/rscode/stepperfx/views/login-view.fxml");
        manager.addScreen(ScreenName.LOGIN_REJECT, "/com/rscode/stepperfx/views/login-reject-view.fxml");
        manager.addScreen(ScreenName.INPUT, "/com/rscode/stepperfx/views/input-view.fxml");
        manager.addScreen(ScreenName.LOADING, "/com/rscode/stepperfx/views/loading-view.fxml");
        manager.addScreen(ScreenName.SETTINGS, "/com/rscode/stepperfx/views/settings-view.fxml");
        manager.addScreen(ScreenName.RESULTS, "/com/rscode/stepperfx/views/results-view.fxml");

        //Add alternate styles for input, settings, results screens
        manager.addAlternateStylesheet(ScreenName.INPUT, "/com/rscode/stepperfx/views/high-contrast-main-styles.css");
        manager.addAlternateStylesheet(ScreenName.SETTINGS, "/com/rscode/stepperfx/views/high-contrast-main-styles.css");
        manager.addAlternateStylesheet(ScreenName.RESULTS, "/com/rscode/stepperfx/views/high-contrast-main-styles.css");

        manager.finishLoading();

        initialStage.setTitle("StepperFX");

        //Show the login screen
        manager.showScreen(ScreenName.LOGIN);
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