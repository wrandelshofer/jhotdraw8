/*
 * @(#)ReadOnlyNonNullWrapperTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 */
public class ReadOnlyNonNullWrapperTest {

    @Test
    public void testBind() {
        double minv = 0;
        double maxv = 1;

        ReadOnlyNonNullWrapper<String> p1 = new ReadOnlyNonNullWrapper<>(null, null, "hello");
        ObjectProperty<String> p2 = new SimpleObjectProperty<>(null);
        p1.addListener((o, oldv, newv) -> {
            assertNotNull(newv);
        });
        try {
            p1.set(null);
            fail("NPE not thrown on set");
        } catch (NullPointerException e) {

        }
        try {
            p1.bind(p2);
            fail("NPE not thrown from bind");
        } catch (NullPointerException e) {

        }
    }

}
