/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Defines functions for 2D geometry processing.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.geom {
    requires transitive javafx.graphics;
    requires transitive org.jspecify;
    requires transitive java.desktop;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.base;
    requires java.logging;
    requires org.jhotdraw8.icollection;

    exports org.jhotdraw8.geom;
    exports org.jhotdraw8.geom.intersect;
    exports org.jhotdraw8.geom.contour;
    exports org.jhotdraw8.geom.biarc;
    exports org.jhotdraw8.geom.shape;
}