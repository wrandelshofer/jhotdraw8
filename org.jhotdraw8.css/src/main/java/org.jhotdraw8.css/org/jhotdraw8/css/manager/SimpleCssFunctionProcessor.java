/*
 * @(#)SimpleCssFunctionProcessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.manager;

import org.jhotdraw8.css.function.CssFunction;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.ListCssTokenizer;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes a list of tokens and evaluates Css functions on them.
 *
 * @param <T> the element type
 */
public class SimpleCssFunctionProcessor<T> implements CssFunctionProcessor<T> {
    protected SelectorModel<T> model;
    protected @Nullable Map<String, PersistentList<CssToken>> customProperties;
    private final Map<String, CssFunction<T>> functions;
    /**
     * Value must be greater equal to zero.
     */
    private int maxRecursionDepth = 256;

    public SimpleCssFunctionProcessor(List<CssFunction<T>> functions) {
        this(functions, null, null);
    }

    public SimpleCssFunctionProcessor(List<CssFunction<T>> functions, @Nullable SelectorModel<T> model, @Nullable Map<String, @Nullable PersistentList<CssToken>> customProperties) {
        this.model = model;
        this.customProperties = customProperties;
        this.functions = new LinkedHashMap<>();
        for (CssFunction<T> function : functions) {
            this.functions.put(function.getName(), function);
        }

    }

    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    public void setMaxRecursionDepth(int maxRecursionDepth) {
        if (maxRecursionDepth < 0) {
            throw new IllegalArgumentException("maxRecursionDepth=" + maxRecursionDepth);
        }
        this.maxRecursionDepth = maxRecursionDepth;
    }

    public SelectorModel<T> getModel() {
        return model;
    }

    @Override
    public void setModel(SelectorModel<T> model) {
        this.model = model;
    }

    @Override
    public Map<String, PersistentList<CssToken>> getCustomProperties() {
        return customProperties;
    }

    @Override
    public void setCustomProperties(Map<String, PersistentList<CssToken>> customProperties) {
        this.customProperties = customProperties;
    }

    public final ReadableList<CssToken> process(T element, PersistentList<CssToken> in) throws ParseException {
        ListCssTokenizer tt = new ListCssTokenizer(in);
        ArrayList<CssToken> out = new ArrayList<>(in.size());
        try {
            process(element, tt, out::add, new ArrayDeque<>());
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

            out.clear();
            for (CssToken t : in) {
                out.add(t);
            }
        }
        return VectorList.copyOf(out);
    }

    @Override
    public String getHelpText() {
        StringBuilder buf = new StringBuilder();
        for (CssFunction<T> value : functions.values()) {
            if (!buf.isEmpty()) {
                buf.append("\n");
            }
            buf.append(value.getHelpText());
        }
        return buf.toString();
    }

    @Override
    public final void process(T element, CssTokenizer tt, Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        while (tt.nextNoSkip() != CssTokenType.TT_EOF) {
            tt.pushBack();
            processToken(element, tt, out, recursionStack);
        }
    }

    @Override
    public final void processToken(T element, CssTokenizer tt, Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        doProcessToken(element, tt, out, recursionStack);
    }

    protected void doProcessToken(T element, CssTokenizer tt, Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        if (recursionStack.size() >= maxRecursionDepth) {
            throw tt.createParseException("Too many recursions. recursionDepth=" + recursionStack.size());
        }
        if (tt.nextNoSkip() == CssTokenType.TT_FUNCTION) {

            final String name = tt.currentStringNonNull();
            final CssFunction<T> function = functions.get(name);
            if (function != null) {
                tt.pushBack();
                function.process(element, tt, model, this, out, recursionStack);
            } else {
                tt.pushBack();
                processUnknownFunction(element, tt, out, recursionStack);
            }
        } else {
            out.accept(tt.getToken());
        }
    }


    /**
     * Processes an unknown function. Unknown functions will just be passed through.
     *
     * @param element        the element
     * @param tt             the tokenizer
     * @param out            the consumer
     * @param recursionStack recursion stack for detecting infinite recursions
     * @throws IOException    on io failure
     * @throws ParseException on parse failure
     */
    private void processUnknownFunction(T element, CssTokenizer tt, Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "〈func〉: function expected.");
        out.accept(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF && tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            tt.pushBack();
            processToken(element, tt, out, recursionStack);
        }
        if (tt.current() != CssTokenType.TT_EOF) {
            out.accept(tt.getToken());
        }
    }

}
