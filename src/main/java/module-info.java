module com.github.arthurdeka.cedromoderndock {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires com.fasterxml.jackson.databind;
    requires static lombok;

    opens com.github.arthurdeka.cedromoderndock.controller to javafx.fxml;
    opens com.github.arthurdeka.cedromoderndock to javafx.fxml;
    opens com.github.arthurdeka.cedromoderndock.fxml to javafx.fxml;
    opens com.github.arthurdeka.cedromoderndock.model to com.fasterxml.jackson.databind;
    opens com.github.arthurdeka.cedromoderndock.util to com.sun.jna;

    exports com.github.arthurdeka.cedromoderndock;
    exports com.github.arthurdeka.cedromoderndock.controller to javafx.fxml;
}
