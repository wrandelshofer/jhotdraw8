/*
 * @(#)ListViewUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.control;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import org.jhotdraw8.fxbase.clipboard.ClipboardIO;
import org.jhotdraw8.fxbase.undo.UndoableEditHelper;
import org.jspecify.annotations.Nullable;

import javax.swing.event.UndoableEditEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Provides static utility methods for {@link ListView}.
 *
 */
public class ListViewUtil {

    /**
     * Don't let anyone instantiate this class.
     */
    private ListViewUtil() {
    }

    private static class DnDSupport<T> {

        private final ListView<T> listView;
        private int draggedCellIndex = -1;
        private final ClipboardIO<T> io;
        private final boolean reorderingOnly;
        private final UndoableEditHelper undoHelper;

        public DnDSupport(ListView<T> listView, ClipboardIO<T> io, boolean reorderingOnly, Consumer<UndoableEditEvent> undoableEditEventConsumer) {
            this.listView = listView;
            this.io = io;
            this.reorderingOnly = reorderingOnly;
            this.undoHelper = new UndoableEditHelper(listView, undoableEditEventConsumer);
        }

        private final EventHandler<? super DragEvent> cellDragHandler = new EventHandler<>() {
            @Override
            public void handle(DragEvent event) {
                if (event.isConsumed()) {
                    return;
                }
                EventType<DragEvent> t = event.getEventType();
                if (t == DragEvent.DRAG_DONE) {
                    onDragDone(event);
                } else if (t == DragEvent.DRAG_DROPPED) {
                    onDragDropped(event);
                } else if (t == DragEvent.DRAG_OVER) {
                    onDragOver(event);
                }
            }

            private void onDragDone(DragEvent event) {
                if (reorderingOnly) {
                    // XXX assumes that the ListView autodetects reordering!
                    draggedCellIndex = -1;
                    event.consume();
                    return;
                }
                if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
                    listView.getItems().remove(draggedCellIndex);
                }
                event.consume();
            }

            private TransferMode[] acceptModes(DragEvent event) {
                ListView<?> gestureTargetListView = null;
                if (event.getGestureSource() instanceof ListCell<?> gestureTargetCell) {
                    gestureTargetListView = gestureTargetCell.getListView();
                }
                TransferMode[] mode;
                if (reorderingOnly) {
                    mode = (listView == gestureTargetListView) ? new TransferMode[]{TransferMode.MOVE} : TransferMode.NONE;
                } else {
                    mode = (listView == gestureTargetListView) ? new TransferMode[]{TransferMode.MOVE} : new TransferMode[]{TransferMode.COPY};
                }

                return mode;
            }

            private void onDragDropped(DragEvent event) {
                boolean isAcceptable = io.canRead(event.getDragboard());
                if (isAcceptable) {
                    boolean success = false;
                    TransferMode[] mode = acceptModes(event);
                    if (mode.length == 0) {
                        return;
                    }
                    event.acceptTransferModes(mode);

                    ListCell<?> source = (ListCell<?>) event.getSource();
                    int droppedCellIndex = source.getIndex();
                    ObservableList<T> listViewItems = listView.getItems();

                    if (reorderingOnly) {
                        // FIXME only supports single item drag
                        int to = draggedCellIndex < droppedCellIndex
                                ? min(listViewItems.size(), droppedCellIndex)
                                : min(listViewItems.size() - 1, droppedCellIndex + 1);
                        if (to < 0) {
                            success = false;
                        } else {
                            T item = listViewItems.get(draggedCellIndex);
                            listViewItems.add(to, item);
                            success = true;
                        }
                    } else {
                        List<T> items = io.read(event.getDragboard());
                        success = items != null;
                        if (success) {
                            for (T item : items) {
                                listViewItems.add(min(droppedCellIndex, listViewItems.size()), item);
                                if (droppedCellIndex <= draggedCellIndex) {
                                    draggedCellIndex++;
                                }
                                droppedCellIndex++;
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                }
            }

            private void onDragOver(DragEvent event) {
                boolean isAcceptable = io.canRead(event.getDragboard());
                if (isAcceptable && (!reorderingOnly || draggedCellIndex != -1)) {
                    event.acceptTransferModes(acceptModes(event));
                    event.consume();
                }
            }
        };

        private final EventHandler<? super MouseEvent> cellMouseHandler = new EventHandler<>() {

            @Override
            public void handle(MouseEvent event) {
                if (event.isConsumed()) {
                    return;
                }
                if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
                    @SuppressWarnings("unchecked")
                    ListCell<T> draggedCell = (ListCell<T>) event.getSource();
                    draggedCellIndex = draggedCell.getIndex();
                    // XXX we currently only support single selection!!
                    if (!listView.getSelectionModel().isSelected(draggedCell.getIndex())) {
                        return;
                    }

                    Dragboard dragboard = draggedCell.startDragAndDrop(reorderingOnly ? new TransferMode[]{TransferMode.MOVE} : TransferMode.COPY_OR_MOVE);
                    ArrayList<T> items = new ArrayList<>();
                    items.add(draggedCell.getItem());
                    io.write(dragboard, items);
                    dragboard.setDragView(draggedCell.snapshot(new SnapshotParameters(), null));
                    event.consume();
                }
            }

        };

        final EventHandler<? super DragEvent> listDragHandler = new EventHandler<>() {

            @Override
            public void handle(DragEvent event) {
                if (event.isConsumed()) {
                    return;
                }
                EventType<DragEvent> t = event.getEventType();
                if (t == DragEvent.DRAG_DROPPED) {
                    onDragDropped(event);
                } else if (t == DragEvent.DRAG_OVER) {
                    onDragOver(event);
                }
            }

            private TransferMode[] acceptModes(DragEvent event) {
                ListView<?> gestureTargetListView = null;
                if (event.getGestureSource() instanceof ListCell<?> gestureTargetCell) {
                    gestureTargetListView = gestureTargetCell.getListView();
                }
                TransferMode[] mode;
                if (reorderingOnly) {
                    mode = (listView == gestureTargetListView) ? new TransferMode[]{TransferMode.MOVE} : TransferMode.NONE;
                } else {
                    mode = (listView == gestureTargetListView) ? new TransferMode[]{TransferMode.MOVE} : new TransferMode[]{TransferMode.COPY};
                }

                return mode;
            }

            private void onDragDropped(DragEvent event) {
                boolean isAcceptable = io.canRead(event.getDragboard());
                if (isAcceptable) {
                    undoHelper.startCompositeEdit(null);
                    boolean success = false;
                    TransferMode[] mode = acceptModes(event);
                    if (mode.length == 0) {
                        return;
                    }
                    event.acceptTransferModes(mode);

                    // XXX foolishly assumes fixed cell height
                    double cellHeight = listView.getFixedCellSize();
                    int index = max(0, min((int) (event.getY() / cellHeight), listView.getItems().size()));

                    if (reorderingOnly) {
                        // FIXME only supports single item drag
                        T item = listView.getItems().get(draggedCellIndex);
                        listView.getItems().add(index, item);
                        success = true;
                    } else {

                        List<T> items = io.read(event.getDragboard());
                        success = items != null;
                        if (success) {
                            for (T item : items) {
                                listView.getItems().add(index, item);
                                if (index <= draggedCellIndex) {
                                    draggedCellIndex++;
                                }
                                index++;
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                    undoHelper.stopCompositeEdit();
                }
            }

            private void onDragOver(DragEvent event) {
                boolean isAcceptable = io.canRead(event.getDragboard());
                if (isAcceptable && (!reorderingOnly || draggedCellIndex != -1)) {
                    event.acceptTransferModes(acceptModes(event));
                    event.consume();
                }
            }
        };
    }

    /**
     * Adds drag and drop support to the list view
     *
     * @param <T>                       the data type of the list view
     * @param listView                  the list view
     * @param clipboardIO               a reader/writer for the clipboard.
     */
    public static <T> void addDragAndDropSupport(ListView<T> listView, ClipboardIO<T> clipboardIO) {
        addDragAndDropSupport(listView, listView.getCellFactory(), clipboardIO, undoableEditEvent -> {
            // empty
        });
    }

    /**
     * Adds drag and drop support to the list view
     *
     * @param <T>                       the data type of the list view
     * @param listView                  the list view
     * @param clipboardIO               a reader/writer for the clipboard.
     * @param undoableEditEventConsumer a consumer for undoable edits
     */
    public static <T> void addDragAndDropSupport(ListView<T> listView, ClipboardIO<T> clipboardIO, Consumer<UndoableEditEvent> undoableEditEventConsumer) {
        addDragAndDropSupport(listView, listView.getCellFactory(), clipboardIO, undoableEditEventConsumer);
    }

    /**
     * Adds drag and drop support to the list view
     * <p>
     * FIXME should also add support for cut, copy and paste keys
     *  @param <T>         the data type of the list view
     *
     * @param listView                  the list view
     * @param cellFactory               the cell factory of the list view
     * @param clipboardIO               a reader/writer for the clipboard.
     * @param undoableEditEventConsumer
     */
    public static <T> void addDragAndDropSupport(ListView<T> listView,
                                                 @Nullable Callback<ListView<T>, ListCell<T>> cellFactory,
                                                 ClipboardIO<T> clipboardIO,
                                                 Consumer<UndoableEditEvent> undoableEditEventConsumer) {
        addDragAndDropSupport(listView, cellFactory, clipboardIO, false, undoableEditEventConsumer);
    }

    private static <T> void addDragAndDropSupport(ListView<T> listView,
                                                  @Nullable Callback<ListView<T>, ListCell<T>> cellFactory,
                                                  ClipboardIO<T> clipboardIO,
                                                  boolean reorderingOnly, Consumer<UndoableEditEvent> undoableEditEventConsumer) {
        DnDSupport<T> dndSupport = new DnDSupport<>(listView, clipboardIO, reorderingOnly, undoableEditEventConsumer);
        Callback<ListView<T>, ListCell<T>> dndCellFactory = lv -> {
            ListCell<T> cell = cellFactory == null ? new SimpleListCell<>() : cellFactory.call(lv);
            cell.addEventHandler(DragEvent.ANY, dndSupport.cellDragHandler);
            cell.addEventHandler(MouseEvent.DRAG_DETECTED, dndSupport.cellMouseHandler);
            return cell;
        };
        listView.setCellFactory(dndCellFactory);
        listView.addEventHandler(DragEvent.ANY, dndSupport.listDragHandler);
    }

    /**
     * Adds reordering support to the list view.
     *
     * @param <T>                       the data type of the list view
     * @param listView                  the list view
     * @param undoableEditEventConsumer
     */
    public static <T> void addReorderingSupport(ListView<T> listView, Consumer<UndoableEditEvent> undoableEditEventConsumer) {
        addReorderingSupport(listView, listView.getCellFactory(), null, undoableEditEventConsumer);
    }

    /**
     * Adds reordering support to the list view.
     *
     * @param <T>                       the data type of the list view
     * @param listView                  the list view
     * @param clipboardIO               the clipboard i/o
     * @param undoableEditEventConsumer
     */
    public static <T> void addReorderingSupport(ListView<T> listView, ClipboardIO<T> clipboardIO, Consumer<UndoableEditEvent> undoableEditEventConsumer) {
        addReorderingSupport(listView, listView.getCellFactory(), clipboardIO, undoableEditEventConsumer);
    }

    /**
     * Adds reordering support to the list view.
     * <p>
     * FIXME should also add support for cut, copy and paste keys.
     * <p>
     * FIXME only supports lists with single item selection (no multiple item selection yet!).
     *  @param <T>         the data type of the list view
     *
     * @param listView                  the list view
     * @param cellFactory               the cell factory of the list view
     * @param clipboardIO               a reader/writer for the clipboard. You can provide null if you don't want cut/copy/paste functionality.
     * @param undoableEditEventConsumer
     */
    public static <T> void addReorderingSupport(ListView<T> listView, Callback<ListView<T>, ListCell<T>> cellFactory, @Nullable ClipboardIO<T> clipboardIO, Consumer<UndoableEditEvent> undoableEditEventConsumer) {
        if (clipboardIO == null) {
            clipboardIO = new ClipboardIO<>() {
                @Override
                public void write(Clipboard clipboard, List<T> items) {
                    // We just write the index of the selected item in the clipboard.
                    if (items.size() != 1) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    ClipboardContent content = new ClipboardContent();
                    content.putString("" + listView.getSelectionModel().getSelectedIndex());
                    clipboard.setContent(content);
                }

                @Override
                public List<T> read(Clipboard clipboard) {
                    // We are not actually interested in the clipboard content.
                    return Collections.emptyList();
                }

                @Override
                public boolean canRead(Clipboard clipboard) {
                    return clipboard.hasString();
                }
            };
        }
        addDragAndDropSupport(listView, cellFactory, clipboardIO, true, undoableEditEventConsumer);
    }

    static class SimpleListCell<T> extends ListCell<T> {
        public void updateItem(T var1, boolean var2) {
            super.updateItem(var1, var2);
            if (var2) {
                this.setText(null);
                this.setGraphic(null);
            } else if (var1 instanceof Node var4) {
                this.setText(null);
                Node var3 = this.getGraphic();
                if (var3 == null || !var3.equals(var4)) {
                    this.setGraphic(var4);
                }
            } else {
                this.setText(var1 == null ? "null" : var1.toString());
                this.setGraphic(null);
            }

        }
    }
}
