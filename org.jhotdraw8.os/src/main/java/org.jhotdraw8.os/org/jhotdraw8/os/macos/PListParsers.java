package org.jhotdraw8.os.macos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.stream.StreamSupport;

/**
 * Provides static methods for parsing PLists in XML format and in binary format.
 */
public class PListParsers {
    /**
     * Don't let anyone instantiate this class.
     */
    private PListParsers() {
    }

    /**
     * Reads the specified PList file and returns it as a document.
     * This method can deal with XML encoded and binary encoded PList files.
     */
    public static Document readPList(File file) throws IOException {
        Document doc;
        try {
            doc = readBinaryPropertyList(file);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            try {
                doc = readXmlPropertyList(file);
            } catch (IOException e3) {
                throw e3;
            }
        }
        if (doc == null) {
            throw new IOException("File is neither an XML PList nor a Binary PList. File: " + file);
        }
        return doc;
    }

    private static Document readXmlPropertyList(File file) throws IOException {
        InputSource inputSource = new InputSource(file.toString());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
            // We do not want that the reader creates a socket connection!
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            return builder.parse(inputSource);
        } catch (ParserConfigurationException e) {
            throw new IOException("Cannot create document builder for file: " + file, e);
        } catch (SAXException e) {
            throw new IOException("Illegal file format in file: " + file, e);
        }
    }

    private static Document readBinaryPropertyList(File file) throws IOException {
        return new BinaryPListParser().parse(file);
    }

    public static Map<String, Object> toMap(Document plist) throws IOException {
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        map.put("plist", readNode(plist.getDocumentElement()));
        return map;
    }

    private static Object readNode(Element node) throws IOException {
        String name = node.getTagName();
        Object value = switch (name) {
            case "plist" -> readPList(node);
            case "dict" -> readDict(node);
            case "array" -> readArray(node);
            default -> readValue(node);
        };
        return value;
    }

    private static Iterable<Node> getChildren(final Element elem) {
        return () -> new Iterator<>() {
            int index = 0;
            final NodeList children = elem.getChildNodes();

            @Override
            public boolean hasNext() {
                return index < children.getLength();
            }

            @Override
            public Node next() {
                return children.item(index++);
            }
        };
    }

    private static Iterable<Element> getChildElements(final Element elem) {
        return () -> StreamSupport.stream(getChildren(elem).spliterator(), false)
                .filter(e -> e instanceof Element).map(e -> (Element) e).iterator();
    }

    private static List<Object> readPList(Element plistElem) throws IOException {
        List<Object> plist = new ArrayList<>();
        for (Element child : getChildElements(plistElem)) {
            plist.add(readNode(child));
        }
        return plist;
    }

    private static String getContent(Element elem) {
        StringBuilder buf = new StringBuilder();
        for (Node child : getChildren(elem)) {
            if (child instanceof Text) {
                buf.append(child.getTextContent());
            }
        }
        return buf.toString().trim();
    }

    private static Map<String, Object> readDict(Element dictElem) throws IOException {
        LinkedHashMap<String, Object> dict = new LinkedHashMap<>();
        for (Iterator<Element> iterator = getChildElements(dictElem).iterator(); iterator.hasNext(); ) {
            Element keyElem = iterator.next();
            if (!"key".equals(keyElem.getTagName())) {
                throw new IOException("missing dictionary key at" + dictElem);
            }
            Element valueElem = iterator.next();
            Object elemValue = readNode(valueElem);
            dict.put(getContent(keyElem), elemValue);
        }
        return dict;
    }

    private static List<Object> readArray(Element arrayElem) throws IOException {
        List<Object> array = new ArrayList<>();
        for (Element child : getChildElements(arrayElem)) {
            array.add(readNode(child));
        }
        return array;
    }

    private static Object readValue(Element value) throws IOException {
        Object parsedValue;
        switch (value.getTagName()) {
            case "true":
                parsedValue = true;
                break;
            case "false":
                parsedValue = false;
                break;
            case "data":
                parsedValue = Base64.getDecoder().decode(getContent(value));
                break;
            case "date":
                try {
                    parsedValue = DatatypeFactory.newInstance().newXMLGregorianCalendar(getContent(value));
                } catch (IllegalArgumentException |
                         DatatypeConfigurationException e) {
                    throw new IOException(e);
                }
                break;
            case "real":
                try {
                    parsedValue = Double.valueOf(getContent(value));
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
                break;
            case "integer":
                try {
                    parsedValue = Long.valueOf(getContent(value));
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
                break;
            default:
                parsedValue = getContent(value);
                break;
        }
        return parsedValue;
    }
}
