/*
 * @(#)XmlConnectorConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.io.CharBufferReader;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.EllipseConnector;
import org.jhotdraw8.draw.connector.LocatorConnector;
import org.jhotdraw8.draw.connector.PathConnector;
import org.jhotdraw8.draw.connector.RectangleConnector;
import org.jhotdraw8.draw.css.converter.LocatorCssConverter;
import org.jhotdraw8.draw.locator.Locator;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * XmlConnectorConverter.
 * <p>
 * This converter supports the following connectors:
 * <ul>
 * <li>PathConnector</li>
 * <li>RectangleConnector</li>
 * <li>EllipseConnector</li>
 * </ul>
 *
 */
public class ConnectorXmlConverter implements Converter<Connector> {

    private final LocatorCssConverter locatorConverter = new LocatorCssConverter();

    private static final HashMap<String, Function<Locator, Connector>> choiceToConnectorMap = new HashMap<>();
    private static final HashMap<Class<? extends Connector>, String> connectorToChoiceMap = new HashMap<>();

    static {
        connectorToChoiceMap.put(PathConnector.class, "path");
        connectorToChoiceMap.put(RectangleConnector.class, "rect");
        connectorToChoiceMap.put(EllipseConnector.class, "ellipse");
        choiceToConnectorMap.put("path", PathConnector::new);
        choiceToConnectorMap.put("rect", RectangleConnector::new);
        choiceToConnectorMap.put("ellipse", EllipseConnector::new);
    }

    public ConnectorXmlConverter() {
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable Connector value) throws IOException {
        if (value == null) {
            out.append("none");
        }
        String name = connectorToChoiceMap.get(value.getClass());
        Objects.requireNonNull(name, "unsupported connector:" + value);
        out.append(name);
        if (value instanceof LocatorConnector lc) {
            out.append(" ");
            locatorConverter.toString(out, idSupplier, lc.getLocator());
        }
    }

    @Override
    public @Nullable Connector fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        Connector c;
        CssTokenizer tt = new StreamCssTokenizer(new CharBufferReader(buf));
        try {
            c = parseConnector(tt, idResolver);
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), 0);
        }

        if (!buf.toString().trim().isEmpty()) {
            throw new ParseException("Locator: End expected, found:" + buf, buf.position());
        }
        return c;
    }

    @Override
    public @Nullable Connector getDefaultValue() {
        return null;
    }

    /**
     * Parses a Locator.
     *
     * @param tt         the tokenizer
     * @param idResolver
     * @return the parsed color
     * @throws ParseException if parsing fails
     * @throws IOException    if IO fails
     */
    public @Nullable Connector parseConnector(CssTokenizer tt, IdResolver idResolver) throws ParseException, IOException {
        Locator locator;
        Function<Locator, Connector> supplier;

        switch (tt.next()) {
        case CssTokenType.TT_IDENT:
            if ("none".equals(tt.currentString())) {
                return null;
            }

            supplier = choiceToConnectorMap.get(tt.currentString());

            if (supplier == null) {
                throw new ParseException("Connector: unsupported connector, found:" + tt.currentValue(), tt.getStartPosition());
            }
            break;
        default:
            throw new ParseException("Connector: identifier expected, found:" + tt.currentValue(), tt.getStartPosition());
        }

        locator = locatorConverter.parseNonNull(tt, idResolver);

        return supplier.apply(locator);
    }

}
