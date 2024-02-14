package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Converts a Java float from/to String.
 */
public class FloatConverter implements Converter<Float> {
    private final boolean nullable;
    private final boolean usesScientificNotation = true;
    private final @NonNull DecimalFormat decimalFormat = new DecimalFormat("#################0.#######", new DecimalFormatSymbols(Locale.ENGLISH));
    private final @NonNull DecimalFormat scientificFormat = new DecimalFormat("0.0########E0", new DecimalFormatSymbols(Locale.ENGLISH));
    private final int minNegativeExponent = -3;
    private final int minPositiveExponent = 7;

    public FloatConverter() {
        this(false);
    }

    public FloatConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public @Nullable Float fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException, IOException {
        try {
            if (in.isEmpty() && nullable) return null;
            var result = Float.parseFloat(in.toString());
            in.position(in.length());
            return result;
        } catch (NumberFormatException e) {
            throw new ParseException("Illegal float value.", 0);
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
            buf.append("Infinity");
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
