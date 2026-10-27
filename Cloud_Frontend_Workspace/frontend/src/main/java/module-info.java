module com.minicloud.client {
    requires javafx.controls;
    requires java.net.http;
    requires java.desktop;
    exports com.minicloud.client;
    exports com.minicloud.client.ui;
    exports com.minicloud.client.ui.dialogs;
    exports com.minicloud.client.models;
    exports com.minicloud.client.controllers;
    opens com.minicloud.client.ui to javafx.base;
    opens com.minicloud.client.models to javafx.base;
}