/*
 * @(#)SimpleDockRoot.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jhotdraw8.fxbase.binding.CustomBinding;
import org.jhotdraw8.fxbase.transition.RectangleTransition;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.function.Supplier;

/**
 * A simple implementation of the {@link DockRoot} interface.
 * <p>
 * This DockPane only shows the first child dock.
 * <p>
 * FIXME DockPane should allow to select which child that it shows, like a card pane.
 */
public class SimpleDockRoot
        extends AbstractDockRoot {

    private static final Insets rootDrawnDropZoneInsets = new Insets(10);
    private static final Insets dockSensedDropZoneInsets = new Insets(30);
    private static final Insets rootSensedDropZoneInsets = new Insets(20);
    private static final Insets dockDrawnDropZoneInsets = new Insets(20);
    private final Rectangle dropRect = new Rectangle(0, 0, 0, 0);
    private final BorderPane contentPane = new BorderPane();
    private @Nullable RectangleTransition transition;
    private Supplier<Track> rootXSupplier = () -> new SplitPaneTrack(Orientation.HORIZONTAL);
    private Supplier<Track> rootYSupplier = () -> new SplitPaneTrack(Orientation.VERTICAL);
    private Supplier<Track> subXSupplier = HBoxTrack::new;
    private Supplier<Track> subYSupplier = VBoxTrack::new;
    private Supplier<Track> zSupplier = TabPaneTrack::new;

    public SimpleDockRoot() {
        getChildren().add(contentPane);
        dropRect.setOpacity(0.4);
        dropRect.setManaged(false);
        dropRect.setMouseTransparent(true);
        dropRect.setVisible(false);

        dockChildren.addListener(this::onRootChanged);
        CustomBinding.bindElements(getDockChildren(), DockChild::showingProperty, showingProperty());
        showingProperty().bind(sceneProperty().isNotNull());

        setOnDragOver(this::onDragOver);
        setOnDragExited(this::onDragExit);
        setOnDragDropped(this::onDragDrop);
    }

    private static Bounds subtractInsets(Bounds b, Insets i) {
        return new BoundingBox(
                b.getMinX() + i.getLeft(),
                b.getMinY() + i.getTop(),
                b.getWidth() - i.getLeft() - i.getRight(),
                b.getHeight() - i.getTop() - i.getBottom()
        );
    }

    private Track createDock(TrackAxis zoneAxis, @Nullable DockParent parent, boolean isRootPicked) {
        Supplier<Track> supplier = switch (zoneAxis) {
            case X -> isRootPicked ? rootXSupplier : subXSupplier;
            case Y -> isRootPicked ? rootYSupplier : subYSupplier;
            case Z -> zSupplier;
        };
        return supplier.get();
    }


    private boolean addToParent(Dockable dockable, DockParent parent, DropZone zone, boolean isRootPicked) {
        DockChild child;
        TrackAxis zoneAxis = getZoneAxis(zone);

        // Make sure that the parent of the dockable is a z-axis dock
        if ((parent instanceof DockRoot) || zoneAxis != TrackAxis.Z) {
            child = createDock(TrackAxis.Z, parent, isRootPicked);
            ((Track) child).getDockChildren().add(dockable);
        } else {
            child = dockable;
        }

        // Add to parent if axis matches
        if (parent.getDockAxis() == zoneAxis) {
            addToZoneInParent(child, parent, zone, -1);
            return true;
        }

        // Add to grand parent if grand parent's axis match
        DockParent grandParent = parent.getDockParent();
        if (grandParent != null && grandParent.getDockAxis() == zoneAxis) {
            addToZoneInParent(child, grandParent, zone, grandParent.getDockChildren().indexOf(parent));
            return true;
        }
        // Add to new grand parent
        Track newGrandParent = createDock(zoneAxis, grandParent, isRootPicked);
        if (grandParent == null) {
            DockChild removed = getDockChildren().set(0, newGrandParent);
            if (removed != null) {
                addToZoneInParent(removed, newGrandParent, zone, -1);
            }
        } else {
            grandParent.getDockChildren().set(grandParent.getDockChildren().indexOf(parent), newGrandParent);
            newGrandParent.getDockChildren().add(parent);
        }
        addToZoneInParent(child, newGrandParent, zone, -1);
        return true;
    }

    private void addToZoneInParent(DockChild child, DockParent parent, DropZone zone, int insertionIndex) {
        DockParent oldParent = child.getDockParent();
        if (oldParent != null) {
            oldParent.getDockChildren().remove(child);
        }

        ObservableList<DockChild> children = parent.getDockChildren();
        switch (zone) {
        case TOP:
        case LEFT:
            children.add(Math.max(insertionIndex, 0), child);
            break;
        case RIGHT:
        case BOTTOM:
        default:
            children.add(insertionIndex < 0 ? children.size() : insertionIndex + 1, child);
        }
    }

    private DragData computeDragData(DragEvent e) {
        Bounds bounds = getBoundsInLocal();

        DockParent pickedDock;
        boolean isRootPicked = true;
        if (subtractInsets(bounds, rootSensedDropZoneInsets).contains(e.getX(), e.getY())) {
            PickResult pick = e.getPickResult();
            Node pickedNode = pick.getIntersectedNode();
            while (pickedNode != this && pickedNode != null
                    && !(pickedNode instanceof Track)) {
                pickedNode = pickedNode.getParent();
            }
            pickedDock = (pickedNode instanceof Track) || pickedNode == this ? (DockParent) pickedNode : null;
        } else {
            pickedDock = this;
        }

        DropZone zone = null;
        Insets insets = rootDrawnDropZoneInsets;
        if (pickedDock == this) {
            if (getDockChildrenReadOnly().isEmpty()) {
                zone = DropZone.CENTER;
            } else {
                zone = getZone(e.getX(), e.getY(), getBoundsInLocal(), rootSensedDropZoneInsets);
                if (zone == DropZone.CENTER) {
                    zone = null;
                }
            }
        } else if (pickedDock != null) {
            isRootPicked = false;
            insets = dockDrawnDropZoneInsets;
            bounds = sceneToLocal(pickedDock.getNode().localToScene(pickedDock.getNode().getBoundsInLocal()));
            zone = getZone(e.getX(), e.getY(), bounds, dockSensedDropZoneInsets);
            if (!pickedDock.isEditable()) {
                zone = null;
            }
            if (zone == DropZone.CENTER) {
                zone = switch (pickedDock.getDockAxis()) {
                    case X -> DropZone.RIGHT;
                    case Y -> DropZone.BOTTOM;
                    default -> zone;
                };
            }
        } else {
            insets = null;
        }
        return new DragData(pickedDock, zone, bounds, insets, isRootPicked);
    }

    @Override
    public TrackAxis getDockAxis() {
        return TrackAxis.Z;
    }

    private @Nullable DropZone getZone(double x, double y, Bounds b, Insets insets) {
        if (y - b.getMinY() < insets.getTop() && b.getHeight() > insets.getTop() + insets.getBottom()) {
            return DropZone.TOP;
        } else if (b.getMaxY() - y < insets.getBottom() && b.getHeight() > insets.getTop() + insets.getBottom()) {
            return DropZone.BOTTOM;
        } else if (x - b.getMinX() < insets.getLeft() && b.getWidth() > insets.getLeft() + insets.getRight()) {
            return DropZone.LEFT;
        } else if (b.getMaxX() - x < insets.getRight() && b.getWidth() > insets.getLeft() + insets.getRight()) {
            return DropZone.RIGHT;
        } else {
            return b.contains(x, y) ? DropZone.CENTER : null;
        }
    }

    private TrackAxis getZoneAxis(DropZone zone) {
        return switch (zone) {
            case TOP, BOTTOM -> TrackAxis.Y;
            case LEFT, RIGHT -> TrackAxis.X;
            default -> TrackAxis.Z;
        };
    }

    private boolean isAcceptable(DragEvent e) {
        Dockable draggedItem = DockRoot.getDraggedDockable();
        return e.getDragboard().getContentTypes().contains(DockRoot.DOCKABLE_DATA_FORMAT)
                //    && e.getGestureSource() != null
                && draggedItem != null
                && (getDockablePredicate().test(draggedItem));
    }

    @Override
    public boolean isResizesDockChildren() {
        return true;
    }

    private void onDockableDropped(Dockable dropped, DragData dragData) {
        DockRoot droppedRoot = dropped.getDockRoot();
        DockParent dragSource = dropped.getDockParent();
        if (dragSource == null
                || dragData.pickedDock == null
                || (dragData.pickedDock instanceof DockRoot) && (dragData.pickedDock != this)) {
            return; // can't do dnd
        }
        int index = dragSource.getDockChildren().indexOf(dropped);
        dragSource.getDockChildren().remove(index);
        if (!addToParent(dropped, dragData.pickedDock, dragData.zone, dragData.isRootPicked)) {
            // failed to add revert to previous state
            dragSource.getDockChildren().add(index, dropped);
        } else {
            removeUnusedDocks(dragSource);
        }
    }

    private void onDragDrop(DragEvent e) {
        dropRect.setVisible(false);
        getChildren().remove(dropRect);
        if (!isAcceptable(e)) {
            return;
        }

        Dockable droppedTab = DockRoot.getDraggedDockable();
        DragData dragData = computeDragData(e);
        if (dragData.zone != null) {
            e.acceptTransferModes(TransferMode.MOVE);
            onDockableDropped(droppedTab, dragData);

        }
        e.consume();
    }

    private void onDragExit(DragEvent e) {
        dropRect.setVisible(false);
    }

    private void onDragOver(DragEvent e) {
        if (!isAcceptable(e)) {
            return;
        }

        DragData dragData = computeDragData(e);
        updateDropRect(dragData);

        if (dragData.zone != null) {
            e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        }
    }

    protected void onRootChanged(ListChangeListener.Change<? extends DockNode> c) {
        contentPane.centerProperty().unbind();
        if (c.getList().isEmpty()) {
            contentPane.centerProperty().set(null);
        } else {
            contentPane.setCenter((c.getList().getFirst().getNode()));
        }

    }

    private void removeUnusedDocks(DockParent node) {
        DockRoot root = node.getDockRoot();
        if (root == null) {
            return;
        }

        ArrayDeque<DockParent> todo = new ArrayDeque<>();
        todo.add(node);

        while (!todo.isEmpty()) {
            DockParent dock = todo.remove();
            DockParent parent = dock.getDockParent();
            if (parent != null) {
                if (dock.getDockChildrenReadOnly().isEmpty()) {
                    // Remove composite if it has zero children
                    parent.getDockChildren().remove(dock);
                    todo.add(parent);
                } else if (dock.getDockAxis() != TrackAxis.Z && dock.getDockChildren().size() == 1) {
                    // Replace xy composite with its child if xy composite has one child
                    DockChild onlyChild = dock.getDockChildren().removeFirst();
                    parent.getDockChildren().set(parent.getDockChildren().indexOf(dock), onlyChild);
                    todo.add(parent);
                }
            }
        }
    }

    private void updateDropRect(DragData dragData) {
        if (dragData.zone == null) {
            dropRect.setVisible(false);
            return;
        }
        if (dropRect.getParent() == null) {
            getChildren().add(dropRect);
        }
        Bounds bounds = dragData.bounds;

        double x = bounds.getMinX(),
                y = bounds.getMinY(),
                w = bounds.getWidth(),
                h = bounds.getHeight();
        Insets ins = dragData.insets;
        double btm = ins.getBottom(),
                lft = ins.getLeft(),
                rgt = ins.getRight(),
                top = ins.getTop();
        BoundingBox rect = switch (dragData.zone) {
            case BOTTOM -> new BoundingBox(x, y + h - btm, w, btm);
            case LEFT -> new BoundingBox(x, y, lft, h);
            case RIGHT -> new BoundingBox(x + w - rgt, y, rgt, h);
            case TOP -> new BoundingBox(x, y, w, top);
            default -> new BoundingBox(x + lft, y + top, w - lft - rgt, h - top - btm);
        };
        if (dropRect.isVisible() && !dropRect.getBoundsInLocal().isEmpty()) {
            if (transition == null || !transition.getToBounds().equals(rect)) {
                if (transition != null) {
                    transition.stop();
                }
                transition = new RectangleTransition(Duration.millis(200), dropRect, dropRect.getBoundsInLocal(), rect);
                transition.play();
                transition.setOnFinished(evt -> transition = null);
            }
        } else {
            dropRect.setVisible(true);
            dropRect.setX(rect.getMinX());
            dropRect.setY(rect.getMinY());
            dropRect.setWidth(rect.getWidth());
            dropRect.setHeight(rect.getHeight());
        }
    }

    private record DragData(DockParent pickedDock, DropZone zone, Bounds bounds,
                            Insets insets, boolean isRootPicked) {
    }

    public Supplier<Track> getZSupplier() {
        return zSupplier;
    }

    public void setZSupplier(Supplier<Track> zSupplier) {
        this.zSupplier = zSupplier;
    }

    public Supplier<Track> getRootXSupplier() {
        return rootXSupplier;
    }

    public void setRootXSupplier(Supplier<Track> rootXSupplier) {
        this.rootXSupplier = rootXSupplier;
    }

    public Supplier<Track> getRootYSupplier() {
        return rootYSupplier;
    }

    public void setRootYSupplier(Supplier<Track> rootYSupplier) {
        this.rootYSupplier = rootYSupplier;
    }

    public Supplier<Track> getSubXSupplier() {
        return subXSupplier;
    }

    public void setSubXSupplier(Supplier<Track> subXSupplier) {
        this.subXSupplier = subXSupplier;
    }

    public Supplier<Track> getSubYSupplier() {
        return subYSupplier;
    }

    public void setSubYSupplier(Supplier<Track> subYSupplier) {
        this.subYSupplier = subYSupplier;
    }
}
