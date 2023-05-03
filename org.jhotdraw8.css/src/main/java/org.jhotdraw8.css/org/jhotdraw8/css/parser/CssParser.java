/*
 * @(#)CssParser.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.parser;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.SimpleUriResolver;
import org.jhotdraw8.base.converter.UriResolver;
import org.jhotdraw8.css.ast.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The {@code CssParser} processes a stream of characters into a
 * {@code Stylesheet} object.
 * <p>
 * The CSS Syntax Module Level 3 defines a grammar which is equivalent to the
 * following EBNF ISO/IEC 14977 productions:
 * <pre>
 * stylesheet_core = { S | CDO | CDC | qualified_rule | at_rule } ;
 *
 * rule_list    = { S | qualified_rule | at_rule} ;
 *
 * at_rule      = AT_KEYWORD , { component_value } , ( curly_block | ';' ) ;
 *
 * qualified_rule
 *              = { component_value } , curly_block ;
 *
 * declaration_list_core
 *              = { S } , ( [ declaration_core ] , [ ';' , declaration_list_core ]
 *                        | at_rule , declaration_list_core ,
 *                        ) ;
 *
 * declaration_core  = IDENT , { S } ,  ":", { component_value } , [ !important ] ;
 *
 * !important   = '!' , { S } , "important" , { S } ;
 *
 * component_value
 *              = ( preserved_token | curly_block | round_block | square_block
 *                | function_block ) ;
 *
 * curly_block  = '{' , { component_value } , '}' ;
 * round_block  = '(' , { component_value } , ')' ;
 * square_block = '[' , { component_value } , ']' ;
 * function_block
 *              = ROUND_BLOCK , { component_value } , ')' ;
 *
 * </pre> This parser parses the following syntax:
 * <pre>
 * stylesheet   = { S | CDO | CDC | qualified_rule | style_rule } ;
 *
 * operator     = ( '/' | ',' ) , { S } ;
 *
 * combinator   = ( '+' | '&gt;' | '~' ) , { S } ;
 *
 * unary_operator
 *              = ( '-' | '+' ) ;
 *
 * property     = IDENT , { S } ;
 *
 * style_rule   = [ selector_group ] , "{" , declaration_list , "}" ;
 *
 * selector_group
 *              = selector , { "," , { S }, selector } ;
 *
 * selector     = simple_selector ,
 *                { ( combinator , selector
 *                  | { S }, [ [ combinator ] , selector ]
 *                  )
 *                } ;
 *
 * simple_selector
 *              = universal_selector | type_selector | id_selector
 *                | class_selector | pseudoclass_selector | attribute_selector ;
 * universal_selector   = '*' ;
 * type_selector        = ns_aware_ident ;
 * id_selector          = HASH ;
 * class_selector       = "." , IDENT ;
 * pseudoclass_selector = ":" , IDENT ;
 * attribute_selector   = "[" , ns_aware_ident
 *                            , [ ( "=" | "~=" | "|=" ) , ( IDENT | STRING ) ],
 *                        "]" ;
 * ns_aware_ident      = IDENT
 *                      | '*' , '|', IDENT
 *                      | IDENT , '|', IDENT
 *                      ;
 *
 * declaration_list
 *              = { S } , [ declaration ] , [ ';' , declaration_list ] ;
 *
 * declaration  = IDENT , { S } ,  ":", { terms } ;
 *
 * terms        = { { S } , ( term | bracketedTerms ) } ;
 *
 * term         = any token - ( "]" | "}" | ";" | S ) ;
 *
 * bracketedTerms = "{", { { S } , term } , { S } , "}"
 *                | "[", { { S } , term } , { S } , "]";
 *
 *
 * function     = ROUND_BLOCK , { S } , expr , ')' , { S } ;
 * expr         = term , { [ operator ] , term } ;
 * </pre>
 * <p>
 * References:
 * <dl>
 * <dt>CSS Syntax Module Level 3, Chapter 5. Parsing</dt>
 * <dd><a href="https://www.w3.org/TR/2021/CRD-css-syntax-3-20211224/#parsing">w3.org</a></dd>
 * <dt>W3C CSS2.2, Appendix G.1 Grammar of CSS 2.2</dt>
 * <dd><a href="https://www.w3.org/TR/2016/WD-CSS22-20160412/">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class CssParser {

    public static final String ANY_NAMESPACE_PREFIX = "*";
    public static final String DEFAULT_NAMESPACE = "|";
    public static final String NAMESPACE_AT_RULE = "namespace";
    private final Map<String, String> prefixToNamespaceMap = new LinkedHashMap<>();

    {
        //If no default namespace is declared, then names without a namespace
        //prefix match all namespaces.
        //See https://drafts.csswg.org/selectors/#type-nmsp
        prefixToNamespaceMap.put(DEFAULT_NAMESPACE, ANY_NAMESPACE_PREFIX);
        prefixToNamespaceMap.put(ANY_NAMESPACE_PREFIX, ANY_NAMESPACE_PREFIX);
    }

    private @NonNull List<ParseException> exceptions = new ArrayList<>();
    private @Nullable URI stylesheetHome;
    private @Nullable URI stylesheetUri;
    private @NonNull UriResolver uriResolver = new SimpleUriResolver();
    /**
     * To reduce memory pressure, we deduplicate selectors.
     */
    private final @NonNull Map<Selector, Selector> deduplicatedSelectors = new LinkedHashMap<>();


    public CssParser() {
    }

    private @NonNull FunctionPseudoClassSelector createFunctionPseudoClassSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "FunctionPseudoClassSelector: Function expected");
        final @NonNull String ident = tt.currentStringNonNull();
        switch (ident) {
            case "not":
                final SimpleSelector simpleSelector = parseSimpleSelector(tt);
                tt.requireNextToken(')', ":not() Selector: ')' expected.");
                return new NegationPseudoClassSelector(tt.getSourceLocator(), ident, simpleSelector);
            default:
                Loop:
                while (tt.next() != CssTokenType.TT_EOF) {
                    switch (tt.current()) {
                        case ')':
                            tt.pushBack();
                            break Loop;
                        case '{':
                        case '}':
                            final ParseException ex = tt.createParseException(":" + ident + "() Selector ')' expected.");
                            tt.pushBack(); // so that we can resume parsing robustly
                            throw ex;
                        default:
                            break;
                    }
                }
                tt.requireNextToken(')', ":" + ident + "() Selector ')' expected.");
                return new FunctionPseudoClassSelector(tt.getSourceLocator(), ident);
        }
    }

    public @NonNull List<ParseException> getParseExceptions() {
        return exceptions;
    }

    /**
     * Some special at-rules contain information for the parser.
     */
    private void interpretAtRule(AtRule atRule, int position) throws ParseException {
        if (NAMESPACE_AT_RULE.equals(atRule.getAtKeyword())) {
            ListCssTokenizer tt = new ListCssTokenizer(atRule.getHeader());
            final String prefix;
            if (tt.next() == CssTokenType.TT_IDENT) {
                prefix = tt.currentStringNonNull();
            } else {
                prefix = DEFAULT_NAMESPACE;
                tt.pushBack();
            }
            if (tt.next() == CssTokenType.TT_URL || tt.current() == CssTokenType.TT_STRING) {
                String namespace = tt.currentStringNonNull();
                prefixToNamespaceMap.put(prefix, namespace);
            }
        } else {
            exceptions.add(new ParseException("Unsupported At-Rule: @" + atRule.getAtKeyword(),
                    position));
        }
    }

    private @NonNull AtRule parseAtRule(@NonNull CssTokenizer tt) throws IOException, ParseException {
        var sourceLocator = tt.getSourceLocator();
        if (tt.nextNoSkip() != CssTokenType.TT_AT_KEYWORD) {
            throw tt.createParseException("AtRule: At-Keyword expected.");
        }
        String atKeyword = tt.currentStringNonNull();
        tt.next();
        List<CssToken> header = new ArrayList<>();
        while (tt.current() != CssTokenType.TT_EOF
                && tt.current() != '{'//
                && tt.current() != ';') {
            tt.pushBack();
            parseComponentValue(tt, header);
            tt.nextNoSkip();
        }
        List<CssToken> body = new ArrayList<>();
        if (tt.current() == ';') {
            return new AtRule(sourceLocator, atKeyword, header, body);
        } else {
            tt.pushBack();
            parseCurlyBlock(tt, body);
            body.remove(0);
            body.remove(body.size() - 1);
            return new AtRule(sourceLocator, atKeyword, header, body);
        }
    }

    private @NonNull AbstractAttributeSelector parseAttributeSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        tt.requireNextNoSkip('[', "AttributeSelector: '[' expected.");
        String prefixOrName = null;
        String namespace = null;
        String attributeName = null;
        if (tt.nextNoSkip() == CssTokenType.TT_IDENT) {
            prefixOrName = tt.currentStringNonNull();
        } else if (tt.current() == '*') {
            prefixOrName = ANY_NAMESPACE_PREFIX;
            tt.requireNextNoSkip(CssTokenType.TT_VERTICAL_LINE, "AttriuteSelector: '|' expected.");
            tt.pushBack();
        } else {
            tt.pushBack();
        }
        if (tt.nextNoSkip() == CssTokenType.TT_VERTICAL_LINE) {
            namespace = prefixOrName == null ? TypeSelector.WITHOUT_NAMESPACE : resolveNamespacePrefix(prefixOrName, tt);
            tt.requireNextNoSkip(CssTokenType.TT_IDENT, "AttributeSelector: Attribute name expected.");
            attributeName = tt.currentStringNonNull();
        } else {
            namespace = ANY_NAMESPACE_PREFIX;
            attributeName = prefixOrName;
            tt.pushBack();
        }
        AbstractAttributeSelector selector;
        final SourceLocator sourceLocator = tt.getSourceLocator();
        switch (tt.nextNoSkip()) {
            case '=':
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING) {
                    throw tt.createParseException("AttributeSelector: identifier, string or number expected. Line:" + tt.getLineNumber() + ".");
                }
                selector = new EqualsMatchSelector(sourceLocator, namespace, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_INCLUDE_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("AttributeSelector: identifier, string or number expected. Line:" + tt.getLineNumber() + ".");
                }
                selector = new IncludeMatchSelector(sourceLocator, namespace, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_DASH_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("AttributeSelector: identifier, string or number expected. Line:" + tt.getLineNumber() + ".");
                }
                selector = new DashMatchSelector(sourceLocator, namespace, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_PREFIX_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("AttributeSelector: identifier, string or number expected. Line:" + tt.getLineNumber() + ".");
                }
                selector = new PrefixMatchSelector(sourceLocator, namespace, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_SUFFIX_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("AttributeSelector: identifier, string or number expected. Line:" + tt.getLineNumber() + ".");
                }
                selector = new SuffixMatchSelector(sourceLocator, namespace, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_SUBSTRING_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("AttributeSelector: identifier, string or number expected. Line:" + tt.getLineNumber() + ".");
                }
                selector = new SubstringMatchSelector(sourceLocator, namespace, attributeName, tt.currentStringNonNull());
                break;
            case ']':
                selector = new ExistsMatchSelector(sourceLocator, namespace, attributeName);
                tt.pushBack();
                break;
            default:
                throw tt.createParseException("AttributeSelector: operator expected. Line " + tt.getLineNumber() + ".");

        }
        if (tt.nextNoSkip() != ']') {
            throw tt.createParseException("AttributeSelector: ']' expected.");
        }
        return selector;
    }

    private void parseBracketedTerms(@NonNull CssTokenizer tt, @NonNull List<CssToken> terms, int endBracket) throws IOException, ParseException {
        terms.add(new CssToken(tt.current(), tt.currentString(), tt.currentNumber(),
                tt.getLineNumber(), tt.getStartPosition(), tt.getEndPosition()));
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        tt.pushBack();
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != endBracket) {
            switch (tt.current()) {
                case CssTokenType.TT_CDC:
                case CssTokenType.TT_CDO:
                    break;
                case CssTokenType.TT_BAD_URI:
                    throw tt.createParseException("BracketedTerms: Bad URI.");
                case CssTokenType.TT_BAD_STRING:
                    throw tt.createParseException("BracketedTerms: Bad String.");
                case CssTokenType.TT_SEMICOLON:
                    throw tt.createParseException("BracketedTerms: '" + endBracket + "' expected.");
                default:
                    tt.pushBack();
                    parseTerms(tt, terms);
                    break;
            }
        }
        terms.add(new CssToken(tt.current(), tt.currentString(), tt.currentNumber(),
                tt.getLineNumber(), tt.getStartPosition(), tt.getEndPosition()));
    }

    private void parseComponentValue(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        switch (tt.nextNoSkip()) {
            case '{':
                tt.pushBack();
                parseCurlyBlock(tt, preservedTokens);
                break;
            case '(':
                tt.pushBack();
                parseRoundBlock(tt, preservedTokens);
                break;
            case '[':
                tt.pushBack();
                parseSquareBlock(tt, preservedTokens);
                break;
            case CssTokenType.TT_FUNCTION:
                tt.pushBack();
                parseFunctionBlock(tt, preservedTokens);
                break;
            default:
                tt.pushBack();
                parsePreservedToken(tt, preservedTokens);
                break;
        }
    }

    private void parseCurlyBlock(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() != '{') {
            throw tt.createParseException("CurlyBlock: '{' expected.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != '}') {
            tt.pushBack();
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != '}') {
            throw tt.createParseException("CurlyBlock: '}' expected.");
        }
        preservedTokens.add(tt.getToken());
    }

    private @NonNull Declaration parseDeclaration(@NonNull CssTokenizer tt) throws IOException, ParseException {
        if (tt.nextNoSkip() != CssTokenType.TT_IDENT) {
            throw tt.createParseException("Declaration: property name expected.");
        }
        int startPos = tt.getStartPosition();
        int lineNumber = tt.getLineNumber();
        String prefixOrName = tt.currentStringNonNull();
        String namespace;
        String name;
        if (tt.nextNoSkip() == CssTokenType.TT_VERTICAL_LINE) {
            namespace = resolveNamespacePrefix(prefixOrName, tt);
            tt.requireNextNoSkip(CssTokenType.TT_IDENT, "Declaration: property name expected");
            name = tt.currentStringNonNull();
        } else {
            namespace = null;
            name = prefixOrName;
            tt.pushBack();
        }
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        if (tt.current() != ':') {
            throw tt.createParseException("Declaration: ':' expected.");
        }
        List<CssToken> terms = parseTerms(tt);
        int endPos = terms.isEmpty() ? tt.getStartPosition() : terms.get(terms.size() - 1).getEndPos();

        return new Declaration(tt.getSourceLocator(), namespace, name, terms, startPos, endPos, lineNumber);

    }

    /**
     * Parses a declaration list.
     *
     * @param css A stylesheet
     * @return the declaration list
     * @throws IOException if parsing fails
     */
    public @NonNull List<Declaration> parseDeclarationList(@NonNull String css) throws IOException {
        return CssParser.this.parseDeclarationList(new StringReader(css));
    }

    /**
     * Parses a declaration list.
     *
     * @param css A stylesheet
     * @return the declaration list
     * @throws IOException if parsing fails
     */
    public @NonNull List<Declaration> parseDeclarationList(Reader css) throws IOException {
        exceptions = new ArrayList<>();
        CssTokenizer tt = new StreamCssTokenizer(css, null);
        try {
            return parseDeclarationList(tt);
        } catch (ParseException ex) {
            exceptions.add(ex);
        }
        return new ArrayList<>();
    }

    private @NonNull List<Declaration> parseDeclarationList(@NonNull CssTokenizer tt) throws IOException, ParseException {
        List<Declaration> declarations = new ArrayList<>();

        while (tt.next() != CssTokenType.TT_EOF
                && tt.current() != '}') {
            switch (tt.current()) {
                case CssTokenType.TT_IDENT:
                    tt.pushBack();
                    try {
                        declarations.add(parseDeclaration(tt));
                    } catch (ParseException e) {
                        // We could not parse the current declaration.
                        // However we will try to parse the next declarations.
                        exceptions.add(e);
                    }
                    break;
                case ';':
                    break;
                default:
                    throw tt.createParseException("Declaration List: declaration or at-rule expected.");

            }
        }

        tt.pushBack();
        return declarations;

    }

    private void parseFunctionBlock(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() != CssTokenType.TT_FUNCTION) {
            throw tt.createParseException("FunctionBlock: function expected.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != ')') {
            tt.pushBack();
            // FIXME do something with component value
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != ')') {
            throw tt.createParseException("FunctionBlock: ')' expected.");
        }
        preservedTokens.add(tt.getToken());
    }

    private void parsePreservedToken(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() == CssTokenType.TT_EOF) {
            throw tt.createParseException("CssToken: token expected.");
        }
        preservedTokens.add(tt.getToken());
    }

    private @NonNull PseudoClassSelector parsePseudoClassSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        if (tt.nextNoSkip() != ':') {
            throw tt.createParseException("Pseudo Class Selector: ':' expected of \"" + tt.currentString() + "\". Line " + tt.getLineNumber() + ".");
        }
        if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                && tt.current() != CssTokenType.TT_FUNCTION) {
            throw tt.createParseException("Pseudo Class Selector: identifier or function expected instead of \"" + tt.currentString() + "\". Line " + tt.getLineNumber() + ".");
        }

        if (tt.current() == CssTokenType.TT_FUNCTION) {
            tt.pushBack();
            return createFunctionPseudoClassSelector(tt);
        } else {

            return new SimplePseudoClassSelector(tt.getSourceLocator(), tt.currentString());
        }
    }

    private void parseRoundBlock(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() != '(') {
            throw tt.createParseException("RoundBlock: '(' expected.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != ')') {
            tt.pushBack();
            // FIXME do something with component value
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != ')') {
            throw tt.createParseException("RoundBlock: ')' expected.");
        }
        preservedTokens.add(tt.getToken());
    }

    private @NonNull Selector parseSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        SimpleSelector simpleSelector = parseSimpleSelector(tt);
        Selector selector = simpleSelector;
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != '{' && tt.current() != ',') {

            boolean potentialDescendantCombinator = false;
            if (tt.current() == CssTokenType.TT_S) {
                potentialDescendantCombinator = true;
                skipWhitespaceAndComments(tt);
            }
            if (tt.current() == CssTokenType.TT_EOF
                    || tt.current() == '{' || tt.current() == ',') {
                break;
            }
            final SourceLocator sourceLocator = tt.getSourceLocator();
            switch (tt.current()) {
                case CssTokenType.TT_GREATER_THAN:
                    selector = new ChildCombinator(sourceLocator, simpleSelector, parseSelector(tt));
                    break;
                case CssTokenType.TT_PLUS:
                    selector = new AdjacentSiblingCombinator(sourceLocator, simpleSelector, parseSelector(tt));
                    break;
                case CssTokenType.TT_TILDE:
                    selector = new GeneralSiblingCombinator(sourceLocator, simpleSelector, parseSelector(tt));
                    break;
                default:
                    tt.pushBack();
                    if (potentialDescendantCombinator) {
                        selector = new DescendantCombinator(sourceLocator, simpleSelector, parseSelector(tt));
                    } else {
                        selector = new AndCombinator(sourceLocator, simpleSelector, parseSelector(tt));
                    }
                    break;
            }
        }
        tt.pushBack();
        return deduplicatedSelectors.computeIfAbsent(selector, Function.identity());
    }

    public @NonNull SelectorGroup parseSelectorGroup(@NonNull CssTokenizer tt) throws IOException, ParseException {
        List<Selector> selectors = new ArrayList<>();
        selectors.add(parseSelector(tt));
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != '{') {
            skipWhitespaceAndComments(tt);
            if (tt.current() != ',') {
                throw tt.createParseException("SelectorGroup: ',' expected.");
            }
            tt.nextNoSkip();
            skipWhitespaceAndComments(tt);
            tt.pushBack();
            selectors.add(parseSelector(tt));
        }
        tt.pushBack();
        return new SelectorGroup(tt.getSourceLocator(), selectors);
    }

    private @NonNull SimpleSelector parseSimpleSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        final SimpleSelector simpleSelector = parseSimpleSelector0(tt);
        return (SimpleSelector) deduplicatedSelectors.computeIfAbsent(simpleSelector, Function.identity());
    }

    private @NonNull SimpleSelector parseSimpleSelector0(@NonNull CssTokenizer tt) throws IOException, ParseException {
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);

        try {
            final SourceLocator sourceLocator = tt.getSourceLocator();
            switch (tt.current()) {
                case '*':
                    if (tt.nextNoSkip() == '|') {
                        tt.requireNextNoSkip(CssTokenType.TT_IDENT, "element name expected after *|");
                        return new TypeSelector(sourceLocator, TypeSelector.ANY_NAMESPACE, tt.currentStringNonNull());
                    } else {
                        tt.pushBack();
                        return new UniversalSelector(sourceLocator);
                    }
                case '|':
                    tt.requireNextNoSkip(CssTokenType.TT_IDENT, "element name expected after |");
                    return new TypeSelector(sourceLocator, TypeSelector.WITHOUT_NAMESPACE, tt.currentStringNonNull());
                case CssTokenType.TT_IDENT:
                    String typeOrPrefix = tt.currentStringNonNull();
                    if (tt.nextNoSkip() == '|') {
                        tt.requireNextNoSkip(CssTokenType.TT_IDENT, "element name expected after " + typeOrPrefix + "|");
                        return new TypeSelector(sourceLocator, resolveNamespacePrefix(typeOrPrefix, tt), tt.currentStringNonNull());
                    } else {
                        tt.pushBack();
                        return new TypeSelector(sourceLocator, resolveNamespacePrefix(DEFAULT_NAMESPACE, tt), typeOrPrefix);
                    }
                case CssTokenType.TT_HASH:
                    return new IdSelector(sourceLocator, tt.currentString());
                case '.':
                    if (tt.nextNoSkip() != CssTokenType.TT_IDENT) {
                        throw tt.createParseException("SimpleSelector: identifier expected.");
                    }
                    return new ClassSelector(sourceLocator, tt.currentString());
                case ':':
                    tt.pushBack();
                    return parsePseudoClassSelector(tt);
                case '[':
                    tt.pushBack();
                    return parseAttributeSelector(tt);
                case '{':
                    tt.pushBack();
                    throw tt.createParseException("SimpleSelector: SimpleSelector expected instead of \"" + tt.currentString() + "\". Line " + tt.getLineNumber() + ".");
                default:
                    // don't push back!
                    throw tt.createParseException("SimpleSelector: SimpleSelector expected instead of \"" + tt.currentString() + "\". Line " + tt.getLineNumber() + ".");
            }
        } catch (ParseException e) {
            exceptions.add(e);
            return new SelectNothingSelector(tt.getSourceLocator());
        }
    }

    private void parseSquareBlock(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() != '[') {
            throw tt.createParseException("SquareBlock: '[' expected.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != ']') {
            tt.pushBack();
            // FIXME do something with component value
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != ']') {
            throw tt.createParseException("SquareBlock: ']' expected.");
        }
        preservedTokens.add(tt.getToken());
    }

    private @NonNull StyleRule parseStyleRule(@NonNull CssTokenizer tt) throws IOException, ParseException {
        SelectorGroup selectorGroup;
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        var sourceLocator = tt.getSourceLocator();
        if (tt.current() == '{') {
            tt.pushBack();
            selectorGroup = new SelectorGroup(sourceLocator, new UniversalSelector(sourceLocator));
        } else {
            tt.pushBack();
            selectorGroup = parseSelectorGroup(tt);
        }
        skipWhitespaceAndComments(tt);
        if (tt.nextNoSkip() != '{') {
            throw tt.createParseException("StyleRule: '{' expected.");
        }
        List<Declaration> declarations = parseDeclarationList(tt);
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        if (tt.current() != '}') {
            throw tt.createParseException("StyleRule: '}' expected.");
        }
        return new StyleRule(sourceLocator, selectorGroup, declarations);
    }

    /**
     * Parses a given stylesheet from the specified URI.
     *
     * @param stylesheetUri  the URI of the stylesheet (must be known)
     * @param stylesheetHome base URI (if it exists)
     * @return the parsed stylesheet
     * @throws IOException on failure
     */
    public @NonNull Stylesheet parseStylesheet(@NonNull URI stylesheetUri, @Nullable URI stylesheetHome) throws IOException {
        try (Reader in = new BufferedReader(new InputStreamReader(stylesheetUri.toURL().openConnection().getInputStream(), StandardCharsets.UTF_8))) {
            return parseStylesheet(in, stylesheetUri, stylesheetHome);
        }
    }

    /**
     * Parses a given stylesheet from the specified String and document home.
     *
     * @param css            the uri of the stylesheet file
     * @param stylesheetUri  the URI of the stylesheet (if known)
     * @param stylesheetHome base URI (if it exists)
     * @return the parsed stylesheet
     * @throws IOException on failure
     */
    public @NonNull Stylesheet parseStylesheet(@NonNull String css, @Nullable URI stylesheetUri, @Nullable URI stylesheetHome) throws IOException {
        return parseStylesheet(new StringReader(css), stylesheetUri, stylesheetHome);
    }

    /**
     * Parses a given selector from the specified String and document home.
     *
     * @param css a literal selector String
     * @return the parsed selector
     * @throws IOException on failure
     */
    public @NonNull Selector parseSelector(@NonNull String css) throws ParseException {
        try {
            return parseSelector(new StreamCssTokenizer(new StringReader(css)));
        } catch (IOException e) {
            var pe = new ParseException("Error parssing selector", 0);
            pe.initCause(e);
            throw pe;
        }
    }

    /**
     * Parses a given stylesheet from the specified String and document home.
     *
     * @param css            the uri of the stylesheet file
     * @param stylesheetUri  the URI of the stylesheet (if known)
     * @param stylesheetHome base URI (if it exists)
     * @return the parsed stylesheet
     * @throws IOException on failure
     */
    public @NonNull Stylesheet parseStylesheet(Reader css, @Nullable URI stylesheetUri, @Nullable URI stylesheetHome) throws IOException {
        exceptions = new ArrayList<>();
        CssTokenizer tt = new StreamCssTokenizer(css, stylesheetUri);
        return parseStylesheet(tt, stylesheetUri, stylesheetHome);
    }

    /**
     * Parses a given stylesheet from the specified String and document home.
     *
     * @param tt             the tokenier
     * @param stylesheetUri  the URI of the stylesheet (if known)
     * @param stylesheetHome base URI (if it exists)
     * @return the parsed stylesheet
     * @throws IOException on failure
     */
    public @NonNull Stylesheet parseStylesheet(@NonNull CssTokenizer tt, @Nullable URI stylesheetUri, @Nullable URI stylesheetHome) throws IOException {
        setStylesheetUri(stylesheetUri);
        setStylesheetHome(stylesheetHome);
        List<Rule> rules = new ArrayList<>();
        while (tt.nextNoSkip() != CssTokenType.TT_EOF) {
            try {
                switch (tt.current()) {
                    case CssTokenType.TT_S:
                    case CssTokenType.TT_CDC:
                    case CssTokenType.TT_CDO:
                    case CssTokenType.TT_COMMENT:
                        break;
                    case CssTokenType.TT_AT_KEYWORD: {
                        tt.pushBack();
                        final int startPosition = tt.getStartPosition();
                        AtRule r = parseAtRule(tt);
                        interpretAtRule(r, startPosition);
                        rules.add(r);
                        break;
                    }
                    default: {
                        tt.pushBack();
                        // FIXME parse qualified rules instead of style rule
                        StyleRule r = parseStyleRule(tt);
                        rules.add(r);
                        break;
                    }
                }
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
        return new Stylesheet(getStylesheetUri(), rules);
    }

    private @NonNull List<CssToken> parseTerms(@NonNull CssTokenizer tt) throws IOException, ParseException {
        List<CssToken> terms = new ArrayList<>();
        return parseTerms(tt, terms);
    }

    private @NonNull List<CssToken> parseTerms(@NonNull CssTokenizer tt, List<CssToken> terms) throws IOException, ParseException {
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        tt.pushBack();
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != CssTokenType.TT_RIGHT_CURLY_BRACKET
                && tt.current() != CssTokenType.TT_RIGHT_SQUARE_BRACKET
                && tt.current() != CssTokenType.TT_SEMICOLON) {
            switch (tt.current()) {
                case CssTokenType.TT_CDC:
                case CssTokenType.TT_CDO:
                    break;
                case CssTokenType.TT_BAD_URI:
                    throw tt.createParseException("Terms: Bad URI.");
                case CssTokenType.TT_BAD_STRING:
                    throw tt.createParseException("Terms: Bad String.");
                case CssTokenType.TT_LEFT_CURLY_BRACKET:
                    parseBracketedTerms(tt, terms, CssTokenType.TT_RIGHT_CURLY_BRACKET);
                    break;
                case CssTokenType.TT_LEFT_SQUARE_BRACKET:
                    parseBracketedTerms(tt, terms, CssTokenType.TT_RIGHT_SQUARE_BRACKET);
                    break;
                case CssTokenType.TT_URL:
                    terms.add(new CssToken(tt.current(), absolutizeUri(tt.currentStringNonNull()), tt.currentNumber(),
                            tt.getLineNumber(), tt.getStartPosition(), tt.getEndPosition()));
                    break;
                default:
                    terms.add(new CssToken(tt.current(), tt.currentString(), tt.currentNumber(),
                            tt.getLineNumber(), tt.getStartPosition(), tt.getEndPosition()));
                    break;
            }
        }
        tt.pushBack();
        return terms;
    }

    /**
     * Resolves an URL with the DocumentHome URL of this parser.
     *
     * @param relativeUri an URL string
     * @return the resolved URL
     */
    @NonNull
    private String absolutizeUri(@NonNull String relativeUri) {
        if (stylesheetHome == null) {
            return relativeUri;
        }
        try {
            return uriResolver.absolutize(stylesheetHome, new URI(relativeUri)).toString();
        } catch (URISyntaxException e) {
            return relativeUri;
        }
    }

    /**
     * Resolves the namespace prefix.
     *
     * @param namespacePrefix a namespace prefix
     * @param tt              the tokenizer
     * @return a namespace URL or null
     * @throws ParseException if the namespace prefix is not declared.
     */
    private @Nullable String resolveNamespacePrefix(@Nullable String namespacePrefix, CssTokenizer tt) throws ParseException {
        if (namespacePrefix == null) {
            return prefixToNamespaceMap.get(DEFAULT_NAMESPACE);
        }

        String s = prefixToNamespaceMap.get(namespacePrefix);
        if (s == null) {
            throw tt.createParseException("namespace prefix is not declared: \"" + namespacePrefix + "\".");
        }
        return s;
    }

    private void skipWhitespaceAndComments(@NonNull CssTokenizer tt) throws IOException {
        while (tt.current() == CssTokenType.TT_S//
                || tt.current() == CssTokenType.TT_CDC//
                || tt.current() == CssTokenType.TT_CDO
                || tt.current() == CssTokenType.TT_COMMENT
                || tt.current() == CssTokenType.TT_BAD_COMMENT) {
            tt.nextNoSkip();
        }
    }

    public @Nullable URI getStylesheetUri() {
        return stylesheetUri;
    }

    public void setStylesheetUri(@Nullable URI stylesheetUri) {
        this.stylesheetUri = stylesheetUri;
    }

    public @Nullable URI getStylesheetHome() {
        return stylesheetHome;
    }

    public void setStylesheetHome(@Nullable URI stylesheetHome) {
        this.stylesheetHome = stylesheetHome;
    }

    public @NonNull UriResolver getUriResolver() {
        return uriResolver;
    }

    public void setUriResolver(@NonNull UriResolver uriResolver) {
        this.uriResolver = uriResolver;
    }
}
