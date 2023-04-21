/*
 * @(#)SimpleFigureIdFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.io;

import javafx.css.Styleable;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.SimpleIdFactory;
import org.jhotdraw8.fxbase.styleable.StyleableBean;

/**
 * SimpleFigureIdFactory.
 *
 * @author Werner Randelshofer
 */
public class SimpleFigureIdFactory extends SimpleIdFactory {
    public SimpleFigureIdFactory() {
    }

    @Override
    public @Nullable String createId(Object object) {
        String id = getId(object);

        if (id == null) {
            if (object instanceof Styleable) {
                Styleable f = (Styleable) object;
                id = f.getId();
                if (id != null && getObject(id) == null) {
                    putIdAndObject(id, object);
                } else {
                    id = super.createId(object, f.getTypeSelector().toLowerCase());
                }
            } else if (object instanceof StyleableBean) {
                StyleableBean f = (StyleableBean) object;
                id = f.getId();
                if (id != null && getObject(id) == null) {
                    putIdAndObject(id, object);
                } else {
                    id = super.createId(object, f.getTypeSelector().toLowerCase());
                }
            } else {
                id = super.createId(object);
            }
        }
        return id;
    }

    public @Nullable String putId(Object object) {
        String id = getId(object);

        if (id == null) {
            if (object instanceof Styleable) {
                Styleable f = (Styleable) object;
                id = f.getId();
                if (id != null) {
                    putIdAndObject(id, object);
                } else {
                    id = super.createId(object, f.getTypeSelector());
                }
            } else {
                id = super.createId(object);
            }
        }
        return id;
    }
}
