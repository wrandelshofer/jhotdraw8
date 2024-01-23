/*
 * @(#)SvgPaths.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import javafx.geometry.Bounds;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.NumberConverter;
import org.jhotdraw8.base.converter.XmlNumberConverter;
import org.jhotdraw8.base.io.StreamPosTokenizer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.logging.Logger;

/**
 * Provides methods for parsing and generating SVG path strings from AWT paths.
 */
public class SvgPaths {
    private static final Logger LOGGER = Logger.getLogger(SvgPaths.class.getName());

    /**
     * Don't let anyone instantiate this class.
     */
    private SvgPaths() {
    }

    /**
     * Returns a value as a SvgPath2D.
     * <p>
     * Also supports elliptical arc commands 'a' and 'A' as specified in
     * <a href="http://www.w3.org/TR/SVG/paths.html#PathDataEllipticalArcCommands">w3.org</a>
     *
     * @param str     the SVG path
     * @param builder the builder
     * @return the path builder
     * @throws ParseException if the String is not a valid path
     */
    public static <T> @NonNull PathBuilder<T> svgStringToBuilder(@NonNull String str, @NonNull PathBuilder<T> builder) throws ParseException {
        StreamPosTokenizer tt = new StreamPosTokenizer(new StringReader(str));
        try {

            tt.resetSyntax();
            tt.parseNumbers();
            tt.parseExponents();
            tt.parsePlusAsNumber();
            tt.whitespaceChars(0, ' ');
            tt.whitespaceChars(',', ',');

            char next = 'M';
            char command = 'M';
            double x = 0, y = 0; // current point
            double cx1 = 0, cy1 = 0, cx2 = 0, cy2 = 0;// control points
            double ix = 0, iy = 0; // initial point of subpath
            Commands:
            while (tt.nextToken() != StreamPosTokenizer.TT_EOF) {
                if (tt.ttype > 0) {
                    command = (char) tt.ttype;
                } else {
                    command = next;
                    tt.pushBack();
                }

                switch (command) {
                    case 'M':
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'M'");
                        ix = x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'M'");
                        iy = y = tt.nval;
                        builder.moveTo(x, y);
                        next = 'L';
                        break;
                    case 'm':
                        // relative-moveto dx dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 'm'");
                        ix = x += tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 'm'");
                        iy = y += tt.nval;
                        builder.moveTo(x, y);
                        next = 'l';

                        break;
                    case 'Z':
                    case 'z':
                        // close path
                        builder.closePath();
                        x = ix;
                        y = iy;
                        break;
                    case 'L':
                        // absolute-lineto x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'L'");
                        x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'L'");
                        y = tt.nval;
                        builder.lineTo(x, y);
                        next = 'L';

                        break;
                    case 'l':
                        // relative-lineto dx dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 'l'");
                        x += tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 'l'");
                        y += tt.nval;
                        builder.lineTo(x, y);
                        next = 'l';

                        break;
                    case 'H':
                        // absolute-horizontal-lineto x
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'H'");
                        x = tt.nval;
                        builder.lineTo(x, y);
                        next = 'H';

                        break;
                    case 'h':
                        // relative-horizontal-lineto dx
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 'h'");
                        x += tt.nval;
                        builder.lineTo(x, y);
                        next = 'h';

                        break;
                    case 'V':
                        // absolute-vertical-lineto y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'V'");
                        y = tt.nval;
                        builder.lineTo(x, y);
                        next = 'V';

                        break;
                    case 'v':
                        // relative-vertical-lineto dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 'v'");
                        y += tt.nval;
                        builder.lineTo(x, y);
                        next = 'v';

                        break;
                    case 'C':
                        // absolute-curveto x1 y1 x2 y2 x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x1 coordinate missing for 'C'");
                        cx1 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y1 coordinate missing for 'C'");
                        cy1 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x2 coordinate missing for 'C'");
                        cx2 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y2 coordinate missing for 'C'");
                        cy2 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'C'");
                        x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'C'");
                        y = tt.nval;
                        builder.curveTo(cx1, cy1, cx2, cy2, x, y);
                        next = 'C';
                        break;

                    case 'c':
                        // relative-curveto dx1 dy1 dx2 dy2 dx dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx1 coordinate missing for 'c'");
                        cx1 = x + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy1 coordinate missing for 'c'");
                        cy1 = y + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx2 coordinate missing for 'c'");
                        cx2 = x + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy2 coordinate missing for 'c'");
                        cy2 = y + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 'c'");
                        x += tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 'c'");
                        y += tt.nval;
                        builder.curveTo(cx1, cy1, cx2, cy2, x, y);
                        next = 'c';
                        break;

                    case 'S':
                        // absolute-shorthand-curveto x2 y2 x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x2 coordinate missing for 'S'");
                        cx2 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y2 coordinate missing for 'S'");
                        cy2 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'S'");
                        x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'S'");
                        y = tt.nval;
                        builder.smoothCurveTo(cx2, cy2, x, y);
                        next = 'S';
                        break;

                    case 's':
                        // relative-shorthand-curveto dx2 dy2 dx dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx2 coordinate missing for 's'");
                        cx2 = x + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy2 coordinate missing for 's'");
                        cy2 = y + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 's'");
                        x += tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 's'");
                        y += tt.nval;
                        builder.smoothCurveTo(cx2, cy2, x, y);
                        next = 's';
                        break;

                    case 'Q':
                        // absolute-quadto x1 y1 x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x1 coordinate missing for 'Q'");
                        cx1 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y1 coordinate missing for 'Q'");
                        cy1 = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'Q'");
                        x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'Q'");
                        y = tt.nval;
                        builder.quadTo(cx1, cy1, x, y);
                        next = 'Q';

                        break;

                    case 'q':
                        // relative-quadto dx1 dy1 dx dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx1 coordinate missing for 'q'");
                        cx1 = x + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy1 coordinate missing for 'q'");
                        cy1 = y + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 'q'");
                        x += tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 'q'");
                        y += tt.nval;
                        builder.quadTo(cx1, cy1, x, y);
                        next = 'q';

                        break;
                    case 'T':
                        // absolute-shorthand-quadto x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'T'");
                        x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'T'");
                        y = tt.nval;
                        builder.smoothQuadTo(x, y);
                        next = 'T';

                        break;

                    case 't':
                        // relative-shorthand-quadto dx dy
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dx coordinate missing for 't'");
                        x += tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "dy coordinate missing for 't'");
                        y += tt.nval;
                        builder.smoothQuadTo(x, y);
                        next = 's';

                        break;

                    case 'A': {
                        // absolute-elliptical-arc rx ry x-axis-rotation large-arc-flag sweep-flag x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "rx coordinate missing for 'A'");
                        // If rX or rY have negative signs, these are dropped;
                        // the absolute value is used instead.
                        double rx = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "ry coordinate missing for 'A'");
                        double ry = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x-axis-rotation missing for 'A'");
                        double xAxisRotation = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "large-arc-flag missing for 'A'");
                        boolean largeArcFlag = tt.nval != 0;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "sweep-flag missing for 'A'");
                        boolean sweepFlag = tt.nval != 0;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'A'");
                        x = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'A'");
                        y = tt.nval;

                        builder.arcTo(rx, ry, xAxisRotation, x, y, largeArcFlag, sweepFlag);
                        next = 'A';
                        break;
                    }
                    case 'a': {
                        // relative-elliptical-arc rx ry x-axis-rotation large-arc-flag sweep-flag x y
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "rx coordinate missing for 'A'");
                        // If rX or rY have negative signs, these are dropped;
                        // the absolute value is used instead.
                        double rx = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "ry coordinate missing for 'A'");
                        double ry = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x-axis-rotation missing for 'A'");
                        double xAxisRotation = tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "large-arc-flag missing for 'A'");
                        boolean largeArcFlag = tt.nval != 0;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "sweep-flag missing for 'A'");
                        boolean sweepFlag = tt.nval != 0;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "x coordinate missing for 'A'");
                        x = x + tt.nval;
                        tt.requireNextToken(StreamPosTokenizer.TT_NUMBER, "y coordinate missing for 'A'");
                        y = y + tt.nval;
                        builder.arcTo(rx, ry, xAxisRotation, x, y, largeArcFlag, sweepFlag);

                        next = 'a';
                        break;
                    }
                    default:
                        throw new ParseException("Illegal command: " + command + ".", tt.getStartPosition());
                }
            }
        } catch (ParseException e) {
            // We must build the path up to the illegal path element!
            // https://www.w3.org/TR/SVG/paths.html#PathDataErrorHandling
        } catch (IllegalPathStateException | IOException e) {
            throw new ParseException(e.getMessage(), tt.getStartPosition());
        }

        builder.pathDone();
        return builder;
    }

    /**
     * Converts a Java AWT Shape iterator to a JavaFX Shape.
     *
     * @param shape AWT Shape
     * @return SVG Path
     */
    public static @NonNull String awtPathIteratorToDoubleSvgString(@NonNull Shape shape) {
        return awtPathIteratorToDoubleSvgString(shape.getPathIterator(null));
    }

    /**
     * Converts a Java AWT Shape iterator to a JavaFX Shape.
     *
     * @param shape AWT Shape
     * @param at    Optional transformation which is applied to the shape
     * @return SVG Path
     */
    public static @NonNull String awtPathIteratorToDoubleSvgString(@NonNull Shape shape, AffineTransform at) {
        return awtPathIteratorToDoubleSvgString(shape.getPathIterator(at));
    }

    /**
     * Converts a Java Path iterator to a SVG path with double precision.
     *
     * @param iter AWT Path Iterator
     * @return SVG Path
     */
    public static @NonNull String awtPathIteratorToDoubleSvgString(@NonNull PathIterator iter) {
        XmlNumberConverter nb = new XmlNumberConverter();
        StringBuilder buf = new StringBuilder();
        double[] coords = new double[6];
        double reflectedX = Double.NaN;
        double reflectedY = Double.NaN;

        char next = 'Z'; // next instruction
        for (; !iter.isDone(); iter.next()) {
            if (!buf.isEmpty()) {
                buf.append(' ');
            }
            switch (iter.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    buf.append('M');
                    next = 'L'; // move implies line
                    buf.append(nb.toString(coords[0]))
                            .append(',')
                            .append(nb.toString(coords[1]));
                    reflectedX = reflectedY = Double.NaN;
                    break;
                case PathIterator.SEG_LINETO:
                    if (next != 'L') {
                        buf.append(next = 'L');
                    }
                    buf.append(nb.toString(coords[0]))
                            .append(',')
                            .append(nb.toString(coords[1]));
                    reflectedX = reflectedY = Double.NaN;
                    break;
                case PathIterator.SEG_QUADTO:
                    if ((next == 'Q' || next == 'T') && Points.almostEqual(coords[0], coords[1], reflectedX, reflectedY)) {
                        if (next != 'T') {
                            buf.append(next = 'T');
                        }
                        buf.append(nb.toString(coords[2]))
                                .append(',')
                                .append(nb.toString(coords[3]));
                    } else {
                        if (next != 'Q') {
                            buf.append(next = 'Q');
                        }
                        buf.append(nb.toString(coords[0]))
                                .append(',')
                                .append(nb.toString(coords[1]))
                                .append(',')
                                .append(nb.toString(coords[2]))
                                .append(',')
                                .append(nb.toString(coords[3]));
                    }
                    reflectedX = 2 * coords[2] - coords[0];
                    reflectedY = 2 * coords[3] - coords[1];
                    break;
                case PathIterator.SEG_CUBICTO:
                    if ((next == 'C' || next == 'S') && Points.almostEqual(coords[0], coords[1], reflectedX, reflectedY)) {
                        if (next != 'S') {
                            buf.append(next = 'S');
                        }
                        buf.append(nb.toString(coords[2]))
                                .append(',')
                                .append(nb.toString(coords[3]))
                                .append(',')
                                .append(nb.toString(coords[4]))
                                .append(',')
                                .append(nb.toString(coords[5]));
                    } else {
                        if (next != 'C') {
                            buf.append(next = 'C');
                        }
                        buf.append(nb.toString(coords[0]))
                                .append(',')
                                .append(nb.toString(coords[1]))
                                .append(',')
                                .append(nb.toString(coords[2]))
                                .append(',')
                                .append(nb.toString(coords[3]))
                                .append(',')
                                .append(nb.toString(coords[4]))
                                .append(',')
                                .append(nb.toString(coords[5]));
                    }
                    reflectedX = 2 * coords[4] - coords[2];
                    reflectedY = 2 * coords[5] - coords[3];
                    break;
                case PathIterator.SEG_CLOSE:
                    if (next != 'Z') {
                        buf.append(next = 'Z');
                    }
                    reflectedX = reflectedY = Double.NaN;
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Converts a Java Path iterator to a SVG path with double precision.
     *
     * @param iter AWT Path Iterator
     * @return SVG Path
     */
    public static @NonNull String awtShapeToDoubleRelativeSvgString(@NonNull PathIterator iter) {
        XmlNumberConverter nb = new XmlNumberConverter();
        StringBuilder buf = new StringBuilder();
        double[] coords = new double[6];
        double x = 0, y = 0;// current point
        double ix = 0, iy = 0;// initial point of a subpath
        char next = 'z'; // next instruction
        for (; !iter.isDone(); iter.next()) {
            double px = x, py = y;// previous point
            if (buf.length() != 0) {
                buf.append(' ');
            }
            switch (iter.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    buf.append('m');
                    next = 'l'; // move implies line
                    buf.append(nb.toString((ix = x = coords[0]) - px))
                            .append(',')
                            .append(nb.toString((iy = y = coords[1]) - py));
                    break;
                case PathIterator.SEG_LINETO:
                    if (next != 'l') {
                        buf.append(next = 'l');
                    }
                    buf.append(nb.toString((x = coords[0]) - px))
                            .append(',')
                            .append(nb.toString((y = coords[1]) - py));
                    break;
                case PathIterator.SEG_QUADTO:
                    if (next != 'q') {
                        buf.append(next = 'q');
                    }
                    buf.append(nb.toString(coords[0] - px))
                            .append(',')
                            .append(nb.toString(coords[1] - py))
                            .append(',')
                            .append(nb.toString((x = coords[2]) - px))
                            .append(',')
                            .append(nb.toString((y = coords[3]) - py));
                    break;
                case PathIterator.SEG_CUBICTO:
                    if (next != 'c') {
                        buf.append(next = 'c');
                    }
                    buf.append(nb.toString(coords[0] - px))
                            .append(',')
                            .append(nb.toString(coords[1] - py))
                            .append(',')
                            .append(nb.toString(coords[2] - px))
                            .append(',')
                            .append(nb.toString(coords[3] - py))
                            .append(',')
                            .append(nb.toString((x = coords[4]) - px))
                            .append(',')
                            .append(nb.toString((y = coords[5]) - py));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (next != 'z') {
                        buf.append(next = 'z');
                    }
                    x = ix;
                    y = iy;
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Converts a Java Path iterator to a SVG path with double precision.
     *
     * @param iter AWT Path Iterator
     * @return SVG Path
     */
    public static @NonNull String awtPathIteratorToFloatRelativeSvgString(@NonNull PathIterator iter) {
        XmlNumberConverter nb = new XmlNumberConverter();
        StringBuilder buf = new StringBuilder();
        float[] coords = new float[6];
        float x = 0, y = 0;// current point
        float ix = 0, iy = 0;// initial point of a subpath
        char next = 'z'; // next instruction
        for (; !iter.isDone(); iter.next()) {
            float px = x, py = y;// previous point
            if (buf.length() != 0) {
                buf.append(' ');
            }
            switch (iter.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    buf.append('m');
                    next = 'l'; // move implies line
                    buf.append(nb.toString((ix = x = coords[0]) - px))
                            .append(',')
                            .append(nb.toString((iy = y = coords[1]) - py));
                    break;
                case PathIterator.SEG_LINETO:
                    if (next != 'l') {
                        buf.append(next = 'l');
                    }
                    buf.append(nb.toString((x = coords[0]) - px))
                            .append(',')
                            .append(nb.toString((y = coords[1]) - py));
                    break;
                case PathIterator.SEG_QUADTO:
                    if (next != 'q') {
                        buf.append(next = 'q');
                    }
                    buf.append(nb.toString(coords[0] - px))
                            .append(',')
                            .append(nb.toString(coords[1] - py))
                            .append(',')
                            .append(nb.toString((x = coords[2]) - px))
                            .append(',')
                            .append(nb.toString((y = coords[3]) - py));
                    break;
                case PathIterator.SEG_CUBICTO:
                    if (next != 'c') {
                        buf.append(next = 'c');
                    }
                    buf.append(nb.toString(coords[0] - px))
                            .append(',')
                            .append(nb.toString(coords[1] - py))
                            .append(',')
                            .append(nb.toString(coords[2] - px))
                            .append(',')
                            .append(nb.toString(coords[3] - py))
                            .append(',')
                            .append(nb.toString((x = coords[4]) - px))
                            .append(',')
                            .append(nb.toString((y = coords[5]) - py));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (next != 'z') {
                        buf.append(next = 'z');
                    }
                    x = ix;
                    y = iy;
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Converts a Java Path iterator to a SVG path with float precision.
     *
     * @param iter AWT Path Iterator
     * @return SVG Path
     */
    public static @NonNull String awtPathIteratorToFloatSvgString(@NonNull PathIterator iter) {
        NumberConverter nb = new NumberConverter(Float.class);
        StringBuilder buf = new StringBuilder();
        float[] coords = new float[6];
        char next = 'Z'; // next instruction
        for (; !iter.isDone(); iter.next()) {
            if (buf.length() != 0) {
                buf.append(' ');
            }
            switch (iter.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    buf.append('M');
                    next = 'L'; // move implies line
                    buf.append(nb.toString(coords[0]))
                            .append(',')
                            .append(nb.toString(coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    if (next != 'L') {
                        buf.append(next = 'L');
                    }
                    buf.append(nb.toString(coords[0]))
                            .append(',')
                            .append(nb.toString(coords[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    if (next != 'Q') {
                        buf.append(next = 'Q');
                    }
                    buf.append(nb.toString(coords[0]))
                            .append(',')
                            .append(nb.toString(coords[1]))
                            .append(',')
                            .append(nb.toString(coords[2]))
                            .append(',')
                            .append(nb.toString(coords[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    if (next != 'C') {
                        buf.append(next = 'C');
                    }
                    buf.append(nb.toString(coords[0]))
                            .append(',')
                            .append(nb.toString(coords[1]))
                            .append(',')
                            .append(nb.toString(coords[2]))
                            .append(',')
                            .append(nb.toString(coords[3]))
                            .append(',')
                            .append(nb.toString(coords[4]))
                            .append(',')
                            .append(nb.toString(coords[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (next != 'Z') {
                        buf.append(next = 'Z');
                    }
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Returns a value as a SvgPath2D.
     * <p>
     * Also supports elliptical arc commands 'a' and 'A' as specified in
     * <a href="http://www.w3.org/TR/SVG/paths.html#PathDataEllipticalArcCommands">w3.org</a>
     *
     * @param str the SVG path
     * @return the SvgPath2D
     * @throws ParseException if the String is not a valid path
     */
    public static Path2D.@NonNull Double svgStringToAwtShape(@NonNull String str) throws ParseException {
        AwtPathBuilder b = new AwtPathBuilder();
        svgStringToBuilder(str, b);
        return b.build();
    }

    /**
     * Fits the specified SVGPath into the given bounds.
     * <p>
     * If parsing the SVG Path fails, logs a warning message and fits a rectangle
     * into the bounds.
     *
     * @param pathstr an SVGPath String
     * @param b       the desired bounds
     * @param builder the builder into which the path is output
     */
    public static void svgStringReshapeToBuilder(@Nullable String pathstr, @NonNull Bounds b, @NonNull PathBuilder<?> builder) {
        if (pathstr != null) {
            Shape shape = null;
            try {
                shape = svgStringToAwtShape(pathstr);
                java.awt.geom.Rectangle2D r2d = shape.getBounds2D();
                Transform tx = FXTransforms.createReshapeTransform(
                        r2d.getX(), r2d.getY(), r2d.getWidth(), r2d.getHeight(),
                        b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()
                );
                AwtShapes.buildFromPathIterator(builder, shape.getPathIterator(FXTransforms.toAwt(tx)));
                return;
            } catch (ParseException e) {
                LOGGER.warning(e.getMessage() + " Path: \"" + pathstr + "\".");
            }
        }

        // We get here if pathstr is null or if we encountered a parse exception

        builder.moveTo(b.getMinX(), b.getMinY());
        builder.lineTo(b.getMaxX(), b.getMinY());
        builder.lineTo(b.getMaxX(), b.getMaxY());
        builder.lineTo(b.getMinX(), b.getMaxY());
        builder.closePath();

    }

}
