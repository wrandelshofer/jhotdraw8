package org.jhotdraw8.xml;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Tests {@link IndentingXMLStreamWriter}.
 */
public class IndentingXMLStreamWriterTest {

    /**
     * Tests examples from the web-site "Understanding xml:space".
     * <dl>
     *     <dt>Understanding xml:space</dt>
     *     <dd><a href="http://www.xmlplease.com/xml/xmlspace/">xmlplease.com</a></dd>
     * </dl>
     *
     * @return
     */
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsShouldFormatIdentically() {
        return Arrays.asList(
                dynamicTest("3. Whitespace only text nodes, xml:space=preserve", () -> shouldHonourXmlSpaceAttribute(
                        """
                                <root xml:space="preserve">
                                           <test> This is    great. </test>
                                </root>
                                """,
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <root xml:space="preserve">
                                           <test> This is    great. </test>
                                </root>"""
                )),
                dynamicTest("3. Whitespace only text nodes, no whitespace, xml:space=preserve", () -> shouldHonourXmlSpaceAttribute(
                        """
                                <root xml:space="preserve">
                                           <test> This is    great. </test>
                                </root>
                                """,
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <root xml:space="preserve">
                                           <test> This is    great. </test>
                                </root>"""
                )),
                dynamicTest("3. Whitespace only text nodes, xml:space=default", () -> shouldHonourXmlSpaceAttribute(
                        """
                                <root xml:space="default">
                                           <test> This is    great. </test>
                                </root>
                                """,
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <root xml:space="default">
                                  <test> This is    great. </test>
                                </root>"""
                )),
                dynamicTest("3. Whitespace only text nodes, no white space, xml:space=default", () -> shouldHonourXmlSpaceAttribute(
                        """
                                <root xml:space="default"><test> This is    great. </test></root>
                                """,
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <root xml:space="default">
                                  <test> This is    great. </test>
                                </root>"""
                )),
                dynamicTest("5. Mixed Content, xml:space=preserve", () -> shouldHonourXmlSpaceAttribute(
                        """
                                <root xml:space="preserve">
                                        <p>I <b>love</b> <i>Mozart</i>.</p>
                                </root>
                                """,
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <root xml:space="preserve">
                                        <p>I <b>love</b> <i>Mozart</i>.</p>
                                </root>"""
                )),
                dynamicTest("5. Mixed Content, xml:space=default", () -> shouldHonourXmlSpaceAttribute(
                        """
                                <root xml:space="default">
                                        <p>I <b>love</b> <i>Mozart</i>.</p>
                                </root>
                                """,
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <root xml:space="default">
                                  <p>I <b>love</b>
                                    <i>Mozart</i>.</p>
                                </root>"""
                ))
        );
    }

    /**
     * Given an expected XML String representation.
     * Parsing that String and writing it out again with the
     * {@link IndentingXMLStreamWriter} should give the same result.
     *
     * @param expected expected output
     */
    private void shouldHonourXmlSpaceAttribute(String input, String expected) throws Exception {
        Document doc = XmlUtil.read(new StringReader(input), true);

        StringWriter out = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StAXResult result = new StAXResult(new IndentingXMLStreamWriter(out));

        transformer.transform(source, result);
        String actual = out.toString();
        assertEquals(expected, actual);
    }
}