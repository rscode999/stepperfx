package stepperfx;

import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import stepperfx.integration.ScreenName;
import stepperfx.integration.ScreenManager;
import stepperfx.integration.StepperFields;

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
        StepperFields fields = new StepperFields();
        ScreenManager manager = new ScreenManager(initialStage, fields);

        //Set the screens
        manager.addScreen(ScreenName.LOGIN, "/views/login-view.fxml", fields);
        manager.addScreen(ScreenName.LOGIN_REJECT, "/views/login-reject-view.fxml", fields);
        manager.addScreen(ScreenName.INPUT, "/views/input-view.fxml", fields);
        manager.addScreen(ScreenName.LOADING, "/views/loading-view.fxml", fields);
        manager.addScreen(ScreenName.SETTINGS, "/views/settings-view.fxml", fields);
        manager.addScreen(ScreenName.RESULTS, "/views/results-view.fxml", fields);

        //Add alternate styles for input, settings, results screens
        manager.addAlternateStylesheet(ScreenName.INPUT, "/views/high-contrast-main-styles.css");
        manager.addAlternateStylesheet(ScreenName.SETTINGS, "/views/high-contrast-main-styles.css");
        manager.addAlternateStylesheet(ScreenName.RESULTS, "/views/high-contrast-main-styles.css");

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