/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.collection {
    requires org.jhotdraw8.annotation;
    requires javafx.base;
    exports org.jhotdraw8.collection;
    exports org.jhotdraw8.collection.typesafekey;
    exports org.jhotdraw8.collection.facade;
    exports org.jhotdraw8.collection.enumerator;
    exports org.jhotdraw8.collection.champ;
    exports org.jhotdraw8.collection.primitive;
    exports org.jhotdraw8.collection.rrb;
    exports org.jhotdraw8.collection.indexedset;
    exports org.jhotdraw8.collection.readonly;
    exports org.jhotdraw8.collection.immutable;
    exports org.jhotdraw8.collection.mapped;
    exports org.jhotdraw8.collection.observable;
    exports org.jhotdraw8.collection.sequenced;
    exports org.jhotdraw8.collection.sharedkeys;
    exports org.jhotdraw8.collection.reflect;
    exports org.jhotdraw8.collection.function;
}