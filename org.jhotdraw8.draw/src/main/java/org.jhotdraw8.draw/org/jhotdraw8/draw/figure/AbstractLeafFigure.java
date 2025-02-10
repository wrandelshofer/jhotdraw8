/*
 * @(#)AbstractLeafFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This base class can be used to implement figures which do not support child
 * figures.
 *
 */
public abstract class AbstractLeafFigure extends AbstractFigure {

    public AbstractLeafFigure() {
    }

    @Override
    public final ObservableList<Figure> getChildren() {
        return FXCollections.emptyObservableList();
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
    public boolean isSuitableParent(Figure newParent) {
        return true;
    }

    /**
     * This method returns false for all children.
     *
     * @param newChild The new child figure.
     * @return false
     */
    @Override
    public boolean isSuitableChild(Figure newChild) {
        return false;
    }
}
