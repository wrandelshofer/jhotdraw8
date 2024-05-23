/*
 * @(#)StylesheetsInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.StringConverter;
import org.jhotdraw8.application.action.file.BrowseFileDirectoryAction;
import org.jhotdraw8.base.converter.SimpleUriResolver;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.clipboard.ClipboardIO;
import org.jhotdraw8.fxbase.concurrent.PlatformUtil;
import org.jhotdraw8.fxbase.control.ListViewUtil;
import org.jhotdraw8.fxbase.converter.StringConverterAdapter;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.xml.converter.UriXmlConverter;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FXML Controller class
 *
 * @author Werner Randelshofer
 */
public class StylesheetsInspector extends AbstractDrawingInspector {

    @FXML
    private ListView<URI> listView;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button refreshButton;

    private ListProperty<URI> stylesheetsProperty;
    private Node node;
    /**
     * Counter for incrementing stylesheet names.
     */
    private int counter;

    public StylesheetsInspector() {
        this(StylesheetsInspector.class.getResource("StylesheetsInspector.fxml"));
    }

    public StylesheetsInspector(URL fxmlUrl) {
        init(fxmlUrl);
    }

    private void init(URL fxmlUrl) {
        // We must use invoke and wait here, because we instantiate Tooltips
        // which immediately instantiate a Window and a Scene.
        PlatformUtil.invokeAndWait(() -> {

            FXMLLoader loader = new FXMLLoader();
            loader.setResources(InspectorLabels.getResources().asResourceBundle());
            loader.setController(this);
            try (InputStream in = fxmlUrl.openStream()) {
                node = loader.load(in);
            } catch (IOException ex) {
                throw new InternalError(ex);
            }
            listView.getItems().addListener((InvalidationListener) (o -> onListChanged()));

            // int counter = 0;
            addButton.addEventHandler(ActionEvent.ACTION, this::onAddAction);
            removeButton.addEventHandler(ActionEvent.ACTION, this::onRemoveAction);
            removeButton.disableProperty().bind(Bindings.equal(listView.getSelectionModel().selectedIndexProperty(), -1));
            refreshButton.addEventHandler(ActionEvent.ACTION, this::onRefreshAction);

            listView.setEditable(true);
            listView.setFixedCellSize(24.0);

            listView.setOnEditCommit(t -> listView.getItems().set(t.getIndex(), t.getNewValue()));

            ClipboardIO<URI> io = new ClipboardIO<>() {

                @Override
                public void write(Clipboard clipboard, List<URI> items) {
                    if (items.size() != 1) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    ClipboardContent content = new ClipboardContent();
                    URI stylesheetUri = items.getFirst();
                    URI documentHome = getDrawing().get(Drawing.DOCUMENT_HOME);
                    stylesheetUri = new SimpleUriResolver().absolutize(documentHome, stylesheetUri);
                    content.putUrl(stylesheetUri.toString());
                    clipboard.setContent(content);
                }

                @Override
                public @Nullable List<URI> read(Clipboard clipboard) {
                    List<URI> list;
                    if (clipboard.hasUrl()) {
                        list = new ArrayList<>();
                        URI documentHome = getDrawing().get(Drawing.DOCUMENT_HOME);
                        URI dragboardUri = URI.create(clipboard.getUrl());
                        URI stylesheetUri = documentHome.relativize(dragboardUri);
                        list.add(stylesheetUri);
                    } else if (clipboard.hasFiles()) {
                        list = new ArrayList<>();
                        URI documentHome = getDrawing().get(Drawing.DOCUMENT_HOME);
                        for (File f : clipboard.getFiles()) {
                            URI dragboardUri = f.toURI();
                            URI stylesheetUri = new SimpleUriResolver().absolutize(documentHome, dragboardUri);
                            list.add(stylesheetUri);
                        }
                    } else {
                        list = null;
                    }
                    return list;
                }

                @Override
                public boolean canRead(Clipboard clipboard) {
                    return clipboard.hasFiles() || clipboard.hasUrl();
                }

            };
            StringConverter<URI> uriConverter = new StringConverterAdapter<>(new UriXmlConverter());

            ListViewUtil.addDragAndDropSupport(listView,
                    (ListView<URI> param) -> {
                        TextFieldListCell<URI> cell = new TextFieldListCell<>(uriConverter);
                        cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(createCellContextMenu(cell));
                            }
                        });
                        return cell;
                    }, io, this::forwardUndoableEditEvent);
        });
    }

    private ContextMenu createCellContextMenu(TextFieldListCell<URI> cell) {
        ContextMenu cm = new ContextMenu();
        MenuItem mi = new MenuItem();
        mi.setText(InspectorLabels.getResources().getString("file.browseFileDirectory.text"));


        // Workaround for memory leak in ControlAcceleratorSupport
        // https://bugs.openjdk.java.net/browse/JDK-8274022
        WeakReference<StylesheetsInspector> weakInspector = new WeakReference<>(this);
        WeakReference<TextFieldListCell<URI>> weakCell = new WeakReference<>(cell);
        mi.setOnAction(event -> {
            StylesheetsInspector stylesheetsInspector = weakInspector.get();
            TextFieldListCell<URI> theCell = weakCell.get();
            if (stylesheetsInspector != null && theCell != null) {
                stylesheetsInspector.onBrowseFileDirectory(event, theCell);
            }
        });
        cm.getItems().add(mi);
        return cm;
    }

    private void onBrowseFileDirectory(ActionEvent actionEvent, TextFieldListCell<URI> cell) {
        URI uri = cell.getItem();
        BrowseFileDirectoryAction.browseFileDirectory(uri);
    }

    private int isReplacingDrawing;

    @Override
    protected void onDrawingChanged(@Nullable ObservableValue<? extends Drawing> observable, @Nullable Drawing oldValue, @Nullable Drawing newValue) {
        isReplacingDrawing++;
        if (oldValue != null) {
            listView.getItems().clear();
        }
        if (newValue != null) {
            // FIXME should listen to property changes of the Drawing object
            ImmutableList<URI> stylesheets = newValue.get(Drawing.AUTHOR_STYLESHEETS);
            if (stylesheets == null) {
                listView.getItems().clear();
            } else {
                listView.getItems().setAll(stylesheets.asList());
            }
        }
        counter = 0;
        isReplacingDrawing--;
    }

    private void onListChanged() {
        if (isReplacingDrawing != 0) {
            // The drawing is currently being replaced by a new one. Don't fire events.
            return;
        }
        getModel().set(getDrawing(), Drawing.AUTHOR_STYLESHEETS, VectorList.copyOf(listView.getItems()));
        updateAllFigures();
    }

    @Override
    public Node getNode() {
        return node;
    }

    private void onRemoveAction(ActionEvent event) {
        ObservableList<URI> items = listView.getItems();
        ArrayList<Integer> indices = new ArrayList<>(listView.getSelectionModel().getSelectedIndices());
        Collections.sort(indices);
        for (int i = indices.size() - 1; i >= 0; i--) {
            items.remove((int) indices.get(i));
        }
    }

    private void onAddAction(ActionEvent event) {
        Drawing drawing = getDrawing();
        if (drawing == null) {
            return;
        }
        URI documentHome = drawing.get(Drawing.DOCUMENT_HOME);
        URI uri = URI.create("stylesheet" + (++counter) + ".css");
        uri = new SimpleUriResolver().absolutize(documentHome, uri);
        listView.getItems().add(uri);
    }

    private void onRefreshAction(ActionEvent event) {
        updateAllFigures();
    }

    private void updateAllFigures() {
        Drawing drawing = getDrawing();
        final DrawingView subject = getSubject();
        if (drawing == null || subject == null) {
            return;
        }
        getDrawing().updateStyleManager();

        getDrawing().updateAllCss(subject);
        for (Figure f : getDrawing().preorderIterable()) {
            getDrawingModel().fireLayoutInvalidated(f);
        }
    }
}
