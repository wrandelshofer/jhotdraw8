/*
 * @(#)SvgReadWriteAndCompareTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package io;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.render.SimpleDrawingRenderer;
import org.jhotdraw8.svg.io.FXSvgTinyWriter;
import org.jhotdraw8.svg.io.FigureSvgTinyReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * The tests in this class perform the following steps:
 * <ol>
 *     <li>Create JavaFX scene graph from an SVG file.</li>
 *     <li>Render the JavaFX scene graph into a Bitmap (expected bitmap).</li>
 *     <li>Write a new SVG file into a temporary file (byte array).</li>
 *     <li>Create a JavaFX scene graph from that temporary file.</li>
 *     <li>Render the JavaFX scene graph into a Bitmap (actual bitmap).</li>
 *     <li>Asser that actual bitmap is equal to expected bitmap.</li>
 * </ol>
 */
public class SvgReadWriteAndCompareTest {
    /**
     * Set this constant to the path of the folder into which you checked
     * out the SVG Tiny 1.2 test suite.
     * <p>
     * <a href="https://dev.w3.org/cvsweb/SVG/profiles/1.2T/test/archives/W3C_SVG_12_TinyTestSuite.tar.gz">dev.w3.org</a>
     */
    private static final String W3C_SVG_12_TINY_TEST_SUITE = "data/W3C_SVG_12_TinyTestSuite";
    private static final boolean INTERACTIVE = true;
    private static final long INTERACTIVE_TIMEOUT_SECONDS = 60;

    /**
     * This launcher starts the JavaFX application thread.
     */
    public static class Launcher extends Application {
        public Launcher() {

        }

        @Override
        public void start(Stage primaryStage) throws Exception {
        }

        public void launch() {
            new Thread(() -> {
                try {
                    Application.launch();
                } catch (IllegalStateException e) {
                    // javafx is already launched
                }
            }).start();
        }
    }

    @BeforeAll
    public static void startJavaFX() throws InterruptedException, ExecutionException, TimeoutException {
        Platform.setImplicitExit(false);
        new Launcher().launch();
    }

    @TestFactory
    public @NonNull Stream<DynamicTest> dynamicTestsW3cSvgTiny12TestSuite() throws IOException {
        if (!Files.isDirectory(Path.of(W3C_SVG_12_TINY_TEST_SUITE))) {
            System.err.println("Please fix the path to W3C SVG 1.2 Tiny Test Suite: " +
                    Path.of(W3C_SVG_12_TINY_TEST_SUITE).toAbsolutePath());
            return Stream.empty();
        }

        return Files.walk(Path.of(W3C_SVG_12_TINY_TEST_SUITE, "svggen"))
                .filter(f -> f.getFileName().toString().endsWith(".svg"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .map(p -> dynamicTest(p.getFileName().toString()
                        , () -> doTest(p)));

    }

    private void doTest(Path testFile) throws Exception {

        FigureSvgTinyReader reader = new FigureSvgTinyReader();
        reader.setBestEffort(true);
        Figure drawing1 = reader.read(testFile);

        SimpleDrawingRenderer r = new SimpleDrawingRenderer();
        Node drawing1Node = r.render(drawing1);

        FXSvgTinyWriter writer = new FXSvgTinyWriter(null, null);
        ByteArrayOutputStream bufOutputStream = new ByteArrayOutputStream();

        CssRectangle2D drawing1Bounds = drawing1.getCssLayoutBounds();
        CssDimension2D drawing1Size = new CssDimension2D(drawing1Bounds.getWidth(), drawing1Bounds.getHeight());
        writer.write(bufOutputStream, drawing1Node, drawing1Size);

        ByteArrayInputStream bufInputStream = new ByteArrayInputStream(bufOutputStream.toByteArray());
        reader.setBestEffort(false);
        Figure drawing2 = reader.read(new StreamSource(bufInputStream));
        Node drawing2Node = r.render(drawing2);

        CompletableFuture<OrderedPair<WritableImage, WritableImage>> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                WritableImage drawing2Image = drawing2Node.snapshot(new SnapshotParameters(), null);
                WritableImage drawing1Image = drawing1Node.snapshot(new SnapshotParameters(), null);
                future.complete(new SimpleOrderedPair<>(drawing1Image, drawing2Image));
            } catch (Throwable t) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception.", t);

                future.completeExceptionally(t);
            }
        });

        OrderedPair<WritableImage, WritableImage> pair = future.get(1, TimeUnit.MINUTES);
        WritableImage actualImage = pair.first();
        WritableImage expectedImage = pair.second();
        checkImages(testFile, actualImage, expectedImage);

    }

    private void checkImages(Path testFile,
                             WritableImage actualImage, WritableImage expectedImage) throws InterruptedException, ExecutionException, TimeoutException {

        IntBuffer actualBuffer = createIntBuffer(actualImage);
        IntBuffer expectedBuffer = createIntBuffer(expectedImage);

        if (INTERACTIVE && !actualBuffer.equals(expectedBuffer)) {
            CompletableFuture<Boolean> waitUntilClosed = new CompletableFuture<>();
            AtomicReference<Stage> stageRef = new AtomicReference<>(null);
            WritableImage markedDifferences = markDifferences(actualImage, actualBuffer, expectedBuffer);
            Platform.runLater(() -> {
                try {
                    Stage stage = new Stage();
                    stageRef.set(stage);
                    GridPane grid = new GridPane();
                    grid.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderStroke.THICK)));
                    grid.add(new ImageView(actualImage), 0, 0);
                    grid.add(new ImageView(expectedImage), 1, 0);
                    grid.add(new ImageView(markedDifferences), 1, 1);
                    StackPane stackPane = new StackPane();
                    ImageView imageView = new ImageView(actualImage);
                    ImageView imageView1 = new ImageView(expectedImage);
                    imageView1.setBlendMode(BlendMode.DIFFERENCE);
                    stackPane.getChildren().setAll(imageView, imageView1);
                    grid.add(stackPane, 0, 1);
                    stage.setScene(new Scene(grid));
                    stage.sizeToScene();
                    stage.setWidth(Math.max(100, stage.getWidth()));
                    stage.setHeight(Math.max(100, stage.getHeight()));
                    stage.setTitle(testFile.getName(testFile.getNameCount() - 1).toString());
                    stage.show();
                    stage.setOnCloseRequest(evt -> {
                        stage.close();
                        waitUntilClosed.complete(Boolean.TRUE);
                    });
                } catch (Throwable t) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception.", t);

                    waitUntilClosed.completeExceptionally(t);

                }
            });
            try {
                waitUntilClosed.get(INTERACTIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                // close stage, move to next test
                Platform.runLater(() -> {
                    Stage stage = stageRef.get();
                    if (stage != null) {
                        stage.close();
                    }
                });
            }
        }

        assertArrayEquals(expectedBuffer.array(), actualBuffer.array());
    }

    @NonNull
    private WritableImage markDifferences(WritableImage actualImage, IntBuffer actualBuffer, IntBuffer expectedBuffer) {
        WritableImage markedDifferences = new WritableImage((int) actualImage.getWidth(), (int) actualImage.getHeight());
        int[] aa = actualBuffer.array();
        int[] ea = expectedBuffer.array();
        PixelWriter pw = markedDifferences.getPixelWriter();
        int w = (int) actualImage.getWidth();
        for (int i = 0; i < aa.length; i++) {
            if (aa[i] != ea[i]) {
                pw.setArgb(i % w, i / w, 0xff000000);
            }
        }
        return markedDifferences;
    }

    private @NonNull IntBuffer createIntBuffer(WritableImage actualImage) {
        int w = (int) actualImage.getWidth();
        int h = (int) actualImage.getHeight();
        IntBuffer intBuffer = IntBuffer.allocate(w * h);
        actualImage.getPixelReader().getPixels(0, 0, w, h,
                WritablePixelFormat.getIntArgbInstance(), intBuffer, w);
        return intBuffer;
    }
}
