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
 * Converts a Java double from/to String.
 */
public class DoubleConverter implements Converter<Double> {
    private final boolean nullable;
    private final boolean usesScientificNotation = true;
    private final @NonNull DecimalFormat decimalFormat;
    private final @NonNull DecimalFormat scientificFormat;
    private final int minNegativeExponent = -3;
    private final int minPositiveExponent = 7;

    public DoubleConverter() {
        this(false);
    }

    public DoubleConverter(boolean nullable) {
        this(nullable,
                new DecimalFormat("#################0.#################", new DecimalFormatSymbols(Locale.ENGLISH)),
                new DecimalFormat("0.0################E0", new DecimalFormatSymbols(Locale.ENGLISH)));
    }

    public DoubleConverter(boolean nullable, @NonNull DecimalFormat decimalFormat, @NonNull DecimalFormat scientificFormat) {
        this.nullable = nullable;
        this.decimalFormat = decimalFormat;
        this.scientificFormat = scientificFormat;
    }

    @Override
    public @Nullable Double fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        try {
            if (in.isEmpty() && nullable) return null;
            var result = Double.parseDouble(str);
            in.position(in.length());
            return result;
        } catch (NumberFormatException e) {
            throw new ParseException("Could not parse a double value from string=\"" + str + "\".", 0);
        }
    }

    @Override
    public <TT extends Double> void toString(Appendable buf, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) return;

        double v = value.doubleValue();
        if (Double.isInfinite(v)) {
            if (v < 0.0) {
                buf.append('-');
            }
            buf.append("Infinity");
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
