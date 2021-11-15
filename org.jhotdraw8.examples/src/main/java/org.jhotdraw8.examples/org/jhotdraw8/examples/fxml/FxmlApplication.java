package org.jhotdraw8.examples.fxml;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jhotdraw8.app.Activity;
import org.jhotdraw8.app.ApplicationLabels;
import org.jhotdraw8.concurrent.FXWorker;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * An application that adds a {@link Parent} created from an FXML file
 * to its primary {@link Stage}.
 */
public abstract class FxmlApplication extends javafx.application.Application {
    private static final String FXML_PROPERTY_NAME = "fxml";

    private final ObjectProperty<URL> fxml = new SimpleObjectProperty<>(this, FXML_PROPERTY_NAME);
    private CompletableFuture<FXMLLoader> loaderFuture;

    @Override
    public final void init() throws Exception {
        initApplication();
        loaderFuture = FXWorker.supply(Executors.newSingleThreadExecutor(), () -> {
            FXMLLoader loader = new FXMLLoader(getFxml());
            loader.load();
            return loader;
        });
    }


    public abstract void initApplication() throws Exception;

    @Override
    public final void start(Stage primaryStage) throws Exception {
        loaderFuture.thenAccept(loader -> {
            Object root = loader.getRoot();
            Object controller = loader.getController();

            if (controller instanceof Activity) {
                Activity activity = (Activity) controller;
                primaryStage.titleProperty().bind(activity.titleProperty());
                //Bindings.bindContent(              primaryStage.getIcons(), activity.iconsProperty());
            }

            if (root instanceof Parent) {
                Scene scene = new Scene((Parent) root);
                scene.setFill(Color.TRANSPARENT);
                primaryStage.setScene(scene);
            }
        });

        //  primaryStage.initStyle(StageStyle.UNIFIED);
        primaryStage.setWidth(640);
        primaryStage.setHeight(480);
        primaryStage.setTitle(ApplicationLabels.getResources().getString("unnamedFile"));
        primaryStage.show();

    }

    public URL getFxml() {
        return fxml.get();
    }

    public ObjectProperty<URL> fxmlProperty() {
        return fxml;
    }

    public void setFxml(URL fxml) {
        this.fxml.set(fxml);
    }
}
