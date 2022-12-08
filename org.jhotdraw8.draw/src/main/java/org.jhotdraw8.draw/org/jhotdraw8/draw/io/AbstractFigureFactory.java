/*
 * @(#)AbstractFigureFactory.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * AbstractFigureFactory.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractFigureFactory implements FigureFactory {
    private static final Logger LOGGER = Logger.getLogger(AbstractFigureFactory.class.getName());
    private final Map<Class<? extends Figure>, HashMap<String, MapAccessor<?>>> attrToKey = new HashMap<>();
    private final Map<FigureAccessorKey<?>, Object> defaultValueMap = new HashMap<>();
    private final Map<Class<? extends Figure>, HashMap<String, MapAccessor<?>>> elemToKey = new HashMap<>();
    private final Map<Class<? extends Figure>, HashSet<MapAccessor<?>>> figureAttributeKeys = new HashMap<>();
    private final Map<Class<? extends Figure>, HashSet<MapAccessor<?>>> figureNodeListKeys = new HashMap<>();
    private final Map<Class<? extends Figure>, String> figureToName = new HashMap<>();
    private final Map<Class<? extends Figure>, HashMap<MapAccessor<?>, String>> keyToAttr = new HashMap<>();
    private final Map<Class<? extends Figure>, HashMap<MapAccessor<?>, String>> keyToElem = new HashMap<>();
    private final Map<MapAccessor<?>, Converter<?>> keyValueFromXML = new HashMap<>();
    private final Map<MapAccessor<?>, Converter<?>> keyValueToXML = new HashMap<>();
    private final Map<String, Supplier<Figure>> nameToFigure = new HashMap<>();
    private String objectIdAttribute = "id";
    private final Map<String, HashSet<Class<? extends Figure>>> skipAttributes = new HashMap<>();
    private final Set<String> skipElements = new HashSet<>();
    private final Set<Class<? extends Figure>> skipFigures = new HashSet<>();
    private final Map<Type, Converter<?>> valueFromXML = new HashMap<>();

    private final Map<Type, Converter<?>> valueToXML = new HashMap<>();
    private @Nullable IdFactory idFactory;

    public AbstractFigureFactory() {
        this(new SimpleFigureIdFactory());
    }

    public AbstractFigureFactory(@Nullable IdFactory idFactory) {
        Objects.requireNonNull(idFactory, "idFactory");
        this.idFactory = idFactory;
    }

    /**
     * Adds a converter for the specified key.
     *
     * @param <T>       the type of the value
     * @param key       the key
     * @param converter the converter
     */
    public <T> void addConverter(MapAccessor<T> key, Converter<T> converter) {
        keyValueToXML.put(key, converter);
        keyValueFromXML.put(key, converter);
    }

    /**
     * Adds a converter.
     *
     * @param fullValueType A value type returned by
     *                      {@code MapAccessor.getFullValueType();}.
     * @param converter     the converter
     */
    public void addConverterForType(Type fullValueType, Converter<?> converter) {
        addConverterForType(fullValueType, converter, false);
    }

    public void addConverterForType(Type fullValueType, Converter<?> converter, boolean force) {
        if (!force && valueToXML.containsKey(fullValueType)) {
            throw new IllegalStateException("you already added " + fullValueType);
        }

        valueToXML.put(fullValueType, converter);
        valueFromXML.put(fullValueType, converter);
    }

    public <T> void addDefaultValue(Class<? extends Figure> figure, MapAccessor<T> acc, T value) {
        defaultValueMap.put(new FigureAccessorKey<>(figure, acc), value);
    }

    /**
     * Adds the provided mappings of XML attribute names from/to
     * {@code Figure}s.
     * <p>
     * {@code figureClass.newInstance()} is used to instantiate a figure from a
     * name.</p>
     * <p>
     * If a figure with this name has already been added, it will be replaced by this figure.
     *
     * @param name        The element name
     * @param figureClass The figure class is used both for instantiation of a
     *                    new figure and for determining the name of a figure.
     */
    public void addFigure(String name, @NonNull Class<? extends Figure> figureClass) {
        final Constructor<? extends Figure> declaredConstructor;
        try {
            declaredConstructor = figureClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("no no-args constructor in " + figureClass, e);
        }
        nameToFigure.put(name, () -> {
            try {
                return declaredConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new InternalError("Couldn't instantiate " + figureClass, e);
            }
        });

        figureToName.put(figureClass, name);
    }


    /**
     * Adds the provided keys to the figure.
     *
     * @param f    the figure
     * @param keys the keys
     */
    public void addFigureAttributeKeys(Class<? extends Figure> f, @NonNull Collection<MapAccessor<?>> keys) {
        for (MapAccessor<?> key : keys) {
            addKey(f, key.getName(), key);
        }
    }

    public void addFigureKeysAndNames(String figureName, @NonNull Class<? extends Figure> f) {
        addFigureKeysAndNames(figureName, f, Figure.getDeclaredAndInheritedMapAccessors(f));
    }

    public void addFigureKeysAndNames(String figureName, @NonNull Class<? extends Figure> f, @NonNull Collection<MapAccessor<?>> keys) {
        addFigure(figureName, f);
        addFigureAttributeKeys(f, keys);
        for (MapAccessor<?> key : keys) {
            addKey(f, key.getName(), key);
        }
    }

    public void addFigureKeysAndNames(Class<? extends Figure> f, @NonNull Collection<MapAccessor<?>> keys) {
        addFigureAttributeKeys(f, keys);
        for (MapAccessor<?> key : keys) {
            addKey(f, key.getName(), key);
        }
    }

    /**
     * Adds the provided mapping of XML attribute names from/to
     * {@code MapAccessor}s.
     * <p>
     * The same key can be added more than once.
     *
     * @param figure the figure
     * @param name   The attribute name
     * @param key    The key
     */
    public void addKey(Class<? extends Figure> figure, String name, MapAccessor<?> key) {
        figureAttributeKeys.computeIfAbsent(figure, k -> new HashSet<>()).add(key);

        HashMap<String, MapAccessor<?>> strToKey = attrToKey.computeIfAbsent(figure, k -> new HashMap<>());
        strToKey.putIfAbsent(name, key);

        HashMap<MapAccessor<?>, String> keyToStr = keyToAttr.computeIfAbsent(figure, k -> new HashMap<>());
        keyToStr.putIfAbsent(key, name);
    }

    /**
     * Adds the provided mapping of XML attribute names from/to
     * {@code MapAccessor}s.
     * <p>
     * The same key can be added more than once.
     *
     * @param f    The figure
     * @param keys The mapping from attribute names to keys
     */
    public void addKeys(Class<? extends Figure> f, @NonNull HashMap<String, MapAccessor<?>> keys) {
        for (Map.Entry<String, MapAccessor<?>> entry : keys.entrySet()) {
            addKey(f, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds the provided keys to the figure.
     *
     * @param figure the figure
     * @param name   the element name
     * @param key    the keys
     */
    public void addNodeListKey(Class<? extends Figure> figure, String name, MapAccessor<?> key) {
        if (figureNodeListKeys.containsKey(figure)) {
            figureNodeListKeys.get(figure).add(key);
        } else {
            HashSet<MapAccessor<?>> hset = new HashSet<>();
            hset.add(key);
            figureNodeListKeys.put(figure, hset);
        }

        HashMap<String, MapAccessor<?>> strToKey = elemToKey.computeIfAbsent(figure, k -> new HashMap<>());
        if (!strToKey.containsKey(name)) {
            strToKey.put(name, key);
        }

        HashMap<MapAccessor<?>, String> keyToStr = keyToElem.computeIfAbsent(figure, k -> new HashMap<>());
        if (!keyToStr.containsKey(key)) {
            keyToStr.put(key, name);
        }
    }

    /**
     * Adds an attribute to the list of attributes which will be skipped when
     * reading the DOM.
     *
     * @param figure        the figure class
     * @param attributeName the attribute name
     */
    public void addSkipAttribute(Class<? extends Figure> figure, String attributeName) {
        HashSet<Class<? extends Figure>> set = skipAttributes.get(attributeName);
        if (set == null) {
            set = new HashSet<>();
            skipAttributes.put(attributeName, set);
        }
        set.add(figure);
    }

    /**
     * Adds an element to the list of elements which will be skipped when
     * reading the DOM.
     *
     * @param elementName the element name
     */
    public void addSkipElement(String elementName) {
        skipElements.add(elementName);
    }

    /**
     * Adds a figure class to the list of {@code Figure}s which will be skipped
     * when writing the DOM.
     *
     * @param figure The figure class
     */
    public void addSkipFigure(Class<? extends Figure> figure) {
        skipFigures.add(figure);
    }

    public void checkConverters(boolean throwException, @NonNull Consumer<String> logger) throws IllegalStateException {
        for (HashMap<MapAccessor<?>, String> map : keyToAttr.values()) {
            for (MapAccessor<?> k : map.keySet()) {
                Type fullValueType = k.getValueType();
                if (!k.isTransient() && !keyValueToXML.containsKey(k) && !valueToXML.containsKey(fullValueType)) {
                    final String msg = getClass() + " can not convert " + fullValueType + " to XML for key " + k + ".";
                    if (throwException) {
                        throw new IllegalStateException(msg);
                    } else {
                        logger.accept(msg);
                    }
                }
            }
        }
    }

    /**
     * Clears the mapping of XML attributes from/to {@code MapAccessor}s.
     */
    public void clearAttributeMap() {
        attrToKey.clear();
        keyToAttr.clear();
    }

    /**
     * Clears the mapping of XML attributes from/to {@code MapAccessor}s.
     */
    public void clearElementMap() {
        attrToKey.clear();
        keyToAttr.clear();
    }

    @Override
    public MapAccessor<?> getKeyByElementName(@NonNull Figure f, String elementName) throws IOException {
        HashMap<String, MapAccessor<?>> strToKey = elemToKey.get(f.getClass());
        if (strToKey == null || !strToKey.containsKey(elementName)) {
            throw new IOException("no mapping for attribute " + elementName
                    + " in figure " + f.getClass());
        }
        return strToKey.get(elementName);
    }

    @Override
    public @NonNull Set<MapAccessor<?>> figureAttributeKeys(@NonNull Figure f) {
        Set<MapAccessor<?>> keys = figureAttributeKeys.get(f.getClass());
        return keys == null ? Collections.emptySet() : keys;
    }

    @Override
    public @NonNull Set<MapAccessor<?>> figureNodeListKeys(@NonNull Figure f) {
        Set<MapAccessor<?>> keys = figureNodeListKeys.get(f.getClass());
        return keys == null ? Collections.emptySet() : keys;

    }

    @Override
    public @Nullable String getElementNameByFigure(@NonNull Figure f) throws IOException {
        if (!figureToName.containsKey(f.getClass())) {
            if (skipFigures.contains(f.getClass())) {
                return null;
            }
            throw new IOException("no mapping for figure " + f.getClass());
        }
        return figureToName.get(f.getClass());
    }

    @Override
    public <T> T getDefaultValue(@NonNull Figure f, @NonNull MapAccessor<T> key) {
        FigureAccessorKey<T> k = new FigureAccessorKey<>(f.getClass(), key);
        if (defaultValueMap.containsKey(k)) {
            @SuppressWarnings("unchecked")
            T defaultValue = (T) defaultValueMap.get(k);
            return defaultValue;
        } else {
            return key.getDefaultValue();
        }
    }

    public @Nullable IdFactory getIdFactory() {
        return idFactory;
    }

    public void setIdFactory(IdFactory idFactory) {
        this.idFactory = idFactory;
    }

    /**
     * Returns the name of the object id attribute. The object id attribute is
     * used for referencing other objects in the XML file.
     * <p>
     * The default value is "oid".
     *
     * @return name of the object id attribute
     */
    @Override
    public String getObjectIdAttribute() {
        return objectIdAttribute;
    }

    /**
     * Sets the name of the object id attribute. The object id attribute is used
     * for referencing other objects in the XML file.
     *
     * @param newValue name of the object id attribute
     */
    public void setObjectIdAttribute(String newValue) {
        objectIdAttribute = newValue;
    }

    @Override
    public <T> boolean isDefaultValue(@NonNull Figure f, @NonNull MapAccessor<T> key, @Nullable T value) {
        FigureAccessorKey<T> k = new FigureAccessorKey<>(f.getClass(), key);
        T defaultValue;
        if (defaultValueMap.containsKey(k)) {
            @SuppressWarnings("unchecked")
            T suppress = defaultValue = (T) defaultValueMap.get(k);
        } else {
            defaultValue = key.getDefaultValue();
        }

        return defaultValue == null ? value == null : (value != null && defaultValue.equals(value));
    }

    @Override
    public String getElementNameByKey(@NonNull Figure f, MapAccessor<?> key) throws IOException {
        HashMap<MapAccessor<?>, String> keyToStr = null;
        if (keyToElem.containsKey(f.getClass())) {
            keyToStr = keyToElem.get(f.getClass());
        }
        if (keyToStr == null || !keyToStr.containsKey(key)) {
            throw new IOException("no mapping for key " + key + " in figure "
                    + f.getClass());
        }
        return keyToStr.get(key);
    }

    @Override
    public String getAttributeNameByKey(@NonNull Figure f, MapAccessor<?> key) throws IOException {
        HashMap<MapAccessor<?>, String> keyToStr = null;
        if (keyToAttr.containsKey(f.getClass())) {
            keyToStr = keyToAttr.get(f.getClass());
        }
        if (keyToStr == null || !keyToStr.containsKey(key)) {
            throw new IOException("no mapping for key " + key + " in figure "
                    + f.getClass());
        }
        return keyToStr.get(key);
    }

    @Override
    public @NonNull Figure createFigureByElementName(String elementName) throws IOException {
        Supplier<Figure> supplier = nameToFigure.get(elementName);
        if (supplier == null) {
            throw new IOException("no mapping for element " + elementName);
        }
        return supplier.get();
    }

    @Override
    public @Nullable MapAccessor<?> getKeyByAttributeName(@NonNull Figure f, String attributeName) throws IOException {
        HashMap<String, MapAccessor<?>> strToKey = attrToKey.get(f.getClass());
        if (strToKey == null || !strToKey.containsKey(attributeName)) {
            Set<Class<? extends Figure>> set = (skipAttributes.get(attributeName));
            if (set == null || !set.contains(f.getClass())) {
                LOGGER.warning("no mapping for attribute " + attributeName
                        + " in figure " + f.getClass());
                return null;
            }
        }
        return strToKey.get(attributeName);
    }

    @Override
    public @NonNull <T> T nodeListToValue(@NonNull MapAccessor<T> key, @NonNull List<Node> nodeList) throws IOException {
        if (key.getValueType() == String.class) {
            StringBuilder buf = new StringBuilder();
            for (Node node : nodeList) {
                if (node.getNodeType() == Node.TEXT_NODE) {
                    buf.append(node.getNodeValue());
                }
            }
            @SuppressWarnings("unchecked")
            T temp = (T) buf.toString();
            return temp;
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    /**
     * Globally removes the specified key.
     *
     * @param key the key
     */
    public void removeKey(MapAccessor<?> key) {
        for (Map.Entry<Class<? extends Figure>, HashMap<String, MapAccessor<?>>> entry : attrToKey.entrySet()) {
            for (Map.Entry<String, MapAccessor<?>> e : new ArrayList<>(entry.getValue().entrySet())) {
                if (e.getValue() == key) {
                    entry.getValue().remove(e.getKey());
                }
            }
        }
        for (Map.Entry<Class<? extends Figure>, HashMap<MapAccessor<?>, String>> entry : keyToAttr.entrySet()) {
            entry.getValue().remove(key);
        }
        for (Map.Entry<Class<? extends Figure>, HashMap<String, MapAccessor<?>>> entry : elemToKey.entrySet()) {
            for (Map.Entry<String, MapAccessor<?>> e : new ArrayList<>(entry.getValue().entrySet())) {
                if (e.getValue() == key) {
                    entry.getValue().remove(e.getKey());
                }
            }
        }
        for (Map.Entry<Class<? extends Figure>, HashMap<MapAccessor<?>, String>> entry : keyToElem.entrySet()) {
            entry.getValue().remove(key);
        }
        for (Map.Entry<Class<? extends Figure>, HashSet<MapAccessor<?>>> entry : figureAttributeKeys.entrySet()) {
            entry.getValue().remove(key);

        }
        for (Map.Entry<Class<? extends Figure>, HashSet<MapAccessor<?>>> entry : figureNodeListKeys.entrySet()) {
            entry.getValue().remove(key);
        }
    }

    @Override
    public <T> boolean needsIdResolver(MapAccessor<T> key) throws IOException {
        return getConverter(key).needsIdResolver();
    }

    @Override
    public <T> T stringToValue(@NonNull MapAccessor<T> key, @NonNull String string) throws IOException {
        try {
            Converter<T> converter = getConverter(key);
            return converter.fromString(string, idFactory);
        } catch (ParseException ex) {
            throw new IOException(ex + "\nstring: \"" + string + "\"", ex);
        }
    }

    protected @NonNull <T> Converter<T> getConverter(@NonNull MapAccessor<T> key) throws IOException {
        Converter<T> converter;
        final Converter<?> converterFromKey = keyValueFromXML.get(key);
        if (converterFromKey != null) {
            @SuppressWarnings("unchecked")
            Converter<T> suppress = converter = (Converter<T>) converterFromKey;
        } else {
            @SuppressWarnings("unchecked")
            Converter<T> suppress = converter = (Converter<T>) valueFromXML.get(key.getValueType());
        }
        if (converter == null) {
            throw new IOException("no converter for key \"" + key + "\" with attribute type "
                    + key.getValueType());
        }
        return converter;
    }

    @Override
    public void valueToNodeList(@NonNull MapAccessor<?> key, Object value, @NonNull XMLStreamWriter w) throws IOException, XMLStreamException {
        if (key.getValueType() == String.class) {
            w.writeCharacters((String) value);
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public @NonNull <T> String valueToString(@NonNull MapAccessor<T> key, T value) throws IOException {
        Converter<T> converter = getConverter(key);
        StringBuilder builder = new StringBuilder();
        converter.toString(builder, idFactory, value);
        return builder.toString();
    }

    private static final class FigureAccessorKey<T> {
        private final Class<? extends Figure> figure;
        private final MapAccessor<T> acc;

        private FigureAccessorKey(Class<? extends Figure> figure,
                                  MapAccessor<T> acc) {
            this.figure = figure;
            this.acc = acc;
        }

        public Class<? extends Figure> figure() {
            return figure;
        }

        public MapAccessor<T> acc() {
            return acc;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            FigureAccessorKey that = (FigureAccessorKey) obj;
            return Objects.equals(this.figure, that.figure) &&
                    Objects.equals(this.acc, that.acc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(figure, acc);
        }

        @Override
        public String toString() {
            return "FigureAccessorKey[" +
                    "figure=" + figure + ", " +
                    "acc=" + acc + ']';
        }
    }
}
