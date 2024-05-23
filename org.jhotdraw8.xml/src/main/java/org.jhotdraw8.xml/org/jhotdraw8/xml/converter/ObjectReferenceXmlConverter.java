/*
 * @(#)XmlObjectReferenceConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.logging.Logger;

/**
 * ObjectReferenceV2XmlConverter.
 * <p>
 * Converts references to figures.
 *
 * @param <T> the type
 * @author Werner Randelshofer
 */
public class ObjectReferenceXmlConverter<T> implements Converter<T> {

    private static final Logger LOGGER = Logger.getLogger(ObjectReferenceXmlConverter.class.getName());
    private final Class<T> clazz;

    /**
     * Creates a new instance
     *
     * @param clazz the type class
     * @throws IllegalArgumentException if clazz is null
     */
    public ObjectReferenceXmlConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public <TT extends T> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (idSupplier == null) {
            throw new IllegalArgumentException("IdSupplier is required for this converter");
        }
        out.append(value == null ? "none" : idSupplier.getId(value));
    }

    @Override
    public @Nullable T fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        return fromString(buf.toString(), idResolver);
    }

    @Override
    public @Nullable T fromString(@Nullable CharSequence buf, @Nullable IdResolver idResolver) {
        if (idResolver == null) {
            throw new IllegalArgumentException("IdResolver is required for this converter");
        }
        String str = buf == null ? "none" : buf.toString();
        if ("none".equals(str)) {
            return null;
        }
        Object obj = idResolver.getObject(str);
        @SuppressWarnings("unchecked")
        T value = clazz.isInstance(obj) ? (T) obj : null;
        if (value == null) {
            LOGGER.warning("Could not find an object with id=\"" + str + "\".");
        }
        return value;
    }

    @Override
    public @Nullable T getDefaultValue() {
        return null;
    }

    @Override
    public boolean needsIdResolver() {
        return true;
    }

}
