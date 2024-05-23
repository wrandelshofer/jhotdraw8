/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Provides additional collection classes that interoperate with JavaFX observable collections.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.fxcollection {
    requires transitive static org.jspecify;

    requires transitive javafx.base;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.icollection;

    exports org.jhotdraw8.fxcollection;
    exports org.jhotdraw8.fxcollection.facade;
    exports org.jhotdraw8.fxcollection.typesafekey;
    exports org.jhotdraw8.fxcollection.indexedset;
    exports org.jhotdraw8.fxcollection.sharedkeys;
    exports org.jhotdraw8.fxcollection.mapped;
}