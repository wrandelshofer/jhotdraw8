/*
 * @(#)CustomSkin.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.skin;

import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;

/**
 * A custom skin without behavior.
 *
 * @author Werner Randelshofer
 */
public class CustomSkin<C extends Control> extends SkinBase<C> {

    public CustomSkin(C control) {
        super(control);
    }

}
