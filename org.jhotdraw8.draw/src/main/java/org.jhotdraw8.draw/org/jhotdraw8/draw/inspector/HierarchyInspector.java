/*
 * @(#)HierarchyInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.EditableComponent;
import org.jhotdraw8.base.text.CachingCollator;
import org.jhotdraw8.base.text.NaturalSortCollator;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.SimpleDrawing;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.model.DrawingModelFigureProperty;
import org.jhotdraw8.draw.model.SimpleDrawingModel;
import org.jhotdraw8.fxbase.control.BooleanPropertyCheckBoxTreeTableCell;
import org.jhotdraw8.fxbase.converter.StringConverterAdapter;
import org.jhotdraw8.fxbase.tree.ExpandedTreeItemIterator;
import org.jhotdraw8.fxbase.tree.SimpleTreePresentationModel;
import org.jhotdraw8.fxbase.tree.TreePresentationModel;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.xml.converter.WordListXmlConverter;
import org.jhotdraw8.xml.converter.WordSetXmlConverter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * FXML Controller class
 *
 * @author Werner Randelshofer
 */
public class HierarchyInspector extends AbstractDrawingViewInspector {

    private final Comparator<String> collator = new CachingCollator(new NaturalSortCollator(Locale.ENGLISH));
    private final @NonNull WordListXmlConverter wordListConverter = new WordListXmlConverter();
    private final WordSetXmlConverter wordSetConverter = new WordSetXmlConverter();
    private final SimpleDrawingModel stubDrawingModel = new SimpleDrawingModel();
    private @Nullable DrawingView drawingView;
    @FXML
    private TreeTableColumn<Figure, String> idColumn;
    private boolean isUpdatingSelectionInView;
    @FXML
    private TreeTableColumn<Figure, Boolean> lockedColumn;
    private TreePresentationModel<Figure> model;
    private Node node;
    @FXML
    private TreeTableColumn<Figure, ImmutableSet<String>> pseudoClassesColumn;
    @FXML
    private TreeTableColumn<Figure, ImmutableSet<String>> styleClassesColumn;
    @FXML
    private TreeTableView<Figure> treeView;
    private final InvalidationListener treeSelectionHandler = change -> {
        if (model.isUpdating()) {
//        updateSelectionInTree();
        } else {
            updateSelectionInDrawingView();
        }
    };
    @FXML
    private TreeTableColumn<Figure, String> typeColumn;
    @FXML
    private TreeTableColumn<Figure, Boolean> visibleColumn;
    private boolean willUpdateSelectionInTree;
    private final SetChangeListener<Figure> viewSelectionHandler = this::updateSelectionInTreeLater;

    public HierarchyInspector() {
        this(HierarchyInspector.class.getResource("HierarchyInspector.fxml"),
                InspectorLabels.getResources().asResourceBundle());
    }

    public HierarchyInspector(@NonNull URL fxmlUrl, ResourceBundle resources) {
        init(fxmlUrl, resources);
    }

    @NonNull
    private Callback<TreeTableView<Figure>, TreeTableRow<Figure>> createRow() {
        return tv -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteMenuItem = new MenuItem(InspectorLabels.getResources().getString("edit.delete.text"));
            contextMenu.getItems().add(deleteMenuItem);

            TreeTableRow<Figure> row = new TreeTableRow<Figure>() {
                @Override
                public void updateItem(Figure item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setContextMenu(null);
                    } else {
                        // configure context menu with appropriate menu items,
                        // depending on value of item
                        setContextMenu(contextMenu);
                        deleteMenuItem.setDisable(!item.isDeletable());
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Figure rowData = row.getItem();
                    DrawingView myDrawingView = this.drawingView;
                    if (myDrawingView != null) {
                        myDrawingView.scrollFigureToVisible(rowData);
                    }
                }
            });

            // XXX Use weak references because of memory leak in JavaFX
            // https://bugs.openjdk.java.net/browse/JDK-8274022
            WeakReference<HierarchyInspector> inspectorRef = new WeakReference<>(this);
            WeakReference<TreeTableRow<Figure>> rowRef = new WeakReference<>(row);
            deleteMenuItem.setOnAction(evt -> {
                HierarchyInspector hierarchyInspector = inspectorRef.get();
                TreeTableRow<Figure> theRow = rowRef.get();
                if (hierarchyInspector != null && theRow != null) {
                    final Figure item = theRow.getItem();
                    if (item != null && item.isDeletable() && hierarchyInspector.drawingView != null) {
                        final DrawingModel model = hierarchyInspector.drawingView.getModel();
                        model.disconnect(item);
                        model.removeFromParent(item);
                    }
                }
            });
            return row;
        };
    }

    @Override
    public Node getNode() {
        return node;
    }

    private void init(@NonNull URL fxmlUrl, ResourceBundle resources) {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setResources(resources);
        try (InputStream in = fxmlUrl.openStream()) {
            node = loader.load(in);
        } catch (IOException ex) {
            throw new InternalError(ex);
        }

        Supplier<Map<Figure, TreeItem<Figure>>> mapSupplier = IdentityHashMap::new;
        model = new SimpleTreePresentationModel<Figure>(mapSupplier);
        stubDrawingModel.setDrawing(new SimpleDrawing());
        model.setTreeModel(stubDrawingModel);
        typeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue() == null ? null : cell.getValue().getValue() == null ? null : cell.getValue().getValue().getTypeSelector())
        );

        idColumn.setCellValueFactory(
                cell -> new DrawingModelFigureProperty<>((DrawingModel) model.getTreeModel(),
                        cell.getValue() == null ? null : cell.getValue().getValue(), StyleableFigure.ID));

        visibleColumn.setCellValueFactory(
                cell -> new DrawingModelFigureProperty<>((DrawingModel) model.getTreeModel(),
                        cell.getValue() == null ? null : cell.getValue().getValue(), HideableFigure.VISIBLE)
        );
        lockedColumn.setCellValueFactory(
                cell -> new DrawingModelFigureProperty<>((DrawingModel) model.getTreeModel(),
                        cell.getValue() == null ? null : cell.getValue().getValue(), LockableFigure.LOCKED)
        );
        // Type arguments needed for Java 8!
        styleClassesColumn.setCellValueFactory(cell -> new DrawingModelFigureProperty<ImmutableSet<String>>((DrawingModel) model.getTreeModel(),
                        cell.getValue() == null ? null : cell.getValue().getValue(), StyleableFigure.STYLE_CLASS) {
                    @Override
                    public @Nullable ImmutableSet<String> getValue() {
                        Figure f = figure.get();
                        return f == null ? null : ChampSet.copyOf(f.getStyleClasses());
                    }
                }
        );
        // Type arguments needed for Java 8!
        pseudoClassesColumn.setCellValueFactory(cell -> new DrawingModelFigureProperty<>((DrawingModel) model.getTreeModel(),
                        cell.getValue() == null ? null : cell.getValue().getValue(), StyleableFigure.PSEUDO_CLASS) {
                    @Override
                    public @Nullable ImmutableSet<String> getValue() {
                        Figure f = figure.get();
                        return f == null ? null : ChampSet.copyOf(f.getPseudoClassStates());
                    }
                }
        );

        // This cell factory ensures that only styleable figures support editing of ids.
        // And it ensures, that the users sees the computed id, and not the one that he entered.
        idColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public @NonNull TreeTableCell<Figure, String> call(TreeTableColumn<Figure, String> paramTableColumn) {
                        return new TextFieldTreeTableCell<Figure, String>(new DefaultStringConverter()) {
                            @Override
                            public void cancelEdit() {
                                super.cancelEdit();
                                updateItem(getItem(), false);
                            }

                            @Override
                            public void updateItem(String t, boolean empty) {
                                // super.updateItem(t, empty);
                                TreeTableRow<Figure> row = getTableRow();
                                boolean isEditable = false;
                                if (row != null) {
                                    Figure item = row.getItem();
                                    //Test for disable condition
                                    if (item != null && item.isEditableKey(StyleableFigure.ID)) {
                                        isEditable = true;
                                    }

                                    // show the computed  id!
                                    // FIXME We should bind to the idProperty here,
                                    //if (item != null) {
                                    //textProperty().bind(item.idProperty());
                                    //setText(item.getId());
                                    //}else{
                                    //textProperty().unbind();
                                    //}
                                    if (item != null) {
                                        setText(item.getId());
                                    }
                                }
                                if (isEditable) {
                                    setEditable(true);
                                    this.setStyle(null);
                                } else {
                                    setEditable(false);
                                    this.setStyle("-fx-text-fill: grey");
                                }
                            }
                        };
                    }

                });

        // This cell factory ensures that only styleable figures support editing of style classes.
        // And it ensures, that the users sees the computed style classes, and not the ones that he entered.
        // And it ensures, that the synthetic synthetic style classes are not stored in the STYLE_CLASSES attribute.
        // Type arguments needed for Java 8!
        styleClassesColumn.setCellFactory(new Callback<TreeTableColumn<Figure, ImmutableSet<String>>, TreeTableCell<Figure, ImmutableSet<String>>>() {
            // FIXME This column should be bound to the styleClasses of the figure,
            //   so that it updates automatically.

            @Override
            public @NonNull TreeTableCell<Figure, ImmutableSet<String>> call(TreeTableColumn<Figure, ImmutableSet<String>> paramTableColumn) {
                // Type arguments needed for Java 8!
                return new TextFieldTreeTableCell<Figure, ImmutableSet<String>>() {
                    private final @NonNull Set<String> syntheticClasses = new HashSet<>();

                    {
                        setConverter(new StringConverterAdapter<>(wordSetConverter));
                    }

                    @Override
                    public void cancelEdit() {
                        super.cancelEdit();
                        updateItem(getItem(), false);
                        syntheticClasses.clear();
                    }

                    @Override
                    public void commitEdit(@NonNull ImmutableSet<String> newValue) {
                        ImmutableSet<String> newValueSet = newValue.removeAll(syntheticClasses);
                        super.commitEdit(newValueSet);
                    }

                    @Override
                    public void startEdit() {
                        Figure figure = getTableRow().getItem();
                        figure.get(StyleableFigure.STYLE_CLASS);
                        syntheticClasses.clear();
                        syntheticClasses.addAll(figure.getStyleClasses().asCollection());
                        syntheticClasses.removeAll(figure.getNonNull(StyleableFigure.STYLE_CLASS).asSet());
                        super.startEdit();
                    }

                    @Override
                    public void updateItem(ImmutableSet<String> t, boolean empty) {
                        super.updateItem(t, empty);
                        TreeTableRow<Figure> row = getTableRow();
                        boolean isEditable = false;
                        if (row != null) {
                            Figure figure = row.getItem();
                            //Test for disable condition
                            if (figure != null && figure.isEditableKey(StyleableFigure.STYLE_CLASS)) {
                                isEditable = true;
                            }
                            // show the computed  classes!
                            if (figure != null) {
                                setText(wordSetConverter.toString(ChampSet.copyOf(figure.getStyleClasses())));
                            }
                        }
                        if (isEditable) {
                            setEditable(true);
                            this.setStyle(null);
                        } else {
                            setEditable(false);
                            this.setStyle("-fx-text-fill: grey");
                        }
                    }
                };
            }
        });
        pseudoClassesColumn.setCellFactory(paramTableColumn -> new TextFieldTreeTableCell<>() {
            {
                // FIXME This column should be bound to the pseudoClasses of the figure,
                //   so that it updates automatically.
                setConverter(new StringConverterAdapter<ImmutableSet<String>>(wordSetConverter));
                setEditable(false);
                this.setStyle("-fx-text-fill: grey");
            }

        });

        final Comparator<String> comparator = collator;
        typeColumn.setComparator(comparator);
        idColumn.setComparator(comparator);

        visibleColumn.setCellFactory(BooleanPropertyCheckBoxTreeTableCell.forTreeTableColumn(InspectorStyleClasses.VISIBLE_CHECK_BOX));
        lockedColumn.setCellFactory(BooleanPropertyCheckBoxTreeTableCell.forTreeTableColumn(InspectorStyleClasses.LOCKED_CHECK_BOX));
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView.getSelectionModel().getSelectedCells().addListener(treeSelectionHandler);
        treeView.setRowFactory(createRow());

        //treeView.setFixedCellSize(22);

        treeView.setRoot(model.getRoot());
        model.getRoot().setExpanded(true);
        showingProperty().addListener(this::onShowingChanged);
    }

    @Override
    protected void onDrawingViewChanged(ObservableValue<? extends DrawingView> observable, @Nullable DrawingView oldValue, @Nullable DrawingView newValue) {
        if (oldValue != null) {
            oldValue.getSelectedFigures().removeListener(viewSelectionHandler);
            treeView.getProperties().put(EditableComponent.EDITABLE_COMPONENT, null);
            model.setTreeModel(stubDrawingModel);
        }
        drawingView = newValue;
        if (newValue != null) {
            if (isShowing()) {
                model.setTreeModel(newValue.getModel());
            }
            newValue.getSelectedFigures().addListener(viewSelectionHandler);
            treeView.getProperties().put(EditableComponent.EDITABLE_COMPONENT, drawingView);
        }
    }

    private void onShowingChanged(Observable observable, Boolean oldValue, Boolean newValue) {
        if (newValue && model.getTreeModel() == stubDrawingModel
                && drawingView != null) {
            model.setTreeModel(drawingView.getModel());
        }
    }

    private void updateSelectionInDrawingView() {
        if (!isUpdatingSelectionInView) {
            isUpdatingSelectionInView = true;
            TreeTableView.TreeTableViewSelectionModel<Figure> selectionModel = treeView.getSelectionModel();
            SequencedSet<Figure> newSelection = new LinkedHashSet<>();
            for (TreeItem<Figure> item : selectionModel.getSelectedItems()) {
                if (item != null) {
                    newSelection.add(item.getValue());
                }
            }
            DrawingView myDrawingView = this.drawingView;
            if (myDrawingView != null) {
                myDrawingView.getSelectedFigures().retainAll(newSelection);
                myDrawingView.getSelectedFigures().addAll(newSelection);
            }
            isUpdatingSelectionInView = false;
        }
    }

    private void updateSelectionInTree() {
        willUpdateSelectionInTree = false;
        if (!isUpdatingSelectionInView) {
            isUpdatingSelectionInView = true;
            TreeTableView.TreeTableViewSelectionModel<Figure> selectionModel = treeView.getSelectionModel();
            // Performance: collecting all indices and then setting them all at once is
            // much faster than invoking selectionModel.select(Object) for each item.
            DrawingView myDrawingView = this.drawingView;
            Set<Figure> selection = myDrawingView == null ? Collections.emptySet() : myDrawingView.getSelectedFigures();
            switch (selection.size()) {
                case 0:
                    selectionModel.clearSelection();
                    break;
                case 1:
                    selectionModel.clearSelection();
                    final TreeItem<Figure> treeItem = model.getTreeItem(selection.iterator().next());
                    if (treeItem != null) {
                        selectionModel.select(treeItem);
                    }
                    break;
                default:
                    int index = 0;
                    int count = 0;
                    final int size = selection.size();
                    for (TreeItem<Figure> node : (Iterable<TreeItem<Figure>>) () -> new ExpandedTreeItemIterator<>(model.getRoot())) {
                        boolean isSelected = selection.contains(node.getValue());
                        if (isSelected != selectionModel.isSelected(index)) {
                            if (isSelected) {
                                selectionModel.select(index);
                            } else {
                                selectionModel.clearSelection(index);
                            }
                        }
                        if (isSelected && ++count == size) {
                            break;
                        }
                        index++;
                    }
            }
            isUpdatingSelectionInView = false;
        }
    }

    private void updateSelectionInTreeLater(SetChangeListener.Change<? extends Figure> change) {
        if (!willUpdateSelectionInTree && !isUpdatingSelectionInView) {
            willUpdateSelectionInTree = true;
            Platform.runLater(this::updateSelectionInTree);
        }
    }


}
