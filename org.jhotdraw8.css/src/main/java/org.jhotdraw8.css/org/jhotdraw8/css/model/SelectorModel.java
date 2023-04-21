/*
 * @(#)SelectorModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.model;

import javafx.beans.property.MapProperty;
import javafx.css.StyleOrigin;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.value.QualifiedName;

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
    @NonNull
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
    default boolean attributeValueContains(@NonNull T element, @Nullable String namespacePattern, @NonNull String attributeName, @NonNull String substring) {
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
    default boolean attributeValueContainsWord(@NonNull T element, @Nullable String namespacePattern, @NonNull String attributeName, @NonNull String word) {
        String value = getAttributeAsString(element, namespacePattern, attributeName);
        if (value != null) {
            String[] words = value.split("\\s+");
            for (int i = 0; i < words.length; i++) {
                if (word.equals(words[i])) {
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
    default boolean attributeValueEndsWith(@NonNull T element, @Nullable String namespacePattern, @NonNull String attributeName, @NonNull String substring) {
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
    default boolean attributeValueEquals(@NonNull T element, @Nullable String namespacePattern, @NonNull String attributeName, @NonNull String attributeValue) {
        String actualValue = getAttributeAsString(element, namespacePattern, attributeName);
        return actualValue != null && actualValue.equals(attributeValue);
    }

    /**
     * Returns true if the element has an attribute with the specified name and
     * the attribute value starts with the specified substring.
     *
     * @param element          An element of the document
     * @param namespacePattern The attribute namespace
     * @param attributeName    an attribute name
     * @param substring        the substring
     * @return true if the element has an attribute with the specified name and
     * the value starts with the specified substring.
     */
    default boolean attributeValueStartsWith(@NonNull T element, @Nullable String namespacePattern, @NonNull String attributeName, @NonNull String substring) {
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
    default @Nullable String getAttributeAsString(@NonNull T element, @Nullable String namespacePattern, @NonNull String name) {
        return getAttributeAsString(element, StyleOrigin.USER, namespacePattern, name);
    }

    default @Nullable String getAttributeAsString(@NonNull T element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, @NonNull String name) {
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

    @Nullable
    List<CssToken> getAttribute(@NonNull T element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, @NonNull String name);

    /**
     * Returns all styleable attributes of the element.
     *
     * @param element An element of the document
     * @return a set of styleable attributes.
     */
    @NonNull Set<QualifiedName> getAttributeNames(@NonNull T element);

    /**
     * Returns all non-decomposed styleable attributes of the element.
     * <p>
     * If an attribute can be decomposed, only the composite attribute is
     * returned.
     *
     * @param element An element of the document
     * @return a set of styleable attributes.
     */
    @NonNull Set<QualifiedName> getComposedAttributeNames(@NonNull T element);

    /**
     * Returns all decomposed styleable attributes of the element.
     * <p>
     * If an attribute can be composed, only the decomposed attributes are
     * returned.
     *
     * @param element An element of the document
     * @return a set of styleable attributes.
     */
    @NonNull Set<QualifiedName> getDecomposedAttributeNames(@NonNull T element);

    /**
     * Returns the id of the element.
     *
     * @param element the element
     * @return the id or null if the element does not have an id.
     */
    @Nullable
    String getId(@NonNull T element);

    /**
     * Gets the parent of the element.
     *
     * @param element An element of the document
     * @return The parent element. Returns null if the element has no parent.
     */
    @Nullable
    T getParent(@NonNull T element);

    /**
     * Gets the previous sibling of the element.
     *
     * @param element An element of the document
     * @return The previous sibling. Returns null if the element has no previous
     * sibling.
     */
    @Nullable
    T getPreviousSibling(@NonNull T element);

    /**
     * Returns the style classes of the element.
     *
     * @param element the element
     * @return the style classes or an empty set.
     */
    @NonNull ReadOnlySet<String> getStyleClasses(@NonNull T element);

    /**
     * Returns the pseudo classes of the element.
     *
     * @param element the element
     * @return the pseudo classes or an empty set.
     */
    @NonNull ReadOnlySet<String> getPseudoClasses(@NonNull T element);

    /**
     * Returns the style type of the element.
     *
     * @param element the element
     * @return the style type of the element,
     * return null if the element is not styleable by type.
     */
    @Nullable
    QualifiedName getType(@NonNull T element);

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
    boolean hasAttribute(@NonNull T element, @Nullable String namespace, @NonNull String attributeName);

    /**
     * Returns true if the element has the specified id.
     *
     * @param element An element of the document
     * @param id      an id
     * @return true if the element has the id
     */
    boolean hasId(@NonNull T element, @NonNull String id);

    /**
     * Returns true if the element has the specified pseudo class.
     *
     * @param element     An element of the document
     * @param pseudoClass a pseudo class
     * @return true if the element has the id
     */
    boolean hasPseudoClass(@NonNull T element, @NonNull String pseudoClass);

    /**
     * Returns true if the element has the specified style class.
     *
     * @param element An element of the document
     * @param clazz   a style class
     * @return true if the element has the id
     */
    boolean hasStyleClass(@NonNull T element, @NonNull String clazz);

    /**
     * Returns true if the element has the specified type.
     *
     * @param element          An element of the document
     * @param namespacePattern a namespace pattern ("*" means any namespace,
     *                         null means no namespace)
     * @param type             an id
     * @return true if the element has the id
     */
    boolean hasType(@NonNull T element, @Nullable String namespacePattern, @NonNull String type);

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
    void setAttribute(@NonNull T element, @NonNull StyleOrigin origin, @Nullable String namespace, @NonNull String name, @Nullable ReadOnlyList<CssToken> value) throws ParseException;


}
