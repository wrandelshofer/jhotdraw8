/*
 * @(#)SimpleXmlWriter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import javafx.css.StyleOrigin;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.figure.Clipping;
import org.jhotdraw8.draw.figure.ClippingFigure;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.input.ClipboardOutputFormat;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.CompositeMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.icollection.ChampMap;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.xml.IndentingXMLStreamWriter;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SimpleXmlWriter.
 * <p>
 * Represents each Figure by an element, and each figure property by an
 * attribute.
 * <p>
 * All attribute values are treated as value types, except if an attribute type
 * is an instance of Figure.
 * <p>
 * This writer only works for drawings which can be described entirely by
 * the properties of its figures.
 * <p>
 * Attempts to preserve comments in the XML file, by associating
 * them to the figures and to the drawing.
 * <p>
 * Does not preserve whitespace in the XML file.
 *
 */
public class SimpleXmlWriter implements OutputFormat, ClipboardOutputFormat {
    protected FigureFactory figureFactory;
    final protected IdFactory idFactory;
    final protected String namespaceQualifier;
    protected String namespaceURI;
    private PersistentMap<Key<?>, Object> options = ChampMap.of();

    /**
     * Specifies the number of characters that should be used for indentation.
     */
    final public static NonNullObjectKey<Integer> INDENT_AMOUNT = new NonNullObjectKey<>("indent-amount", Integer.class, 2);

    public SimpleXmlWriter(FigureFactory factory, IdFactory idFactory) {
        this(factory, idFactory, null, null);
    }

    public SimpleXmlWriter(FigureFactory factory, IdFactory idFactory, String namespaceURI, String namespaceQualifier) {
        this.figureFactory = factory;
        this.idFactory = idFactory;
        this.namespaceURI = namespaceURI;
        this.namespaceQualifier = namespaceQualifier;
    }

    private DataFormat getDataFormat() {
        String mimeType = "application/xml";
        DataFormat df = DataFormat.lookupMimeType(mimeType);
        if (df == null) {
            df = new DataFormat(mimeType);
        }
        return df;
    }

    public boolean isNamespaceAware() {
        return namespaceURI != null;
    }

    public void setFigureFactory(FigureFactory figureFactory) {
        this.figureFactory = figureFactory;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public Document toDocument(@Nullable URI documentHome, Drawing internal, Collection<Figure> selection) throws IOException {
        if (selection.isEmpty() || selection.contains(internal)) {
            return toDocument(documentHome, internal);
        }

        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = null;
            builder = builderFactory.newDocumentBuilder();
            // We do not want that the builder creates a socket connection!
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            Document doc = builder.newDocument();
            DOMResult result = new DOMResult(doc);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter w = xmlOutputFactory.createXMLStreamWriter(result);

            writeClipping(w, internal, selection, documentHome);

            w.close();
            return doc;
        } catch (ParserConfigurationException | XMLStreamException e) {
            throw new IOException("Could not create document builder.", e);
        }

    }

    public Document toDocument(@Nullable URI documentHome, Drawing internal) throws IOException {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            // We do not want that the builder creates a socket connection!
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            Document doc = builder.newDocument();
            DOMResult result = new DOMResult(doc);
            XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
            writeDocument(w, documentHome, internal);
            w.close();
            return doc;
        } catch (XMLStreamException | ParserConfigurationException e) {
            throw new IOException("Error writing to DOM.", e);
        }
    }

    @Override
    public void setOptions(PersistentMap<Key<?>, Object> newValue) {
        options = newValue;
    }

    @Override
    public PersistentMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void write(OutputStream out, @Nullable URI documentHome, Drawing drawing, WorkState<Void> workState) throws IOException {
        write(documentHome, new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)),
                drawing, workState);
    }

    protected void write(@Nullable URI documentHome, Writer out, Drawing drawing, WorkState<Void> workState) throws IOException {
        XMLStreamWriter w = createXmlStreamWriter(out);
        workState.updateProgress(0.0);
        try {
            writeDocument(w, documentHome, drawing);
            w.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(Map<DataFormat, Object> out, Drawing drawing, Collection<Figure> selection) throws IOException {
        StringWriter sw = new StringWriter();
        XMLStreamWriter w = createXmlStreamWriter(sw);
        URI documentHome = null;
        try {
            if (selection == null || selection.isEmpty()) {
                writeDocument(w, null, drawing);
            } else {
                writeClipping(w, drawing, selection, null);
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        out.put(getDataFormat(), sw.toString());
    }


    private XMLStreamWriter createXmlStreamWriter(Writer sw) {
        IndentingXMLStreamWriter w = new IndentingXMLStreamWriter(sw);
        w.setIndentation(" ".repeat(INDENT_AMOUNT.get(options)));
        return w;
    }

    protected void writeClipping(XMLStreamWriter w, Drawing internal, Collection<Figure> selection, @Nullable URI documentHome) throws IOException, XMLStreamException {
        // bring selection in z-order
        Set<Figure> s = new HashSet<>(selection);
        ArrayList<Figure> ordered = new ArrayList<>(selection.size());
        for (Figure f : internal.preorderIterable()) {
            if (s.contains(f)) {
                ordered.add(f);
            }
        }
        Clipping external = new ClippingFigure();
        idFactory.reset();
        idFactory.setDocumentHome(documentHome);
        final String docElemName = figureFactory.getElementNameByFigure(external);
        w.writeStartDocument();
        w.setDefaultNamespace(namespaceURI);
        w.writeStartElement(docElemName);
        w.writeDefaultNamespace(namespaceURI);
        for (Figure child : ordered) {
            writeNodeRecursively(w, child, 1);
        }
        w.writeEndElement();
        w.writeEndDocument();
    }

    protected void writeDocument(XMLStreamWriter w, @Nullable URI documentHome, Drawing internal) throws XMLStreamException {
        try {
            Drawing external = figureFactory.toExternalDrawing(internal);
            idFactory.reset();
            idFactory.setDocumentHome(documentHome);
            final String docElemName = figureFactory.getElementNameByFigure(external);
            w.writeStartDocument();
            w.setDefaultNamespace(namespaceURI);
            writeProcessingInstructions(w, external);
            w.writeStartElement(docElemName);
            w.writeDefaultNamespace(namespaceURI);
            writeElementAttributes(w, external);
            for (Figure child : external.getChildren()) {
                writeNodeRecursively(w, child, 1);
            }
            w.writeEndElement();
            w.writeEndDocument();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private void writeElementAttribute(XMLStreamWriter w, Figure figure, MapAccessor<Object> k) throws IOException, XMLStreamException {
        Object value = figure.get(k);
        if (!k.isTransient() && !figureFactory.isDefaultValue(figure, k, value)) {
            String name = figureFactory.getAttributeNameByKey(figure, k);
            if (figureFactory.getObjectIdAttribute().equals(name)) {
                return;
            }
            if (Figure.class.isAssignableFrom(k.getRawValueType())) {
                w.writeAttribute(name, idFactory.createId(value));
            } else {
                String stringValue = figureFactory.valueToString(k, value);
                if (stringValue != null && !stringValue.isEmpty()) {
                    w.writeAttribute(name, stringValue);
                }
            }
        }
    }

    protected void writeElementAttributes(XMLStreamWriter w, Figure figure) throws IOException, XMLStreamException {
        String id = idFactory.createId(figure);
        String objectIdAttribute = figureFactory.getObjectIdAttribute();
        w.writeAttribute(objectIdAttribute, id);
        final Set<MapAccessor<?>> keys = figureFactory.figureAttributeKeys(figure);
        Set<MapAccessor<?>> done = new HashSet<>(keys.size());

        // First write all non-transient composite attributes, then write the remaining non-transient non-composite attributes
        for (MapAccessor<?> k : keys) {
            if (k instanceof CompositeMapAccessor) {
                done.add(k);
                if (!k.isTransient()) {
                    @SuppressWarnings("unchecked") CompositeMapAccessor<Object> cmap = (CompositeMapAccessor<Object>) k;
                    done.addAll(cmap.getSubAccessors().asSet());
                    writeElementAttribute(w, figure, cmap);
                }
            }
        }
        for (MapAccessor<?> k : keys) {
            if (!k.isTransient() && !done.contains(k)) {
                @SuppressWarnings("unchecked") MapAccessor<Object> cmap = (MapAccessor<Object>) k;
                writeElementAttribute(w, figure, cmap);
            }
        }
    }

    private void writeElementNodeList(XMLStreamWriter w, Figure figure) throws IOException, XMLStreamException {
        for (MapAccessor<?> k : figureFactory.figureNodeListKeys(figure)) {
            @SuppressWarnings("unchecked")
            MapAccessor<Object> key = (MapAccessor<Object>) k;
            Object value = figure.get(key);
            if (!key.isTransient() && figure.containsMapAccessor(StyleOrigin.USER, key) && !figureFactory.isDefaultValue(figure, key, value)) {
                figureFactory.valueToNodeList(key, value, w);
            }
        }
    }

    protected void writeNodeRecursively(XMLStreamWriter w, Figure figure, int depth) throws IOException {
        try {
            String elementName = figureFactory.getElementNameByFigure(figure);
            if (elementName == null) {
                // => the figureFactory decided that we should skip the figure
                return;
            }
            w.writeStartElement(elementName);
            writeElementAttributes(w, figure);
            writeElementNodeList(w, figure);
            for (Figure child : figure.getChildren()) {
                if (figureFactory.getElementNameByFigure(child) != null) {
                    writeNodeRecursively(w, child, depth + 1);
                }
            }
            w.writeEndElement();
        } catch (IOException | XMLStreamException e) {
            throw new IOException("Error writing figure " + figure, e);
        }
    }

    // XXX maybe this should not be in SimpleXmlIO?
    protected void writeProcessingInstructions(XMLStreamWriter w, Drawing external) throws XMLStreamException {
        if (figureFactory.getStylesheetsKey() != null) {
            PersistentList<URI> stylesheets = external.get(figureFactory.getStylesheetsKey());
            if (stylesheets != null) {
                for (URI stylesheet : stylesheets) {
                    stylesheet = idFactory.relativize(stylesheet);

                    String stylesheetString = stylesheet.toString();
                    String type = "text/" + stylesheetString.substring(stylesheetString.lastIndexOf('.') + 1);
                    if ("text/".equals(type)) {
                        type = "text/css";
                    }
                    w.writeProcessingInstruction("xml-stylesheet", //
                            "type=\"" + type + "\" href=\"" + stylesheet + "\"");
                }
            }
        }
    }
}
