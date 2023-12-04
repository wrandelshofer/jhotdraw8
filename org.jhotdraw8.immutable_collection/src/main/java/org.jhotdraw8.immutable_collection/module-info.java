/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines interfaces for primitive collections, read-only collections and immutable collections,
 * and provides efficient implementations of these interfaces.
 */
@SuppressWarnings("module")
module org.jhotdraw8.immutable_collection {
    requires static org.jhotdraw8.annotation;
    exports org.jhotdraw8.immutable_collection.immutable;
    exports org.jhotdraw8.immutable_collection.readonly;
    exports org.jhotdraw8.immutable_collection.sequenced;
    exports org.jhotdraw8.immutable_collection;
}