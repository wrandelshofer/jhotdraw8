/*
 * @(#)StrokeStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.StrokeStyleCssConverter;
import org.jhotdraw8.draw.css.value.CssStrokeStyle;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.util.Map;

/**
 * Stroke Style combines all stroke attributes.
 *
 * @author Werner Randelshofer
 */
public class StrokeStyleableMapAccessor extends AbstractStyleableMapAccessor<CssStrokeStyle> {


    private final @NonNull MapAccessor<CssSize> dashOffsetKey;
    private final @NonNull MapAccessor<ImmutableList<CssSize>> dashArrayKey;
    private final @NonNull MapAccessor<StrokeType> typeKey;
    private final @NonNull MapAccessor<StrokeLineJoin> lineJoinKey;
    private final @NonNull MapAccessor<StrokeLineCap> lineCapKey;
    private final @NonNull MapAccessor<CssSize> miterLimitKey;
    private final @NonNull Converter<CssStrokeStyle> converter = new StrokeStyleCssConverter(false);

    public StrokeStyleableMapAccessor(String name,
                                      @NonNull MapAccessor<StrokeType> typeKey,
                                      @NonNull MapAccessor<StrokeLineCap> lineCapKey,
                                      @NonNull MapAccessor<StrokeLineJoin> lineJoinKey,
                                      @NonNull MapAccessor<CssSize> miterLimitKey,
                                      @NonNull MapAccessor<CssSize> dashOffsetKey,
                                      @NonNull MapAccessor<ImmutableList<CssSize>> dashArrayKey
    ) {
        super(name, CssStrokeStyle.class, new MapAccessor<?>[]{
                        typeKey,
                        lineJoinKey,
                        lineCapKey,
                        miterLimitKey,
                        dashOffsetKey,
                        dashArrayKey
                },
                new CssStrokeStyle(
                        typeKey.getDefaultValue(), lineCapKey.getDefaultValue(), lineJoinKey.getDefaultValue(),
                        miterLimitKey.getDefaultValue(),
                        dashOffsetKey.getDefaultValue(),
                        dashArrayKey.getDefaultValue()
                ));

        this.dashOffsetKey = dashOffsetKey;
        this.dashArrayKey = dashArrayKey;
        this.typeKey = typeKey;
        this.lineJoinKey = lineJoinKey;
        this.lineCapKey = lineCapKey;
        this.miterLimitKey = miterLimitKey;
    }

    @Override
    public CssStrokeStyle get(@NonNull Map<? super Key<?>, Object> a) {
        return new CssStrokeStyle(
                typeKey.get(a),
                lineCapKey.get(a),
                lineJoinKey.get(a),
                miterLimitKey.get(a),
                dashOffsetKey.get(a),
                dashArrayKey.get(a)
        );
    }

    @Override
    public @NonNull Converter<CssStrokeStyle> getCssConverter() {
        return converter;
    }

    /**
     * This is a non-standard map composite map accessor and thus it is transient.
     * We only used in the GUI to get a more concise presentation of attributes.
     *
     * @return true
     */
    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public CssStrokeStyle remove(@NonNull Map<? super Key<?>, Object> a) {
        CssStrokeStyle oldValue = get(a);
        typeKey.remove(a);
        lineJoinKey.remove(a);
        lineCapKey.remove(a);
        miterLimitKey.remove(a);
        dashOffsetKey.remove(a);
        dashArrayKey.remove(a);
        return oldValue;
    }

    @Override
    public void set(@NonNull Map<? super Key<?>, Object> a, @Nullable CssStrokeStyle value) {
        if (value == null) {
            remove(a);
        } else {
            dashOffsetKey.put(a, value.getDashOffset());
            dashArrayKey.put(a, value.getDashArray());
            typeKey.put(a, value.getType());
            lineJoinKey.put(a, value.getLineJoin());
            lineCapKey.put(a, value.getLineCap());
            miterLimitKey.put(a, value.getMiterLimit());
        }
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> put(@NonNull ImmutableMap<Key<?>, Object> a, @Nullable CssStrokeStyle value) {
        if (value == null) {
            return remove(a);
        } else {
            a = dashOffsetKey.put(a, value.getDashOffset());
            a = dashArrayKey.put(a, value.getDashArray());
            a = typeKey.put(a, value.getType());
            a = lineJoinKey.put(a, value.getLineJoin());
            a = lineCapKey.put(a, value.getLineCap());
            return miterLimitKey.put(a, value.getMiterLimit());
        }
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> remove(@NonNull ImmutableMap<Key<?>, Object> a) {
        a = typeKey.remove(a);
        a = lineJoinKey.remove(a);
        a = lineCapKey.remove(a);
        a = miterLimitKey.remove(a);
        a = dashOffsetKey.remove(a);
        return dashArrayKey.remove(a);
    }
}
