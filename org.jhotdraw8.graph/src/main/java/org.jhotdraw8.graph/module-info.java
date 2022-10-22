/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.graph {
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.base;
    exports org.jhotdraw8.graph;
    exports org.jhotdraw8.graph.algo;
    exports org.jhotdraw8.graph.iterator;
    exports org.jhotdraw8.graph.path;
    exports org.jhotdraw8.graph.path.algo;
    exports org.jhotdraw8.graph.io;
}