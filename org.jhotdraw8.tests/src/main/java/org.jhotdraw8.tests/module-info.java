/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/// Contains tests that only work on the module-path.
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.tests {
    requires transitive org.jspecify;

    requires java.logging;
    requires org.jhotdraw8.icollection;
    requires jol.core;
    requires kotlinx.collections.immutable;
    requires scala.library;
    requires io.vavr;
}