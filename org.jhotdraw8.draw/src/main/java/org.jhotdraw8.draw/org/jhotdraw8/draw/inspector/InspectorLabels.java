/*
 * @(#)InspectorLabels.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import org.jhotdraw8.application.resources.Resources;

/**
 * InspectorLabels.
 *
 * @author Werner Randelshofer
 */
public class InspectorLabels {

    public static final String RESOURCE_BUNDLE = "org.jhotdraw8.draw.inspector.Labels";

    /**
     * Don't let anyone instantiate this class.
     */
    private InspectorLabels() {
    }

    public static Resources getResources() {
        return Resources.getResources("org.jhotdraw8.draw", RESOURCE_BUNDLE);
    }
}
