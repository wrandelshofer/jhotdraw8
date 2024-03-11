package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts a Java double from/to String.
 */
public class IntegerConverter implements Converter<Integer> {
    private final boolean nullable;

    public IntegerConverter() {
        this(false);
    }

    public IntegerConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public @Nullable Integer fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        try {
            if (in.isEmpty() && nullable) {
                return null;
            }
            var result = Integer.parseInt(str);
            in.position(in.length());
            return result;
        } catch (NumberFormatException e) {
            throw new ParseException("Could not parse the int value=\"" + str + "\".", 0);
        }
    }

    @Override
    public <TT extends Integer> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value != null) {
            out.append(Integer.toString(value));
        }
    }

    @Override
    public @Nullable Integer getDefaultValue() {
        return 0;
    }
}
