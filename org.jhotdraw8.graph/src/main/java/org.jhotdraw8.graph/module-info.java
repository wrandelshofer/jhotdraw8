/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

@SuppressWarnings("module")
module org.jhotdraw8.graph {
    requires transitive org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.collection;

    exports org.jhotdraw8.graph;
    exports org.jhotdraw8.graph.algo;
    exports org.jhotdraw8.graph.iterator;
    exports org.jhotdraw8.graph.path;
    exports org.jhotdraw8.graph.path.algo;
    exports org.jhotdraw8.graph.path.backlink;
    exports org.jhotdraw8.graph.io;
}