/*
 * @(#)NonNullObjectPropertyTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 */
public class NonNullObjectPropertyTest {

    @Test
    public void testBind() {
        double minv = 0;
        double maxv = 1;

        NonNullObjectProperty<String> p1 = new NonNullObjectProperty<>(null, null, "hello");
        ObjectProperty<String> p2 = new SimpleObjectProperty<>(null);
        p1.addListener((o, oldv, newv) -> {
            Objects.requireNonNull(newv, "newValue");
        });

        // must not set a null value
        p1.set(null);
        String s = p1.get();
        assert s != null;

    }
}
