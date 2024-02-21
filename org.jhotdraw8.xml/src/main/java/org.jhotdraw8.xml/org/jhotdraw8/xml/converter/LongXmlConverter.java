package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts a XML integer from/to String.
 * <p>
 * Reference:
 * <a href="https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#integer">W3C: XML
 * Schema Part 2: Datatypes Second Edition: 3.2.13 integer</a>
 * </p>
 */
public class LongXmlConverter implements Converter<Long> {


    private final boolean nullable;

    public LongXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }

    public LongXmlConverter() {
        this(false);
    }


    @Override
    public @Nullable Long fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        try {
            if (in.isEmpty() && nullable) return null;
            var result = Long.parseLong(in.toString());
            in.position(in.length());
            return result;
        } catch (NumberFormatException e) {
            throw new ParseException("Illegal double value.", 0);
        }
    }

    @Override
    public <TT extends Long> void toString(Appendable buf, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value != null) {
            buf.append(Long.toString(value));
        }
    }

    @Override
    public @Nullable Long getDefaultValue() {
        return 0L;
    }
}
