/**
 * Sample Skeleton for 'ColorChooserPane.fxml' Controller Class
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * HSL Chooser.
 * <p>
 * Useful for the following color spaces:
 * <dl>
 *     <dt>HSL</dt><dd>Hue, Saturation, Lightness</dd>
 *     <dt>HSB</dt><dd>Hue, Saturation, Brightness</dd>
 *     <dt></dt><dd></dd>
 * </dl>
 * <pre>
 *           saturation→
 *           +--------------+ +---+
 *           |              | |   |
 *         ↑ |              | |   | ↑
 * lightness |              | |   | hue
 *           +--------------+ +---+
 * </pre>
 */
public class HlsChooser extends HBox {


    public HlsChooser() {
        load();
    }

    private void load() {
        try {
            FXMLLoader loader = new FXMLLoader(HlsChooser.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.setResources(ResourceBundle.getBundle("org.jhotdraw8.fxcontrols.colorchooser.Labels"));
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private static URL getFxml() {
        String name = "HlsChooser.fxml";
        return Objects.requireNonNull(HlsChooser.class.getResource(name), name);
    }


    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="huePane"
    private StackPane huePane; // Value injected by FXMLLoader

    @FXML // fx:id="colorRectPane"
    private StackPane colorRectPane; // Value injected by FXMLLoader

    private ColorRectangleSlider colorRectSlider;
    private ColorSlider hueSlider;

    private final @NonNull ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model");
    private ChangeListener<NamedColorSpace> targetColorSpaceListener;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert huePane != null : "fx:id=\"huePane\" was not injected: check your FXML file 'HlsChooser.fxml'.";
        assert colorRectPane != null : "fx:id=\"colorRectPane\" was not injected: check your FXML file 'HlsChooser.fxml'.";

        Background checkerboardBackground = new Background(new BackgroundFill(createCheckerboardPattern(4, 0xffffffff, 0xffaaaaaa), null, null));
        colorRectPane.setBackground(checkerboardBackground);
        colorRectSlider = new ColorRectangleSlider();
        hueSlider = new ColorSlider();
        colorRectPane.getChildren().add(colorRectSlider);
        huePane.getChildren().add(hueSlider);

        Via<ColorChooserPaneModel> viaModel = new Via<>(model);

        hueSlider.setThumbTranslateX(1);
        hueSlider.setOrientation(Orientation.VERTICAL);
        hueSlider.c0Property().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get().map((NamedColorSpace v) ->
                0.5f * (v.getMaxValue(0) - v.getMinValue(0)) + v.getMinValue(0)
        ));
        hueSlider.c1Property().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get().map((NamedColorSpace v) ->
                0.5f * (v.getMaxValue(1) - v.getMinValue(1)) + v.getMinValue(1)
        ));
        hueSlider.c2Property().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get().map((NamedColorSpace v) ->
                0.5f * (v.getMaxValue(2) - v.getMinValue(2)) + v.getMinValue(2)
        ));
        hueSlider.valueProperty().addListener((o, oldv, newv) -> {
            ColorChooserPaneModel m = model.get();
            if (m != null && newv != null) {
                m.setComponent(m.getSourceColorSpaceHueIndex(), newv.floatValue());
            }
        });
        hueSlider.componentIndexProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceHueIndexProperty).get());
        hueSlider.sourceColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get());
        hueSlider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        hueSlider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        hueSlider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));

        colorRectSlider.c0Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c0Property).get());
        colorRectSlider.c1Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c1Property).get());
        colorRectSlider.c2Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c2Property).get());
        colorRectSlider.c3Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c3Property).get());
        colorRectSlider.alphaProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::alphaProperty).get());
        colorRectSlider.sourceColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get());
        colorRectSlider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        colorRectSlider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        colorRectSlider.xComponentIndexProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceSaturationChromaIndexProperty).get());
        colorRectSlider.yComponentIndexProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceLightnessValueIndexProperty).get());
        colorRectSlider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));


    }

    /**
     * Creates a checkerboard image pattern.
     *
     * @param size     size of a checkerboard tile
     * @param evenArgb color for even tiles
     * @param oddArgb  color for odd tiles
     * @return the image pattern
     */
    public static ImagePattern createCheckerboardPattern(int size, int evenArgb, int oddArgb) {
        var p = checkerboardPattern;
        if (p == null || p.getWidth() != size * 2) {
            int width = size * 2;
            int height = width;
            PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
            IntBuffer intBuffer = IntBuffer.allocate(width * height);
            var pixelBuffer = new PixelBuffer<>(width, height, intBuffer, pixelFormat);
            WritableImage image = new WritableImage(pixelBuffer);
            int[] a = intBuffer.array();

            // fill first even line
            Arrays.fill(a, 0, size, evenArgb);
            Arrays.fill(a, size, size * 2, oddArgb);

            // fill first odd line
            int xy = size * width;
            Arrays.fill(a, xy, xy + size, oddArgb);
            Arrays.fill(a, xy + size, xy + size * 2, evenArgb);

            for (int y = 1; y < size; y++) {
                xy = width * y;
                System.arraycopy(a, 0, a, xy, width);
                System.arraycopy(a, size * width, a, xy + size * width, width);
            }

            p = new ImagePattern(image, 0, 0, width, height, false);
            checkerboardPattern = p;
        }
        return p;
    }

    private static @Nullable ImagePattern checkerboardPattern;

    public ColorChooserPaneModel getModel() {
        return model.get();
    }

    public @NonNull ObjectProperty<ColorChooserPaneModel> modelProperty() {
        return model;
    }

    public void setModel(ColorChooserPaneModel model) {
        this.model.set(model);
    }
}
