/*
 * @(#)DockingFrameworkExampleMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.spliterator.SpliteratorIterable;
import org.jhotdraw8.fxbase.tree.PreorderSpliterator;
import org.jhotdraw8.fxcontrols.dock.DockChild;
import org.jhotdraw8.fxcontrols.dock.DockNode;
import org.jhotdraw8.fxcontrols.dock.DockParent;
import org.jhotdraw8.fxcontrols.dock.DockRoot;
import org.jhotdraw8.fxcontrols.dock.SimpleDockRoot;
import org.jhotdraw8.fxcontrols.dock.SimpleDockable;
import org.jhotdraw8.fxcontrols.dock.TabPaneTrack;
import org.jhotdraw8.fxcontrols.dock.TabbedAccordionTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DockingFrameworkExampleMain extends Application {

    public @NonNull DockRoot initStage(String title,
                                       @NonNull Stage primaryStage) {
        SimpleDockRoot root = new SimpleDockRoot();
        root.setZSupplier(TabbedAccordionTrack::new);
        Scene scene = new Scene(root.getNode(), 300, 250);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
        return root;
    }

    @Override
    public void start(@NonNull Stage primaryStage) {


        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e));

        List<DockRoot> roots = new ArrayList<>();
        DockRoot root = initStage("DockRoot initially empty", primaryStage);
        roots.add(root);

        root = initStage("DockRoot initially 3 tabs", new Stage());
        roots.add(root);
        TabPaneTrack zComp = new TabPaneTrack();

        zComp.getDockChildren().add(new SimpleDockable("Label 1", new Label("The quick brown fox\njumps over the lazy dog\n1")));
        zComp.getDockChildren().add(new SimpleDockable("Label 2", new Label("The quick brown fox\njumps over the lazy dog\n2")));
        zComp.getDockChildren().add(new SimpleDockable("Label 3", new Label("The quick brown fox\njumps over the lazy dog\n3")));
        root.getDockChildren().add(zComp);

        root = initStage("DockRoot initially 3 tabs", new Stage());
        roots.add(root);
        zComp = new TabPaneTrack();
        zComp.getDockChildren().add(new SimpleDockable("Label 4", new Label("The quick brown fox\njumps over the lazy dog\n4")));
        zComp.getDockChildren().add(new SimpleDockable("Label 5", new Label("The quick brown fox\njumps over the lazy dog\n5")));
        zComp.getDockChildren().add(new SimpleDockable("Label 6", new Label("The quick brown fox\njumps over the lazy dog\n6")));
        root.getDockChildren().add(zComp);


        root = initStage("DockRoot initially central view", new Stage());
        roots.add(root);
        final TextArea textArea = new TextArea();
        textArea.setText("This is a text area\nin a dock leaf directly added to the Dock.");
        SimpleDockable leaf = new SimpleDockable(textArea);
        root.getDockChildren().add(leaf);
        //dp.getVerticalTrackFactoryMap().put(SingleItemDock.class, () -> new SplitPaneTrack(Orientation.VERTICAL));


        // Dump all roots after a change for debugging.
        boolean[] willDumpRoots = new boolean[1];
        Runnable dumpRoots = () -> {
            willDumpRoots[0] = false;
            System.out.println("-------");
            for (DockRoot r : roots) {
                for (DockChild dockChild : new SpliteratorIterable<>(() -> new PreorderSpliterator<>(DockNode::getDockChildrenReadOnly, r))) {
                    for (DockParent dockParent = dockChild.getDockParent(); dockParent != null; dockParent = dockParent.getDockParent()) {
                        System.out.print('.');
                    }
                    System.out.println(dockChild);
                }
            }

        };
        InvalidationListener invalidationListener = observable -> {
            if (!willDumpRoots[0]) {
                willDumpRoots[0] = true;
                Platform.runLater(dumpRoots);
            }
        };
        for (DockRoot r : roots) {
            for (DockChild dockChild : new SpliteratorIterable<>(() -> new PreorderSpliterator<>(DockNode::getDockChildrenReadOnly, r))) {
                dockChild.dockParentProperty().addListener(invalidationListener);
            }
        }


    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
