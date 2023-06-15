/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines functions for 2D geometry processing.
 */
@SuppressWarnings("module")
module org.jhotdraw8.geom {
    exports org.jhotdraw8.geom;
    exports org.jhotdraw8.geom.intersect;
    exports org.jhotdraw8.geom.contour;
    exports org.jhotdraw8.geom.biarc;
    exports org.jhotdraw8.geom.shape;
    requires transitive javafx.graphics;
    requires static org.jhotdraw8.annotation;
    requires transitive java.desktop;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.base;
    requires java.logging;
}