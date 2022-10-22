/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.geom {
    exports org.jhotdraw8.geom;
    exports org.jhotdraw8.geom.intersect;
    exports org.jhotdraw8.geom.contour;
    exports org.jhotdraw8.geom.biarc;
    requires javafx.graphics;
    requires org.jhotdraw8.annotation;
    requires java.desktop;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.base;
    requires java.logging;
}