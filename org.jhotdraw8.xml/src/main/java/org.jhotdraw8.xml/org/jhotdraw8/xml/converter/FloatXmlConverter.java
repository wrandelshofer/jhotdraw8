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
 * Converts a XML float from/to String.
 * <p>
 * Reference:
 * <a href="https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#float">W3C: XML
 * Schema Part 2: Datatypes Second Edition: 3.2.4 float</a>
 * </p>
 */
public class FloatXmlConverter implements Converter<Float> {

    private final boolean usesScientificNotation = true;
    private final @NonNull DecimalFormat decimalFormat = new DecimalFormat("#################0.#######", new DecimalFormatSymbols(Locale.ENGLISH));
    private final @NonNull DecimalFormat scientificFormat = new DecimalFormat("0.0########E0", new DecimalFormatSymbols(Locale.ENGLISH));
    private final int minNegativeExponent = -3;
    private final int minPositiveExponent = 7;

    private final boolean nullable;

    public FloatXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }

    public FloatXmlConverter() {
        this(false);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean getAllowsNullValue() {
        return nullable;
    }

    @Override
    public @Nullable Float fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException, IOException {
        String str = in.toString();
        try {
            if (str.strip().length() != str.length()) throw new NumberFormatException();
            Float result = switch (str) {
                case "-INF" -> Float.NEGATIVE_INFINITY;
                case "INF" -> Float.POSITIVE_INFINITY;
                case "NaN" -> Float.NaN;
                case "" -> {
                    if (!nullable) throw new NumberFormatException();
                    yield null;
                }
                default -> {
                    float v = Float.parseFloat(str);
                    if (!Float.isFinite(v)) {
                        throw new NumberFormatException();
                    }
                    yield v;
                }
            };

            in.position(in.length());
            return result;
        } catch (NumberFormatException e) {
            throw new ParseException("Illegal float value: \"" + str + "\"", 0);
        }
    }

    @Override
    public <TT extends Float> void toString(Appendable buf, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) return;

        float v = value;
        if (Float.isInfinite(v)) {
            if (v < 0.0) {
                buf.append('-');
            }
            buf.append("INF");
        } else if (Float.isNaN(v)) {
            buf.append("NaN");
        } else {
            String str;// = Float.toString(v);
            double exponent = v == 0 ? 0 : Math.log10(Math.abs(v));
            if (!usesScientificNotation || exponent > minNegativeExponent
                                           && exponent < minPositiveExponent) {
                // DecimalFormat produces too many digits, because it
                // promotes the float to a double before it converts it.
                str = Float.toString(v);
                int exponentIndex = str.indexOf('E');
                int pointIndex = str.indexOf('.');
                int fractionDigits = (exponentIndex == -1 ? str.length() : exponentIndex) - pointIndex;
                if (str.endsWith(".0")) {
                    str = str.substring(0, str.length() - 2);
                }
                if (exponentIndex >= 0 || fractionDigits > decimalFormat.getMaximumFractionDigits()) {
                    str = decimalFormat.format(v);
                }
            } else {
                str = scientificFormat.format(v);
            }
            buf.append(str);
        }
    }

    @Override
    public @Nullable Float getDefaultValue() {
        return 0.0f;
    }
}
