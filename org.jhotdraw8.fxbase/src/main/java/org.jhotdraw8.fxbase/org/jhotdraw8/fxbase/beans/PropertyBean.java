/*
 * @(#)PropertyBean.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxcollection.MapEntryProperty;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

import java.util.Objects;

/**
 * Interface for beans which support an open number of properties in a
 * property map.
 * <p>
 * A property can be accessed using a {@link Key}. The type parameter
 * of the key is used to ensure that property accesses are type safe.
 * </p>
 * <p>
 * To implement this interface, you need to implement method
 * {@link #getProperties()} as shown below.
 * </p>
 *
 * <pre><code>{@literal
 * public class MyBean implements PropertyBean {
 *      protected final ObservableMap<Key<?>, Object> properties = FXCollections.observableMap(new LinkedHashMap<>());
 *
 *     {@literal @}Override
 *     public ObservableMap{@literal <Key<?>, Object>} getProperties() {
 *        return properties;
 *      }
 * }
 * }</code></pre>
 *
 * @author Werner Randelshofer
 */
public interface PropertyBean {

    // ---
    // Properties
    // ---

    /**
     * Returns an observable map of property keys and their values.
     *
     * @return the map
     */
    @NonNull ObservableMap<Key<?>, Object> getProperties();

    default @NonNull <T> ObjectProperty<T> getProperty(@NonNull Key<T> key) {
        return new MapEntryProperty<>(getProperties(), key, key.getValueType());
    }

    // ---
    // convenience methods
    // ---

    /**
     * Sets a property value.
     *
     * @param <T>      the value type
     * @param key      the key
     * @param newValue the value
     */
    default <T> void set(@NonNull MapAccessor<T> key, @Nullable T newValue) {
        key.set(getProperties(), newValue);
    }

    /**
     * Sets a non-null property value.
     *
     * @param <T>      the value type
     * @param key      the key
     * @param newValue the value
     */
    default <T> void setNonNull(@NonNull NonNullMapAccessor<T> key, @NonNull T newValue) {
        key.set(getProperties(), newValue);
    }

    /**
     * Puts a property value.
     *
     * @param <T>      the value type
     * @param key      the key
     * @param newValue the value
     * @return the old value
     */
    default @Nullable <T> T put(@NonNull MapAccessor<T> key, @Nullable T newValue) {
        return key.put(getProperties(), newValue);
    }

    /**
     * Gets a property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    default @Nullable <T> T get(@NonNull MapAccessor<T> key) {
        return key.get(getProperties());
    }

    /**
     * Gets a nonnull property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    default @NonNull <T> T getNonNull(@NonNull NonNullMapAccessor<T> key) {
        T value = key.get(getProperties());
        return Objects.requireNonNull(value, "value is null for key=" + key);
    }

    /**
     * Removes a property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the removed value
     */
    default @Nullable <T> T remove(Key<T> key) {
        return key.getRawValueType().cast(getProperties().remove(key));
    }

    @SuppressWarnings("unchecked")
    default @NonNull <T> ObservableValue<T> valueAt(Key<T> key) {
        return (ObservableValue<T>) Bindings.valueAt(getProperties(), key);
    }
}
