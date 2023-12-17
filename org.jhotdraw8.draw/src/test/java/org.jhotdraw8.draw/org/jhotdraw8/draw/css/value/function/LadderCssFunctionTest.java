/*
 * @(#)LadderCssFunctionTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.function.CssFunction;
import org.jhotdraw8.css.function.VarCssFunction;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.manager.SimpleCssFunctionProcessor;
import org.jhotdraw8.css.model.DocumentSelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.draw.css.function.LadderCssFunction;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LadderCssFunctionTest extends AbstractCssFunctionProcessorTest {
    @Override
    protected CssFunctionProcessor<Element> createInstance(DocumentSelectorModel model, Map<String, ImmutableList<CssToken>> customProperties) {
        List<CssFunction<Element>> functions = new ArrayList<>();
        functions.add(new LadderCssFunction<>());
        functions.add(new VarCssFunction<>());
        return new SimpleCssFunctionProcessor<>(functions, model, customProperties);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsLadderCssFunctionTest() {
        return Arrays.asList(
                dynamicTest("1", () -> doTestProcess("foo", "foo")),
                dynamicTest("black on white, default", () -> doTestProcess("ladder(white, white 0.49, black 0.5)", "black")),
                dynamicTest("white on black, default", () -> doTestProcess("ladder(black, white 0.49, black 0.5)", "white")),
                dynamicTest("black on white, percentage", () -> doTestProcess("ladder(white, white 49%, black 50%)", "black")),
                dynamicTest("white on black, percentage", () -> doTestProcess("ladder(black, white 49%, black 50%)", "white")),
                dynamicTest("black on white, percentage up-to", () -> doTestProcess("ladder(white, white 50%, black 100%)", "black")),
                dynamicTest("white on black, percentage up-to", () -> doTestProcess("ladder(black, white 50%, black 100%)", "white")),
                dynamicTest("black on white, percentage corners", () -> doTestProcess("ladder(white, white 0%, black 100%)", "black")),
                dynamicTest("white on black, percentage corners", () -> doTestProcess("ladder(black, white 0%, black 100%)", "white")),
                dynamicTest("black on white, percentage center", () -> doTestProcess("ladder(white, white 50%, black 50%)", "black")),
                dynamicTest("white on black, percentage center", () -> doTestProcess("ladder(black, white 50%, black 50%)", "white")),
                dynamicTest("black on gray", () -> doTestProcess("ladder(gray, white 49%, black 50%)", "black"))
        );
    }


}
