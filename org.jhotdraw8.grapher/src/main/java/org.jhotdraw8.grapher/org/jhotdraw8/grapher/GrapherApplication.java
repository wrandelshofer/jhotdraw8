/*
 * @(#)GrapherApplication.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.grapher;

import javafx.stage.Screen;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.AbstractFileBasedApplication;
import org.jhotdraw8.application.controls.urichooser.FileURIChooser;
import org.jhotdraw8.application.controls.urichooser.URIExtensionFilter;
import org.jhotdraw8.draw.DrawStylesheets;
import org.jhotdraw8.draw.io.BitmapExportOutputFormat;
import org.jhotdraw8.draw.io.XmlEncoderOutputFormat;
import org.jhotdraw8.fxbase.fxml.FxmlUtil;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.os.macos.MacOSPreferencesUtil;
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
    public static final @NonNull NonNullKey<Boolean> DARK_MODE_KEY = new NonNullObjectKey<>("darkMode", Boolean.class, Boolean.FALSE);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.getLogger(GrapherApplication.class.getName()).log(Level.WARNING, "Unexpected Exception.", e));

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

        // https://stackoverflow.com/questions/57303286/how-to-detect-if-osx-in-dark-or-light-mode-in-auto-appearance-mode-in-catalina

        final Object interfaceStyle = MacOSPreferencesUtil.get(MacOSPreferencesUtil.GLOBAL_PREFERENCES, "AppleInterfaceStyle");
        final Object interfaceStyleSwitchesAutomatically = MacOSPreferencesUtil.get(MacOSPreferencesUtil.GLOBAL_PREFERENCES, "AppleInterfaceStyleSwitchesAutomatically");
        if ("Dark".equals(interfaceStyle)
                || interfaceStyle == null && "true".equals(interfaceStyleSwitchesAutomatically)) {
            set(DARK_MODE_KEY, true);
            getStylesheets().add(getClass().getResource("dark-theme.css").toString());
        } else {
            set(DARK_MODE_KEY, false);
            getStylesheets().add(getClass().getResource("light-theme.css").toString());
        }
        getStylesheets().add(DrawStylesheets.getInspectorsStylesheet());
    }
}
