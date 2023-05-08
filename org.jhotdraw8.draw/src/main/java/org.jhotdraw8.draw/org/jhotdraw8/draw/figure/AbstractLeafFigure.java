/*
 * @(#)AbstractLeafFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.EmptySpliterator;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;

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
    public @NonNull EnumeratorSpliterator<Figure> getChildEnumerator() {
        return EmptySpliterator.emptyEnumerator();
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
