module com.strategy.client {
    // JavaFX modules we actually use
    requires javafx.controls;
    requires javafx.media;

    // Our other modules
    requires com.strategy.common;

    // Libraries
    requires com.fasterxml.jackson.databind;

    // Export our package so JavaFX can launch the GameApp
    exports com.strategy.client;
}