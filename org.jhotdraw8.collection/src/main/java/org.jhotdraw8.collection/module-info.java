/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines primitive collections, read-only collections and immutable collections.
 */
@SuppressWarnings("module")
module org.jhotdraw8.collection {
    requires static org.jhotdraw8.annotation;
    requires org.jhotdraw8.icollection;
    exports org.jhotdraw8.collection.enumerator;
    exports org.jhotdraw8.collection.function;
    exports org.jhotdraw8.collection.iterator;
    exports org.jhotdraw8.collection.mapped;
    exports org.jhotdraw8.collection.primitive;
    exports org.jhotdraw8.collection.reflect;
    exports org.jhotdraw8.collection.pair;
    exports org.jhotdraw8.collection.spliterator;
    exports org.jhotdraw8.collection.transform;
    exports org.jhotdraw8.collection.util;
}