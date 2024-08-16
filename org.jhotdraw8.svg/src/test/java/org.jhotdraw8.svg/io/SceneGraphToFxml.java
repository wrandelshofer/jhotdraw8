
package io;


import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.jhotdraw8.fxbase.tree.PreorderSpliterator;
import org.jhotdraw8.xml.XmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Exports a scene graph to a FXML file.
 */
public class SceneGraphToFxml {

    /**
     * Set of styleable and writeable properties,
     * which we are not allowed to write in an FXML.
     */
    private static final Set<String> unsupportedAttributes = Set.of(
            "cssDashArray", "skinClassName", "messageLocationGap", "showDuration"
    );

    /**
     * Map of layout constraints.
     * <p>
     * Key: property name
     * Value: XML attribute name
     */
    private static final Map<String, String> layoutConstraints = Map.ofEntries(
            // GridPane constraint
            Map.entry("gridpane-margin", "GridPane.margin"),
            Map.entry("gridpane-halignment", "GridPane.halignment"),
            Map.entry("gridpane-valignment", "GridPane.valignment"),
            Map.entry("gridpane-hgrow", "GridPane.hgrow"),
            Map.entry("gridpane-vgrow", "GridPane.vgrow"),
            Map.entry("gridpane-row", "GridPane.rowIndex"),
            Map.entry("gridpane-column", "GridPane.columnIndex"),
            Map.entry("gridpane-row-span", "GridPane.rowSpan"),
            Map.entry("gridpane-column-span", "GridPane.columnSpan"),
            Map.entry("gridpane-fill-width", "GridPane.fillWidth"),
            Map.entry("gridpane-fill-height", "GridPane.fillHeight"),

            // VBox constraints
            Map.entry("vbox-margin", "VBox.margin"),
            Map.entry("vbox-vgrow", "VBox.vgrow"),
            // HBox constraints
            Map.entry("hbox-margin", "HBox.margin"),
            Map.entry("hbox-hgrow", "HBox.hgrow")
    );


    public static final String JAVAFX_COM_FXML_PREFIX = "fx";
    public static final String JAVAFX_COM_FXML = "http://javafx.com/fxml";
    public static final String JAVAFX_COM_JAVAFX = "http://javafx.com/javafx/";

    public void export(final Node node, final Path path) throws XMLStreamException, IOException {
        try (final var out = new BufferedOutputStream(Files.newOutputStream(path))) {
            final XMLStreamWriter w = XmlUtil.streamWriter(new StreamResult(out), null);
            w.writeStartDocument();
            writeImports(node, w);
            writeNodes(node, w, true);
            w.writeEndDocument();
            w.close();
        }
    }

    private void writeNodes(final Node node, final XMLStreamWriter w, final boolean root) throws XMLStreamException {
        final Class<? extends Node> fxClass = getJavaFXClass(node);
        w.writeStartElement(fxClass.getSimpleName());
        if (root) {
            w.writeDefaultNamespace(JAVAFX_COM_JAVAFX);
            w.writeNamespace(JAVAFX_COM_FXML_PREFIX, JAVAFX_COM_FXML);
        }
        if (node.getId() instanceof final String id) {
            w.writeAttribute(JAVAFX_COM_FXML_PREFIX, JAVAFX_COM_FXML, "id", id);
        }
        final String styleClass = String.join(" ", node.getStyleClass());
        if (!styleClass.isEmpty()) {
            w.writeAttribute(JAVAFX_COM_JAVAFX, "styleClass", styleClass);
        }

        if (node instanceof final Labeled l && !l.getText().isEmpty()) {
            w.writeAttribute(JAVAFX_COM_JAVAFX, "text", l.getText());
        }
        if (node instanceof final Rectangle r) {
            w.writeAttribute(JAVAFX_COM_JAVAFX, "x", Double.toString(r.getX()));
            w.writeAttribute(JAVAFX_COM_JAVAFX, "y", Double.toString(r.getY()));
            w.writeAttribute(JAVAFX_COM_JAVAFX, "width", Double.toString(r.getWidth()));
            w.writeAttribute(JAVAFX_COM_JAVAFX, "height", Double.toString(r.getHeight()));
            w.writeAttribute(JAVAFX_COM_JAVAFX, "arcHeight", Double.toString(r.getArcHeight()));
            w.writeAttribute(JAVAFX_COM_JAVAFX, "arcWidth", Double.toString(r.getArcWidth()));
        }
        for (final var entry : node.getProperties().entrySet()) {
            final String attributeName = layoutConstraints.get(entry.getKey());
            final Object value = entry.getValue();
            if (attributeName != null && value != null) {
                w.writeAttribute(attributeName, value.toString());
            }
        }
        ;

        for (final CssMetaData<? extends Styleable, ?> metaData : node.getCssMetaData()) {
            final CssMetaData<Styleable, Object> md = (CssMetaData<Styleable, Object>) metaData;
            final StyleableProperty<Object> styleableProperty = md.getStyleableProperty(node);
            final Object value = styleableProperty.getValue();
            if (md.isSettable(node) && styleableProperty instanceof final Property<?> p &&
                    !isInitialValue(node, md, value)) {
                final String name = ((ReadOnlyProperty<Object>) styleableProperty).getName();
                if (!unsupportedAttributes.contains(name)) {
                    w.writeAttribute(JAVAFX_COM_FXML, name, Objects.toString(value));
                }
            }
        }


        if (node instanceof final Parent p && isNodeTraversable(node)) {
            for (final Node c : p.getChildrenUnmodifiable()) {
                writeNodes(c, w, false);
            }

        }
        w.writeEndElement();
    }

    private static boolean isInitialValue(final Node node, final CssMetaData<Styleable, Object> md, final Object value) {
        final Object initialValue = md.getInitialValue(node);
        if (initialValue instanceof final Number n && value instanceof final Number vn) {
            if (n.doubleValue() == vn.doubleValue()) {
                return true;
            }
        }
        return Objects.equals(initialValue, value);
    }

    private static boolean isNodeTraversable(final Node node) {
        return node instanceof ScrollPane || !(node instanceof Control);
    }

    private Class<? extends Node> getJavaFXClass(final Node node) {
        Class<? extends Node> clazz = node.getClass();
        while (!clazz.getName().startsWith("javafx") || clazz.getName().contains("$")) {
            //noinspection unchecked
            clazz = (Class<? extends Node>) clazz.getSuperclass();
        }
        if (clazz.equals(Parent.class)) {
            // Return Pane, because we can not write an abstract class
            return Pane.class;
        }
        return clazz;
    }

    private void writeImports(final Node node, final XMLStreamWriter w) throws XMLStreamException {
        final NavigableSet<String> imports = new TreeSet<>();
        new PreorderSpliterator<>(n -> n instanceof final Parent p ? p.getChildrenUnmodifiable() : List.of(), node)
                .forEachRemaining(n -> {
                    imports.add(getJavaFXClass(n).getName());
                });
        for (final var imp : imports) {
            w.writeProcessingInstruction("import", imp);
            w.writeCharacters("\n");
        }
    }
}
