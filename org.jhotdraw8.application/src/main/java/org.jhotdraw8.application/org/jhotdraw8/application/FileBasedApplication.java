/*
 * @(#)FileBasedApplication.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;

/**
 * A {@code FileBasedApplication} handles the life-cycle of {@link FileBasedActivity} objects and
 * provides windows to present them on screen.
 *
 * @author Werner Randelshofer
 */
public interface FileBasedApplication extends Application {
    @NonNull
    NonNullKey<Boolean> ALLOW_MULTIPLE_ACTIVITIES_WITH_SAME_URI = new NonNullObjectKey<Boolean>("allowMultipleActivitiesWithSameURI", Boolean.class,
            Boolean.FALSE);
}
