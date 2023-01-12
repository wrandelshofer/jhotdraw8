/*
 * @(#)AboutAction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.app;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.action.AbstractApplicationAction;

import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.jhotdraw8.application.Application.COPYRIGHT_KEY;
import static org.jhotdraw8.application.Application.LICENSE_KEY;
import static org.jhotdraw8.application.Application.NAME_KEY;
import static org.jhotdraw8.application.Application.VERSION_KEY;

/**
 * Displays a dialog showing information about the application.
 *
 * @author Werner Randelshofer
 */
public class AboutAction extends AbstractApplicationAction {

    public static final String ID = "application.about";

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    public AboutAction(@NonNull Application app) {
        super(app);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event, @NonNull Application app) {
        String name = app.get(NAME_KEY);
        String version = app.get(VERSION_KEY);
        String vendor = app.get(COPYRIGHT_KEY);
        String license = app.get(LICENSE_KEY);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        VBox graphic = new VBox();
        TextArea textArea = new TextArea(
                (name == null ? "unnamed" : name) + (version == null ? "" : " " + version)
                        + (vendor == null ? "" : "\n" + vendor)
                        + (license == null ? "" : "\n" + license)
                        + "\n\nRunning on"
                        + "\n  Java: " + System.getProperty("java.version")
                        + ", " + System.getProperty("java.vendor")
                        + "\n  JVM: " + System.getProperty("java.vm.version")
                        + ", " + System.getProperty("java.vm.vendor")
                        + "\n  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                        + ", " + System.getProperty("os.arch")
                        + "\n\nModules:\n"
                        + getDependencies());
        textArea.setEditable(false);
        graphic.getChildren().add(textArea);
        alert.setGraphic(graphic);
        alert.getDialogPane().setMaxWidth(640.0);
        alert.setHeaderText("");
        if (event.getSource() instanceof Node) {
            Scene scene = ((Node) event.getSource()).getScene();
            Window window = scene == null ? null : scene.getWindow();
            alert.initOwner(window);
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.getDialogPane().getScene().getStylesheets().addAll(
                getApplication().getStylesheets()
        );
        alert.show();
    }

    private String getDependencies() {
        Pattern pattern = Pattern.compile("-(\\w+(?:[.\\-+]\\w+)*).jar$");

        return
                ModuleLayer.boot().modules().stream()
                        .map(m -> {
                            // Get version string from descriptor if available
                            if (m.getDescriptor().version().isPresent()) {
                                return m.getDescriptor().toNameAndVersion();
                            }
                            // Construct version string from jar file name
                            String version = m.getLayer().configuration()
                                    .findModule(m.getName())
                                    .map(ResolvedModule::reference)
                                    .map(ModuleReference::location).flatMap(Function.identity())
                                    .map(uri -> {
                                        Matcher matcher = pattern.matcher(uri.getPath());
                                        return matcher.find() ? matcher.group(1) : null;
                                    }).orElse(null);

                            return version == null ? m.getName() : m.getName() + "@" + version;
                        })
                        .filter(str -> !str.startsWith("java.")
                                && !str.startsWith("jdk."))
                        .sorted()
                        .collect(Collectors.joining("\n  ", "  ", ""));
    }
}
