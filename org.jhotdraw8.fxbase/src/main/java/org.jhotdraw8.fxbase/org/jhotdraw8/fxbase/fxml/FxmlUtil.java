/*
 * @(#)FxmlUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class FxmlUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private FxmlUtil() {
    }

    public static <T> Supplier<T> createFxmlControllerSupplier(URL fxml,
                                                               ResourceBundle resources) {
        return createFxmlControllerSupplier(fxml, resources, (Callback<Class<?>, Object>) null);
    }


    public static <T> Supplier<T> createFxmlControllerSupplier(URL fxml,
                                                               ResourceBundle resources,
                                                                        @Nullable Supplier<T> controllerFactory) {
        return () -> FxmlUtil.<T>createFxmlControllerSupplier(fxml, resources, controllerFactory == null ? null : clazz -> controllerFactory.get()).get();
    }

    public static <T> Supplier<T> createFxmlControllerSupplier(URL fxml,
                                                               ResourceBundle resources,
                                                                        @Nullable Callback<Class<?>, Object> controllerFactory) {
        return () -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);
            loader.setControllerFactory(controllerFactory);
            try (InputStream in = fxml.openStream()) {
                loader.load(in);
                return loader.getController();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };
    }

    public static <T extends Node> Supplier<T> createFxmlNodeSupplier(URL fxml, @Nullable ResourceBundle resourceBundle) {
        return () -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resourceBundle);
            try (InputStream in = fxml.openStream()) {
                return loader.load(in);
            } catch (IOException ex) {
                throw new InternalError(ex);
            }
        };
    }

    public static FXMLLoader load(@Nullable URL fxml, @Nullable ResourceBundle resourceBundle) {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(resourceBundle);
        try (InputStream in = fxml.openStream()) {
            loader.load(in);
        } catch (IOException ex) {
            throw new InternalError(ex);
        }
        return loader;
    }
}
