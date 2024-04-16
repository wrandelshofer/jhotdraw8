/*
 * @(#)SplitPaneTrack.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxbase.binding.CustomBinding;

import static javafx.geometry.Orientation.VERTICAL;


public class SplitPaneTrack
        extends AbstractDockParent implements Track {
    private final @NonNull SplitPane splitPane = new SplitPane();

    public SplitPaneTrack(Orientation orientation) {
        splitPane.setOrientation(orientation);
        getStyleClass().add("track");
        getChildren().add(splitPane);
        CustomBinding.bindContent(splitPane.getItems(), getDockChildren(),
                DockNode::getNode);
        CustomBinding.bindElements(getDockChildren(), DockChild::showingProperty, showingProperty());
    }


    @Override
    public @NonNull TrackAxis getDockAxis() {
        return splitPane.getOrientation() == Orientation.HORIZONTAL ? TrackAxis.X : TrackAxis.Y;
    }


    @Override
    public boolean isResizesDockChildren() {
        return true;
    }

    public static @NonNull SplitPaneTrack createVerticalTrack() {
        return new SplitPaneTrack(VERTICAL);
    }

    public static @NonNull SplitPaneTrack createHorizontalTrack() {
        return new SplitPaneTrack(Orientation.HORIZONTAL);
    }
}
