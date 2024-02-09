/*
 * @(#)FigureSelectorModelTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.model;

import javafx.css.StyleOrigin;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.FillableFigure;
import org.jhotdraw8.draw.figure.LabelFigure;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.icollection.VectorList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * FigureSelectorModelTest.
 *
 * @author Werner Randelshofer
 */
public class FigureSelectorModelTest {

    public FigureSelectorModelTest() {
    }

    /**
     * Test of getProperties method, of class SimplePropertyBean.
     */
    @Test
    public void testNullValueIsNotSameAsDefaultPropertyValue() throws ParseException {
        LabelFigure figure = new LabelFigure();
        FigureSelectorModel instance = new FigureSelectorModel();

        final NullablePaintableStyleableKey key = FillableFigure.FILL;
        final String attrName = key.getCssName();
        final String namespace = key.getCssNamespace();
        final Converter<Paintable> converter = key.getCssConverter();


        Assertions.assertNotNull(key.getDefaultValue(), "need a key with a non-null default value for this test");

        assertEquals(null, instance.getAttributeAsString(figure, namespace, attrName), "no value has been set, must be null");

        instance.setAttribute(figure, StyleOrigin.USER, namespace, attrName, VectorList.of(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE)));

        assertNull(figure.get(key), "figure.get(key) value has been explicitly set to null");

        assertEquals(instance.getAttributeAsString(figure, namespace, attrName), converter.toString(null), "model.get(figure,key) value has been explicitly set to null");

    }

}
