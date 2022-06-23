/*
 * @(#)AbstractLeafFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.EmptyEnumerator;
import org.jhotdraw8.collection.enumerator.Enumerator;

/**
 * This base class can be used to implement figures which do not support child
 * figures.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractLeafFigure extends AbstractFigure {

    public AbstractLeafFigure() {
    }

    @Override
    public final @NonNull ObservableList<Figure> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public @NonNull Enumerator<Figure> getChildEnumerator() {
        return EmptyEnumerator.emptyEnumerator();
    }

    /**
     * This method returns false.
     *
     * @return false
     */
    @Override
    public final boolean isAllowsChildren() {
        return false;
    }

    @Override
    public boolean isSuitableParent(@NonNull Figure newParent) {
        return true;
    }

    /**
     * This method returns false for all children.
     *
     * @param newChild The new child figure.
     * @return false
     */
    @Override
    public boolean isSuitableChild(@NonNull Figure newChild) {
        return false;
    }
}
