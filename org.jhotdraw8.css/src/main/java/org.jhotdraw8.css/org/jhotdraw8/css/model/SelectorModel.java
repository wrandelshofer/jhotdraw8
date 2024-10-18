/*
 * @(#)SelectorModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.model;

import javafx.beans.property.MapProperty;
import javafx.css.StyleOrigin;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.value.QualifiedName;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

/**
 * This is a model on which a {@code CssAST.SelectorGroup} can perform a match
 * operation.
 *
 * @param <T> the element type
 * @author Werner Randelshofer
 */
public interface SelectorModel<T> {

    /**
     * Pseudo classes set on the selector model.
     *
     * @return a map
     */
    MapProperty<String, Set<T>> additionalPseudoClassStatesProperty();

    /**
     * Returns true if the element has an attribute with the specified name and
     * the value contains the specified substring.
     *
     * @param element          An element of the document
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName    an attribute name
     * @param substring        the substring
     * @return true if the element has an attribute with the specified name and
     * the value contains the specified substring.
     */
    default boolean attributeValueContains(T element, @Nullable String namespacePattern, String attributeName, String substring) {
        String actualValue = getAttributeAsString(element, namespacePattern, attributeName);
        return actualValue != null && (actualValue.contains(substring));
    }

    /**
     * Returns true if the element has an attribute with the specified name and
     * the value is a list of words which contains the specified word.
     *
     * @param element          An element of the document
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName    an attribute name
     * @param word             the word
     * @return true if the element has an attribute with the specified name and
     * the value contains the specified word.
     */
    default boolean attributeValueContainsWord(T element, @Nullable String namespacePattern, String attributeName, String word) {
        String value = getAttributeAsString(element, namespacePattern, attributeName);
        if (value != null) {
            String[] words = value.split("\\s+");
            for (String s : words) {
                if (word.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the element has an attribute with the specified name and
     * the attribute value ends with the specified substring.
     *
     * @param element          An element of the document
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName    an attribute name
     * @param substring        the substring
     * @return true if the element has an attribute with the specified name and
     * the value ends with the specified substring.
     */
    default boolean attributeValueEndsWith(T element, @Nullable String namespacePattern, String attributeName, String substring) {
        String actualValue = getAttributeAsString(element, namespacePattern, attributeName);
        return actualValue != null && (actualValue.endsWith(substring));
    }

    /**
     * Returns true if the element has an attribute with the specified name and
     * value.
     *
     * @param element          An element of the document
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName    an attribute name
     * @param attributeValue   the attribute value
     * @return true if the element has an attribute with the specified name and
     * value
     */
    default boolean attributeValueEquals(T element, @Nullable String namespacePattern, String attributeName, String attributeValue) {
        String actualValue = getAttributeAsString(element, namespacePattern, attributeName);
        return actualValue != null && actualValue.equals(attributeValue);
    }

    /**
     * Returns true if the element has an attribute with the specified name and
     * the attribute value starts with the specified substring.
     *
     * @param element          An element of the document
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName    an attribute name
     * @param substring        the substring
     * @return true if the element has an attribute with the specified name and
     * the value starts with the specified substring.
     */
    default boolean attributeValueStartsWith(T element, @Nullable String namespacePattern, String attributeName, String substring) {
        String actualValue = getAttributeAsString(element, namespacePattern, attributeName);
        return actualValue != null && (actualValue.startsWith(substring));
    }

    /**
     * Returns the attribute value with the given name from the USER style origin.
     *
     * @param element          The element
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param name             The attribute name
     * @return The attribute value. Returns "initial" if the element does not have an
     * attribute with this name.
     */
    default @Nullable String getAttributeAsString(T element, @Nullable String namespacePattern, String name) {
        return getAttributeAsString(element, StyleOrigin.USER, namespacePattern, name);
    }

    /**
     * Returns the attribute value with the given name from the specified style origin.
     *
     * @param element          The element
     * @param origin           The style origin
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param name             The attribute name
     * @return The attribute value. Returns "initial" if the element does not have an
     * attribute with this name.
     */
    default @Nullable String getAttributeAsString(T element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, String name) {
        List<CssToken> list = getAttribute(element, origin, namespacePattern, name);
        if (list == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (CssToken t : list) {
            buf.append(t.fromToken());
        }
        return buf.toString();
    }

    /**
     * Returns the attribute value with the given name from the specified style origin.
     *
     * @param element          The element
     * @param origin The style origin
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param name             The attribute name
     * @return The attribute value as a list of {@link CssToken}s.
     */

    @Nullable
    List<CssToken> getAttribute(T element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, String name);

    /**
     * Returns all styleable attributes of the element.
     *
     * @param element An element of the document
     * @return a set of styleable attributes.
     */
    Set<QualifiedName> getAttributeNames(T element);

    /**
     * Returns all non-decomposed styleable attributes of the element.
     * <p>
     * If an attribute can be decomposed, only the composite attribute is
     * returned.
     *
     * @param element An element of the document
     * @return a set of styleable attributes.
     */
    Set<QualifiedName> getComposedAttributeNames(T element);

    /**
     * Returns all decomposed styleable attributes of the element.
     * <p>
     * If an attribute can be composed, only the decomposed attributes are
     * returned.
     *
     * @param element An element of the document
     * @return a set of styleable attributes.
     */
    Set<QualifiedName> getDecomposedAttributeNames(T element);

    /**
     * Returns the id of the element.
     *
     * @param element the element
     * @return the id or null if the element does not have an id.
     */
    @Nullable
    String getId(T element);

    /**
     * Gets the parent of the element.
     *
     * @param element An element of the document
     * @return The parent element. Returns null if the element has no parent.
     */
    @Nullable
    T getParent(T element);

    /**
     * Gets the previous sibling of the element.
     *
     * @param element An element of the document
     * @return The previous sibling. Returns null if the element has no previous
     * sibling.
     */
    @Nullable
    T getPreviousSibling(T element);

    /**
     * Returns the style classes of the element.
     *
     * @param element the element
     * @return the style classes or an empty set.
     */
    ReadableSet<String> getStyleClasses(T element);

    /**
     * Returns the pseudo classes of the element.
     *
     * @param element the element
     * @return the pseudo classes or an empty set.
     */
    ReadableSet<String> getPseudoClasses(T element);

    /**
     * Returns the style type of the element.
     *
     * @param element the element
     * @return the style type of the element,
     * return null if the element is not styleable by type.
     */
    @Nullable
    QualifiedName getType(T element);

    /**
     * Returns true if the element has the specified attribute.
     *
     * @param element       An element of the document
     * @param namespace     an optional namespace (null means any namespace
     *                      ,
     *                      an empty String means no namespace)
     * @param attributeName an attribute name
     * @return true if the element has an attribute with the specified name
     */
    boolean hasAttribute(T element, @Nullable String namespace, String attributeName);

    /**
     * Returns true if the element has the specified id.
     *
     * @param element An element of the document
     * @param id      an id
     * @return true if the element has the id
     */
    boolean hasId(T element, String id);

    /**
     * Returns true if the element has the specified pseudo class.
     *
     * @param element     An element of the document
     * @param pseudoClass a pseudo class
     * @return true if the element has the id
     */
    boolean hasPseudoClass(T element, String pseudoClass);

    /**
     * Returns true if the element has the specified style class.
     *
     * @param element An element of the document
     * @param clazz   a style class
     * @return true if the element has the id
     */
    boolean hasStyleClass(T element, String clazz);

    /**
     * Returns true if the element has the specified type.
     *
     * @param element          An element of the document
     * @param namespacePattern a namespace pattern ("*" means any namespace,
     *                         null means no namespace)
     * @param type             an id
     * @return true if the element has the id
     */
    boolean hasType(T element, @Nullable String namespacePattern, String type);

    /**
     * Resets all values with non-{@link StyleOrigin#USER} origin.
     */
    void reset(T elem);


    /**
     * Sets an attribute value.
     *
     * @param element   The element
     * @param origin    The style origin
     * @param namespace an optional namespace ("*" means any namespace,
     *                  null means no namespace)
     * @param name      The attribute name
     * @param value     The attribute value. {@code null} removes the attribute from the
     *                  element. That is, {@code null} has the same effect like the
     *                  keyword "unset".
     * @throws ParseException if parsing the value failed
     */
    void setAttribute(T element, StyleOrigin origin, @Nullable String namespace, String name, @Nullable ReadableList<CssToken> value) throws ParseException;


}
