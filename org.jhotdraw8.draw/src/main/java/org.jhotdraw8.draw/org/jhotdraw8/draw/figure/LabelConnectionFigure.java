/*
 * @(#)LabelConnectionFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;

public interface LabelConnectionFigure extends Figure {
    /**
     * The label target.
     * <p>
     * This property can not be styled with CSS because it affects the
     * layout observer relationship between the label and the target
     * figure.
     */
    SimpleNullableKey<Figure> LABEL_TARGET = new SimpleNullableKey<>("labelTarget", Figure.class, null);
    /**
     * The connector.
     */
    SimpleNullableKey<Connector> LABEL_CONNECTOR = new SimpleNullableKey<>("labelConnector", Connector.class, null);

}
