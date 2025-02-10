/*
 * @(#)ExportFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.scene.control.Dialog;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.controls.urichooser.FileURIChooser;
import org.jhotdraw8.application.controls.urichooser.URIChooser;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Presents a file chooser to the user and then exports the contents of the
 * active view to the chosen file.
 *
 */
public class ExportFileAction extends AbstractSaveFileAction {

    public static final String ID = "file.export";
    private final @Nullable Function<DataFormat, Dialog<SequencedMap<Key<?>, Object>>> optionsDialogFactory;
    public static final Key<URIChooser> EXPORT_CHOOSER_KEY = new NullableObjectKey<>("exportChooser", URIChooser.class);
    public static final Key<Supplier<URIChooser>> EXPORT_CHOOSER_FACTORY_KEY = new NullableObjectKey<>("exportChooserFactory",
            new SimpleParameterizedType(Supplier.class, URIChooser.class));

    /**
     * Creates a new instance.
     *
     * @param activity the view
     */
    public ExportFileAction(FileBasedActivity activity) {
        this(activity, ID, null);
    }

    public ExportFileAction(FileBasedActivity activity, @Nullable Function<DataFormat, Dialog<SequencedMap<Key<?>, Object>>> optionsDialog) {
        this(activity, ID, optionsDialog);
    }


    /**
     * Creates a new instance.
     *
     * @param activity      the view, nullable
     * @param id            the id, nonnull
     * @param optionsDialog the dialog for specifying export options
     */
    public ExportFileAction(FileBasedActivity activity, String id, Function<DataFormat, Dialog<SequencedMap<Key<?>, Object>>> optionsDialog) {
        super(activity, id, true);
        this.optionsDialogFactory = optionsDialog;
    }

    @Override
    protected URIChooser getChooser(FileBasedActivity view) {
        URIChooser chooser = app.get(EXPORT_CHOOSER_KEY);
        if (chooser == null) {
            Supplier<URIChooser> factory = app.get(EXPORT_CHOOSER_FACTORY_KEY);
            chooser = factory == null ? new FileURIChooser(FileURIChooser.Mode.SAVE) : factory.get();
            app.set(EXPORT_CHOOSER_KEY, chooser);
        }
        return chooser;
    }

    @Override
    protected @Nullable Dialog<SequencedMap<Key<?>, Object>> createOptionsDialog(DataFormat format) {
        return optionsDialogFactory == null ? null : optionsDialogFactory.apply(format);
    }

    @Override
    protected void onSaveSucceeded(FileBasedActivity v, URI uri, DataFormat format) {
        // empty
    }
}
