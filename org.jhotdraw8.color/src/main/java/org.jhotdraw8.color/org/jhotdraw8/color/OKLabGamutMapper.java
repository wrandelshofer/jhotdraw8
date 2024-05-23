/*
 * @(#)OKLabGamutMapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;


/**
 * OKLab Gamut Mapper.
 * <p>
 * References:
 * <dl>
 *     <dt>Björn Ottoson. sRGB gamut clipping.
 *     <a href="https://github.com/bottosson/bottosson.github.io/blob/3d3f17644d7f346e1ce1ca08eb8b01782eea97af/misc/colorpicker/License.txt">MIT License</a></dt>
 *     <dd><a href="https://bottosson.github.io/posts/gamutclipping/">github.io</a></dd>
 * </dl>
 */
public class OKLabGamutMapper implements GamutMapper {
    @Override
    public float[] map(float[] value, float[] mapped) {
        return new float[0];
    }
}
