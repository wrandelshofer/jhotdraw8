/*
 * @(#)CssColorConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package io;

import javafx.scene.paint.Color;
import org.jhotdraw8.css.converter.ColorCssConverter;
import org.jhotdraw8.css.value.CssColor;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColorCssConverterTest {
    @TestFactory
    public List<DynamicTest> dynamicTestsSvgColor() throws IOException {
        return List.of(
                DynamicTest.dynamicTest("reddish", () -> testSvgColor(
                        "#cfffff", Color.web("#cfffff"))),
                DynamicTest.dynamicTest("greenish", () -> testSvgColor(
                        "rgb(20% 73.333% 20%)", Color.web("#33bb33")))
        )
                ;

    }

    private void testSvgColor(String inputStr, Color expected) throws IOException, ParseException {
        ColorCssConverter c = new ColorCssConverter();
        CssColor actual = c.fromString(inputStr);
        String actualStr = c.toString(actual);
        CssColor cssColorRecreatedFromJavaFXColor = new CssColor(actual.getColor());
        String recreatedActualStr = cssColorRecreatedFromJavaFXColor.getName();
        CssColor recreatedColor = c.fromString(recreatedActualStr);

        assertEquals(expected.toString(), actual.getColor().toString());
        assertEquals(inputStr, actualStr);
        assertEquals(expected.toString(), recreatedColor.getColor().toString());
    }
}
