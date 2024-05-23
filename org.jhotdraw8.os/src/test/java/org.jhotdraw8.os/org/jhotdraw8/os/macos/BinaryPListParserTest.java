/*
 * @(#)BinaryPListParserTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.os.macos;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Properties;

public class BinaryPListParserTest {
    private static final Properties INDENT_XML_PROPERTIES = new Properties();

    static {
        INDENT_XML_PROPERTIES.put(OutputKeys.INDENT, "yes");
        INDENT_XML_PROPERTIES.put(OutputKeys.ENCODING, "UTF-8");
        INDENT_XML_PROPERTIES.put("{http://xml.apache.org/xslt}indent-amount", "2");
    }

    private static final Properties NO_INDENT_XML_PROPERTIES = new Properties();

    static {
        NO_INDENT_XML_PROPERTIES.put(OutputKeys.ENCODING, "UTF-8");
    }

    /**
     * Tests a small property plist where the binary presentation has
     * less than 256 objects. In this case objects are encoded in
     * a byte array.
     *
     * @throws Exception on failure
     */
    @Test
    public void testSmallPropertyList() throws Exception {
        File xmlFile = new File(getClass().getResource("SmallXmlPropertyList.plist").toURI());
        final Document docFromXml = readXmlPropertyList(xmlFile);
        File binaryFile = new File(getClass().getResource("SmallBinaryPropertyList.plist").toURI());
        final Document docFromBinary = readBinaryPropertyList(binaryFile);
        Assertions.assertEquals(docFromXml, docFromXml);
        // writeDocument(System.out, docFromXml, NO_INDENT_XML_PROPERTIES);
        // System.out.println();
        // writeDocument(System.out, docFromBinary, INDENT_XML_PROPERTIES);
    }

    /**
     * Tests a small property plist where the binary presentation has
     * exactly 256 objects.  In this case objects are encoded in
     * a short array.
     *
     * @throws Exception on failure
     */
    @Test
    public void testLargePropertyList() throws Exception {
        File xmlFile = new File(getClass().getResource("LargeXmlPropertyList.plist").toURI());
        final Document docFromXml = readXmlPropertyList(xmlFile);
        File binaryFile = new File(getClass().getResource("LargeBinaryPropertyList.plist").toURI());
        final Document docFromBinary = readBinaryPropertyList(binaryFile);
        Assertions.assertEquals(docFromXml, docFromXml);
        // writeDocument(System.out, docFromXml, NO_INDENT_XML_PROPERTIES);
        // System.out.println();
        // writeDocument(System.out, docFromBinary, INDENT_XML_PROPERTIES);
    }

    private static Document readXmlPropertyList(File file) throws Exception {
        InputSource inputSource = new InputSource(file.toString());
        Assertions.assertTrue(file.exists(), "file does not exist, file=" + file);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        // We do not want that the reader creates a socket connection!
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        Document doc = builder.parse(inputSource);
        return doc;
    }

    private static Document readBinaryPropertyList(File file) throws Exception {
        return new BinaryPListParser().parse(file);
    }

    private static void writeDocument(OutputStream writer, Document doc, @Nullable Properties outputProperties) throws Exception {
        StreamResult result = new StreamResult(writer);
        writeDocument(result, doc, outputProperties);
    }

    private static void writeDocument(StreamResult result, Document doc, @Nullable Properties outputProperties) throws Exception {
        final TransformerFactory factory = TransformerFactory.newInstance();
        Transformer t = factory.newTransformer();
        if (outputProperties != null) {
            t.setOutputProperties(outputProperties);
        }
        DOMSource source = new DOMSource(doc);
        t.transform(source, result);
    }
}