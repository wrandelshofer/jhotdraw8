/*
 * @(#)ColorSpaceUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;


import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * A utility class for {@code ColorSpace} objects.
 *
 * @author Werner Randelshofer

 */
public class ColorSpaceUtil {

    private static final float EPSILON = 1f / 512;
    private static final ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /**
     * Don't let anyone instantiate this class.
     */
    private ColorSpaceUtil() {
    }


    /**
     * Returns the name of the color space. If the color space is an
     * {@code ICC_ColorSpace} the name is retrieved from the "desc" data element
     * of the color profile.
     *
     * @param a A ColorSpace.
     * @return The name.
     */
    public static String getName(ColorSpace a) {
        if (a instanceof NamedColorSpace) {
            return ((NamedColorSpace) a).getName();
        }
        if ((a instanceof ICC_ColorSpace)) {
            ICC_ColorSpace icc = (ICC_ColorSpace) a;
            ICC_Profile p = icc.getProfile();
            // Get the name from the profile description tag
            byte[] desc = p.getData(ICC_Profile.icSigProfileDescriptionTag);
            if (desc != null) {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(desc));
                try {
                    int magic = in.readInt();
                    int reserved = in.readInt();
                    if (magic != 0x64657363) {
                        throw new IOException("Illegal magic:" + Integer.toHexString(magic));
                    }
                    if (reserved != 0x0) {
                        throw new IOException("Illegal reserved:" + Integer.toHexString(reserved));
                    }
                    long nameLength = in.readInt() & 0xffffffffL;
                    StringBuilder buf = new StringBuilder();
                    for (int i = 0; i < nameLength - 1; i++) {
                        buf.append((char) in.readUnsignedByte());
                    }
                    return buf.toString();
                } catch (IOException e) {
                    // fall back
                    e.printStackTrace();
                }
            }
        }

        if (a instanceof ICC_ColorSpace) {
            // Fall back if no description is available
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < a.getNumComponents(); i++) {
                if (buf.length() > 0) {
                    buf.append("-");
                }
                buf.append(a.getName(i));
            }
            return buf.toString();
        } else {
            return a.getClass().getSimpleName();
        }
    }


}
