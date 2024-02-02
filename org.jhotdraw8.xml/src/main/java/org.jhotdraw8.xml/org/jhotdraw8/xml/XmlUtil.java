/*
 * @(#)XmlUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * XmlUtil.
 *
 * @author Werner Randelshofer
 */
public class XmlUtil {

    public static final String LOCATION_ATTRIBUTE = "location";
    public static final String LOCATION_NAMESPACE = "http://location.xmlutil.ch";
    private static final String QUALIFIED_LOCATION_ATTRIBUTE = "xmlutil:location";
    private static final String SEPARATOR = "\0";
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    public static final String HTTP_XML_APACHE_ORG_XALAN_LINE_SEPARATOR = "{http://xml.apache.org/xalan}line-separator";

    public static final String HTTP_XML_APACHE_ORG_XSLT_INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";

    public static final String CANONICAL_LINE_SEPARATOR = "\n";

    static {
        DEFAULT_PROPERTIES.put(OutputKeys.INDENT, "yes");
        DEFAULT_PROPERTIES.put(OutputKeys.ENCODING, "UTF-8");
        DEFAULT_PROPERTIES.put(HTTP_XML_APACHE_ORG_XSLT_INDENT_AMOUNT, "2");
        DEFAULT_PROPERTIES.put(HTTP_XML_APACHE_ORG_XALAN_LINE_SEPARATOR, CANONICAL_LINE_SEPARATOR);
    }

    private XmlUtil() {
    }

    /**
     * Creates a namespace aware document.
     *
     * @param nsURI       nullable namespace URI
     * @param nsQualifier nullable namespace qualifier
     * @param docElemName notnull name of the document element
     * @return a new Document
     * @throws IOException if the parser configuration fails
     */
    public static Document createDocument(@Nullable String nsURI, @Nullable String nsQualifier, String docElemName) throws IOException {
        try {
            Document doc;
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            if (nsURI != null) {
                builderFactory.setNamespaceAware(true);
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                DOMImplementation domImpl = builder.getDOMImplementation();
                doc = domImpl.createDocument(nsURI, nsQualifier == null ? docElemName : nsQualifier + ":" + docElemName, null);
            } else {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                doc = builder.newDocument();
                Element elem = doc.createElement(docElemName);
                doc.appendChild(elem);
            }
            return doc;
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex);
        }

    }

    public static Document read(Reader in, boolean namespaceAware) throws IOException {
        InputSource inputSource = new InputSource(in);
        return XmlUtil.read(inputSource, namespaceAware);

    }

    public static Document read(InputStream in, boolean namespaceAware) throws IOException {
        InputSource inputSource = new InputSource(in);
        return XmlUtil.read(inputSource, namespaceAware);

    }

    public static Document read(@NonNull Path in, boolean namespaceAware) throws IOException {
        InputSource inputSource = new InputSource(in.toUri().toASCIIString());
        return XmlUtil.read(inputSource, namespaceAware);
    }

    public static @NonNull Document readWithLocations(@NonNull Path in, boolean namespaceAware) throws IOException {
        InputSource inputSource = new InputSource(in.toUri().toASCIIString());
        return XmlUtil.readWithLocations(inputSource, namespaceAware);
    }

    public static Document read(InputSource inputSource, boolean namespaceAware) throws IOException {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(namespaceAware);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            // We do not want that the reader creates a socket connection!
            builder.setEntityResolver((publicId, systemId) -> null);
            return builder.parse(inputSource);
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Reads the specified input into a document. Adds a "location" attribute to each node,
     * specifying the file, the line number and the column number of the node.
     * <p>
     * References:
     * <dl>
     *     <dt>Stackoverflow, Is there a way to parse XML via SAX/DOM with line numbers available per node,
     *     Copyright Reg Whitton, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/2798376/is-there-a-way-to-parse-xml-via-sax-dom-with-line-numbers-available-per-node">stackoverflow.com</a>.</dd>
     * </dl>
     *
     * @param inputSource    the input source
     * @param namespaceAware whether to be name space aware
     * @return the document
     * @throws java.io.IOException in case of failure
     */
    public static @NonNull Document readWithLocations(InputSource inputSource, boolean namespaceAware) throws IOException {
        try {
            // Create transformer SAX source that adds current element position to
            // the element as attributes.
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(namespaceAware);
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            LocationFilter locationFilter = new LocationFilter(xmlReader);
            SAXSource saxSource = new SAXSource(locationFilter, inputSource);

            // Perform an empty transformation from SAX source to DOM result.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            DOMResult domResult = new DOMResult();
            transformer.transform(saxSource, domResult);
            Node root = domResult.getNode();
            return (Document) root;
        } catch (TransformerException | SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    public static @Nullable Locator getLocator(@NonNull Node node) {
        final NamedNodeMap attributes = node.getAttributes();
        Node attrNode = attributes == null ? null : attributes.getNamedItemNS(LOCATION_NAMESPACE, LOCATION_ATTRIBUTE);
        if (attrNode != null) {
            String[] parts = attrNode.getNodeValue().split(SEPARATOR);
            if (parts.length == 4) {
                return new MyLocator(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2], parts[3]);
            }
        }
        return null;
    }

    public static void validate(@NonNull Document doc, @NonNull URI schemaUri) throws IOException {
        XmlUtil.validate(doc, schemaUri.toURL());
    }

    public static void validate(@NonNull Document doc, @NonNull URL schemaUrl) throws IOException {
        try (InputStream schemaStream = schemaUrl.openStream()) {
            validate(new DOMSource(doc), new StreamSource(schemaStream));
        }
    }

    public static void validate(@NonNull Source docSource, @NonNull Source schemaSource) throws IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(docSource);
        } catch (SAXException e) {
            throw new IOException("The document is invalid.\n" + e.getMessage(), e);
        }
    }

    public static void validate(@NonNull Path xmlPath, @NonNull URI schemaUri) throws IOException {
        validate(new StreamSource(xmlPath.toUri().toString()), new StreamSource(schemaUri.toString()));
    }

    public static void validate(@NonNull URI xmlUri, @NonNull URI schemaUri) throws IOException {
        validate(new StreamSource(xmlUri.toString()), new StreamSource(schemaUri.toString()));
    }

    public static void write(OutputStream out, Document doc) throws IOException {
        StreamResult result = new StreamResult(out);
        write(result, doc);
    }

    public static void write(Writer out, Document doc) throws IOException {
        StreamResult result = new StreamResult(out);
        write(result, doc);
    }

    public static void write(@NonNull Path out, Document doc) throws IOException {
        write(out, doc, DEFAULT_PROPERTIES);
    }

    public static void write(@NonNull Path out, Document doc, Properties outputProperties) throws IOException {
        StreamResult result = new StreamResult(out.toFile());
        write(result, doc, outputProperties);
    }

    public static void write(@NonNull Result result, @NonNull Document doc) throws IOException {
        write(result, doc, DEFAULT_PROPERTIES);
    }

    public static void write(@NonNull Result result, @NonNull Document doc, @Nullable Properties outputProperties) throws IOException {
        try {
            // We replace the StreamResult by a StAXResult,
            // because with a StreamResult we would produce platform-dependent line-breaks.
            result = replaceStreamResultByStAXResult(result, outputProperties);

            final TransformerFactory factory = TransformerFactory.newInstance();
            Transformer t = factory.newTransformer();
            if (outputProperties != null) {
                t.setOutputProperties(outputProperties);
            }
            DOMSource source = new DOMSource(doc);
            t.transform(source, result);
        } catch (TransformerException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Creates an XMLStreamReader that only reads data from the specified file.
     * That is, it does not create socket connections.
     *
     * @param file a file
     * @return an XMLStreamReader
     * @throws XMLStreamException when the XMLStreamReader could not be created
     */

    public static XMLStreamReader streamReader(final @NonNull Source file) throws XMLStreamException {
        final XMLInputFactory dbf = XMLInputFactory.newInstance();

        // We do not want that the reader creates a socket connection,
        // even if it would benefit the result!
        dbf.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        dbf.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        dbf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        dbf.setXMLResolver((publicID,
                            systemID,
                            baseURI,
                            namespace) -> null
        );
        return dbf.createXMLStreamReader(file);

    }

    /**
     * Attempts to replace the provided result by a StaxResult.
     *
     * @param result           a result
     * @param outputProperties the desired output properties of the StaxResult
     * @return the StaxResult if replacement was successful, the provided result otherwise
     */
    private static Result replaceStreamResultByStAXResult(@NonNull Result result, @Nullable Properties outputProperties) {
        if (result instanceof StreamResult sr) {
            IndentingXMLStreamWriter w;
            if (sr.getOutputStream() != null)
                w = new IndentingXMLStreamWriter(sr.getOutputStream(), Charset.forName((String) outputProperties.getOrDefault(OutputKeys.ENCODING, "UTF-8")));
            else if (sr.getWriter()!=null){
                w = new IndentingXMLStreamWriter(sr.getWriter());
            } else {
                return result;
            }
            //w.setSortAttributes(false);
            try {
                int indentation = Integer.parseInt((String) outputProperties.getOrDefault(HTTP_XML_APACHE_ORG_XSLT_INDENT_AMOUNT, "0"));
                w.setIndentation(" ".repeat(indentation));
            } catch (NumberFormatException e) {
                // bail
            }
            w.setLineSeparator((String) outputProperties.getOrDefault(HTTP_XML_APACHE_ORG_XALAN_LINE_SEPARATOR, CANONICAL_LINE_SEPARATOR));
            result = new StAXResult(w);
        }
        return result;
    }



    private static class LocationFilter extends XMLFilterImpl {
        private Locator locator = null;

        LocationFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            super.setDocumentLocator(locator);
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            // Add extra attribute to elements to hold location
            Attributes2Impl attrs = new Attributes2Impl(attributes);
            attrs.addAttribute(LOCATION_NAMESPACE, LOCATION_ATTRIBUTE, QUALIFIED_LOCATION_ATTRIBUTE, "CDATA",
                    locator.getLineNumber() + SEPARATOR + locator.getColumnNumber() + SEPARATOR + locator.getSystemId() + SEPARATOR + locator.getPublicId());
            super.startElement(uri, localName, qName, attrs);
        }
    }

    private static class MyLocator implements Locator {

        private final int line;
        private final int column;
        private final String systemId;
        private final String publicId;

        public MyLocator(int line, int column, String systemId, String publicId) {
            this.line = line;
            this.column = column;
            this.systemId = systemId;
            this.publicId = publicId;
        }

        @Override
        public int getColumnNumber() {
            return column;
        }

        @Override
        public int getLineNumber() {
            return line;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

    }

    public static String readNamespaceUri(URI file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(file)))) {
            return readNamespaceUri(new StreamSource(in));
        }

    }

    public static String readNamespaceUri(Source source) throws IOException {
        try {
            for (XMLStreamReader r = streamReader(source); r.hasNext(); ) {
                int next = r.next();
                switch (next) {
                    case XMLStreamReader.START_ELEMENT -> {
                        return r.getNamespaceURI();
                    }
                    case XMLStreamReader.END_ELEMENT,
                            XMLStreamReader.PROCESSING_INSTRUCTION,
                            XMLStreamReader.CHARACTERS,
                            XMLStreamReader.ENTITY_DECLARATION,
                            XMLStreamReader.NOTATION_DECLARATION,
                            XMLStreamReader.NAMESPACE,
                            XMLStreamReader.CDATA,
                            XMLStreamReader.DTD,
                            XMLStreamReader.ATTRIBUTE,
                            XMLStreamReader.ENTITY_REFERENCE,
                            XMLStreamReader.END_DOCUMENT,
                            XMLStreamReader.START_DOCUMENT,
                            XMLStreamReader.SPACE,
                            XMLStreamReader.COMMENT -> {
                    }
                    default -> throw new IOException("unsupported XMLStream event: " + next);
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        return null;
    }
}
