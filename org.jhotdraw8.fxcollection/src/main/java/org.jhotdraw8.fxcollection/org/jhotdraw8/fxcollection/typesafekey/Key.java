/*
 * @(#)Key.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.typesafekey;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Objects;

/**
 * An <em>name</em> which provides typesafe access to a map entry.
 * <p>
 * A Key has a name, a type and a default value.
 * <p>
 * A key is identified by its identity. The hashcode and equals are based on
 * the identity of the key.
 * <p>
 * The following code example shows how to set and get a value from a map.
 * <pre>
 * {@code
 * String value = "Werner";
 * Key<String> stringKey = new Key("name",String.class,null);
 * Map<Key<?>,Object> map = new HashMap<>();
 * stringKey.put(map, value);
 * }
 * </pre>
 * <p>
 * Note that {@code Key} is not a value type. Thus using two distinct instances
 * of a Key will result in two distinct entries in the hash map, even if both
 * keys have the same name.
 *
 * @author Werner Randelshofer
 * @param <T> the value type
 */
public interface Key<T> extends MapAccessor<T> {

    long serialVersionUID = 1L;

    @Override
    default boolean containsKey(Map<Key<?>, Object> map) {
        return map.containsKey(this);
    }

    /**
     * Gets the value of the attribute denoted by this Key from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    @Override
    default @Nullable T get(Map<? super Key<?>, Object> a) {
        // Performance: explicit cast is nice, but is very slow
        //return getRawValueType().cast(a.getOrDefault(this, getDefaultValue()));
        @SuppressWarnings("unchecked")
        T result = (T) a.getOrDefault(this, getDefaultValue());
        return result;
    }

    /**
     * Gets the value of the attribute denoted by this Key from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    @Override
    default @Nullable T get(ReadableMap<? super Key<?>, Object> a) {
        // Performance: explicit cast is nice, but is very slow
        //return getRawValueType().cast(a.getOrDefault(this, getDefaultValue()));
        @SuppressWarnings("unchecked")
        T result = (T) a.getOrDefault(this, getDefaultValue());
        return result;
    }

    /**
     * Gets the value of the attribute denoted by this Key from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    default ObjectProperty<T> getValueProperty(Map<? super Key<?>, ObjectProperty<?>> a) {
        if (!a.containsKey(this)) {
            a.put(this, new SimpleObjectProperty<>(getDefaultValue()));
        }
        @SuppressWarnings("unchecked")
        SimpleObjectProperty<T> value = (SimpleObjectProperty<T>) a.get(this);
        return value;
    }

    /**
     * Gets the value of the attribute denoted by this Key from a Map.
     *
     * @param a A Map.
     * @return The value of the attribute.
     */
    default @Nullable T getValue(Map<? super Key<?>, ObjectProperty<?>> a) {
        if (!a.containsKey(this)) {
            a.put(this, new SimpleObjectProperty<>(getDefaultValue()));
        }
        @SuppressWarnings("unchecked")
        SimpleObjectProperty<T> value = (SimpleObjectProperty<T>) a.get(this);
        return value.get();
    }

    /**
     * Use this method to perform a type-safe put operation of an attribute into
     * a Map.
     *
     * @param a     An attribute map.
     * @param value The new value.
     * @return The old value.
     */
    @Override
    default @Nullable T put(Map<? super Key<?>, Object> a, @Nullable T value) {
        if (a.containsKey(this)) {
            return getRawValueType().cast(a.put(this, value));
        } else {
            a.put(this, value);
            return getDefaultValue();
        }
    }

    /**
     * Use this method to perform a type-safe put operation of an attribute into
     * a Map.
     *
     * @param a     An attribute map.
     * @param value The new value.
     * @return The updated map
     */
    @Override
    default PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable T value) {
        return a.put(this, value);
    }

    /**
     * Use this method to perform a type-safe remove operation of an attribute
     * on a Map.
     *
     * @param a An attribute map.
     * @return The old value.
     */
    @Override
    default @Nullable T remove(Map<? super Key<?>, Object> a) {
        if (a.containsKey(this)) {
            return getRawValueType().cast(a.remove(this));
        } else {
            return getDefaultValue();
        }
    }

    /**
     * Use this method to perform a type-safe remove operation of an attribute
     * on a Map.
     *
     * @param a An attribute map.
     * @return The old value.
     */
    @Override
    default PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        return a.remove(this);
    }

    /**
     * Use this method to perform a type-safe put operation of an attribute into
     * a Map.
     *
     * @param a     An attribute map.
     * @param value The new value.
     * @return The old value.
     */
    default @Nullable T putValue(Map<? super Key<?>, ObjectProperty<?>> a, @Nullable T value) {
        if (!isAssignable(value)) {
            throw new IllegalArgumentException("Value is not assignable to key. key="
                    + this + ", value=" + value);
        }
        if (a.containsKey(this)) {
            @SuppressWarnings("unchecked")
            ObjectProperty<T> p = (ObjectProperty<T>) a.get(this);
            T oldValue = p.get();
            p.set(value);
            return oldValue;
        } else {
            a.put(this, new SimpleObjectProperty<>(value));
            return null;
        }
    }

    /**
     * Whether the value may be set to null.
     *
     * @return true if nullable
     */
    boolean isNullable();

    /**
     * Returns true if the specified value is assignable with this key.
     *
     * @param value The object to be verified for assignability.
     * @return True if assignable.
     */
    default boolean isAssignable(@Nullable Object value) {
        if (getValueType() instanceof Class<?> clazz) {
            return value == null && isNullable() || clazz.isInstance(value);
        }
        if (getValueType() instanceof ParameterizedType pt) {
            if (pt.getRawType() instanceof Class<?> clazz) {
                return value == null && isNullable() || clazz.isInstance(value);
            }
        }
        // We cannot check if the value type is assignable.
        return value == null && isNullable();
    }

    /**
     * Returns true if the specified value is the default value of this key.
     *
     * @param value The object to be verified for assignability.
     * @return True if assignable.
     */
    default boolean isDefault(@Nullable Object value) {
        return Objects.equals(getDefaultValue(), value);
    }

    /**
     * Creates a new binding for the map entry specified by this key.
     *
     * @param map a map
     * @return a binding for the map entry
     */
    default Binding<T> valueAt(ObservableMap<Key<?>, Object> map) {
        ObjectBinding<Object> value = Bindings.valueAt(map, this);
        @SuppressWarnings("unchecked")
        Binding<T> binding = (ObjectBinding<T>) value;
        return binding;
    }

    /**
     * Creates a new property for the map entry specified by this key.
     *
     * @param map a map
     * @return a property for the map entry
     */
    default ObjectProperty<T> propertyAt(final ObservableMap<Key<?>, Object> map) {
        return new KeyMapEntryProperty<>(map, this);
    }

    /**
     * Creates a new read-only property for the map entry specified by this key.
     *
     * @param map a map
     * @return a property for the map entry
     */
    default ReadOnlyProperty<T> readOnlyPropertyAt(final ObservableMap<Key<?>, Object> map) {
        return new KeyMapEntryProperty<>(map, this);
    }

    default T cast(Object value) {
        return getRawValueType().cast(value);
    }
}
