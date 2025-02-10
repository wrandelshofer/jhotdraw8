/*
 * @(#)FontStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.FontCssConverter;
import org.jhotdraw8.draw.css.value.CssFont;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * FontStyleableMapAccessor.
 *
 */
public class FontStyleableMapAccessor extends AbstractStyleableMapAccessor<CssFont>
        implements NonNullMapAccessor<CssFont> {


    private final MapAccessor<String> familyKey;
    private final MapAccessor<FontWeight> weightKey;
    private final MapAccessor<FontPosture> postureKey;
    private final MapAccessor<CssSize> sizeKey;
    private final Converter<CssFont> converter = new FontCssConverter(false);

    /**
     * Creates a new instance with the specified name.
     *
     * @param name       the name of the accessor
     * @param familyKey  the font family key
     * @param weightKey  the font weight key
     * @param postureKey the font posture key
     * @param sizeKey    the font size key
     */
    public FontStyleableMapAccessor(String name,
                                    MapAccessor<String> familyKey, MapAccessor<FontWeight> weightKey,
                                    MapAccessor<FontPosture> postureKey, MapAccessor<CssSize> sizeKey) {
        super(name, CssFont.class, new MapAccessor<?>[]{familyKey, sizeKey, weightKey, postureKey},
                CssFont.font(familyKey.getDefaultValue(), weightKey.getDefaultValue(), postureKey.getDefaultValue(),
                        sizeKey.getDefaultValue()));

        this.familyKey = familyKey;
        this.sizeKey = sizeKey;
        this.weightKey = weightKey;
        this.postureKey = postureKey;
    }

    @Override
    public CssFont get(Map<? super Key<?>, Object> a) {
        CssFont f = CssFont.font(familyKey.get(a), weightKey.get(a), postureKey.get(a), sizeKey.get(a));
        return f;
    }

    @Override
    public Converter<CssFont> getCssConverter() {
        return converter;
    }

    @Override
    public CssFont remove(Map<? super Key<?>, Object> a) {
        CssFont oldValue = get(a);
        familyKey.remove(a);
        weightKey.remove(a);
        postureKey.remove(a);
        sizeKey.remove(a);
        return oldValue;
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable CssFont value) {
        if (value == null) {
            remove(a);
        } else {
            familyKey.put(a, value.getFamily());
            weightKey.put(a, value.getWeight());
            postureKey.put(a, value.getPosture());
            sizeKey.put(a, value.getSize());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, CssFont value) {
        if (value == null) {
            return remove(a);
        } else {
            a = familyKey.put(a, value.getFamily());
            a = weightKey.put(a, value.getWeight());
            a = postureKey.put(a, value.getPosture());
            return sizeKey.put(a, value.getSize());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        a = familyKey.remove(a);
        a = weightKey.remove(a);
        a = postureKey.remove(a);
        return sizeKey.remove(a);
    }
}
