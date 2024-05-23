/*
 * @(#)CssPaperSizeConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Consumer;

/**
 * Converts a {@code CssDimension2D} into a {@code String} and vice versa.
 *
 * @author Werner Randelshofer
 */
public class PaperSizeCssConverter extends AbstractCssConverter<CssDimension2D> {

    private final SizeCssConverter sizeConverter = new SizeCssConverter(false);
    private static final Map<String, CssDimension2D> paperSizes;
    private static final Map<CssDimension2D, String> sizePapers;

    static {
        SequencedMap<String, CssDimension2D> m = new LinkedHashMap<>();
        m.put("A0", new CssDimension2D(CssSize.of(841, "mm"), CssSize.of(1189, "mm")));
        m.put("A1", new CssDimension2D(CssSize.of(594, "mm"), CssSize.of(841, "mm")));
        m.put("A2", new CssDimension2D(CssSize.of(420, "mm"), CssSize.of(594, "mm")));
        m.put("A3", new CssDimension2D(CssSize.of(297, "mm"), CssSize.of(420, "mm")));
        m.put("A4", new CssDimension2D(CssSize.of(210, "mm"), CssSize.of(297, "mm")));
        m.put("A5", new CssDimension2D(CssSize.of(148, "mm"), CssSize.of(210, "mm")));
        m.put("A6", new CssDimension2D(CssSize.of(105, "mm"), CssSize.of(148, "mm")));
        m.put("DesignatedLong", new CssDimension2D(CssSize.of(110, "mm"), CssSize.of(220, "mm")));
        m.put("Letter", new CssDimension2D(CssSize.of(8.5, "in"), CssSize.of(11, "in")));
        m.put("Legal", new CssDimension2D(CssSize.of(8.4, "in"), CssSize.of(14, "in")));
        m.put("Tabloid", new CssDimension2D(CssSize.of(11.0, "in"), CssSize.of(17.0, "in")));
        m.put("Executive", new CssDimension2D(CssSize.of(7.25, "in"), CssSize.of(10.5, "in")));
        m.put("x8x10", new CssDimension2D(CssSize.of(8, "in"), CssSize.of(10, "in")));
        m.put("MonarchEnvelope", new CssDimension2D(CssSize.of(3.87, "in"), CssSize.of(7.5, "in")));
        m.put("Number10Envelope", new CssDimension2D(CssSize.of(4.125, "in"), CssSize.of(9.5, "in")));
        m.put("C", new CssDimension2D(CssSize.of(17.0, "in"), CssSize.of(22.0, "in")));
        m.put("B4", new CssDimension2D(CssSize.of(257, "mm"), CssSize.of(364, "mm")));
        m.put("B5", new CssDimension2D(CssSize.of(182, "mm"), CssSize.of(257, "mm")));
        m.put("B6", new CssDimension2D(CssSize.of(128, "mm"), CssSize.of(182, "mm")));
        m.put("JapanesePostcard", new CssDimension2D(CssSize.of(100, "mm"), CssSize.of(148, "mm")));
        paperSizes = m;

        SequencedMap<CssDimension2D, String> x = new LinkedHashMap<>();
        for (Map.Entry<String, CssDimension2D> e : m.entrySet()) {
            final CssDimension2D v = e.getValue();
            x.put(v, e.getKey() + " portrait");
            x.put(new CssDimension2D(v.getHeight(), v.getWidth()), e.getKey() + " landscape");
        }
        sizePapers = x;
    }

    private static final String LANDSCAPE = "landscape";
    private static final String PORTRAIT = "portrait";

    public PaperSizeCssConverter() {
        super(false);
    }

    private @Nullable CssDimension2D parsePageSize(CssTokenizer tt, IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() == CssTokenType.TT_IDENT) {
            CssDimension2D paperSize = paperSizes.get(tt.currentString());
            if (paperSize == null) {
                throw new ParseException("Illegal paper format:" + tt.currentString(), tt.getStartPosition());
            }
            if (tt.next() == CssTokenType.TT_IDENT) {
                switch (tt.currentString()) {
                case LANDSCAPE:
                    paperSize = new CssDimension2D(paperSize.getHeight(), paperSize.getWidth());
                    break;
                case PORTRAIT:
                    break;
                default:
                    tt.pushBack();
                }
            } else {
                tt.pushBack();
            }
            return paperSize;
        } else {
            tt.pushBack();
            CssSize x = sizeConverter.parse(tt, idResolver);
            CssSize y = sizeConverter.parse(tt, idResolver);
            return new CssDimension2D(x, y);
        }
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, CssDimension2D value) throws IOException {
        String paper = sizePapers.get(value);
        if (paper != null) {
            out.append(paper);
        } else {
            out.append(sizeConverter.toString(value.getWidth()));
            out.append(' ');
            out.append(sizeConverter.toString(value.getHeight()));
        }
    }

    @Override
    public CssDimension2D parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        return parsePageSize(tt, idResolver);
    }

    @Override
    protected <TT extends CssDimension2D> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        String paper = sizePapers.get(value);
        if (paper != null) {
            boolean first = true;
            for (String ident : paper.split("\\s+")) {
                if (first) {
                    first = false;
                } else {
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                out.accept(new CssToken(CssTokenType.TT_IDENT, ident));
            }
        } else {
            sizeConverter.produceTokens(value.getWidth(), idSupplier, out);
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            sizeConverter.produceTokens(value.getHeight(), idSupplier, out);
        }

    }

    @Override
    public @Nullable CssDimension2D getDefaultValue() {
        return new CssDimension2D(CssSize.of(0), CssSize.of(0));
    }

    @Override
    public @Nullable String getHelpText() {
        StringBuilder buf = new StringBuilder();
        for (String s : paperSizes.keySet()) {
            if (!buf.isEmpty()) {
                buf.append('｜');
            }
            buf.append(s);
        }
        return "Format of ⟨PageSize⟩: " + "⟨width⟩mm ⟨height⟩mm｜⟨PaperFormat⟩ landscape｜⟨PaperFormat⟩ portrait"
                + "\nFormat of ⟨PaperFormat⟩: " + buf;
    }
}
