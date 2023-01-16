/*
 * @(#)InputFormat.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * InputFormat.
 *
 * @author Werner Randelshofer
 */
public interface InputFormat {
    void setOptions(@NonNull ReadOnlyMap<Key<?>, Object> newValue);

    @NonNull ReadOnlyMap<Key<?>, Object> getOptions();

    /**
     * Reads a figure from an URI
     *
     * @param uri       The uri.
     * @param drawing   If you provide a non-null value, the ids of the returned
     *                  figure are coerced so that they do not clash with ids in the drawing.
     *                  Also all URIs in the figure are made relative to DOCUMENT_HOME of the
     *                  drawing.
     * @param workState for progress monitoring and cancelling the operation
     * @return the figure
     * @throws java.io.IOException if an IO error occurs
     */
    default @NonNull Figure read(@NonNull URI uri, @Nullable Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        return read(Paths.get(uri), drawing, workState);
    }

    /**
     * Reads a figure from a file.
     *
     * @param file      the file
     * @param drawing   If you provide a non-null value, the ids of the returned
     *                  figure are coerced so that they do not clash with ids in the drawing.
     *                  Also all URIs in the figure are made relative to DOCUMENT_HOME of the
     *                  drawing.
     * @param workState for progress monitoring and cancelling the operation
     * @return the figure
     * @throws java.io.IOException if an IO error occurs
     */
    default @NonNull Figure read(@NonNull Path file, @Nullable Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        URI documentHome = file.getParent() == null ? FileSystems.getDefault().getPath(System.getProperty("user.home")).toUri() : file.getParent().toUri();
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            return read(in, drawing, documentHome, workState);
        } catch (IOException e) {
            throw new IOException("Error reading file " + file, e);
        }
    }

    /**
     * Reads figures from an input stream and adds them to the specified
     * drawing.
     *
     * @param in           The input stream.
     * @param drawing      If you provide a non-null value, the contents of the file
     *                     is added to the drawing. Otherwise a new drawing is created.
     * @param documentHome the URI used to resolve external references from the document
     * @param workState    for progress monitoring and cancelling the operation
     * @return the drawing
     * @throws java.io.IOException if an IO error occurs
     */
    @NonNull Figure read(@NonNull InputStream in, Drawing drawing, URI documentHome, @NonNull WorkState<Void> workState) throws IOException;

}
