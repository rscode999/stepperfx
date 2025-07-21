package stepperfx;

import javafx.application.Application;
import javafx.stage.Stage;
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
        ScreenManager manager = new ScreenManager(initialStage);

        //Set the screens
        manager.addScreen("login", "/views/login-view.fxml", fields);
        manager.addScreen("login-reject", "/views/login-reject-view.fxml", fields);
        manager.addScreen("input", "/views/input-view.fxml", fields);
        manager.addScreen("loading", "/views/loading-view.fxml", fields);
        manager.addScreen("settings", "/views/settings-view.fxml", fields);
        manager.addScreen("results", "/views/results-view.fxml", fields);

        //Add alternate styles for input, settings, results screens
        manager.addAlternateStylesheet("input", "/views/high-contrast-main-styles.css");
        manager.addAlternateStylesheet("settings", "/views/high-contrast-main-styles.css");
        manager.addAlternateStylesheet("results", "/views/high-contrast-main-styles.css");

        manager.finishLoading();

        initialStage.setTitle("StepperFX");

        //Show the login screen
        manager.showScreen("login");
        initialStage.show();
    }


    /**
     * Launches the app.
     * @param args Java Virtual Machine arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
