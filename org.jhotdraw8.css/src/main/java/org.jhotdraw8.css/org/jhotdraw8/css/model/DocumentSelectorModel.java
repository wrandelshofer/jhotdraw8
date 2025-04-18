/*
 * @(#)DocumentSelectorModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.model;

import javafx.css.StyleOrigin;
import org.jhotdraw8.css.ast.TypeSelector;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.css.value.QualifiedName;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;

/**
 * {@code DocumentSelectorModel} provides an API for CSS
 * {@link org.jhotdraw8.css.ast.SelectorGroup}'s.
 *
 */
public class DocumentSelectorModel extends AbstractSelectorModel<Element> {

    public DocumentSelectorModel() {
    }

    @Override
    public String getAttributeAsString(Element elem, StyleOrigin origin, @Nullable String namespacePattern, String name) {
        return getAttributeAsString(elem, namespacePattern, name);
    }

    @Override
    public @Nullable List<CssToken> getAttribute(Element element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, String name) {
        String str = getAttributeAsString(element, origin, namespacePattern, name);
        if (str == null) {
            return null;
        }
        try {
            return new StreamCssTokenizer(str, null).toTokenList();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected Exception: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasId(Element elem, String id) {
        String value = elem.getAttribute("id");
        return value.equals(id);
    }

    @Override
    public String getId(Element elem) {
        return elem.getAttribute("id");
    }

    @Override
    public boolean hasType(Element elem, @Nullable String namespacePattern, String type) {
        String localName = elem.getLocalName();
        if (TypeSelector.ANY_NAMESPACE.equals(namespacePattern)) {
            return localName.equals(type);
        } else {
            return localName.equals(type) && Objects.equals(namespacePattern, elem.getNamespaceURI());
        }
    }

    @Override
    public void reset(Element elem) {
        // do nothing
    }

    @Override
    public @Nullable QualifiedName getType(Element elem) {
        return new QualifiedName(elem.getNamespaceURI(), elem.getNodeName());
    }

    @Override
    public boolean hasStyleClass(Element elem, String clazz) {
        String value = elem.getAttribute("class");
        String[] clazzes = value.split(" +");
        for (String c : clazzes) {
            if (c.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ReadableSet<String> getStyleClasses(Element elem) {
        String value = elem.getAttribute("class");
        String[] clazzes = value.split(" +");
        return ChampSet.copyOf(Arrays.asList(clazzes));
    }

    @Override
    public ReadableSet<String> getPseudoClasses(Element elem) {
        return ChampSet.of();
    }

    /**
     * Supports the following pseudo classes:
     * <ul>
     * <li>root</li>
     * <li>nth-child(odd)</li>
     * <li>nth-child(even)</li>
     * <li>first-child</li>
     * <li>last-child</li>
     * </ul>
     * Does not support the following pseudo classes:
     * <ul>
     * <li>nth-child(2n+1)</li>
     * <li>nth-last-child(2n+1)</li>
     * <li>nth-last-child(odd)</li>
     * <li>nth-last-child(even)</li>
     * <li>nth-of-type(2n+1)</li>
     * <li>nth-of-type(even)</li>
     * <li>nth-of-type(odd)</li>
     * <li>nth-last-of-type(2n+1)</li>
     * <li>nth-last-of-type(even)</li>
     * <li>nth-last-of-type(odd)</li>
     * <li>first-of-type()</li>
     * <li>last-of-type()</li>
     * <li>only-child()</li>
     * <li>only-of-type()</li>
     * <li>empty</li>
     * <li>not(...)</li>
     * </ul>
     *
     * @param element     the element
     * @param pseudoClass the desired pseudo clas
     * @return true if the element has the pseudo class
     */
    @Override
    public boolean hasPseudoClass(Element element, String pseudoClass) {
        return switch (pseudoClass) {
            case "root" -> element.getOwnerDocument() != null
                    && element.getOwnerDocument().getDocumentElement() == element;
            case "nth-child(even)" -> {
                int i = getChildIndex(element);
                yield (i & 1) == 0;
            }
            case "nth-child(odd)" -> {
                int i = getChildIndex(element);
                yield (i & 1) == 1;
            }
            case "first-child" -> isFirstChild(element);
            case "last-child" -> isLastChild(element);
            default -> false;
        };
    }

    private int getChildIndex(Element element) {
        if (element.getParentNode() != null) {
            NodeList list = element.getParentNode().getChildNodes();

            for (int i = 0, j = 0, n = list.getLength(); i < n; i++) {
                if (list.item(i) == element) {
                    return j;
                }
                if (list.item(i) instanceof Element) {
                    j++;
                }
            }
        }
        return -1;
    }

    private boolean isFirstChild(Element element) {
        if (element.getParentNode() != null) {
            NodeList list = element.getParentNode().getChildNodes();

            for (int i = 0, n = list.getLength(); i < n; i++) {
                if (list.item(i) == element) {
                    return true;
                }
                if (list.item(i) instanceof Element) {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isLastChild(Element element) {
        if (element.getParentNode() != null) {
            NodeList list = element.getParentNode().getChildNodes();

            for (int i = list.getLength() - 1; i >= 0; i--) {
                if (list.item(i) == element) {
                    return true;
                }
                if (list.item(i) instanceof Element) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Element getParent(Element elem) {
        Node n = elem.getParentNode();
        while (n != null && !(n instanceof Element)) {
            n = n.getParentNode();
        }
        return (Element) n;
    }

    @Override
    public Element getPreviousSibling(Element element) {
        Node n = element.getPreviousSibling();
        while (n != null && !(n instanceof Element)) {
            n = n.getPreviousSibling();
        }
        return (Element) n;
    }

    @Override
    public boolean hasAttribute(Element element, @Nullable String namespace, String attributeName) {
        return getAttributeAsString(element, namespace, attributeName) != null;
    }

    @Override
    public boolean attributeValueStartsWith(Element element, @Nullable String namespacePattern, String attributeName, String substring) {
        String actualValue = getAttributeAsString(element, namespacePattern, attributeName);
        return actualValue != null && (actualValue.startsWith(substring));
    }

    @Override
    public @Nullable String getAttributeAsString(Element element, @Nullable String namespacePattern, String attributeName) {
        if (TypeSelector.ANY_NAMESPACE.equals(namespacePattern)) {
            NamedNodeMap nnm = element.getAttributes();
            for (int i = 0, n = nnm.getLength(); i < n; i++) {
                Node item = nnm.item(i);
                if (Objects.equals(item.getLocalName(), attributeName)
                        || Objects.equals(item.getNodeName(), attributeName)) {
                    return item.getNodeValue();
                }
            }
            return null;
        }
        return element.getAttributeNS(namespacePattern, attributeName);
    }

    @Override
    public Set<QualifiedName> getAttributeNames(Element element) {
        SequencedSet<QualifiedName> attr = new LinkedHashSet<>();
        NamedNodeMap nnm = element.getAttributes();
        for (int i = 0, n = nnm.getLength(); i < n; i++) {
            Node node = nnm.item(i);
            attr.add(new QualifiedName(node.getNamespaceURI(), node.getLocalName()));
        }
        return attr;
    }

    @Override
    public Set<QualifiedName> getComposedAttributeNames(Element element) {
        return getAttributeNames(element);
    }

    @Override
    public Set<QualifiedName> getDecomposedAttributeNames(Element element) {
        return getAttributeNames(element);
    }

    @Override
    public void setAttribute(Element element, StyleOrigin origin, @Nullable String namespace, String name, @Nullable ReadableList<CssToken> value) {
        StringBuilder buf = new StringBuilder();
        for (CssToken t : value) {
            buf.append(t.fromToken());
        }
        String value1 = buf.toString();
        switch (origin) {
        case USER:
        case USER_AGENT:
        case INLINE:
        case AUTHOR:
            if (TypeSelector.ANY_NAMESPACE.equals(namespace)) {
                NamedNodeMap nnm = element.getAttributes();
                for (int i = 0, n = nnm.getLength(); i < n; i++) {
                    Node item = nnm.item(i);
                    if (item.getLocalName().equals(name)) {
                        item.setNodeValue(value1);
                        return;
                    }
                }
                element.setAttribute(name, value1);
            } else {
                element.setAttributeNS(namespace, name, value1);
            }
            break;
        default:
            throw new UnsupportedOperationException("Unsupported origin=" + origin);
        }
    }
}
