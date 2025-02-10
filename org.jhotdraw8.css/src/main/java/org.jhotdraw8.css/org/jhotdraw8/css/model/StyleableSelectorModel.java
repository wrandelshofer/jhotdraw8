/*
 * @(#)StyleableSelectorModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.model;

import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.css.value.QualifiedName;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link SelectorModel} for objects that implement the {@link Styleable}
 * interface.
 *
 */
public class StyleableSelectorModel extends AbstractSelectorModel<Styleable> {


    public StyleableSelectorModel() {
    }

    @Override
    public @Nullable String getAttributeAsString(Styleable element, StyleOrigin origin, @Nullable String namespacePattern, String name) {
        if (origin == StyleOrigin.USER) {
            String attribute = getAttributeAsString(element, namespacePattern, name);
            return attribute == null ? "" : attribute;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasId(Styleable element, String id) {
        return id.equals(element.getId());
    }

    @Override
    public String getId(Styleable element) {
        return element.getId();
    }

    @Override
    public boolean hasType(Styleable element, @Nullable String namespacePattern, String type) {
        return type.equals(element.getTypeSelector());
    }

    @Override
    public void reset(Styleable elem) {
        // do nothing
    }

    @Override
    public @Nullable QualifiedName getType(Styleable element) {
        return new QualifiedName(null, element.getTypeSelector());
    }

    @Override
    public boolean hasStyleClass(Styleable element, String clazz) {
        return element.getStyleClass().contains(clazz);
    }

    @Override
    public ReadableSet<String> getStyleClasses(Styleable element) {
        return ChampSet.copyOf(element.getStyleClass());
    }

    @Override
    public ReadableSet<String> getPseudoClasses(Styleable element) {
        return ChampSet.copyOf(element.getPseudoClassStates().stream().map(PseudoClass::getPseudoClassName)
                .collect(Collectors.toList()));
    }

    @Override
    public boolean hasPseudoClass(Styleable element, String pseudoClass) {
        // Warning: PseudoClass is not thread safe!
        return element.getPseudoClassStates().contains(PseudoClass.getPseudoClass(pseudoClass));
    }

    @Override
    public Styleable getParent(Styleable element) {
        return element.getStyleableParent();
    }

    @Override
    public @Nullable Styleable getPreviousSibling(Styleable element) {
        return null;
    }

    @Override
    public boolean hasAttribute(Styleable element, @Nullable String namespace, String attributeName) {
        // XXX linear time!
        List<CssMetaData<? extends Styleable, ?>> list = element.getCssMetaData();
        for (CssMetaData<? extends Styleable, ?> item : list) {
            if (attributeName.equals(item.getProperty())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<CssToken> getAttribute(Styleable element, StyleOrigin origin, @Nullable String namespacePattern, String attributeName) {
        List<CssMetaData<? extends Styleable, ?>> list = element.getCssMetaData();
        // XXX linear time!
        for (CssMetaData<? extends Styleable, ?> i : list) {
            @SuppressWarnings("unchecked")
            CssMetaData<Styleable, ?> item = (CssMetaData<Styleable, ?>) i;
            if (attributeName.equals(item.getProperty())) {
                Object value = item.getStyleableProperty(element).getValue();
                try {
                    return value == null ? null : new StreamCssTokenizer(value.toString(), null).toTokenList();
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected IOException", e);
                }
            }
        }
        return null;
    }

    private @Nullable Set<String> getWordListAttribute(Styleable element, @Nullable String namespace, String attributeName) {
        List<CssMetaData<? extends Styleable, ?>> list = element.getCssMetaData();
        // XXX linear time!
        for (CssMetaData<? extends Styleable, ?> i : list) {
            @SuppressWarnings("unchecked")
            CssMetaData<Styleable, ?> item = (CssMetaData<Styleable, ?>) i;
            if (attributeName.equals(item.getProperty())) {
                Object value = item.getStyleableProperty(element).getValue();

                if (value instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> olist = (Collection<Object>) value;
                    Set<String> slist = new HashSet<>();
                    for (Object o : olist) {
                        slist.add(o.toString());
                    }
                    return slist;
                } else {
                    Set<String> slist = new HashSet<>();
                    if (value != null) {
                        String[] words = value.toString().split("\\s+");
                        slist.addAll(Arrays.asList(words));
                    }

                    return slist;
                }
            }
        }
        return null;
    }

    @Override
    public boolean attributeValueContainsWord(Styleable element, @Nullable String namespacePattern, String attributeName, String word) {
        Set<String> value = getWordListAttribute(element, namespacePattern, attributeName);

        return value != null && value.contains(word);
    }

    @Override
    public Set<QualifiedName> getAttributeNames(Styleable element) {
        Set<QualifiedName> attr = new HashSet<>();
        for (CssMetaData<? extends Styleable, ?> item : element.getCssMetaData()) {
            attr.add(new QualifiedName(null, item.getProperty()));
        }
        return attr;
    }

    @Override
    public Set<QualifiedName> getComposedAttributeNames(Styleable element) {
        // FIXME we actually can do this!
        return getAttributeNames(element);
    }

    @Override
    public Set<QualifiedName> getDecomposedAttributeNames(Styleable element) {
        // FIXME we actually can do this!
        return getAttributeNames(element);
    }

    @Override
    public void setAttribute(Styleable elem, StyleOrigin origin, @Nullable String namespace, String name, @Nullable ReadableList<CssToken> valueAsTokens) {
        String value;
        if (valueAsTokens == null) {
            value = null;
        } else {
            value = valueAsTokens.stream().map(CssToken::fromToken).collect(Collectors.joining());
        }

        List<CssMetaData<? extends Styleable, ?>> metaList = elem.getCssMetaData();
        HashMap<String, CssMetaData<? extends Styleable, ?>> metaMap = new HashMap<>();
        for (CssMetaData<? extends Styleable, ?> m : metaList) {
            metaMap.put(m.getProperty(), m);
        }
        @SuppressWarnings("unchecked")
        CssMetaData<Styleable, ?> m = (CssMetaData<Styleable, ?>) metaMap.get(name);
        if (m != null && m.isSettable(elem)) {
            @SuppressWarnings("unchecked")
            StyleConverter<Object, Object> converter = (StyleConverter<Object, Object>) m.getConverter();
            ParsedValueImpl<Object, Object> parsedValue = new ParsedValueImpl<>(value, null);

            Object convertedValue = converter.convert(parsedValue, null);
            @SuppressWarnings("unchecked")
            StyleableProperty<Object> styleableProperty = (StyleableProperty<Object>) m.getStyleableProperty(elem);
            styleableProperty.applyStyle(origin, convertedValue);
        }

    }

    private static class ParsedValueImpl<V, T> extends ParsedValue<V, T> {

        public ParsedValueImpl(V value, StyleConverter<V, T> converter) {
            super(value, converter);
        }

    }

}
