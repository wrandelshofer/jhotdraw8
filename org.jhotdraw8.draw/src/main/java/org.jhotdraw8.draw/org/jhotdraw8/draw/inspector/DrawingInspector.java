/*
 * @(#)DrawingInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.converter.ColorCssConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.ViewBoxableDrawing;
import org.jhotdraw8.fxbase.binding.CustomBinding;
import org.jhotdraw8.fxbase.concurrent.PlatformUtil;
import org.jhotdraw8.fxbase.converter.StringConverterAdapter;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * FXML Controller class
 *
 * @author Werner Randelshofer
 */
public class DrawingInspector extends AbstractDrawingInspector {

    @FXML
    private TextField backgroundColorField;

    @FXML
    private ColorPicker backgroundColorPicker;

    private final Property<CssColor> myBackgroundProperty = new SimpleObjectProperty<>();
    private @Nullable Property<CssColor> boundBackgroundProperty;

    private final ChangeListener<CssSize> sizeCommitHandler = (o, oldv, newv) -> commitEdits();
    private final ChangeListener<CssColor> colorCommitHandler = (o, oldv, newv) -> commitEdits();
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private TextField heightField;
    private @Nullable Property<CssSize> xProperty;
    private @Nullable Property<CssSize> yProperty;
    private @Nullable Property<CssSize> heightProperty;

    private Node node;
    @FXML
    private TextField widthField;
    private @Nullable Property<CssSize> widthProperty;

    public DrawingInspector() {
        this(LayersInspector.class.getResource("DrawingInspector.fxml"));
    }

    public DrawingInspector(URL fxmlUrl) {
        init(fxmlUrl);
    }

    private void commitEdits() {
        DrawingView subject = getSubject();
        subject.getModel().fireTreeModelEvent(TreeModelEvent.nodeChanged(subject.getModel(), subject.getDrawing()));
    }

    @Override
    public Node getNode() {
        return node;
    }

    private void init(URL fxmlUrl) {
        // We must use invoke and wait here, because we instantiate Tooltips
        // which immediately instanciate a Window and a Scene.
        PlatformUtil.invokeAndWait(() -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(InspectorLabels.getResources().asResourceBundle());
            loader.setController(this);

            try (InputStream in = fxmlUrl.openStream()) {
                node = loader.load(in);
            } catch (IOException ex) {
                throw new InternalError(ex);
            }

            //noinspection ReturnOfNull
            CustomBinding.bindBidirectionalAndConvert(//
                    myBackgroundProperty,//
                    backgroundColorPicker.valueProperty(),//
                    (CssColor c) -> c == null ? null : c.getColor(), //
                    CssColor::new//
            );
            backgroundColorField.textProperty().bindBidirectional(myBackgroundProperty,
                    new StringConverterAdapter<>(new ColorCssConverter(false)));

        });


    }

    @Override
    protected void onDrawingChanged(@Nullable ObservableValue<? extends Drawing> observable, @Nullable Drawing oldValue, @Nullable Drawing newValue) {
        if (widthProperty != null) {
            widthField.textProperty().unbindBidirectional(widthProperty);
            widthProperty.removeListener(sizeCommitHandler);
        }
        if (heightProperty != null) {
            heightField.textProperty().unbindBidirectional(heightProperty);
            heightProperty.removeListener(sizeCommitHandler);
        }
        if (xProperty != null) {
            xField.textProperty().unbindBidirectional(xProperty);
            xProperty.removeListener(sizeCommitHandler);
        }
        if (yProperty != null) {
            yField.textProperty().unbindBidirectional(yProperty);
            yProperty.removeListener(sizeCommitHandler);
        }
        xProperty = null;
        yProperty = null;
        widthProperty = null;
        heightProperty = null;
        if (oldValue != null) {
            myBackgroundProperty.unbindBidirectional(boundBackgroundProperty);
            myBackgroundProperty.removeListener(colorCommitHandler);
            boundBackgroundProperty = null;
        }
        if (newValue != null) {
            xProperty = drawingModel.propertyAt(newValue, ViewBoxableDrawing.VIEW_BOX_X);
            yProperty = drawingModel.propertyAt(newValue, ViewBoxableDrawing.VIEW_BOX_Y);
            widthProperty = drawingModel.propertyAt(newValue, ViewBoxableDrawing.WIDTH);
            heightProperty = drawingModel.propertyAt(newValue, ViewBoxableDrawing.HEIGHT);
            boundBackgroundProperty = drawingModel.propertyAt(newValue, ViewBoxableDrawing.BACKGROUND);
            /*
            xProperty = ViewBoxableDrawing.VIEW_BOX_X.propertyAt(newValue.getProperties());
            yProperty = ViewBoxableDrawing.VIEW_BOX_Y.propertyAt(newValue.getProperties());
            widthProperty = ViewBoxableDrawing.WIDTH.propertyAt(newValue.getProperties());
            heightProperty = ViewBoxableDrawing.HEIGHT.propertyAt(newValue.getProperties());
            boundBackgroundProperty = Drawing.BACKGROUND.propertyAt(newValue.getProperties());
             */
            xProperty.addListener(sizeCommitHandler);
            yProperty.addListener(sizeCommitHandler);
            widthProperty.addListener(sizeCommitHandler);
            heightProperty.addListener(sizeCommitHandler);
            myBackgroundProperty.bindBidirectional(boundBackgroundProperty);
            myBackgroundProperty.addListener(colorCommitHandler);

            xField.textProperty().bindBidirectional(xProperty, new StringConverterAdapter<>(new SizeCssConverter(false)));
            yField.textProperty().bindBidirectional(yProperty, new StringConverterAdapter<>(new SizeCssConverter(false)));
            widthField.textProperty().bindBidirectional(widthProperty, new StringConverterAdapter<>(new SizeCssConverter(false)));
            heightField.textProperty().bindBidirectional(heightProperty, new StringConverterAdapter<>(new SizeCssConverter(false)));
        }
    }

}
