package stepperfx;

import javafx.application.Application;

/**
 * Launches the app. Required for exporting the app as a JAR file.
 */
public class Launcher {
    /**
     * Launches the main application
     * @param args Java Virtual Machine arguments
     */
    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
