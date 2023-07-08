/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines primitive collections, read-only collections and immutable collections.
 */
@SuppressWarnings("module")
module org.jhotdraw8.pcollection {
    requires static org.jhotdraw8.annotation;
    exports org.jhotdraw8.pcollection.immutable;
    exports org.jhotdraw8.pcollection.readonly;
    exports org.jhotdraw8.pcollection.sequenced;
    exports org.jhotdraw8.pcollection;
}