/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Defines interfaces for read-only collections and immutable collections,
 * and provides efficient implementations of these interfaces.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.icollection {
    requires transitive org.jspecify;

    requires java.logging;
    exports org.jhotdraw8.icollection.exception;
    exports org.jhotdraw8.icollection.facade;
    exports org.jhotdraw8.icollection.immutable;
    exports org.jhotdraw8.icollection.readonly;
    exports org.jhotdraw8.icollection.sequenced;
    exports org.jhotdraw8.icollection;
}