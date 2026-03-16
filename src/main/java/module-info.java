module com.rscode.stepperfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.rscode.stepperfx to javafx.fxml;
    opens com.rscode.stepperfx.controllers to javafx.fxml;
    opens com.rscode.stepperfx.integration to javafx.fxml;
    opens com.rscode.stepperfx.threading to javafx.fxml;
    exports com.rscode.stepperfx;
}