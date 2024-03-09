/*
 * @(#)DrawStylesheets.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw;

import java.net.URL;
import java.util.MissingResourceException;

public class DrawStylesheets {
    private DrawStylesheets() {
    }

    public static String getInspectorsStylesheet() {
        String name = "/org/jhotdraw8/draw/inspector/inspector.css";
        URL resource = DrawStylesheets.class.getResource(name);
        if (resource == null) {
            throw new MissingResourceException("Could not find a resource with name=\"" + name + "\".",
                    DrawStylesheets.class.getName(),
                    name);
        }
        return resource.toString();
    }
}
