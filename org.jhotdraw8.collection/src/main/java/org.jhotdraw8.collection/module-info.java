/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines primitive collections, read-only collections and immutable collections.
 */
@SuppressWarnings("module")
module org.jhotdraw8.collection {
    requires transitive org.jhotdraw8.annotation;
    requires org.jhotdraw8.base;
    exports org.jhotdraw8.collection;
    exports org.jhotdraw8.collection.facade;
    exports org.jhotdraw8.collection.enumerator;
    exports org.jhotdraw8.collection.champ;
    exports org.jhotdraw8.collection.primitive;
    exports org.jhotdraw8.collection.rrb;
    exports org.jhotdraw8.collection.readonly;
    exports org.jhotdraw8.collection.immutable;
    exports org.jhotdraw8.collection.mapped;
    exports org.jhotdraw8.collection.sequenced;
    exports org.jhotdraw8.collection.reflect;
    exports org.jhotdraw8.collection.function;
}