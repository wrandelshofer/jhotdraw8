/*
 * @(#)SimpleXmlStaxReader.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.io;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.GroupFigure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.input.ClipboardInputFormat;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.xml.XmlUtil;
import org.jspecify.annotations.Nullable;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This reader does not support {@link FigureFactory#nodeListToValue(MapAccessor, List)}.
 */
public class SimpleXmlReader extends AbstractInputFormat implements ClipboardInputFormat {
    private static final Pattern hrefPattern = Pattern.compile("\\s+href=\"([^\"]*?)\"");
    private final IdFactory idFactory;
    private @Nullable String namespaceURI;
    private FigureFactory figureFactory;
    private final String idAttribute = "id";
    private Supplier<Layer> layerFactory;

    public SimpleXmlReader(FigureFactory figureFactory, IdFactory idFactory, @Nullable String namespaceURI) {
        this.idFactory = idFactory;
        this.figureFactory = figureFactory;
        this.namespaceURI = namespaceURI;
    }

    private Figure createFigure(XMLStreamReader r, Deque<Figure> stack) throws IOException {
        Figure figure;
        try {
            figure = figureFactory.createFigureByElementName(r.getLocalName());
        } catch (IOException e) {
            throw new IOException("Cannot create figure for element <" + r.getLocalName() + "> at line " + r.getLocation().getLineNumber() + ", col " + r.getLocation().getColumnNumber(), e);
        }
        if (stack.isEmpty()) {
            stack.addFirst(figure);// add twice, so that it will remain after we finish the file
        } else {
            Figure parent = stack.peek();
            if (!figure.isSuitableParent(parent) || !parent.isSuitableChild(figure)) {
                throw new IOException("Cannot add figure to parent in element <" + r.getLocalName() + "> at line " + r.getLocation().getLineNumber() + ", col " + r.getLocation().getColumnNumber());
            }
            parent.getChildren().add(figure);
        }
        stack.addFirst(figure);
        return figure;
    }

    private DataFormat getDataFormat() {
        String mimeType = "application/xml";
        DataFormat df = DataFormat.lookupMimeType(mimeType);
        if (df == null) {
            df = new DataFormat(mimeType);
        }
        return df;
    }

    public IdFactory getIdFactory() {
        return idFactory;
    }

    public Supplier<Layer> getLayerFactory() {
        return layerFactory;
    }

    public void setLayerFactory(Supplier<Layer> layerFactory) {
        this.layerFactory = layerFactory;
    }

    @Override
    public Figure read(InputStream in, @Nullable Drawing drawing, @Nullable URI documentHome, WorkState<Void> workState) throws IOException {
        return read((AutoCloseable) in, drawing, documentHome, workState);
    }

    /**
     * Reads from the specified input stream.
     *
     * @param in must be an instanceof {@link InputStream} or of {@link Reader}.
     * @return a deque of the figures in the files
     * @throws IOException on IO failure
     */
    private Deque<Figure> doRead(AutoCloseable in) throws IOException {

        Deque<Figure> stack = new ArrayDeque<>();
        List<Runnable> secondPass = new ArrayList<>();
        List<Runnable> parallelPass = new ArrayList<>();
        List<FutureTask<Void>> futures = new ArrayList<>();
        List<Consumer<Figure>> processingInstructions = new ArrayList<>();
        try {
            XMLStreamReader xmlStreamReader = XmlUtil.streamReader(
                    (in instanceof InputStream inputStream) ? new StreamSource(inputStream)
                            : new StreamSource((Reader)in));
            while (xmlStreamReader.hasNext()) {
                readNode(xmlStreamReader, xmlStreamReader.next(), stack, processingInstructions,
                        secondPass, parallelPass, futures);
            }

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        if (stack.size() != 1) {
            throw new IOException("The file does not contain a root element in namespace=\"" + namespaceURI + "\".");
        }

        for (Consumer<Figure> processingInstruction : processingInstructions) {
            processingInstruction.accept(stack.getFirst());
        }
        forkParallelPass(futures, parallelPass);
        try {
            secondPass.parallelStream().forEach(Runnable::run);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        try {
            // If there is not enough parallelism, this future may never return!
            for (FutureTask<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
        return stack;
    }

    public @Nullable Figure read(Reader in, @Nullable Drawing drawing, @Nullable URI documentHome, WorkState<Void> workState) throws IOException {
        return read((AutoCloseable) in, drawing, documentHome, workState);
    }

    private Figure read(AutoCloseable in, @Nullable Drawing drawing, @Nullable URI documentHome, WorkState<Void> workState) throws IOException {
        workState.updateProgress(0.0);
        idFactory.setDocumentHome(documentHome);
        Deque<Figure> stack = doRead(in);

        Figure figure = stack.isEmpty() ? null : stack.getFirst();
        if (figure == null) {
            throw new IOException("Input file is empty.");
        }
        if ((figure instanceof Drawing d)) {
            figure.set(Drawing.DOCUMENT_HOME, documentHome);

            for (Figure f : figure.preorderIterable()) {
                f.addedToDrawing(d);
            }

        }
        workState.updateProgress(1.0);
        return figure;
    }


    @Override
    public SequencedSet<Figure> read(Clipboard clipboard, DrawingModel model, Drawing drawing, @Nullable Figure parent) throws IOException {
        Object content = clipboard.getContent(getDataFormat());
        if (content instanceof String) {
            SequencedSet<Figure> figures = new LinkedHashSet<>();
            Figure newDrawing = read(new StringReader((String) content), null, null, new SimpleWorkState<>());
            if (newDrawing == null) {
                return figures;
            }
            idFactory.reset();
            idFactory.setDocumentHome(null);
            for (Figure f : drawing.preorderIterable()) {
                idFactory.createId(f);
            }
            if (parent == null) {
                parent = layerFactory.get();
            }
            if (parent.getDrawing() != drawing) {
                model.addChildTo(parent, drawing);
            }
            for (Figure f : new ArrayList<>(newDrawing.getChildren())) {
                figures.add(f);
                newDrawing.removeChild(f);
                String id = idFactory.createId(f);
                f.set(StyleableFigure.ID, id);
                if (f instanceof Layer) {
                    model.addChildTo(f, drawing);
                } else {
                    model.addChildTo(f, parent);
                }
            }
            return figures;
        } else {
            throw new IOException("no data found");
        }

    }

    private void readAttributes(XMLStreamReader r, Figure figure, List<Runnable> secondPass, List<Runnable> parallelPass) throws IOException {
        for (int i = 0, n = r.getAttributeCount(); i < n; i++) {
            String ns = r.getAttributeNamespace(i);
            if (namespaceURI != null && ns != null && !namespaceURI.equals(ns)) {
                continue;
            }
            String attributeLocalName = r.getAttributeLocalName(i);
            String attributeValue = r.getAttributeValue(i);
            Location location = r.getLocation();
            if (idAttribute.equals(attributeLocalName)) {
                idFactory.putIdToObject(attributeValue, figure);
                setId(figure, attributeValue);
            } else {
                @SuppressWarnings("unchecked")
                MapAccessor<Object> key =
                        (MapAccessor<Object>) figureFactory.getKeyByAttributeName(figure, attributeLocalName);
                if (key == null) {
                    throw new IOException("Unsupported attribute \"" + attributeLocalName + "\" at line " + location.getLineNumber() + ", col " + location.getColumnNumber());
                }
                List<Runnable> pass = figureFactory.needsIdResolver(key) ? secondPass : parallelPass;
                pass.add(() -> {
                    try {
                        figure.set(key, figureFactory.stringToValue(key, attributeValue));
                    } catch (IOException e) {
                        throw new UncheckedIOException(
                                "Error reading attribute" + attributeLocalName + "\" at line " + location.getLineNumber() + ", col " + location.getColumnNumber(),
                                e);
                    }
                });

            }
        }
    }

    private void readEndElement(XMLStreamReader r, Deque<Figure> stack) {
        stack.removeFirst();
    }

    private void readNode(XMLStreamReader r, int next, Deque<Figure> stack,
                          List<Consumer<Figure>> processingInstructions,
                          List<Runnable> secondPass, List<Runnable> parallelPass,
                          List<FutureTask<Void>> futures) throws IOException {
        switch (next) {
        case XMLStreamReader.START_ELEMENT:
            readStartElement(r, stack, secondPass, parallelPass);
            break;
        case XMLStreamReader.END_ELEMENT:
            readEndElement(r, stack);
            break;
        case XMLStreamReader.PROCESSING_INSTRUCTION:
            Consumer<Figure> processingInstruction = readProcessingInstruction(r, stack, secondPass);
            if (processingInstruction != null) {
                processingInstructions.add(processingInstruction);
            }
            break;
        case XMLStreamReader.CHARACTERS:
        case XMLStreamReader.ENTITY_DECLARATION:
        case XMLStreamReader.NOTATION_DECLARATION:
        case XMLStreamReader.NAMESPACE:
        case XMLStreamReader.CDATA:
        case XMLStreamReader.DTD:
        case XMLStreamReader.ATTRIBUTE:
        case XMLStreamReader.ENTITY_REFERENCE:
        case XMLStreamReader.END_DOCUMENT:
        case XMLStreamReader.START_DOCUMENT:
        case XMLStreamReader.SPACE:
        case XMLStreamReader.COMMENT:
            break;
        default:
            throw new IOException("unsupported XMLStream event: " + next);
        }

        if (parallelPass.size() > 1_000) {
            ArrayList<Runnable> runParallel = new ArrayList<>(parallelPass);
            parallelPass.clear();
            forkParallelPass(futures, runParallel);
        }
    }

    private void forkParallelPass(List<FutureTask<Void>> futures, List<Runnable> runParallel) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            for (final Runnable runner : runParallel) {
                runner.run();
            }
            return null;
        });
        if (ForkJoinPool.getCommonPoolParallelism() < 2) {
            // When there is not enough parallelism, then the reader may saturate
            // the pool!
            task.run();
        } else {
            ForkJoinPool.commonPool().execute(task);
        }
        futures.add(task);
    }

    private @Nullable Consumer<Figure> readProcessingInstruction(XMLStreamReader r, Deque<Figure> stack, List<Runnable> secondPass) {
        if (figureFactory.getStylesheetsKey() != null) {
            String piTarget = r.getPITarget();
            String piData = r.getPIData();

            if ("xml-stylesheet".equals(piTarget) && piData != null) {
                return (drawing -> {
                    Matcher m = hrefPattern.matcher(piData);
                    if (m.find()) {
                        String href = m.group(1);

                        URI uri = URI.create(href);
                        uri = idFactory.absolutize(uri);
                        ImmutableList<URI> listOrNull = drawing.get(figureFactory.getStylesheetsKey());
                        List<URI> stylesheets = listOrNull == null ? new ArrayList<>() : new ArrayList<>(listOrNull.asList());
                        stylesheets.add(uri);
                        drawing.set(figureFactory.getStylesheetsKey(), VectorList.copyOf(stylesheets));
                    }
                });
            }
        }
        return null;
    }

    private void readStartElement(XMLStreamReader r, Deque<Figure> stack,
                                  List<Runnable> secondPass, List<Runnable> parallelPass) throws IOException {
        if (namespaceURI != null && !namespaceURI.equals(r.getNamespaceURI())) {
            stack.push(new GroupFigure());// push a dummy figure
            return;
        }

        Figure figure = createFigure(r, stack);
        readAttributes(r, figure, secondPass, parallelPass);
    }

    public void setFigureFactory(FigureFactory figureFactory) {
        this.figureFactory = figureFactory;
    }

    protected void setId(Figure figure, String id) {
        figure.set(StyleableFigure.ID, id);
    }

    public void setNamespaceURI(@Nullable String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }
}
