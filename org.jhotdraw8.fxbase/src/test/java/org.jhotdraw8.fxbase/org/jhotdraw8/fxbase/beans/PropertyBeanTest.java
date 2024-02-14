/*
 * @(#)PropertyBeanTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.value.ObservableValue;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PropertyBeanTest.
 *
 * @author Werner Randelshofer
 */
public class PropertyBeanTest {

    public PropertyBeanTest() {
    }

    /**
     * Test of valueAt method, of class PropertyBean.
     */
    @Test
    public void testGetObservableValue() {
        Key<String> key = new NullableObjectKey<String>("key", String.class);
        PropertyBean bean = new SimplePropertyBean();
        ObservableValue<String> ov = bean.valueAt(key);
        String[] newValue = new String[1];
        ov.addListener((o, oldv, newv) -> {
            newValue[0] = newv;
        });
        bean.set(key, "hello");
        assertEquals("hello", newValue[0]);
    }

}
