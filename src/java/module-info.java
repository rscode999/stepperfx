module test.stepperfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires java.desktop;

    opens stepperfx to javafx.fxml;
    exports stepperfx;
    exports stepperfx.administration;
    exports stepperfx.controllers;
    exports stepperfx.threading;
    opens stepperfx.controllers to javafx.fxml;

}