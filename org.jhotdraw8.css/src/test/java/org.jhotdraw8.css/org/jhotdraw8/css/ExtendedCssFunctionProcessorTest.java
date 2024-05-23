/*
 * @(#)ExtendedCssFunctionProcessorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css;

import org.jhotdraw8.css.function.AttrCssFunction;
import org.jhotdraw8.css.function.CalcCssFunction;
import org.jhotdraw8.css.function.ConcatCssFunction;
import org.jhotdraw8.css.function.CssFunction;
import org.jhotdraw8.css.function.ReplaceCssFunction;
import org.jhotdraw8.css.function.RoundCssFunction;
import org.jhotdraw8.css.function.VarCssFunction;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.manager.SimpleCssFunctionProcessor;
import org.jhotdraw8.css.model.DocumentSelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ExtendedCssFunctionProcessorTest extends AbstractCssFunctionProcessorTest {

    @Override
    protected CssFunctionProcessor<Element> createInstance(DocumentSelectorModel model, Map<String, ImmutableList<CssToken>> customProperties) {
        List<CssFunction<Element>> functions = new ArrayList<>();
        functions.add(new AttrCssFunction<>());
        functions.add(new CalcCssFunction<>());
        functions.add(new VarCssFunction<>());
        functions.add(new ConcatCssFunction<>());
        functions.add(new ReplaceCssFunction<>());
        functions.add(new RoundCssFunction<>());
        return new SimpleCssFunctionProcessor<>(functions, model, customProperties);
    }


    @TestFactory
    public List<DynamicTest> dynamicTestsProcessingOfExtendedFunctions() {
        return Arrays.asList(
                dynamicTest("301", () -> doTestProcess("concat()", "\"\"")),
                dynamicTest("302", () -> doTestProcess("concat(\"a\",\"b\")", "\"ab\"")),
                //
                dynamicTest("401", () -> doTestProcess("concat(attr(id),\"x\")", "\"o1x\"")),
                //
                dynamicTest("501", () -> doTestProcess("replace(\"aabfooaabfooabfoob\")", null)),
                dynamicTest("502", () -> doTestProcess("replace(\"aabfooaabfooabfoob\",\"a*b\")", null)),
                dynamicTest("503", () -> doTestProcess("replace(\"aabfooaabfooabfoob\",\"a*b\",\"-\")", "\"-foo-foo-foo-\"")),
                //
                dynamicTest("601", () -> doTestProcess("replace(attr(id),\"\\\\d\",\"x\")", "\"ox\"")),
                //
                dynamicTest("701", () -> doTestProcess("round(0.5)", "1")),
                dynamicTest("702", () -> doTestProcess("round(-0.5)", "0")),
                dynamicTest("703", () -> doTestProcess("round(1.6)", "2")),
                dynamicTest("704", () -> doTestProcess("round(1.4)", "1")),
                dynamicTest("705", () -> doTestProcess("round(1.6m)", "2m")),
                dynamicTest("706", () -> doTestProcess("round(1.6%)", "2%"))
        );
    }
}