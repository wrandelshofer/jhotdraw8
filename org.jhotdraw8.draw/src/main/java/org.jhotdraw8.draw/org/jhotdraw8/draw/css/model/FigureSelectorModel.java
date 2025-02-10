/*
 * @(#)FigureSelectorModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.model;

import javafx.css.StyleOrigin;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.ast.TypeSelector;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.StringCssConverter;
import org.jhotdraw8.css.model.AbstractSelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.ListCssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.css.value.QualifiedName;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.CompositeMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * FigureSelectorModel.
 *
 */
public class FigureSelectorModel extends AbstractSelectorModel<Figure> {
    public static final String JAVA_CLASS_NAMESPACE = "http://java.net";

    /**
     * Maps an attribute name to a key.
     */
    private final Map<Class<?>, Map<QualifiedName, WritableStyleableMapAccessor<?>>> nameToKeyMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<QualifiedName, ReadOnlyStyleableMapAccessor<?>>> nameToReadableKeyMap = new ConcurrentHashMap<>();
    /**
     * Maps a key to an attribute name.
     */
    private final ConcurrentHashMap<WritableStyleableMapAccessor<?>, QualifiedName> keyToNameMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Figure>, Map<QualifiedName, List<WritableStyleableMapAccessor<Object>>>> figureToMetaMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Figure>, Map<QualifiedName, List<ReadOnlyStyleableMapAccessor<Object>>>> figureToReadOnlyMetaMap = new ConcurrentHashMap<>();

    public FigureSelectorModel() {
    }


    @Override
    public boolean hasId(Figure element, String id) {
        return id.equals(element.getId());
    }

    @Override
    public String getId(Figure element) {
        return element.getId();
    }

    @Override
    public boolean hasType(Figure element, @Nullable String namespacePattern, String type) {
        if (namespacePattern == null || TypeSelector.ANY_NAMESPACE.equals(namespacePattern)) {
            return type.equals(element.getTypeSelector());
        }
        if (JAVA_CLASS_NAMESPACE.equals(namespacePattern)) {
            return element.getClass().getSimpleName().equals(type);
        }
        return false;
    }

    @Override
    public @Nullable QualifiedName getType(Figure element) {
        return new QualifiedName(null, element.getTypeSelector());
    }

    @Override
    public boolean hasStyleClass(Figure element, String clazz) {
        return element.getStyleClasses().contains(clazz);
    }

    @Override
    public ReadableSet<String> getStyleClasses(Figure element) {
        return element.getStyleClasses();
    }

    @Override
    public ReadableSet<String> getPseudoClasses(final Figure element) {
        return element.getPseudoClassStates();
    }


    private WritableStyleableMapAccessor<?> findKey(Figure element, @Nullable String namespace, String attributeName) {
        Map<QualifiedName, WritableStyleableMapAccessor<?>> mm = nameToKeyMap.computeIfAbsent(element.getClass(), k -> {
            SequencedMap<QualifiedName, WritableStyleableMapAccessor<?>> m = new LinkedHashMap<>();
            for (MapAccessor<?> kk : element.getSupportedKeys()) {
                if (kk instanceof WritableStyleableMapAccessor<?> sk) {
                    m.put(new QualifiedName(sk.getCssNamespace(), element.getClass() + "$" + sk.getCssName()), sk);
                    if (sk.getCssNamespace() != null) {
                        m.put(new QualifiedName(null, element.getClass() + "$" + sk.getCssName()), sk);
                    }
                }
            }
            return m;
        });
        return mm.get(new QualifiedName(namespace, element.getClass() + "$" + attributeName));
    }

    private ReadOnlyStyleableMapAccessor<?> findReadableKey(Figure element, @Nullable String namespacePattern, String attributeName) {
        Map<QualifiedName, ReadOnlyStyleableMapAccessor<?>> mm = nameToReadableKeyMap.computeIfAbsent(element.getClass(), k -> {
            SequencedMap<QualifiedName, ReadOnlyStyleableMapAccessor<?>> m = new LinkedHashMap<>();
            for (MapAccessor<?> kk : element.getSupportedKeys()) {
                if (kk instanceof ReadOnlyStyleableMapAccessor<?> sk) {
                    String cssNamespace = sk.getCssNamespace();
                    m.put(new QualifiedName(cssNamespace, element.getClass() + "$" + sk.getCssName()), sk);
                    if (cssNamespace != null) {
                        m.put(new QualifiedName(TypeSelector.WITHOUT_NAMESPACE, element.getClass() + "$" + sk.getCssName()), sk);
                    }
                    m.put(new QualifiedName(TypeSelector.ANY_NAMESPACE, element.getClass() + "$" + sk.getCssName()), sk);
                }
            }
            return m;
        });
        return mm.get(new QualifiedName(namespacePattern, element.getClass() + "$" + attributeName));
    }

    @Override
    public boolean hasAttribute(Figure element, @Nullable String namespace, String attributeName) {
        return getReadableMetaMap(element).containsKey(new QualifiedName(namespace, attributeName));
    }

    @Override
    public boolean attributeValueEquals(Figure element, @Nullable String namespacePattern, String attributeName, String requestedValue) {
        String stringValue = getReadOnlyAttributeValueAsString(element, namespacePattern, attributeName);
        return Objects.equals(stringValue, requestedValue);
    }

    @Override
    public boolean attributeValueStartsWith(Figure element, @Nullable String namespacePattern, String attributeName, String substring) {
        String stringValue = getReadOnlyAttributeValueAsString(element, namespacePattern, attributeName);
        return stringValue != null && stringValue.startsWith(substring);
    }

    protected @Nullable ReadOnlyStyleableMapAccessor<Object> getReadableAttributeAccessor(Figure element, @Nullable String namespace, String attributeName) {
        @SuppressWarnings("unchecked")
        ReadOnlyStyleableMapAccessor<Object> k = (ReadOnlyStyleableMapAccessor<Object>) findReadableKey(element, namespace, attributeName);
        return k;
    }

    protected @Nullable String getReadOnlyAttributeValueAsString(Figure element, @Nullable String namespace, String attributeName) {
        ReadOnlyStyleableMapAccessor<Object> k = getReadableAttributeAccessor(element, namespace, attributeName);
        if (k == null) {
            return null;
        }
        Object value = element.get(k);

        // FIXME get rid of special treatment for CssStringConverter
        Converter<Object> c = k.getCssConverter();
        String stringValue = (((Converter<?>) c) instanceof StringCssConverter) ? (String) value : k.getCssConverter().toString(value);
        return stringValue;
    }

    @Override
    public boolean attributeValueEndsWith(Figure element, @Nullable String namespacePattern, String attributeName, String substring) {
        String stringValue = getReadOnlyAttributeValueAsString(element, namespacePattern, attributeName);
        return stringValue != null && stringValue.endsWith(substring);
    }

    @Override
    public boolean attributeValueContains(Figure element, @Nullable String namespacePattern, String attributeName, String substring) {
        String stringValue = getReadOnlyAttributeValueAsString(element, namespacePattern, attributeName);
        return stringValue != null && stringValue.contains(substring);
    }

    @Override
    public boolean attributeValueContainsWord(Figure element, @Nullable String namespacePattern, String attributeName, String word) {
        ReadOnlyStyleableMapAccessor<Object> k = getReadableAttributeAccessor(element, namespacePattern, attributeName);
        if (k == null) {
            return false;
        }
        Object value = element.get(k);
        if (value instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null && word.equals(o.toString())) {
                    return true;
                }
            }
        } else if (value instanceof String) {
            for (String s : ((String) value).split("\\s+")) {
                if (s.equals(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasPseudoClass(Figure element, String pseudoClass) {
        Set<Figure> fs = additionalPseudoClassStatesProperty().get(pseudoClass);
        if (fs != null && fs.contains(element)) {
            return true;
        }

        // XXX Pseudo class is not thread safe!
        // XXX we unnecessarily create many pseudo class states!
        return element.getPseudoClassStates().contains(pseudoClass);
    }

    @Override
    public Figure getParent(Figure element) {
        return element.getParent();
    }

    @Override
    public @Nullable Figure getPreviousSibling(Figure element) {
        if (element.getParent() == null) {
            return null;
        }
        int i = element.getParent().getChildren().indexOf(element);
        return i == 0 ? null : element.getParent().getChild(i - 1);
    }

    @Override
    public Set<QualifiedName> getAttributeNames(Figure element) {
        return getWritableMetaMap(element).keySet();
    }

    @Override
    public Set<QualifiedName> getComposedAttributeNames(Figure element) {
        Set<QualifiedName> attr = new HashSet<>();
        Set<WritableStyleableMapAccessor<?>> attrk = new HashSet<>();
        for (MapAccessor<?> key : element.getSupportedKeys()) {
            if (key instanceof WritableStyleableMapAccessor<?> sk) {
                attrk.add(sk);
            }
        }
        for (MapAccessor<?> key : element.getSupportedKeys()) {
            if (key instanceof CompositeMapAccessor) {
                attrk.removeAll(((CompositeMapAccessor<?>) key).getSubAccessors().asSet());
            }
        }
        for (WritableStyleableMapAccessor<?> key : attrk) {
            attr.add(new QualifiedName(key.getCssNamespace(), key.getCssName()));
        }
        return attr;
    }

    @Override
    public Set<QualifiedName> getDecomposedAttributeNames(Figure element) {
        // FIXME use keyToName map
        Set<QualifiedName> attr = new HashSet<>();
        Set<WritableStyleableMapAccessor<?>> attrk = new HashSet<>();
        for (MapAccessor<?> key : element.getSupportedKeys()) {
            if ((key instanceof WritableStyleableMapAccessor<?> sk) && !(key instanceof CompositeMapAccessor)) {
                attrk.add(sk);
            }
        }
        for (WritableStyleableMapAccessor<?> key : attrk) {
            attr.add(new QualifiedName(key.getCssNamespace(), key.getCssName()));
        }
        return attr;
    }

    @Override
    public @Nullable String getAttributeAsString(Figure element, @Nullable String namespacePattern, String attributeName) {
        return getAttributeAsString(element, StyleOrigin.USER, namespacePattern, attributeName);
    }

    @Override
    public @Nullable String getAttributeAsString(Figure element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, String attributeName) {
        ReadOnlyStyleableMapAccessor<?> key = findReadableKey(element, namespacePattern, attributeName);
        if (key == null) {
            return null;
        }
        boolean isInitialValue = origin != null && !element.containsMapAccessor(origin, key);
        if (isInitialValue) {
            if ((key instanceof CompositeMapAccessor)) {
                for (MapAccessor<?> subkey : ((CompositeMapAccessor<?>) key).getSubAccessors()) {
                    if (element.containsMapAccessor(origin, subkey)) {
                        isInitialValue = false;
                        break;
                    }
                }
            }
        }
        if (isInitialValue) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        @SuppressWarnings("unchecked")
        Converter<Object> converter = (Converter<Object>) key.getCssConverter();
        if (converter instanceof CssConverter<Object> c) {
            try {
                List<CssToken> cssTokens = c.toTokens(element.getStyled(origin, key), null);
                if (cssTokens.size() == 1) {
                    // If the value is scalar, then append it without CSS syntax adornments.
                    for (CssToken t : cssTokens) {
                        switch (t.getType()) {
                        case CssTokenType.TT_NUMBER:
                            buf.append(t.getNumericValueNonNull());
                            break;
                        case CssTokenType.TT_PERCENTAGE:
                            buf.append(t.getNumericValueNonNull());
                            buf.append('%');
                            break;
                        case CssTokenType.TT_DIMENSION:
                            buf.append(t.getNumericValueNonNull());
                            if (t.getStringValue() != null) {
                                buf.append(t.getStringValue());
                            }
                            break;
                        default:
                            if (t.getStringValue() != null) {
                                buf.append(t.getStringValue());
                            }
                            break;
                        }
                    }
                } else {
                    // If the value is non-scalar, then append it with all CSS syntax adornments.
                    for (CssToken t : cssTokens) {
                        buf.append(t.toString());
                    }
                }
            } catch (IOException e) {
                Logger.getLogger(FigureSelectorModel.class.getName()).log(Level.WARNING, "Could not produce tokens for key: " + key + " value: " + element.getStyled(origin, key), e);
            }
        } else {
            buf.append(converter.toString(element.getStyled(origin, key)));// XXX THIS IS WRONG!!)
        }

        return buf.toString();
    }

    @Override
    public @Nullable List<CssToken> getAttribute(Figure element, @Nullable StyleOrigin origin, @Nullable String namespacePattern, String attributeName) {
        ReadOnlyStyleableMapAccessor<?> key = findReadableKey(element, namespacePattern, attributeName);
        if (key == null) {
            return null;
        }
        boolean isInitialValue = origin != null && !element.containsMapAccessor(origin, key);
        if (isInitialValue) {
            if ((key instanceof CompositeMapAccessor)) {
                for (MapAccessor<?> subkey : ((CompositeMapAccessor<?>) key).getSubAccessors()) {
                    if (element.containsMapAccessor(origin, subkey)) {
                        isInitialValue = false;
                        break;
                    }
                }
            }
        }
        if (isInitialValue) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Converter<Object> converter = (Converter<Object>) key.getCssConverter();
        if (converter instanceof CssConverter) {
            try {
                return ((CssConverter<Object>) converter).toTokens(element.getStyled(origin, key), null);
            } catch (IOException e) {
                Logger.getLogger(FigureSelectorModel.class.getName()).log(Level.WARNING, "Could not produce tokens for key: " + key + " value: " + element.getStyled(origin, key), e);
                return null;
            }
        } else {
            try {
                CssTokenizer tt = new StreamCssTokenizer(converter.toString(element.getStyled(origin, key)), null);
                return tt.toTokenList();
            } catch (IOException e) {
                throw new RuntimeException("unexpected exception", e);
            }
        }
    }

    public @Nullable Converter<?> getConverter(Figure element, @Nullable String namespace, String attributeName) {
        WritableStyleableMapAccessor<?> k = findKey(element, namespace, attributeName);
        return k == null ? null : k.getCssConverter();
    }

    public @Nullable WritableStyleableMapAccessor<?> getAccessor(Figure element, @Nullable String namespace, String attributeName) {
        return findKey(element, namespace, attributeName);
    }

    protected Map<QualifiedName, List<WritableStyleableMapAccessor<Object>>> getWritableMetaMap(Figure elem) {
        return figureToMetaMap.computeIfAbsent(elem.getClass(), klass -> {
            Map<QualifiedName, List<WritableStyleableMapAccessor<Object>>> metaMap = new HashMap<>();

            Function<QualifiedName, List<WritableStyleableMapAccessor<Object>>> arrayListSupplier = key -> new ArrayList<>();
            for (MapAccessor<?> k : elem.getSupportedKeys()) {
                if (k instanceof WritableStyleableMapAccessor) {
                    @SuppressWarnings("unchecked")
                    WritableStyleableMapAccessor<Object> sk = (WritableStyleableMapAccessor<Object>) k;
                    metaMap.computeIfAbsent(new QualifiedName(sk.getCssNamespace(), sk.getCssName()), arrayListSupplier).add(sk);
                    if (sk.getCssNamespace() != null) {
                        // all names can be accessed without specificying a namespace
                        metaMap.computeIfAbsent(new QualifiedName(null, sk.getCssName()), arrayListSupplier).add(sk);
                    }
                }
            }

            return metaMap;
        });
    }

    private Map<QualifiedName, List<ReadOnlyStyleableMapAccessor<Object>>> getReadableMetaMap(Figure elem) {
        return figureToReadOnlyMetaMap.computeIfAbsent(elem.getClass(), klass -> {
            Map<QualifiedName, List<ReadOnlyStyleableMapAccessor<Object>>> metaMap = new HashMap<>();

            Function<QualifiedName, List<ReadOnlyStyleableMapAccessor<Object>>> arrayListSupplier = key -> new ArrayList<>();
            for (MapAccessor<?> k : elem.getSupportedKeys()) {
                if (k instanceof ReadOnlyStyleableMapAccessor) {
                    @SuppressWarnings("unchecked")
                    ReadOnlyStyleableMapAccessor<Object> sk = (ReadOnlyStyleableMapAccessor<Object>) k;
                    metaMap.computeIfAbsent(new QualifiedName(sk.getCssNamespace(), sk.getCssName()), arrayListSupplier).add(sk);
                    if (sk.getCssNamespace() != null) {
                        // all names can be accessed without specificying a namespace
                        metaMap.computeIfAbsent(new QualifiedName(null, sk.getCssName()), arrayListSupplier).add(sk);
                    }
                }
            }

            return metaMap;
        });
    }

    @Override
    public void setAttribute(Figure elem, StyleOrigin origin, @Nullable String namespace, String name, @Nullable ReadableList<CssToken> value) {
        Map<QualifiedName, List<WritableStyleableMapAccessor<Object>>> metaMap = getWritableMetaMap(elem);

        List<WritableStyleableMapAccessor<Object>> ks = metaMap.get(new QualifiedName(namespace, name));
        if (ks != null) {
            for (WritableStyleableMapAccessor<Object> k : ks) {
                if (value == null || isInitialRevertOrUnset(value)) {
                    // FIXME: When "initial" is requested, we must set the property to its initial value
                    // When value is nul, we remove the key from this origin.
                    // When 'revert is requested, we remove the key from this origin.
                    // When 'unset is requested, we remove the key from this origin.
                    elem.remove(origin, k);
                } else {
                    @SuppressWarnings("unchecked")
                    Converter<Object> converter = k.getCssConverter();
                    Object convertedValue;
                    try {
                        if (converter instanceof CssConverter) {
                            convertedValue = ((CssConverter<Object>) converter).parse(new ListCssTokenizer(value), null);
                        } else {
                            convertedValue = converter.fromString(value.stream().map(CssToken::fromToken).collect(Collectors.joining()));
                        }
                        elem.setStyled(origin, k, intern(convertedValue));
                    } catch (ParseException | IOException ex) {
                        Logger.getLogger(FigureSelectorModel.class.getName()).log(Level.WARNING, "error setting attribute " + name + " with tokens " + value, ex);
                    }
                }
            }
        }
    }

    private final Map<Object, Object> inlinedValues = new ConcurrentHashMap<>();

    protected @Nullable Object intern(@Nullable Object convertedValue) {
        return convertedValue == null ? null : inlinedValues.computeIfAbsent(convertedValue, k -> convertedValue);
    }

    /**
     * FIXME All selector models must support the keywords "initial","inherit","revert","unset".
     *
     * @param value the token
     * @return true if the value is "initial".
     */
    protected boolean isInitialRevertOrUnset(@Nullable ReadableList<CssToken> value) {
        if (value != null) {
            boolean isInitial = false;
            Loop:
            for (CssToken token : value) {

                switch (token.getType()) {
                    case CssTokenType.TT_IDENT:
                        if ("initial".equals(token.getStringValue())
                                || "revert".equals(token.getStringValue())
                                || "unset".equals(token.getStringValue())) {
                            isInitial = true;
                        }
                        break;
                    case CssTokenType.TT_S:
                        break;
                    default:
                        isInitial = false;
                        break Loop;
                }
            }
            return isInitial;
        }
        return false;
    }

    @Override
    public void reset(Figure elem) {
        elem.resetStyledValues();
    }

}
