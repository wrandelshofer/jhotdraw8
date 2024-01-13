package org.jhotdraw8.examples.mini;


import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jhotdraw8.draw.inspector.InspectorLabels;
import org.jhotdraw8.geom.AwtPathBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.MultipleMasterShape;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MultipleMasterShapeController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="defaultShapeField"
    private TextArea defaultSvgPathField; // Value injected by FXMLLoader

    @FXML // fx:id="dim1Slider"
    private Slider dim1Slider; // Value injected by FXMLLoader

    @FXML // fx:id="region"
    private Region region; // Value injected by FXMLLoader
    @FXML // fx:id="dim2Slider"
    private Slider dim2Slider; // Value injected by FXMLLoader
    @FXML // fx:id="scaleFactorSlider"
    private Slider scaleFactorSlider; // Value injected by FXMLLoader

    @FXML // fx:id="dim1SvgPathField"
    private TextArea dim1SvgPathField; // Value injected by FXMLLoader

    @FXML // fx:id="dim2SvgPathField"
    private TextArea dim2SvgPathField; // Value injected by FXMLLoader

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert defaultSvgPathField != null : "fx:id=\"defaultShapeField\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";
        assert scaleFactorSlider != null : "fx:id=\"scaleFactorSlider\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";
        assert dim1Slider != null : "fx:id=\"dim1Slider\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";
        assert dim2Slider != null : "fx:id=\"dim2Slider\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";
        assert dim1SvgPathField != null : "fx:id=\"maxDim1Field\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";
        assert dim2SvgPathField != null : "fx:id=\"maxDim2Field\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";
        assert region != null : "fx:id=\"region\" was not injected: check your FXML file 'MultipleMasterShape.fxml'.";


        region.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        region.setCenterShape(true);
        region.setScaleShape(false);
        defaultSvgPathProperty().addListener(this::updateShape);
        dim1SvgPathProperty().addListener(this::updateShape);
        dim2SvgPathProperty().addListener(this::updateShape);
        dim1Slider.valueProperty().addListener(this::updateShape);
        dim2Slider.valueProperty().addListener(this::updateShape);
        scaleFactorSlider.valueProperty().addListener(this::updateShape);
    }

    private void updateShape(Observable observable) {
        region.setShape(null);

        List<PathIterator> paths = new ArrayList<>();
        try {
            if (defaultSvgPathProperty().get().isEmpty()) return;
            paths.add(SvgPaths.buildFromSvgString(new AwtPathBuilder(), defaultSvgPathProperty().get()).build().getPathIterator(null));
            paths.add(SvgPaths.buildFromSvgString(new AwtPathBuilder(), dim1SvgPathProperty().get().isEmpty() ? defaultSvgPathProperty().get() : dim1SvgPathProperty().get()).build().getPathIterator(null));
            paths.add(SvgPaths.buildFromSvgString(new AwtPathBuilder(), dim2SvgPathProperty().get().isEmpty() ? defaultSvgPathProperty().get() : dim2SvgPathProperty().get()).build().getPathIterator(null));
            MultipleMasterShape mmShape = new MultipleMasterShape(paths);
            mmShape.setWeight(0, dim1Slider.getValue() / 100.0);
            mmShape.setWeight(1, dim2Slider.getValue() / 100.0);

            double scaleFactor = Math.pow(2.0, scaleFactorSlider.getValue());
            AffineTransform tx = new AffineTransform();
            tx.scale(scaleFactor, scaleFactor);
            region.setShape(FXShapes.fxShapeFromAwt(mmShape, tx));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Map.Entry<Parent, MultipleMasterShapeController> newInstance() {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(InspectorLabels.getResources().asResourceBundle());
        MultipleMasterShapeController controller = new MultipleMasterShapeController();
        loader.setController(controller);

        URL fxmlUrl = MultipleMasterShapeController.class.getResource("MultipleMasterShape.fxml");
        try (InputStream in = fxmlUrl.openStream()) {
            Parent root = loader.load(in);
            return new AbstractMap.SimpleImmutableEntry<>(root, controller);
        } catch (IOException ex) {
            throw new InternalError(ex);
        }
    }

    StringProperty defaultSvgPathProperty() {
        return defaultSvgPathField.textProperty();
    }

    StringProperty dim1SvgPathProperty() {
        return dim1SvgPathField.textProperty();
    }

    StringProperty dim2SvgPathProperty() {
        return dim2SvgPathField.textProperty();
    }

    DoubleProperty scaleFactorProperty() {
        return scaleFactorSlider.valueProperty();
    }

    DoubleProperty dim1Property() {
        return dim1Slider.valueProperty();
    }

    DoubleProperty dim2Property() {
        return dim2Slider.valueProperty();
    }
}



