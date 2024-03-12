/*
 * @(#)CssParser.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.parser;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.SimpleUriResolver;
import org.jhotdraw8.base.converter.UriResolver;
import org.jhotdraw8.css.ast.AbstractAttributeSelector;
import org.jhotdraw8.css.ast.AdjacentSiblingCombinator;
import org.jhotdraw8.css.ast.AndCombinator;
import org.jhotdraw8.css.ast.AtRule;
import org.jhotdraw8.css.ast.ChildCombinator;
import org.jhotdraw8.css.ast.ClassSelector;
import org.jhotdraw8.css.ast.DashMatchSelector;
import org.jhotdraw8.css.ast.Declaration;
import org.jhotdraw8.css.ast.DescendantCombinator;
import org.jhotdraw8.css.ast.EqualsMatchSelector;
import org.jhotdraw8.css.ast.ExistsMatchSelector;
import org.jhotdraw8.css.ast.FunctionPseudoClassSelector;
import org.jhotdraw8.css.ast.GeneralSiblingCombinator;
import org.jhotdraw8.css.ast.IdSelector;
import org.jhotdraw8.css.ast.IncludeMatchSelector;
import org.jhotdraw8.css.ast.NegationPseudoClassSelector;
import org.jhotdraw8.css.ast.PrefixMatchSelector;
import org.jhotdraw8.css.ast.PseudoClassSelector;
import org.jhotdraw8.css.ast.Rule;
import org.jhotdraw8.css.ast.SelectNothingSelector;
import org.jhotdraw8.css.ast.Selector;
import org.jhotdraw8.css.ast.SelectorGroup;
import org.jhotdraw8.css.ast.SimplePseudoClassSelector;
import org.jhotdraw8.css.ast.SimpleSelector;
import org.jhotdraw8.css.ast.SourceLocator;
import org.jhotdraw8.css.ast.StyleRule;
import org.jhotdraw8.css.ast.Stylesheet;
import org.jhotdraw8.css.ast.SubstringMatchSelector;
import org.jhotdraw8.css.ast.SuffixMatchSelector;
import org.jhotdraw8.css.ast.TypeSelector;
import org.jhotdraw8.css.ast.UniversalSelector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
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
 * The parser interprets the following at rules:
 * <pre>
 * namespace_rule = "@namespace" , [ namespace_prefix ] , ( STRING | URI ) ;
 * namespace_prefix = IDENT ;
 * </pre>
 * <p>
 * References:
 * <dl>
 * <dt>CSS Syntax Module Level 3, Paragraph 5. Parsing</dt>
 * <dd><a href="https://drafts.csswg.org/css-namespaces/#declaration">w3.org</a></dd>
 *
 * <dt>CSS Namespaces Module Level 3, Paragraph 2 Declaring namespaces: the @namespace rulex</dt>
 * <dd><a href="https://drafts.csswg.org/css-namespaces/#declaration">w3.org</a></dd>
 *
 * <dt>W3C CSS2.2, Appendix G.1 Grammar of CSS 2.2</dt>
 * <dd><a href="https://www.w3.org/TR/2016/WD-CSS22-20160412/">w3.org</a></dd>
 * </dl>
 * <p>
 * FIXME The parser does not support the !important declaration.
 *
 * @author Werner Randelshofer
 */
public class CssParser {

    public static final String ANY_NAMESPACE_PREFIX = "*";
    public static final String DEFAULT_NAMESPACE = "|";
    public static final String NAMESPACE_AT_RULE = "namespace";
    private final SequencedMap<String, String> prefixToNamespaceMap = new LinkedHashMap<>();

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

    private boolean strict = false;

    /**
     * To reduce memory pressure, we deduplicate selectors.
     */
    private final @NonNull SequencedMap<Selector, Selector> deduplicatedSelectors = new LinkedHashMap<>();


    public CssParser() {
    }

    private @NonNull FunctionPseudoClassSelector createFunctionPseudoClassSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "FunctionPseudoClassSelector: Function expected");
        final @NonNull String ident = tt.currentStringNonNull();
        switch (ident) {
            case "not":
                final SimpleSelector simpleSelector = parseSimpleSelector(tt);
                tt.requireNextToken(')', "Could not parse the \":not()\" pseudo-class selector, because it does not end with a closing bracket ')' character.");
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
                            final ParseException ex = tt.createParseException("Could not parse the \":" + ident + "()\" pseudo-class selector, because it contains unexpected curly bracket '{', '}' characters.");
                            tt.pushBack(); // so that we can resume parsing robustly
                            throw ex;
                        default:
                            break;
                    }
                }
                tt.requireNextToken(')', "Could not parse the \":" + ident + "()\" pseudo-class selector, because it does not end with a closing bracket ')' character.");
                return new FunctionPseudoClassSelector(tt.getSourceLocator(), ident);
        }
    }

    public @NonNull List<ParseException> getParseExceptions() {
        return exceptions;
    }

    /**
     * Some special at-rules contain information for the parser.
     */
    private void interpretAtRule(AtRule atRule, int position) {
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
            exceptions.add(new ParseException("Could not parse the At-Rule \"@" + atRule.getAtKeyword() + "\".",
                    position));
        }
    }

    private @NonNull AtRule parseAtRule(@NonNull CssTokenizer tt) throws IOException, ParseException {
        var sourceLocator = tt.getSourceLocator();
        if (tt.nextNoSkip() != CssTokenType.TT_AT_KEYWORD) {
            throw tt.createParseException("Could not parse the At-Rule, because it does not start with an '@' character.");
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
            body.removeFirst();
            body.removeLast();
            return new AtRule(sourceLocator, atKeyword, header, body);
        }
    }

    private @NonNull AbstractAttributeSelector parseAttributeSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        tt.requireNextNoSkip('[', "Could not parse an AttributeSelector because it does not start with an opening square bracket '[' character.");
        String prefixOrName = null;
        String namespacePattern = null;
        String attributeName = null;
        if (tt.nextNoSkip() == CssTokenType.TT_IDENT) {
            prefixOrName = tt.currentStringNonNull();
        } else if (tt.current() == '*') {
            prefixOrName = ANY_NAMESPACE_PREFIX;
            tt.requireNextNoSkip(CssTokenType.TT_VERTICAL_LINE, "Could not parse a '*|' namespace prefix because it does not contain the '|' character.");
            tt.pushBack();
        } else {
            tt.pushBack();
        }
        if (tt.nextNoSkip() == CssTokenType.TT_VERTICAL_LINE) {
            namespacePattern = prefixOrName == null ? TypeSelector.WITHOUT_NAMESPACE : resolveNamespacePrefix(prefixOrName, tt);
            tt.requireNextNoSkip(CssTokenType.TT_IDENT, "Could not parse an AttributeSelector because it does not contain an attribute name after the square bracket '[' character.");
            attributeName = tt.currentStringNonNull();
        } else {
            namespacePattern = ANY_NAMESPACE_PREFIX;
            attributeName = prefixOrName;
            tt.pushBack();
        }
        AbstractAttributeSelector selector;
        final SourceLocator sourceLocator = tt.getSourceLocator();
        switch (tt.nextNoSkip()) {
            case '=':
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING) {
                    throw tt.createParseException("Could not parse an EqualityMatch because it does not contain an attribute value after the '=' character.");
                }
                selector = new EqualsMatchSelector(sourceLocator, namespacePattern, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_INCLUDE_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("Could not parse an IncludeMatch because it does not contain an attribute value after the '~=' characters.");
                }
                selector = new IncludeMatchSelector(sourceLocator, namespacePattern, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_DASH_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("Could not parse a DashMatch because it does not contain an attribute value after the '-=' characters.");
                }
                selector = new DashMatchSelector(sourceLocator, namespacePattern, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_PREFIX_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("Could not parse a PrefixMatch because it does not contain an attribute value after the '^=' characters.");
                }
                selector = new PrefixMatchSelector(sourceLocator, namespacePattern, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_SUFFIX_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("Could not parse a SuffixMatch because it does not contain an attribute after the '$=' characters.");
                }
                selector = new SuffixMatchSelector(sourceLocator, namespacePattern, attributeName, tt.currentStringNonNull());
                break;
            case CssTokenType.TT_SUBSTRING_MATCH:
                if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                        && tt.current() != CssTokenType.TT_STRING
                        && tt.current() != CssTokenType.TT_NUMBER) {
                    throw tt.createParseException("Could not parse a SubstringMatch because it does not contain an attribute after the '*=' characters.");
                }
                selector = new SubstringMatchSelector(sourceLocator, namespacePattern, attributeName, tt.currentStringNonNull());
                break;
            case ']':
                selector = new ExistsMatchSelector(sourceLocator, namespacePattern, attributeName);
                tt.pushBack();
                break;
            default:
                throw tt.createParseException("Could not parse an AttributeSelector because it does contain an unexpected operand: " + tt.getToken() + ".");

        }
        if (tt.nextNoSkip() != ']') {
            throw tt.createParseException("Could not parse an AttributeSelector because it does not end with a closing square bracket ']' character.");
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
                    throw tt.createParseException("Could not parse BracketedTerms because it contains a bad URI.");
                case CssTokenType.TT_BAD_STRING:
                    throw tt.createParseException("Could not parse BracketedTerms because it contains a bad String.");
                case CssTokenType.TT_SEMICOLON:
                    throw tt.createParseException("Could not parse BracketedTerms because of an unexpected semicolon ';' character.");
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
            throw tt.createParseException("Could not parse a CurlyBlock because it does not start with an opening curly bracket '{' character.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != '}') {
            tt.pushBack();
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != '}') {
            throw tt.createParseException("Could not parse a CurlyBlock because it does not end with an closing curly bracket '}' character.");
        }
        preservedTokens.add(tt.getToken());
    }

    private @NonNull Declaration parseDeclaration(@NonNull CssTokenizer tt) throws IOException, ParseException {
        if (tt.nextNoSkip() != CssTokenType.TT_IDENT) {
            throw tt.createParseException("Could not parse a Declaration because it does not start with an identifier.");
        }
        int startPos = tt.getStartPosition();
        int lineNumber = tt.getLineNumber();
        String prefixOrName = tt.currentStringNonNull();
        String namespace;
        String name;
        if (tt.nextNoSkip() == CssTokenType.TT_VERTICAL_LINE) {
            namespace = resolveNamespacePrefix(prefixOrName, tt);
            tt.requireNextNoSkip(CssTokenType.TT_IDENT, "Could not parse a Declaration because it does not contain a property name.");
            name = tt.currentStringNonNull();
        } else {
            namespace = null;
            name = prefixOrName;
            tt.pushBack();
        }
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        if (tt.current() != ':') {
            throw tt.createParseException("Could not parse a Declaration because it does not contain a colon ':' character.");
        }
        List<CssToken> terms = parseTerms(tt);
        int endPos = terms.isEmpty() ? tt.getStartPosition() : terms.getLast().getEndPos();

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
                    throw tt.createParseException("Could not parse a DeclarationList because it does not contain a Declaration or At-rule.");

            }
        }

        tt.pushBack();
        return declarations;

    }

    private void parseFunctionBlock(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() != CssTokenType.TT_FUNCTION) {
            throw tt.createParseException("Could not parse a FunctionBlock because it does not start with a function.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != ')') {
            tt.pushBack();
            // FIXME do something with component value
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != ')') {
            throw tt.createParseException("Could not parse a FunctionBlock because it does not end with a closing bracket ')' character.");
        }
        preservedTokens.add(tt.getToken());
    }

    private void parsePreservedToken(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() == CssTokenType.TT_EOF) {
            throw tt.createParseException("Could not parse a PreservedToken because of unexpected end-of-file.");
        }
        preservedTokens.add(tt.getToken());
    }

    private @NonNull PseudoClassSelector parsePseudoClassSelector(@NonNull CssTokenizer tt) throws IOException, ParseException {
        if (tt.nextNoSkip() != ':') {
            throw tt.createParseException("Could not parse a PseudoClassSelector because it does not start with a colon ':' character.");
        }
        if (tt.nextNoSkip() != CssTokenType.TT_IDENT
                && tt.current() != CssTokenType.TT_FUNCTION) {
            throw tt.createParseException("Could not parse a PseudoClassSelector because it does not contain an identifier or a function after the colon ':' character.");
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
            throw tt.createParseException("Could not parse a RoundBlock because it does not start with an opening bracket '(' character.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != ')') {
            tt.pushBack();
            // FIXME do something with component value
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != ')') {
            throw tt.createParseException("Could not parse a RoundBlock because it does not end with an closing bracket ')' character.");
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
                throw tt.createParseException("Could not parse a SelectorGroup because it does not contain a comma ',' character.");
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

    private @NonNull SimpleSelector parseSimpleSelector0(@NonNull CssTokenizer tt) throws IOException {
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
                        throw tt.createParseException("Could not parse a SimpleSelector because it does not contain an identifier.");
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
                    throw tt.createParseException("Could not parse a SimpleSelector because it contains an unexpected curly bracket '{' character.");
                default:
                    // don't push back!
                    throw tt.createParseException("Could not parse a SimpleSelector because it contains an unexpected " + tt.getToken() + ".");
            }
        } catch (ParseException e) {
            exceptions.add(e);
            return new SelectNothingSelector(tt.getSourceLocator());
        }
    }

    private void parseSquareBlock(@NonNull CssTokenizer tt, @NonNull List<CssToken> preservedTokens) throws IOException, ParseException {
        if (tt.nextNoSkip() != '[') {
            throw tt.createParseException("Could not parse a SquareBlock because it does not start with an opening square bracket '[' character.");
        }
        preservedTokens.add(tt.getToken());
        while (tt.nextNoSkip() != CssTokenType.TT_EOF
                && tt.current() != ']') {
            tt.pushBack();
            // FIXME do something with component value
            parseComponentValue(tt, preservedTokens);
        }
        if (tt.current() != ']') {
            throw tt.createParseException("Could not parse a SquareBlock because it does not end with a closing square bracket ']' character.");
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
            throw tt.createParseException("Could not parse a StyleRule because it does not contain an opening curly bracket '{' character.");
        }
        List<Declaration> declarations = parseDeclarationList(tt);
        tt.nextNoSkip();
        skipWhitespaceAndComments(tt);
        if (tt.current() != '}') {
            throw tt.createParseException("Could not parse a StyleRule because it does not end with a closing curly bracket '}' character.");
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
     * @throws ParseException on failure
     */
    public @NonNull Selector parseSelector(@NonNull String css) throws ParseException {
        try {
            return parseSelector(new StreamCssTokenizer(new StringReader(css)));
        } catch (IOException e) {
            throw (ParseException) new ParseException("Could not parse a Selector.", 0).initCause(e);
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
                    throw tt.createParseException("Could not parse Terms because it contains a bad URI.");
                case CssTokenType.TT_BAD_STRING:
                    throw tt.createParseException("Could not parse Terms because it contains a bad String.");
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
        return switch (namespacePrefix) {
            case null -> null;// null means no namespace
            case ANY_NAMESPACE_PREFIX -> ANY_NAMESPACE_PREFIX;// '*' means any namespace
            default -> {
                String s = prefixToNamespaceMap.get(namespacePrefix);
                if (s == null) {
                    if (strict) {
                        throw tt.createParseException("Could not find a namespace with namespacePrefix=\"" + namespacePrefix + "\".");
                    }
                    s = ANY_NAMESPACE_PREFIX;
                }
                yield s;
            }
        };
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

    /**
     * Return strict parsing mode.
     *
     * @return true if the parser is in strict mode.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Sets strict parsing mode.
     *
     * @param strict true to parse in strict mode, the default value is false.
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
