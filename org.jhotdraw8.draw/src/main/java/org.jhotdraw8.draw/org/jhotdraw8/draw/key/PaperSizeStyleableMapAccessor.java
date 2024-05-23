/*
 * @(#)PaperSizeStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.PaperSizeCssConverter;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * CssSize2DStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class PaperSizeStyleableMapAccessor extends AbstractStyleableMapAccessor<CssDimension2D> {


    private final Converter<CssDimension2D> converter = new PaperSizeCssConverter();

    private final NonNullMapAccessor<CssSize> widthKey;
    private final NonNullMapAccessor<CssSize> heightKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param widthKey  the key for the x coordinate of the point
     * @param heightKey the key for the y coordinate of the point
     */
    public PaperSizeStyleableMapAccessor(String name, NonNullMapAccessor<CssSize> widthKey, NonNullMapAccessor<CssSize> heightKey) {
        super(name, CssDimension2D.class, new MapAccessor<?>[]{widthKey, heightKey}, new CssDimension2D(widthKey.getDefaultValue(), heightKey.getDefaultValue()));

        this.widthKey = widthKey;
        this.heightKey = heightKey;
    }

    @Override
    public CssDimension2D get(Map<? super Key<?>, Object> a) {
        return new CssDimension2D(widthKey.getNonNull(a), heightKey.getNonNull(a));
    }


    @Override
    public Converter<CssDimension2D> getCssConverter() {
        return converter;
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable CssDimension2D value) {
        if (value == null) {
            remove(a);
        } else {
            widthKey.put(a, value.getWidth());
            heightKey.put(a, value.getHeight());
        }
    }

    @Override
    public CssDimension2D remove(Map<? super Key<?>, Object> a) {
        CssDimension2D oldValue = get(a);
        widthKey.remove(a);
        heightKey.remove(a);
        return oldValue;
    }

    @Override
    public ImmutableMap<Key<?>, Object> put(ImmutableMap<Key<?>, Object> a, @Nullable CssDimension2D value) {
        if (value == null) {
            return remove(a);
        } else {
            a = widthKey.put(a, value.getWidth());
            return heightKey.put(a, value.getHeight());
        }
    }

    @Override
    public ImmutableMap<Key<?>, Object> remove(ImmutableMap<Key<?>, Object> a) {
        a = widthKey.remove(a);
        return heightKey.remove(a);
    }
}
