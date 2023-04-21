/*
 * @(#)OutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * OutputFormat.
 *
 * @author Werner Randelshofer
 */
public interface OutputFormat {
    void setOptions(@NonNull ImmutableMap<Key<?>, Object> newValue);

    @NonNull ImmutableMap<Key<?>, Object> getOptions();

    /**
     * Writes a Drawing into the resource identified by the given URI.
     *
     * @param uri       The resource identifier
     * @param drawing   The drawing.
     * @param workState for progress monitoring and cancelling the operation
     * @throws java.io.IOException if an IO error occurs
     */
    default void write(@NonNull URI uri, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        write(Paths.get(uri), drawing, workState);
    }

    /**
     * Writes the drawing to the specified file. This method ensures that all
     * figures of the drawing are visible on the image.
     *
     * @param file      the file
     * @param drawing   the drawing
     * @param workState for progress monitoring and cancelling the operation
     * @throws java.io.IOException if an IO error occurs
     */
    default void write(@NonNull Path file, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            write(out, file.getParent().toUri(), drawing, workState);
        }
    }

    /**
     * Writes a Drawing into an output stream.
     *
     * @param out          The output stream.
     * @param documentHome Document home URI for creating relative URIs in the document
     *                     if this URI is null, all URIs in the document will be absolute
     * @param drawing      The drawing.
     * @param workState    for progress monitoring and cancelling the operation
     * @throws java.io.IOException if an IO error occurs
     */
    void write(@NonNull OutputStream out, @Nullable URI documentHome, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException;

}
