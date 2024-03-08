package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.application.Platform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.os.macos.MacOSPreferencesUtil;

import java.util.function.Supplier;

public class FontChooserModelFactories {
    private static Supplier<FontCollectionsFactory> singleton;

    static {
        if (MacOSPreferencesUtil.isMacOs()) singleton = MacOSFontCollectionsFactory::new;
        else singleton = DefaultFontCollectionsFactory::new;
    }

    private FontChooserModelFactories() {
    }

    public static Supplier<FontCollectionsFactory> getSingleton() {
        return singleton;
    }

    public static void setSingleton(Supplier<FontCollectionsFactory> singleton) {
        FontChooserModelFactories.singleton = singleton;
    }

    public static @NonNull FontChooserModel create() {
        FontChooserModel model = new FontChooserModel();
        if (Platform.isFxApplicationThread()) {
            singleton.get().createAsync().thenAccept(m -> model.getFontCollections().addAll(m));
        } else {
            model.getFontCollections().addAll(singleton.get().create());
        }
        return model;

    }
}
