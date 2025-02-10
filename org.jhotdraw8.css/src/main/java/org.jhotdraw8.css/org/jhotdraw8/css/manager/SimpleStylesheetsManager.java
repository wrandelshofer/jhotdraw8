/*
 * @(#)SimpleStylesheetsManager.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.manager;

import javafx.css.StyleOrigin;
import org.jhotdraw8.base.converter.SimpleUriResolver;
import org.jhotdraw8.base.converter.UriResolver;
import org.jhotdraw8.base.function.Consumer3;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.css.ast.Declaration;
import org.jhotdraw8.css.ast.Selector;
import org.jhotdraw8.css.ast.StyleRule;
import org.jhotdraw8.css.ast.Stylesheet;
import org.jhotdraw8.css.ast.TypeSelector;
import org.jhotdraw8.css.function.CssFunction;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssParser;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.value.QualifiedName;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * SimpleStylesheetsManager.
 *
 * @param <E> the element type that can be styled by this style manager
 */
public class SimpleStylesheetsManager<E> implements StylesheetsManager<E> {
    private @Nullable String defaultNamespace;

    private Supplier<CssParser> parserFactory = CssParser::new;
    private UriResolver uriResolver = new SimpleUriResolver();
    private SelectorModel<E> selectorModel;
    /**
     * Cache for parsed user agent stylesheets.
     * <p>
     * The key is either an URI or a literal CSS String for which we cache the
     * data. The value contains the parsed stylesheet entry.
     */
    private LinkedHashMap<Object, StylesheetEntry> userAgentList = new LinkedHashMap<>();
    /**
     * @see #userAgentList
     */
    private LinkedHashMap<Object, StylesheetEntry> authorList = new LinkedHashMap<>();
    /**
     * @see #userAgentList
     */
    private LinkedHashMap<Object, StylesheetEntry> inlineList = new LinkedHashMap<>();
    private final Executor executor = ForkJoinPool.commonPool();
    private @Nullable Map<String, PersistentList<CssToken>> cachedAuthorCustomProperties;
    private @Nullable Map<String, PersistentList<CssToken>> cachedInlineCustomProperties;
    private @Nullable Map<String, PersistentList<CssToken>> cachedUserAgentCustomProperties;

    private Consumer3<Level, String, Throwable> logger = (l, s, t) -> {
    };

    public SimpleStylesheetsManager(SelectorModel<E> selectorModel) {
        this(selectorModel, Collections.emptyList());
    }

    public SimpleStylesheetsManager(SelectorModel<E> selectorModel, List<CssFunction<E>> functions) {
        this.selectorModel = selectorModel;
        this.functions = functions;
    }

    private void doSetAttribute(
            @Nullable Stylesheet stylesheet,
            SelectorModel<E> selectorModel1, E elem, StyleOrigin styleOrigin,
            @Nullable String namespace, String name, @Nullable PersistentList<CssToken> value,
            Map<String, PersistentList<CssToken>> customProperties,
            @Nullable CssFunctionProcessor<E> functionProcessor) throws ParseException {
        if (value == null) {
            selectorModel1.setAttribute(elem, styleOrigin, namespace, name, null);
        } else {
            if (functionProcessor != null) {
                PersistentList<CssToken> processed = preprocessTerms(stylesheet, elem, functionProcessor, value);
                selectorModel1.setAttribute(elem, styleOrigin, namespace, name, processed);
            } else {
                selectorModel1.setAttribute(elem, styleOrigin, namespace, name, value);
            }
        }
    }

    public void setSelectorModel(SelectorModel<E> newValue) {
        selectorModel = newValue;
    }

    @Override
    public SelectorModel<E> getSelectorModel() {
        return selectorModel;
    }

    @Override
    public Consumer3<Level, String, Throwable> getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Consumer3<Level, String, Throwable> logger) {
        this.logger = logger;
    }

    @Override
    public void addStylesheet(StyleOrigin origin, URI stylesheetUri, @Nullable URI documentHome) {
        URI absolutizedUri = uriResolver.absolutize(documentHome, stylesheetUri);
        invalidate();
        getMap(origin).put(absolutizedUri, new StylesheetEntry(origin, absolutizedUri, documentHome, logger));
    }

    @Override
    public void addStylesheet(StyleOrigin origin, Stylesheet stylesheet) {
        invalidate();
        getMap(origin).put(stylesheet, new StylesheetEntry(origin, stylesheet, logger));
    }

    @Override
    public void addStylesheet(StyleOrigin origin, String str, @Nullable URI documentHome) {
        invalidate();
        getMap(origin).put(str, new StylesheetEntry(origin, str, null, documentHome, logger));
    }

    private void invalidate() {
        cachedAuthorCustomProperties = null;
        cachedInlineCustomProperties = null;
        cachedUserAgentCustomProperties = null;
        candidateRules.clear();
    }

    @Override
    public void clearStylesheets(@Nullable StyleOrigin origin) {
        if (origin == null) {
            authorList.clear();
            userAgentList.clear();
            inlineList.clear();
            invalidate();
        } else {
            getMap(origin).clear();
        }
    }

    private LinkedHashMap<Object, StylesheetEntry> getMap(StyleOrigin origin) {
        return switch (origin) {
            case AUTHOR -> authorList;
            case USER_AGENT -> userAgentList;
            case INLINE -> inlineList;
            default -> throw new IllegalArgumentException("illegal origin:" + origin);
        };
    }

    private void setMap(StyleOrigin origin, LinkedHashMap<Object, StylesheetEntry> newValue) {
        switch (origin) {
            case AUTHOR:
                authorList = newValue;
                break;
            case USER_AGENT:
                userAgentList = newValue;
                break;
            case INLINE:
                inlineList = newValue;
                break;
            default:
                throw new IllegalArgumentException("illegal origin:" + origin);
        }
    }

    @Override
    public <T> void setStylesheets(StyleOrigin origin, @Nullable URI documentHome, @Nullable List<T> stylesheets) {
        invalidate();
        LinkedHashMap<Object, StylesheetEntry> oldMap = getMap(origin);
        if (stylesheets == null) {
            oldMap.clear();
            return;
        }
        LinkedHashMap<Object, StylesheetEntry> newMap = new LinkedHashMap<>();
        for (T t : stylesheets) {
            if (t instanceof URI uri) {
                URI absolutizedUri = uriResolver.absolutize(documentHome, uri);
                StylesheetEntry old = oldMap.get(absolutizedUri);
                newMap.put(absolutizedUri, new StylesheetEntry(origin, absolutizedUri, documentHome, logger));
            } else if (t instanceof String) {
                StylesheetEntry old = oldMap.get(t);
                newMap.put(t, old != null ? old : new StylesheetEntry(origin, (String) t, null, documentHome, logger));
            } else {
                throw new IllegalArgumentException("illegal item " + t);
            }
        }
        setMap(origin, newMap);
    }

    protected Collection<StylesheetEntry> getAuthorStylesheets() {
        return authorList.values();
    }

    protected Collection<StylesheetEntry> getUserAgentStylesheets() {
        return userAgentList.values();
    }

    protected Collection<StylesheetEntry> getInlineStylesheets() {
        return inlineList.values();
    }

    @Override
    public void applyStylesheetsTo(E elem) {
        applyStylesheetsTo(Collections.singleton(elem));
    }

    @Override
    public void applyStylesheetsTo(Iterable<E> iterable) {
        SelectorModel<E> selectorModel = getSelectorModel();

        // Compute custom properties
        Map<String, PersistentList<CssToken>> customProperties = computeCustomProperties();
        final CssFunctionProcessor<E> functionProcessor = functions.isEmpty() ? null : createCssFunctionProcessor(selectorModel, customProperties);

        StreamSupport.stream(iterable.spliterator(), false).toList()
                .stream()
                .parallel()
                .forEach(elem -> {
                    // Clear stylesheet values
                    selectorModel.reset(elem);

                    // The stylesheet is a user-agent stylesheet
                    for (ApplicableDeclaration entry : collectApplicableDeclarations(elem, getUserAgentStylesheets())) {
                        try {
                            Declaration d = entry.declaration();
                            doSetAttribute(entry.stylesheet(), selectorModel, elem, StyleOrigin.USER_AGENT, d.getNamespace(), d.getPropertyName(), d.getTerms(), customProperties, functionProcessor);
                        } catch (ParseException e) {
                            logger.accept(Level.FINE, "user-agent stylesheet=" + entry.stylesheet.getUri() + " line=" + entry.declaration.getLineNumber(), e);
                        }
                    }

                    // The value of a property was set by the user through a call to a set method with StyleOrigin.USER
                    // ... nothing to do!

                    // The stylesheet is an external file
                    for (ApplicableDeclaration entry : collectApplicableDeclarations(elem, getAuthorStylesheets())) {
                        try {
                            Declaration d = entry.declaration();
                            doSetAttribute(entry.stylesheet(), selectorModel, elem, StyleOrigin.AUTHOR, d.getNamespace(), d.getPropertyName(), d.getTerms(), customProperties, functionProcessor);
                        } catch (ParseException e) {
                            logger.accept(Level.FINE, "external stylesheet=" + entry.stylesheet.getUri() + " line=" + entry.declaration.getLineNumber(), e);
                        }
                    }

                    // The stylesheet is an internal file
                    for (ApplicableDeclaration entry : collectApplicableDeclarations(elem, getInlineStylesheets())) {
                        try {
                            Declaration d = entry.declaration();
                            doSetAttribute(entry.stylesheet(), selectorModel, elem, StyleOrigin.INLINE, d.getNamespace(), d.getPropertyName(), d.getTerms(), customProperties, functionProcessor);
                        } catch (ParseException e) {
                            logger.accept(Level.FINE, "internal stylesheet=" + entry.stylesheet.getUri() + " line=" + entry.declaration.getLineNumber(), e);
                        }
                    }

                    // 'inline style attributes' can override all other values
                    CssParser parser = parserFactory.get();
                    if (selectorModel.hasAttribute(elem, null, "style")) {
                        Map<QualifiedName, PersistentList<CssToken>> inlineDeclarations = new HashMap<>();
                        String styleValue = selectorModel.getAttributeAsString(elem, null, "style");
                        if (styleValue != null) {
                            try {
                                for (Declaration d : parser.parseDeclarationList(styleValue)) {
                                    // Declarations without terms are ignored
                                    if (d.getTerms().isEmpty()) {
                                        continue;
                                    }

                                    inlineDeclarations.put(new QualifiedName(d.getNamespace(), d.getPropertyName()), d.getTerms());
                                }
                            } catch (IOException ex) {
                                logger.accept(Level.WARNING, "invalid inline style attribute on element. style=" + styleValue, null);
                                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

                            }
                        }
                        Map<String, PersistentList<CssToken>> inlineStyleAttrCustomProperties = Collections.emptyMap();
                        for (Map.Entry<QualifiedName, PersistentList<CssToken>> entry : inlineDeclarations.entrySet()) {
                            try {
                                doSetAttribute(null, selectorModel, elem, StyleOrigin.INLINE, entry.getKey().namespace(), entry.getKey().name(), entry.getValue(), inlineStyleAttrCustomProperties, functionProcessor);
                            } catch (ParseException e) {
                                logger.accept(Level.WARNING, "error applying inline style attribute. style=" + styleValue, e);
                            }
                        }
                        inlineDeclarations.clear();
                    }
                });
    }

    private Map<String, PersistentList<CssToken>> computeCustomProperties() {
        SequencedMap<String, PersistentList<CssToken>> customProperties = new LinkedHashMap<>();
        customProperties.putAll(getUserAgentCustomProperties());
        customProperties.putAll(getAuthorCustomProperties());
        customProperties.putAll(getInlineCustomProperties());
        return customProperties;
    }

    private Map<String, PersistentList<CssToken>> getInlineCustomProperties() {
        if (cachedInlineCustomProperties == null) {
            cachedInlineCustomProperties = collectCustomProperties(getInlineStylesheets());
        }
        return cachedInlineCustomProperties;
    }

    private Map<String, PersistentList<CssToken>> getAuthorCustomProperties() {
        if (cachedAuthorCustomProperties == null) {
            cachedAuthorCustomProperties = collectCustomProperties(getAuthorStylesheets());
        }
        return cachedAuthorCustomProperties;
    }

    private Map<String, PersistentList<CssToken>> getUserAgentCustomProperties() {
        if (cachedUserAgentCustomProperties == null) {
            cachedUserAgentCustomProperties = collectCustomProperties(getUserAgentStylesheets());
        }
        return cachedUserAgentCustomProperties;
    }

    /**
     * Collects all declarations in all specified stylesheets which are
     * applicable to the specified element.
     *
     * @param elem        an element
     * @param stylesheets the stylesheets
     * @return list of applicable declarations
     */
    private List<ApplicableDeclaration> collectApplicableDeclarations(
            E elem,
            Collection<StylesheetEntry> stylesheets) {
        List<ApplicableDeclaration> applicableDeclarations = new ArrayList<>();
        for (StylesheetEntry e : stylesheets) {
            Stylesheet s = e.getStylesheet();
            if (s == null) {
                continue;
            }
            collectApplicableDeclarations(elem, s, applicableDeclarations);
        }

        applicableDeclarations.sort(Comparator.comparingInt(ApplicableDeclaration::specificity));
        return applicableDeclarations;
    }

    private record ApplicableDeclaration(int specificity, Stylesheet stylesheet,
                                         Declaration declaration) {
    }

    private final ConcurrentHashMap<OrderedPair<Stylesheet, QualifiedName>, List<StyleRule>>
            candidateRules = new ConcurrentHashMap<>();

    private Iterable<StyleRule> getCandidateStyleRules(Stylesheet s, E elem) {
        QualifiedName qualifiedTypeName = getSelectorModel().getType(elem);
        String typeName = qualifiedTypeName.name();
        OrderedPair<Stylesheet, QualifiedName> key = new SimpleOrderedPair<>(s, qualifiedTypeName);
        return candidateRules.computeIfAbsent(key, k -> {
            List<StyleRule> candidates = new ArrayList<>();
            for (StyleRule styleRule : s.getStyleRules()) {
                TypeSelector typeSelector = styleRule.getSelectorGroup().matchesOnlyOnASpecificType();
                String candidateTypeName = typeSelector == null || !Objects.equals(typeSelector.getNamespacePattern(), TypeSelector.ANY_NAMESPACE)
                        ? null
                        : typeSelector.getType();
                if (candidateTypeName == null || candidateTypeName.equals(typeName)) {
                    candidates.add(styleRule);
                }
            }
            return candidates;
        });

    }

    private List<ApplicableDeclaration> collectApplicableDeclarations(
            E elem, Stylesheet s,
            List<ApplicableDeclaration> applicableDeclarations) {
        SelectorModel<E> selectorModel = getSelectorModel();
        for (StyleRule r : getCandidateStyleRules(s, elem)) {
            Selector selector;
            if (null != (selector = r.getSelectorGroup().matchSelector(selectorModel, elem))) {
                for (Declaration d : r.getDeclarations()) {
                    // Declarations without terms are ignored
                    if (d.getTerms().isEmpty()) {
                        continue;
                    }

                    applicableDeclarations.add(new ApplicableDeclaration(selector.getSpecificity(),
                            s, d));
                }
            }
        }
        return applicableDeclarations;
    }

    @Override
    public boolean applyStylesheetTo(StyleOrigin styleOrigin, Stylesheet s, E elem, boolean suppressParseException) throws ParseException {
        SelectorModel<E> selectorModel = getSelectorModel();
        final Map<String, PersistentList<CssToken>> customProperties = collectCustomProperties(s);

        CssFunctionProcessor<E> processor = createCssFunctionProcessor(selectorModel, customProperties);
        final List<ApplicableDeclaration> applicableDeclarations = collectApplicableDeclarations(elem, s,
                new ArrayList<>());
        if (applicableDeclarations.isEmpty()) {
            return false;
        }
        for (ApplicableDeclaration entry : applicableDeclarations) {
            Declaration d = entry.declaration();
            PersistentList<CssToken> value = preprocessTerms(s, elem, processor, d.getTerms());
            try {

                ReadableList<CssToken> appliedValue;
                CssToken first = value.isEmpty() ? null : value.getFirst();
                if (first != null && first.getType() == CssTokenType.TT_IDENT) {
                    appliedValue = switch (first.getStringValueNonNull()) {
                        case CssTokenType.IDENT_UNSET -> null;
                        default -> value;
                    };
                } else {
                    appliedValue = value;
                }
                selectorModel.setAttribute(elem, styleOrigin, d.getNamespace(), d.getPropertyName(),
                        appliedValue);
            } catch (ParseException e) {
                if (suppressParseException) {
                    logger.accept(Level.WARNING, "error parsing stylesheet, uri=" + s.getUri(), e);
                } else {
                    throw e;
                }
            }
        }
        return true;
    }

    private CssFunctionProcessor<E> createCssFunctionProcessor(SelectorModel<E> selectorModel, Map<String, PersistentList<CssToken>> customProperties) {
        return new SimpleCssFunctionProcessor<>(functions, selectorModel, customProperties);
    }


    private List<CssFunction<E>> functions = new ArrayList<>();

    public List<CssFunction<E>> getFunctions() {
        return functions;
    }

    public void setFunctions(List<CssFunction<E>> functions) {
        this.functions = functions;
    }

    @Override
    public String getHelpText() {
        StringBuilder buf = new StringBuilder();
        for (CssFunction<E> value : functions) {
            if (!buf.isEmpty()) {
                buf.append("\n");
            }
            buf.append(value.getHelpText());
        }
        return buf.toString();
    }

    private Map<String, PersistentList<CssToken>> collectCustomProperties(Collection<StylesheetEntry> stylesheets) {
        SequencedMap<String, PersistentList<CssToken>> customProperties = new LinkedHashMap<>();
        for (StylesheetEntry s : stylesheets) {
            Stylesheet stylesheet = s.getStylesheet();
            if (stylesheet != null) {
                collectCustomProperties(stylesheet, customProperties);
            }
        }
        return customProperties;
    }

    private Map<String, PersistentList<CssToken>> collectCustomProperties(Stylesheet s) {
        SequencedMap<String, PersistentList<CssToken>> customProperties = new LinkedHashMap<>();
        collectCustomProperties(s, customProperties);
        return customProperties;
    }

    private void collectCustomProperties(Stylesheet s, Map<String, PersistentList<CssToken>> customProperties) {
        for (StyleRule styleRule : s.getStyleRules()) {
            for (Declaration declaration : styleRule.getDeclarations()) {
                if (declaration.getPropertyName().startsWith("--")) {
                    customProperties.put(declaration.getPropertyName(), declaration.getTerms());
                }
            }
        }
    }

    private PersistentList<CssToken> preprocessTerms(
            @Nullable Stylesheet stylesheet,
            E elem,
            CssFunctionProcessor<E> processor, PersistentList<CssToken> terms) {
        try {
            return processor.process(elem, terms);
        } catch (ParseException e) {
            // This may happen extremely often!
            logger.accept(Level.WARNING, stylesheet == null
                    ? "error preprocessing token from stylesheet"
                    : "error preprocessing token from stylesheet, uri=" + stylesheet.getUri(), e);
            return terms;
        }
    }

    protected class StylesheetEntry implements StylesheetInfo {

        private final @Nullable URI uri;
        private final StyleOrigin origin;
        private @Nullable FutureTask<Stylesheet> future;
        private @Nullable Stylesheet stylesheet;
        private final Consumer3<Level, String, Throwable> logger;


        public StylesheetEntry(StyleOrigin origin, URI stylesheetUri, @Nullable URI documentHome, Consumer3<Level, String, Throwable> logger) {
            this.origin = origin;
            this.uri = stylesheetUri;
            this.future = new FutureTask<>(() -> {
                CssParser p = new CssParser();
                final URI stylesheetHome = uriResolver.getParent(stylesheetUri);
                Stylesheet s = p.parseStylesheet(stylesheetUri, stylesheetHome);
                logger.accept(Level.FINE, "Parsed " + stylesheetUri + "\n#rules: " + s.getStyleRules().size() + ", #errors: " + p.getParseExceptions().size(), null);
                List<ParseException> parseExceptions = p.getParseExceptions();
                if (!parseExceptions.isEmpty()) {
                    logger.accept(Level.FINE, "Parsed " + stylesheetUri + "\nExceptions:\n  " + parseExceptions.stream().map(ParseException::getMessage).collect(Collectors.joining("\n  ")), null);
                }
                return s;
            });
            this.logger = logger;
            executor.execute(future);
        }

        public StylesheetEntry(StyleOrigin origin, Stylesheet stylesheet, Consumer3<Level, String, Throwable> logger) {
            this.logger = logger;
            this.uri = null;
            this.origin = origin;
            this.stylesheet = stylesheet;
        }

        public StylesheetEntry(StyleOrigin origin, String str, @Nullable URI stylesheetUri, @Nullable URI documentHome, Consumer3<Level, String, Throwable> logger) {
            this.logger = logger;
            this.uri = null;
            this.origin = origin;
            this.future = new FutureTask<>(() -> {
                CssParser p = parserFactory.get();
                Stylesheet s = p.parseStylesheet(str, stylesheetUri, documentHome);
                logger.accept(Level.FINE, "Parsed " + str + "\nRules: " + s.getStyleRules(), null);
                List<ParseException> parseExceptions = p.getParseExceptions();
                if (!parseExceptions.isEmpty()) {
                    logger.accept(Level.FINE, "Parsed " + str + "\nExceptions:\n  " + parseExceptions.stream().map(ParseException::getMessage).collect(Collectors.joining("\n  ")), null);
                }
                return s;
            });
            executor.execute(future);
        }

        @Override
        public @Nullable URI getUri() {
            return uri;
        }

        @Override
        public StyleOrigin getOrigin() {
            return origin;
        }

        @Override
        public @Nullable Stylesheet getStylesheet() {
            if (future != null) {
                try {
                    stylesheet = future.get();
                    future = null;
                } catch (InterruptedException ex) {
                    // retry later
                } catch (ExecutionException ex) {
                    logger.accept(Level.WARNING, "failed to get stylesheet", ex);
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

                    stylesheet = null;
                    future = null;
                }
            }
            return stylesheet;
        }
    }

    @Override
    public List<StylesheetInfo> getStylesheets() {
        final ArrayList<StylesheetInfo> list = new ArrayList<>();
        list.addAll(userAgentList.values());
        list.addAll(authorList.values());
        list.addAll(inlineList.values());
        return list;
    }

    @Override
    public boolean hasStylesheets() {
        return !userAgentList.isEmpty()
                || !authorList.isEmpty()
                || !inlineList.isEmpty();
    }

    public Supplier<CssParser> getParserFactory() {
        return parserFactory;
    }

    public void setParserFactory(Supplier<CssParser> parserFactory) {
        this.parserFactory = parserFactory;
    }

    public UriResolver getUriResolver() {
        return uriResolver;
    }

    public void setUriResolver(UriResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public @Nullable String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(@Nullable String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }
}
