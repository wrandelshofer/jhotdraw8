/* @(#)CMYKGenericColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color.tmp;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;

/**
 * A {@code ColorSpace} for CMYK color components using a generic CMYK profile.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class CmykGenericColorSpace extends ICC_ColorSpace {
    private static final long serialVersionUID = 1L;

    private static @Nullable NamedColorSpace instance;

    public static @NonNull NamedColorSpace getInstance() {
        if (instance == null) {
            try {
                instance = new NamedColorSpaceAdapter("CMYK generic", new CmykGenericColorSpace());
            } catch (IOException ex) {
                InternalError error = new InternalError("Can't instanciate CMYKColorSpace");
                error.initCause(ex);
                throw error;
            }
        }
        return instance;
    }

    public CmykGenericColorSpace() throws IOException {
        super(ICC_Profile.getInstance(CmykGenericColorSpace.class.getResourceAsStream("Generic CMYK Profile.icc")));
    }
}
