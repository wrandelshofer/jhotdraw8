/*
 * @(#)GrapherApplication.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.grapher;

import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.jhotdraw8.application.AbstractFileBasedApplication;
import org.jhotdraw8.application.controls.urichooser.FileURIChooser;
import org.jhotdraw8.application.controls.urichooser.URIExtensionFilter;
import org.jhotdraw8.draw.DrawStylesheets;
import org.jhotdraw8.draw.io.BitmapExportOutputFormat;
import org.jhotdraw8.draw.io.XmlEncoderOutputFormat;
import org.jhotdraw8.fxbase.fxml.FxmlUtil;
import org.jhotdraw8.svg.io.FXSvgFullWriter;
import org.jhotdraw8.svg.io.FXSvgTinyWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jhotdraw8.application.action.file.ExportFileAction.EXPORT_CHOOSER_FACTORY_KEY;
import static org.jhotdraw8.fxbase.clipboard.DataFormats.registerDataFormat;

/**
 * GrapherApplication.
 *
 * @author Werner Randelshofer
 */
public class GrapherApplication extends AbstractFileBasedApplication {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.getLogger(GrapherApplication.class.getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e));

        // See
        // https://bugs.openjdk.java.net/browse/JDK-8091497

        if (Screen.getPrimary().getOutputScaleX() >= 2.0) {
            // The following settings improve font rendering quality on
            // retina displays (no color fringes around characters).
            System.setProperty("prism.subpixeltext", "on");
            System.setProperty("prism.lcdtext", "false");
        } else {
            // The following settings improve font rendering on
            // low-res lcd displays (less color fringes around characters).
            System.setProperty("prism.text", "t2k");
            System.setProperty("prism.lcdtext", "true");
        }

        launch(args);
    }

    @Override
    protected void initFactories() {
        setActivityFactory(GrapherActivity::new);
        setMenuBarFactory(FxmlUtil.createFxmlNodeSupplier(
                getClass().getResource("GrapherMenuBar.fxml"),
                getResources().asResourceBundle()));
    }

    @Override
    protected void initProperties() {
        set(NAME_KEY, "Grapher");
        set(COPYRIGHT_KEY, "Copyright © 2021 The authors and contributors of JHotDraw.");
        set(LICENSE_KEY, "MIT License.");

        List<URIExtensionFilter> exportExtensions = new ArrayList<>();
        exportExtensions.add(new URIExtensionFilter("SVG Full", registerDataFormat(FXSvgFullWriter.SVG_MIME_TYPE_WITH_VERSION), "*.svg"));
        exportExtensions.add(new URIExtensionFilter("SVG Tiny", registerDataFormat(FXSvgTinyWriter.SVG_MIME_TYPE_WITH_VERSION), "*.svg"));
        exportExtensions.add(new URIExtensionFilter("PNG", registerDataFormat(BitmapExportOutputFormat.PNG_MIME_TYPE), "*.png"));
        exportExtensions.add(new URIExtensionFilter("XMLSerialized", registerDataFormat(XmlEncoderOutputFormat.XML_SERIALIZER_MIME_TYPE), "*.ser.xml"));
        set(EXPORT_CHOOSER_FACTORY_KEY, () -> new FileURIChooser(FileURIChooser.Mode.SAVE, exportExtensions));
    }

    @Override
    protected void initResourceBundle() {
        setResources(GrapherLabels.getResources());
    }

    protected void initTheme() {
        /*
        final ThemeManager mgr = ThemeManager.getInstance();
        final NordLightTheme lightTheme = new NordLightTheme();
        final NordDarkTheme darkTheme = new NordDarkTheme();
        mgr.getThemes().addAll(new PrimerDarkTheme(), new PrimerLightTheme(), lightTheme, darkTheme);
        final Object value = MacOSPreferencesUtil.get(MacOSPreferencesUtil.GLOBAL_PREFERENCES, "AppleInterfaceStyle");
        if ("Dark".equals(value)) {
            mgr.setTheme(darkTheme);
        } else {
            mgr.setTheme(lightTheme);
        }
        */


        Platform.Preferences preferences = Platform.getPreferences();
        ChangeListener<ColorScheme> listener = (observable, oldValue, newValue) -> {
            if (oldValue != null) {
                String oldTheme = getClass().getResource(switch (oldValue) {
                    case LIGHT -> "light-theme.css";
                    case DARK -> "dark-theme.css";
                }).toString();
                getStylesheets().remove(oldTheme);
                Window.getWindows().forEach(w -> System.out.println(w.getScene().getStylesheets().remove(oldTheme)));
            }
            if (newValue != null) {
                String newTheme = getClass().getResource(switch (newValue) {
                    case LIGHT -> "light-theme.css";
                    case DARK -> "dark-theme.css";
                }).toString();
                getStylesheets().add(newTheme);
                Window.getWindows().forEach(w -> System.out.println(w.getScene().getStylesheets().add(newTheme)));
            }

        };
        listener.changed(null, null, preferences.getColorScheme());
        preferences.colorSchemeProperty().addListener(listener);

        getStylesheets().add(DrawStylesheets.getInspectorsStylesheet());
    }
}
