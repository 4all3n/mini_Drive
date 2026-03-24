module com.minicloud.client {
    requires javafx.controls;
    requires java.net.http; 
    exports com.minicloud.client;
    exports com.minicloud.client.ui;
    exports com.minicloud.client.controllers;
    opens com.minicloud.client.ui to javafx.base;
}