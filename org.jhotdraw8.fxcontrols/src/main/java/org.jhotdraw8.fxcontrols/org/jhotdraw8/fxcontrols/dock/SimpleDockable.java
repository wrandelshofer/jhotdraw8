/*
 * @(#)SimpleDockable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.scene.Node;
import javafx.scene.text.Text;
import org.jhotdraw8.annotation.NonNull;

public class SimpleDockable extends AbstractDockable {
    private final @NonNull Node node;

    public SimpleDockable(Node content) {
        this(null, content);
    }

    public SimpleDockable(String text, Node content) {
        this.node = content;
        setText(text);
        final Text textualIcon = new Text("❏");
        setGraphic(textualIcon);
    }


    @Override
    public @NonNull Node getNode() {
        return node;
    }


}
