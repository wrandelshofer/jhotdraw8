/*
 * @(#)AbstractInspector.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jhotdraw8.annotation.NonNull;

/**
 * AbstractInspector.
 *
 * @param <S> the subject type
 * @author Werner Randelshofer
 */
public abstract class AbstractInspector<S> implements Inspector<S> {

    protected final @NonNull ObjectProperty<S> subject = new SimpleObjectProperty<>(this, SUBJECT_PROPERTY);
    protected final @NonNull BooleanProperty showing = new SimpleBooleanProperty(this, SHOWING_PROPERTY, true);

    public AbstractInspector() {
    }

    @Override
    public @NonNull ObjectProperty<S> subjectProperty() {
        return subject;
    }

    @Override
    public @NonNull BooleanProperty showingProperty() {
        return showing;
    }


}
