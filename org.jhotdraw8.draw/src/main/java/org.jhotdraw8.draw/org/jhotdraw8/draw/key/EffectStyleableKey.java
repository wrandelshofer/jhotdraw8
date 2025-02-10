/*
 * @(#)EffectStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.effect.Effect;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.EffectCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentList;

/**
 * EffectStyleableKey.
 *
 */
public class EffectStyleableKey extends AbstractStyleableKey<Effect> implements WritableStyleableMapAccessor<Effect> {

    private static final long serialVersionUID = 1L;
    private final EffectCssConverter converter = new EffectCssConverter();

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public EffectStyleableKey(String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public EffectStyleableKey(String name, Effect defaultValue) {
        super(name, Effect.class, defaultValue);
    }

    @Override
    public Converter<Effect> getCssConverter() {
        return converter;
    }

    @Override
    public PersistentList<String> getExamples() {
        return converter.getExamples();
    }
}
