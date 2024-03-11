package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Converts a XML double from/to String.
 * <p>
 * Reference:
 * <a href="http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#double">W3C: XML
 * Schema Part 2: Datatypes Second Edition: 3.2.5 double</a>
 * </p>
 */
public class DoubleXmlConverter implements Converter<Double> {

    private final boolean usesScientificNotation = true;
    private final @NonNull DecimalFormat decimalFormat = new DecimalFormat("#################0.#################", new DecimalFormatSymbols(Locale.ENGLISH));
    private final @NonNull DecimalFormat scientificFormat = new DecimalFormat("0.0################E0", new DecimalFormatSymbols(Locale.ENGLISH));
    private final int minNegativeExponent = -3;
    private final int minPositiveExponent = 7;

    private final boolean nullable;

    public DoubleXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }

    public DoubleXmlConverter() {
        this(false);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean getAllowsNullValue() {
        return nullable;
    }

    @Override
    public @Nullable Double fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        try {
            if (str.strip().length() != str.length()) {
                throw new NumberFormatException();
            }
            Double result = switch (str) {
                case "-INF" -> Double.NEGATIVE_INFINITY;
                case "INF" -> Double.POSITIVE_INFINITY;
                case "NaN" -> Double.NaN;
                case "" -> {
                    if (!nullable) {
                        throw new NumberFormatException();
                    }
                    yield null;
                }
                default -> {
                    double v = Double.parseDouble(str);
                    if (!Double.isFinite(v)) {
                        throw new NumberFormatException();
                    }
                    yield v;
                }
            };

            in.position(in.length());
            return result;
        } catch (NumberFormatException e) {
            throw new ParseException("Illegal double value: \"" + str + "\"", 0);
        }
    }

    @Override
    public <TT extends Double> void toString(Appendable buf, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) {
            return;
        }

        double v = value.doubleValue();
        if (Double.isInfinite(v)) {
            if (v < 0.0) {
                buf.append('-');
            }
            buf.append("INF");
        } else if (Double.isNaN(v)) {
            buf.append("NaN");
        } else {
            String str;
            double exponent = v == 0 ? 1 : Math.log10(Math.abs(v));
            if (!usesScientificNotation || exponent > minNegativeExponent
                                           && exponent < minPositiveExponent) {
                str = decimalFormat.format(v);
            } else {
                str = scientificFormat.format(v);
            }
            buf.append(str);
        }
    }

    @Override
    public @Nullable Double getDefaultValue() {
        return 0.0;
    }
}
