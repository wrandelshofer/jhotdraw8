/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Defines data structures and algorithms for graphs.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.graph {
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.collection;
    requires org.jhotdraw8.icollection;
    requires java.desktop;

    exports org.jhotdraw8.graph;
    exports org.jhotdraw8.graph.algo;
    exports org.jhotdraw8.graph.iterator;
    exports org.jhotdraw8.graph.path.algo;
    exports org.jhotdraw8.graph.path.backlink;
    exports org.jhotdraw8.graph.io;
}