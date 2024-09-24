/*
 * @(#)GrapherActivity.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.grapher;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jhotdraw8.application.AbstractFileBasedActivity;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.action.edit.RedoAction;
import org.jhotdraw8.application.action.edit.UndoAction;
import org.jhotdraw8.application.action.file.BrowseFileDirectoryAction;
import org.jhotdraw8.application.action.file.ExportFileAction;
import org.jhotdraw8.application.action.file.PrintFileAction;
import org.jhotdraw8.application.action.view.ToggleBooleanAction;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingEditorPreferencesHandler;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.EditorActivity;
import org.jhotdraw8.draw.SimpleDrawingEditor;
import org.jhotdraw8.draw.SimpleDrawingView;
import org.jhotdraw8.draw.action.AddToGroupAction;
import org.jhotdraw8.draw.action.AlignBottomAction;
import org.jhotdraw8.draw.action.AlignHorizontalAction;
import org.jhotdraw8.draw.action.AlignLeftAction;
import org.jhotdraw8.draw.action.AlignRightAction;
import org.jhotdraw8.draw.action.AlignTopAction;
import org.jhotdraw8.draw.action.AlignVerticalAction;
import org.jhotdraw8.draw.action.BringForwardAction;
import org.jhotdraw8.draw.action.BringToFrontAction;
import org.jhotdraw8.draw.action.DistributeHorizontallyAction;
import org.jhotdraw8.draw.action.DistributeVerticallyAction;
import org.jhotdraw8.draw.action.GroupAction;
import org.jhotdraw8.draw.action.RemoveFromGroupAction;
import org.jhotdraw8.draw.action.RemoveTransformationsAction;
import org.jhotdraw8.draw.action.SelectChildrenAction;
import org.jhotdraw8.draw.action.SelectSameAction;
import org.jhotdraw8.draw.action.SendBackwardAction;
import org.jhotdraw8.draw.action.SendToBackAction;
import org.jhotdraw8.draw.action.UngroupAction;
import org.jhotdraw8.draw.constrain.GridConstrainer;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.draw.css.value.CssInsets;
import org.jhotdraw8.draw.figure.AbstractDrawing;
import org.jhotdraw8.draw.figure.BezierPathFigure;
import org.jhotdraw8.draw.figure.CombinedPathFigure;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.ElbowConnectionWithMarkersFigure;
import org.jhotdraw8.draw.figure.EllipseFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.FillableFigure;
import org.jhotdraw8.draw.figure.GroupFigure;
import org.jhotdraw8.draw.figure.ImageFigure;
import org.jhotdraw8.draw.figure.LabelFigure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.LayerFigure;
import org.jhotdraw8.draw.figure.LineConnectionWithMarkersFigure;
import org.jhotdraw8.draw.figure.LineFigure;
import org.jhotdraw8.draw.figure.PageFigure;
import org.jhotdraw8.draw.figure.PageLabelFigure;
import org.jhotdraw8.draw.figure.PolygonFigure;
import org.jhotdraw8.draw.figure.PolylineFigure;
import org.jhotdraw8.draw.figure.RectangleFigure;
import org.jhotdraw8.draw.figure.SimpleLayeredDrawing;
import org.jhotdraw8.draw.figure.SliceFigure;
import org.jhotdraw8.draw.figure.StrokableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.figure.TextAreaFigure;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.input.MultiClipboardInputFormat;
import org.jhotdraw8.draw.input.MultiClipboardOutputFormat;
import org.jhotdraw8.draw.inspector.DrawingInspector;
import org.jhotdraw8.draw.inspector.GridInspector;
import org.jhotdraw8.draw.inspector.HandlesInspector;
import org.jhotdraw8.draw.inspector.HelpTextInspector;
import org.jhotdraw8.draw.inspector.HierarchyInspector;
import org.jhotdraw8.draw.inspector.Inspector;
import org.jhotdraw8.draw.inspector.InspectorLabels;
import org.jhotdraw8.draw.inspector.LayersInspector;
import org.jhotdraw8.draw.inspector.StyleAttributesInspector;
import org.jhotdraw8.draw.inspector.StyleClassesInspector;
import org.jhotdraw8.draw.inspector.StylesheetsInspector;
import org.jhotdraw8.draw.inspector.ToolsToolbar;
import org.jhotdraw8.draw.inspector.ZoomToolbar;
import org.jhotdraw8.draw.io.BitmapExportOutputFormat;
import org.jhotdraw8.draw.io.DefaultFigureFactory;
import org.jhotdraw8.draw.io.FigureFactory;
import org.jhotdraw8.draw.io.PrinterExportFormat;
import org.jhotdraw8.draw.io.SimpleFigureIdFactory;
import org.jhotdraw8.draw.io.SimpleXmlReader;
import org.jhotdraw8.draw.io.SimpleXmlWriter;
import org.jhotdraw8.draw.io.XmlEncoderOutputFormat;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.render.SimpleRenderContext;
import org.jhotdraw8.draw.tool.BezierCreationTool;
import org.jhotdraw8.draw.tool.ConnectionTool;
import org.jhotdraw8.draw.tool.CreationTool;
import org.jhotdraw8.draw.tool.ImageCreationTool;
import org.jhotdraw8.draw.tool.LineCreationTool;
import org.jhotdraw8.draw.tool.PolyCreationTool;
import org.jhotdraw8.draw.tool.SelectionTool;
import org.jhotdraw8.draw.tool.TextCreationTool;
import org.jhotdraw8.draw.tool.TextEditingTool;
import org.jhotdraw8.draw.tool.Tool;
import org.jhotdraw8.draw.undo.DrawingModelUndoAdapter;
import org.jhotdraw8.fxbase.concurrent.FXWorker;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxbase.undo.FXUndoManager;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcontrols.dock.DockChild;
import org.jhotdraw8.fxcontrols.dock.DockRoot;
import org.jhotdraw8.fxcontrols.dock.Dockable;
import org.jhotdraw8.fxcontrols.dock.SimpleDockRoot;
import org.jhotdraw8.fxcontrols.dock.SimpleDockable;
import org.jhotdraw8.fxcontrols.dock.SplitPaneTrack;
import org.jhotdraw8.fxcontrols.dock.TabbedAccordionTrack;
import org.jhotdraw8.fxcontrols.dock.Track;
import org.jhotdraw8.fxcontrols.dock.VBoxTrack;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.svg.gui.SvgDrawingExportOptionsPane;
import org.jhotdraw8.svg.io.FXSvgFullWriter;
import org.jhotdraw8.svg.io.FXSvgTinyWriter;
import org.jhotdraw8.svg.io.SvgExportOutputFormat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.SequencedSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jhotdraw8.fxbase.clipboard.DataFormats.registerDataFormat;

/**
 * GrapherActivityController.
 *
 * @author Werner Randelshofer
 */
public class GrapherActivity extends AbstractFileBasedActivity implements FileBasedActivity, EditorActivity {

    private static final String GRAPHER_NAMESPACE_URI = "http://jhotdraw.org/samples/grapher";
    private static final String VIEWTOGGLE_PROPERTIES = "view.toggleProperties";
    /**
     * Counter for incrementing layer names.
     */
    private final Map<String, Integer> counters = new HashMap<>();
    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private VBox detailsVBox;
    private final BooleanProperty detailsVisible = new SimpleBooleanProperty(this, "detailsVisible", true);

    private DrawingView drawingView;

    private DrawingEditor editor;
    @FXML
    private BorderPane contentPane;
    private Node node;
    @FXML
    private ToolBar toolsToolBar;
    private DockRoot dockRoot;

    private final FXUndoManager undoManager = new FXUndoManager();

    private Dockable addInspector(Inspector<DrawingView> inspector, String id, Priority grow) {
        Resources r = InspectorLabels.getResources();
        Dockable dockable = new SimpleDockable(r.getString(id + ".toolbar"), inspector.getNode());
        inspector.showingProperty().bind(dockable.showingProperty());
        inspector.getNode().getProperties().put("inspector", inspector);
        VBox.setVgrow(dockable.getNode(), grow);
        return dockable;
    }

    @Override
    public CompletionStage<Void> clear() {
        Drawing d = new SimpleLayeredDrawing();
        d.set(StyleableFigure.ID, "drawing1");
        LayerFigure layer = new LayerFigure();
        layer.set(StyleableFigure.ID, "layer1");
        d.addChild(layer);
        for (final Figure f : d.preorderIterable()) {
            f.addedToDrawing(d);
        }
        applyUserAgentStylesheet(d);
        drawingView.setDrawing(d);
        undoManager.discardAllEdits();
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates a figure with a unique id.
     *
     * @param <T>      the figure type
     * @param supplier the supplier
     * @return the created figure
     */
    public <T extends Figure> T createFigure(Supplier<T> supplier) {
        T created = supplier.get();
        String prefix = created.getTypeSelector().toLowerCase();
        Integer counter = counters.get(prefix);
        Set<String> ids = new HashSet<>();
        counter = counter == null ? 1 : counter + 1;
        // XXX O(n) !!!
        for (Figure f : drawingView.getDrawing().preorderIterable()) {
            ids.add(f.getId());
        }
        String id = prefix + counter;
        while (ids.contains(id)) {
            counter++;
            id = prefix + counter;
        }
        counters.put(created.getTypeSelector(), counter);
        created.set(StyleableFigure.ID, id);
        return created;
    }

    @Override
    public DrawingEditor getEditor() {
        return editor;
    }

    @Override
    public Node getNode() {
        return node;
    }

    public Node getPropertiesPane() {
        return detailsScrollPane;
    }

    @Override
    protected void initActions(ObservableMap<String, Action> map) {
        super.initActions(map);
        map.put(PrintFileAction.ID, new PrintFileAction(this));
        map.put(ExportFileAction.ID, new ExportFileAction(this, SvgDrawingExportOptionsPane::createDialog));
        map.put(RemoveTransformationsAction.ID, new RemoveTransformationsAction(editor));
        map.put(BrowseFileDirectoryAction.ID, new BrowseFileDirectoryAction(this));
        map.put(SelectSameAction.ID, new SelectSameAction(editor));
        map.put(SelectChildrenAction.ID, new SelectChildrenAction(editor));
        map.put(SendToBackAction.ID, new SendToBackAction(editor));
        map.put(BringToFrontAction.ID, new BringToFrontAction(editor));
        map.put(BringForwardAction.ID, new BringForwardAction(editor));
        map.put(SendBackwardAction.ID, new SendBackwardAction(editor));
        map.put(VIEWTOGGLE_PROPERTIES, new ToggleBooleanAction(
                this,
                VIEWTOGGLE_PROPERTIES,
                GrapherLabels.getResources(), detailsVisible));
        map.put(GroupAction.ID, new GroupAction(editor, () -> createFigure(GroupFigure::new)));
        map.put(GroupAction.COMBINE_PATHS_ID, new GroupAction(GroupAction.COMBINE_PATHS_ID, editor, () -> createFigure(CombinedPathFigure::new)));
        map.put(UngroupAction.ID, new UngroupAction(editor));
        map.put(AddToGroupAction.ID, new AddToGroupAction(editor));
        map.put(RemoveFromGroupAction.ID, new RemoveFromGroupAction(editor));
        map.put(AlignTopAction.ID, new AlignTopAction(editor));
        map.put(AlignRightAction.ID, new AlignRightAction(editor));
        map.put(AlignBottomAction.ID, new AlignBottomAction(editor));
        map.put(AlignLeftAction.ID, new AlignLeftAction(editor));
        map.put(AlignHorizontalAction.ID, new AlignHorizontalAction(editor));
        map.put(AlignVerticalAction.ID, new AlignVerticalAction(editor));
        map.put(DistributeHorizontallyAction.ID, new DistributeHorizontallyAction(editor));
        map.put(DistributeVerticallyAction.ID, new DistributeVerticallyAction(editor));
        map.put(UndoAction.ID, new UndoAction(this, undoManager));
        map.put(RedoAction.ID, new RedoAction(this, undoManager));
    }

    private Supplier<Layer> initToolBar() throws MissingResourceException {
        //drawingView.setConstrainer(new GridConstrainer(0,0,10,10,45));
        ToolsToolbar ttbar = new ToolsToolbar(editor);
        Resources labels = GrapherLabels.getResources();
        Supplier<Layer> layerFactory = () -> createFigure(LayerFigure::new);
        Tool defaultTool;
        ttbar.addTool(defaultTool = new SelectionTool("tool.resizeFigure", HandleType.RESIZE, null, HandleType.LEAD, labels), 0, 0);
        ttbar.addTool(new SelectionTool("tool.moveFigure", HandleType.MOVE, null, HandleType.LEAD, labels), 1, 0);
        ttbar.addTool(new SelectionTool("tool.selectPoint", HandleType.POINT, labels), 0, 1);
        ttbar.addTool(new SelectionTool("tool.transform", HandleType.TRANSFORM, labels), 1, 1);
        ttbar.addTool(new TextEditingTool("tool.editText", labels), 2, 1);

        ttbar.addTool(new CreationTool("edit.createRectangle", labels, () -> createFigure(RectangleFigure::new), layerFactory), 13, 0, 16);
        ttbar.addTool(new CreationTool("edit.createEllipse", labels, () -> createFigure(EllipseFigure::new), layerFactory), 14, 0);
        ttbar.addTool(new ConnectionTool("edit.createLineConnection", labels, () -> createFigure(LineConnectionWithMarkersFigure::new), layerFactory), 14, 1);
        ttbar.addTool(new ConnectionTool("edit.createElbowConnection", labels, () -> createFigure(ElbowConnectionWithMarkersFigure::new), layerFactory), 15, 1);
        ttbar.addTool(new LineCreationTool("edit.createLine", labels, () -> createFigure(LineFigure::new), layerFactory), 13, 1, 16);
        ttbar.addTool(new PolyCreationTool("edit.createPolyline", labels, PolylineFigure.POINTS, () -> createFigure(PolylineFigure::new), layerFactory),
                16, 1);
        ttbar.addTool(new PolyCreationTool("edit.createPolygon", labels,
                        PolygonFigure.POINTS, () -> createFigure(PolygonFigure::new), layerFactory),
                16, 0, 0);
        ttbar.addTool(new BezierCreationTool("edit.createBezier", labels,
                        BezierPathFigure.PATH, () -> createFigure(BezierPathFigure::new), layerFactory),
                17, 1);
        ttbar.addTool(new TextCreationTool("edit.createText", labels,//
                () -> createFigure(() -> new LabelFigure(0, 0, "Hello", FillableFigure.FILL, null, StrokableFigure.STROKE, null)), //
                layerFactory), 18, 1);
        ttbar.addTool(new TextCreationTool("edit.createTextArea", labels,
                        () -> createFigure(TextAreaFigure::new), layerFactory),
                18, 0);
        ttbar.addTool(new ImageCreationTool("edit.createImage", labels,
                () -> createFigure(ImageFigure::new), layerFactory), 17, 0, 0);
        ttbar.addTool(new CreationTool("edit.createSlice", labels,
                () -> createFigure(SliceFigure::new), layerFactory), 21, 0, 0);
        ttbar.addTool(new CreationTool("edit.createPage", labels,
                () -> createFigure(() -> {
                    PageFigure pf = new PageFigure();
                    pf.set(PageFigure.PAPER_SIZE, new CssDimension2D(297, 210, "mm"));
                    pf.set(PageFigure.PAGE_INSETS, new CssInsets(2, 1, 2, 1, "cm"));
                    PageLabelFigure pl = new PageLabelFigure(940, 700, labels.getFormatted("pageLabel.text",
                            PageLabelFigure.PAGE_PLACEHOLDER, PageLabelFigure.NUM_PAGES_PLACEHOLDER),
                            FillableFigure.FILL, null, StrokableFigure.STROKE, null);
                    pf.addChild(pl);
                    return pf;
                }), layerFactory), 20, 0, 16);
        ttbar.addTool(new CreationTool("edit.createPageLabel", labels,//
                () -> createFigure(() -> new PageLabelFigure(0, 0,
                        labels.getFormatted("pageLabel.text", PageLabelFigure.PAGE_PLACEHOLDER, PageLabelFigure.NUM_PAGES_PLACEHOLDER),
                        FillableFigure.FILL, null, StrokableFigure.STROKE, null)), //
                layerFactory), 20, 1, 16);
        ttbar.setDrawingEditor(editor);
        editor.setDefaultTool(defaultTool);
        toolsToolBar.getItems().add(ttbar);
        return layerFactory;
    }

    @Override
    public void initView() {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);

        try {
            node = loader.load(getClass().getResourceAsStream("GrapherActivity.fxml"));
        } catch (IOException ex) {
            throw new InternalError(ex);
        }

        drawingView = new SimpleDrawingView();
        drawingView.getNode().setId("drawingView");
        // FIXME should use preferences!
        drawingView.setConstrainer(new GridConstrainer(0, 0, 10, 10, 11.25, 5, 5));
        //drawingView.setHandleType(HandleType.TRANSFORM);
        //
        DrawingModel model = drawingView.getModel();
        model.addListener(drawingModel -> modified.set(true));
        final DrawingModelUndoAdapter undoAdapter = new DrawingModelUndoAdapter(model);
        undoAdapter.setResourceBundle(getApplication().getResources().asResourceBundle());
        undoAdapter.addUndoEditListener(undoManager);

        IdFactory idFactory = new SimpleFigureIdFactory();
        FigureFactory factory = new DefaultFigureFactory(idFactory);
        SimpleXmlWriter iow = new SimpleXmlWriter(factory, idFactory, GRAPHER_NAMESPACE_URI, null);
        SimpleXmlReader ior = new SimpleXmlReader(factory, idFactory, GRAPHER_NAMESPACE_URI);
        ior.setLayerFactory(LayerFigure::new);
        drawingView.setClipboardOutputFormat(new MultiClipboardOutputFormat(
                iow, new SvgExportOutputFormat(), new BitmapExportOutputFormat(BitmapExportOutputFormat.PNG_MIME_TYPE, BitmapExportOutputFormat.PNG_EXTENSION)));
        drawingView.setClipboardInputFormat(new MultiClipboardInputFormat(ior));

        editor = new SimpleDrawingEditor();
        editor.setUndoManager(undoManager);
        new DrawingEditorPreferencesHandler(editor,
                getApplication().getPreferences());
        editor.addDrawingView(drawingView);

        ScrollPane viewScrollPane = new ScrollPane();
        viewScrollPane.setFitToHeight(true);
        viewScrollPane.setFitToWidth(true);
        viewScrollPane.getStyleClass().addAll("view", "flush");
        viewScrollPane.setContent(drawingView.getNode());

        Supplier<Layer> layerFactory = initToolBar();

        ZoomToolbar ztbar = new ZoomToolbar();
        ztbar.zoomFactorProperty().bindBidirectional(drawingView.zoomFactorProperty());
        toolsToolBar.getItems().add(ztbar);
        initInspectors(viewScrollPane, layerFactory);

    }

    private void initInspectors(ScrollPane viewScrollPane, Supplier<Layer> layerFactory) {
        // set up the docking framework
        SimpleDockRoot root = new SimpleDockRoot();
        this.dockRoot = root;
        root.setZSupplier(TabbedAccordionTrack::new);
        root.setSubYSupplier(VBoxTrack::new);
        root.setRootXSupplier(SplitPaneTrack::createHorizontalTrack);
        root.setRootYSupplier(SplitPaneTrack::createVerticalTrack);
        root.setSubYSupplier(SplitPaneTrack::createVerticalTrack);
        Dockable viewScrollPaneDockItem = new SimpleDockable(null, viewScrollPane);
        root.getDockChildren().add(viewScrollPaneDockItem);

        contentPane.setCenter(this.dockRoot.getNode());

        FXWorker.supply(() -> {
            List<Track> d = new ArrayList<>();
            Track track = new TabbedAccordionTrack();
            track.getDockChildren().addAll(addInspector(new StyleAttributesInspector(), "styleAttributes", Priority.ALWAYS),
                    addInspector(new StyleClassesInspector(), "styleClasses", Priority.NEVER),
                    addInspector(new StylesheetsInspector(), "styleSheets", Priority.ALWAYS));
            d.add(track);
            track = new TabbedAccordionTrack();
            track.getDockChildren().addAll(addInspector(new LayersInspector(layerFactory), "layers", Priority.ALWAYS),
                    addInspector(new HierarchyInspector(), "figureHierarchy", Priority.ALWAYS));
            d.add(track);
            track = new TabbedAccordionTrack();
            track.getDockChildren().addAll(addInspector(new DrawingInspector(), "drawing", Priority.NEVER),
                    addInspector(new GridInspector(), "grid", Priority.NEVER),
                    addInspector(new HandlesInspector(), "handles", Priority.NEVER),
                    addInspector(new HelpTextInspector(), "helpText", Priority.NEVER));
            d.add(track);
            return d;
        }).whenComplete((list, e) -> {
            if (e == null) {
                VBoxTrack vtrack = new VBoxTrack();
                SequencedSet<Dockable> items = new LinkedHashSet<>();
                for (Track track : list) {
                    for (DockChild n : track.getDockChildren()) {
                        if (n instanceof Dockable dd) {
                            items.add(dd);
                            @SuppressWarnings("unchecked")
                            Inspector<DrawingView> i = (Inspector<DrawingView>) dd.getNode().getProperties().get("inspector");
                            i.setSubject(drawingView);
                        }
                    }
                    vtrack.getDockChildren().add(track);
                }
                SplitPaneTrack htrack = SplitPaneTrack.createHorizontalTrack();
                htrack.getDockChildren().add(viewScrollPaneDockItem);
                htrack.getDockChildren().add(vtrack);
                this.dockRoot.getDockChildren().setAll(htrack);
                this.dockRoot.setDockablePredicate(items::contains);
            } else {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

            }
        }).exceptionally((e) -> {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

            //noinspection ReturnOfNull
            return null;
        });
    }

    @Override
    public CompletionStage<Void> print(PrinterJob job, WorkState<Void> workState) {
        Drawing drawing = drawingView.getDrawing();
        return FXWorker.run(() -> {
            try {
                PrinterExportFormat pof = new PrinterExportFormat();
                pof.print(job, drawing);
            } finally {
                job.endJob();
            }
        });

    }

    @Override
    public CompletionStage<DataFormat> read(URI uri, DataFormat format, ImmutableMap<Key<?>, Object> options, boolean insert, WorkState<Void> workState) {
        return FXWorker.supply(Executors.newSingleThreadExecutor(), () -> {
            IdFactory idFactory = new SimpleFigureIdFactory();
            FigureFactory factory = new DefaultFigureFactory(idFactory);
            SimpleXmlReader io = new SimpleXmlReader(factory, idFactory, GRAPHER_NAMESPACE_URI);
            AbstractDrawing drawing = (AbstractDrawing) io.read(uri, null, workState);
            applyUserAgentStylesheet(drawing);
            return drawing;
        }).thenApply(drawing -> {
            drawingView.setDrawing(drawing);
            undoManager.discardAllEdits();
            return format;
        });
    }

    @Override
    public CompletionStage<Void> write(URI uri, DataFormat format, ImmutableMap<Key<?>, Object> options, WorkState<Void> workState) {
        Drawing drawing = drawingView.getDrawing();
        return FXWorker.run(Executors.newSingleThreadExecutor(), () -> {
            if (registerDataFormat(FXSvgTinyWriter.SVG_MIME_TYPE_WITH_VERSION).equals(format)) {
                SvgExportOutputFormat io = new SvgExportOutputFormat();
                io.setExporterFactory(FXSvgTinyWriter::new);
                io.setOptions(options);
                io.write(uri, drawing, workState);
            } else if (registerDataFormat(FXSvgFullWriter.SVG_MIME_TYPE).equals(format)
                    || registerDataFormat(FXSvgFullWriter.SVG_MIME_TYPE_WITH_VERSION).equals(format)
                    || uri.getPath().endsWith(".svg")) {
                SvgExportOutputFormat io = new SvgExportOutputFormat();
                io.setOptions(options);
                io.write(uri, drawing, workState);
            } else if (registerDataFormat(BitmapExportOutputFormat.PNG_MIME_TYPE).equals(format) || uri.getPath().endsWith(".png")) {
                BitmapExportOutputFormat io = new BitmapExportOutputFormat(BitmapExportOutputFormat.PNG_MIME_TYPE, BitmapExportOutputFormat.PNG_EXTENSION);
                io.setOptions(options);
                io.write(uri, drawing, workState);
            } else if (registerDataFormat(BitmapExportOutputFormat.JPEG_MIME_TYPE).equals(format) || uri.getPath().endsWith(".jpg")) {
                BitmapExportOutputFormat io = new BitmapExportOutputFormat(BitmapExportOutputFormat.JPEG_MIME_TYPE, BitmapExportOutputFormat.JPEG_EXTENSION);
                io.setOptions(options);
                io.write(uri, drawing, workState);
            } else if (registerDataFormat(XmlEncoderOutputFormat.XML_SERIALIZER_MIME_TYPE).equals(format) || uri.getPath().endsWith(".ser.xml")) {
                XmlEncoderOutputFormat io = new XmlEncoderOutputFormat();
                io.write(uri, drawing, workState);
            } else {
                DefaultFigureFactory factory = new DefaultFigureFactory();
                IdFactory idFactory = factory.getIdFactory();
                SimpleXmlWriter io = new SimpleXmlWriter(factory, idFactory, GRAPHER_NAMESPACE_URI, null);
                io.write(uri, drawing, workState);
            }
        }).handle((voidvalue, ex) -> {
            if (ex != null) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

            }
            return null;
        });
    }

    private void applyUserAgentStylesheet(final Drawing d) {
        try {
            d.set(Drawing.USER_AGENT_STYLESHEETS,
                    VectorList.of(
                            GrapherActivity.class.getResource("user-agent.css").toURI()));
            d.updateStyleManager();
            final SimpleRenderContext ctx = new SimpleRenderContext();
            for (final Figure f : d.preorderIterable()) {
                f.updateCss(ctx);
            }
            // d.layoutAll(ctx);

        } catch (final URISyntaxException e) {
            throw new RuntimeException("can't load my own resources", e);
        }
    }
}
