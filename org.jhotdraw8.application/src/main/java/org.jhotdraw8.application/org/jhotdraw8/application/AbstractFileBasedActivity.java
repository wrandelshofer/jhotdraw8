/*
 * @(#)AbstractFileBasedActivity.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.action.file.CloseFileAction;
import org.jhotdraw8.application.action.file.ExportFileAction;
import org.jhotdraw8.application.action.file.RevertFileAction;
import org.jhotdraw8.application.action.file.SaveFileAction;
import org.jhotdraw8.application.action.file.SaveFileAsAction;
import org.jhotdraw8.base.net.UriUtil;
import org.jhotdraw8.fxbase.binding.CustomBinding;

import java.net.URI;

/**
 * AbstractFileBasedActivity.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractFileBasedActivity extends AbstractActivity implements FileBasedActivity {

    protected final @NonNull BooleanProperty modified = new SimpleBooleanProperty(this, MODIFIED_PROPERTY) {
        @Override
        public void set(boolean newValue) {
            super.set(newValue); //To change body of generated methods, choose Tools | Templates.
        }

    };
    @SuppressWarnings("this-escape")
    protected final @NonNull ObjectProperty<URI> uri = new SimpleObjectProperty<>(this, URI_PROPERTY);
    @SuppressWarnings("this-escape")
    protected final @NonNull ObjectProperty<DataFormat> dataFormat = new SimpleObjectProperty<>(this, DATA_FORMAT_PROPERTY);

    public AbstractFileBasedActivity() {
    }

    @Override
    protected void initTitle() {
        titleProperty().bind(CustomBinding.convert(uri, uri ->
                uri == null ?
                        getApplication().getResources().getString("unnamedFile") : UriUtil.getName(uri)));
    }

    @Override
    public @NonNull BooleanProperty modifiedProperty() {
        return modified;
    }

    @Override
    public void clearModified() {
        modified.set(false);
    }

    protected void markAsModified() {
        modified.set(true);
    }

    @Override
    public @NonNull ObjectProperty<URI> uriProperty() {
        return uri;
    }

    @Override
    public @NonNull ObjectProperty<DataFormat> dataFormatProperty() {
        return dataFormat;
    }

    @Override
    protected void initActions(@NonNull ObservableMap<String, Action> map) {
        map.put(RevertFileAction.ID, new RevertFileAction(this));
        map.put(SaveFileAction.ID, new SaveFileAction(this));
        map.put(SaveFileAsAction.ID, new SaveFileAsAction(this));
        map.put(ExportFileAction.ID, new ExportFileAction(this));
        map.put(CloseFileAction.ID, new CloseFileAction(this));
    }

    @Override
    public void destroy() {
        clear();
        super.destroy();
    }
}
