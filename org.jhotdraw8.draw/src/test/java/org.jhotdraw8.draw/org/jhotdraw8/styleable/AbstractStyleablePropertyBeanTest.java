/*
 * @(#)AbstractStyleablePropertyBeanTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.styleable;

import javafx.beans.property.ReadOnlyProperty;
import org.jhotdraw8.draw.figure.FillableFigure;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.fxbase.styleable.AbstractStyleablePropertyBean;
import org.jhotdraw8.fxbase.styleable.StyleableBean;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AbstractStyleablePropertyBeanNGTest.
 *
 */
public class AbstractStyleablePropertyBeanTest {

    public AbstractStyleablePropertyBeanTest() {
    }

    @Test
    public void testNullValueIsNotSameAsDefaultPropertyValue() {
        AbstractStyleablePropertyBean instance = new AbstractStyleablePropertyBeanImpl();
        final NullablePaintableStyleableKey key = FillableFigure.FILL;


        assertNotNull(key.getDefaultValue(), "need a key with a non-null default value for this test");
        assertFalse(instance.getProperties().containsKey(key), "value has not been set, map must not contain key");
        assertEquals(instance.get(key), key.getDefaultValue(), "value has not been set, must deliver default value");

        instance.set(key, null);

        assertNull(instance.get(key), "value has been explicitly set to null");
        assertTrue(instance.getProperties().containsKey(key), "map must contain key after explicit set");

        instance.remove(key);

        assertEquals(instance.get(key), key.getDefaultValue(), "key has been removed, value must be default value");
        assertFalse(instance.getProperties().containsKey(key), "key has been removed, map must not contain key");

    }

    public static class AbstractStyleablePropertyBeanImpl extends AbstractStyleablePropertyBean {

        @Override
        public String getTypeSelector() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ReadOnlyProperty<String> idProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReadableSet<String> getStyleClasses() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getStyle() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }


        @Override
        public StyleableBean getStyleableParent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ReadableSet<String> getPseudoClassStates() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
