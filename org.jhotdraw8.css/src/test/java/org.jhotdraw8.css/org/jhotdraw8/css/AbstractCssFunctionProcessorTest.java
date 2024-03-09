/*
 * @(#)AbstractCssFunctionProcessorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css;

import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.model.DocumentSelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

abstract class AbstractCssFunctionProcessorTest {

    protected abstract CssFunctionProcessor<Element> createInstance(DocumentSelectorModel model, Map<String, ImmutableList<CssToken>> customProperties);


    protected void doTestProcess(String expression, @Nullable String expected) throws Exception {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // We do not want that the reader creates a socket connection!
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        Document doc = builder.newDocument();
        doc.getDocumentElement();
        Element elem = doc.createElement("Car");
        elem.setAttribute("id", "o1");
        elem.setAttribute("doors", "5");
        elem.setAttribute("length", "3475mm");
        elem.setAttribute("width", "1475mm");
        elem.setAttribute("height", "1608mm");
        elem.setAttribute("rearBrakes", "Drum");
        doc.appendChild(elem);

        StreamCssTokenizer tt = new StreamCssTokenizer(expression, null);
        StringBuilder buf = new StringBuilder();
        Consumer<CssToken> consumer = t -> buf.append(t.fromToken());

        DocumentSelectorModel model = new DocumentSelectorModel();
        SequencedMap<String, ImmutableList<CssToken>> customProperties = new LinkedHashMap<>();
        customProperties.put("--blarg", VectorList.of(new CssToken(CssTokenType.TT_STRING, "blarg")));
        customProperties.put("--recursion-base", VectorList.of(new CssToken(CssTokenType.TT_STRING, "recursion base")));
        customProperties.put("--recursive-1", VectorList.of(new CssToken(CssTokenType.TT_FUNCTION, "var"),
                new CssToken(CssTokenType.TT_IDENT, "--recursion-base"),
                new CssToken(CssTokenType.TT_RIGHT_BRACKET)));
        customProperties.put("--recursive-2", VectorList.of(new CssToken(CssTokenType.TT_FUNCTION, "var"),
                new CssToken(CssTokenType.TT_IDENT, "--recursive-1"),
                new CssToken(CssTokenType.TT_RIGHT_BRACKET)));
        customProperties.put("--endless-recursion", VectorList.of(new CssToken(CssTokenType.TT_FUNCTION, "var"),
                new CssToken(CssTokenType.TT_IDENT, "--endless-recursion"),
                new CssToken(CssTokenType.TT_RIGHT_BRACKET)));
        CssFunctionProcessor<Element> instance = createInstance(model, customProperties);

        try {
            instance.process(elem, tt, consumer, new ArrayDeque<>());
            if (expected == null) {
                fail("must throw ParseException");
            }
            assertEquals(expected, buf.toString());
        } catch (ParseException e) {
            if (expected != null) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception.", e);

                fail("must not throw ParseException " + e);
            }
        }


    }

}