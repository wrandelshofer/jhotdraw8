/*
 * @(#)OKLabGamutMapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

/**
 * OKLab Gamut Mapper.
 * <p>
 * References:
 * <dl>
 *     <dt>Björn Ottoson. sRGB gamut clipping.</dt>
 *     <dd><a href="https://bottosson.github.io/posts/gamutclipping/">github.io</dd>
 * </dl>
 */
public class OKLabGamutMapper implements GamutMapper {
    @Override
    public float[] map(@NonNull float[] value, @NonNull float[] mapped) {
        return new float[0];
    }
}
