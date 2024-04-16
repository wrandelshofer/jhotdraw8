/*
 * @(#)IndentingXMLStreamWriter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.xml;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * IndentingXMLStreamWriter is a {@link XMLStreamWriter} that supports automatic
 * indentation of XML elements and alphabetic sorting of XML attributes.
 * <p>
 * The textual representation of the output is non-canonical. The
 * following aspects of the textual representation are the same as in Canonical
 * XML:
 * <ul>
 *     <li>Attributes are sorted by namespace URI, rather than by the namespace
 *     prefix.</li>
 *     <li>The hex-encoding of special characters, is written in uppercase
 *     hexadecimal with no leading zeroes, for example {@code #xD}.</li>
 * </ul>
 *
 * <p>
 * This writer writes an XML 1.0 document with the following syntax rules.
 * <pre>
 * Document
 *  [1]  document       ::=  prolog element Misc*
 *
 * Character Range
 *  [2]  Char           ::=  #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
 *
 * White Space
 *  [3]     S           ::=  (#x20 | #x9 | #xD | #xA)+
 *
 * Names and Tokens
 *  [4]  NameStartChar  ::=  ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
 *  [4a] NameChar       ::=  NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
 *  [5]  Name           ::=  NameStartChar (NameChar)*
 *  [6]  Names          ::=  Name (#x20 Name)*
 *  [7]  Nmtoken        ::=  (NameChar)+
 *  [8]  Nmtokens       ::=  Nmtoken (#x20 Nmtoken)*
 *
 * Literals
 *  [9]  EntityValue    ::=  '"' ([^%&amp;"] | PEReference | Reference)* '"'
 *                        |  "'" ([^%&amp;'] | PEReference | Reference)* "'"
 * [10]  AttValue       ::=  '"' ([^&lt;&amp;"] | Reference)* '"'
 *                        |  "'" ([^&lt;&amp;'] | Reference)* "'"
 * [11]  SystemLiteral  ::=  ('"' [^"]* '"') | ("'" [^']* "'")
 * [12]  PubidLiteral   ::=  '"' PubidChar* '"' | "'" (PubidChar - "'")* "'"
 * [13]  PubidChar      ::=  #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]
 *
 * Character Data
 * [14]  CharData       ::=  [^&lt;&amp;]* - ([^&lt;&amp;]* ']]&gt;' [^&lt;&amp;]*)
 *
 * Comments
 * [15]  Comment        ::=  '&lt;!--' ((Char - '-') | ('-' (Char - '-')))* '--&gt;'
 *
 * Processing Instructions
 * [16]  PI             ::=  '&lt;?' PITarget (S (Char* - (Char* '?&gt;' Char*)))? '?&gt;'
 * [17]  PITarget       ::=  Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))
 *
 * CDATA Sections
 * [18]  CDSect         ::=  CDStart CData CDEnd
 * [19]  CDStart        ::=  '&lt;![CDATA['
 * [20]  CData          ::=  (Char* - (Char* ']]&gt;' Char*))
 * [21]  CDEnd          ::=  ']]&gt;'
 *
 * Prolog
 * [22] prolog          ::=  XMLDecl? Misc* (doctypedecl Misc*)?
 * [23] XMLDecl         ::=  '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?&gt;'
 * [24] VersionInfo     ::=  S 'version' Eq ("'" VersionNum "'" | '"' VersionNum '"')
 * [25] Eq              ::=  S? '=' S?
 * [26] VersionNum      ::=  '1.' [0-9]+
 * [27] Misc            ::=  Comment | PI | S
 *
 * Document Type Definition
 * [28]  doctypedecl    ::=  '&lt;!DOCTYPE' S Name (S ExternalID)? S? ('[' intSubset ']' S?)? '&gt;'
 * [28a] DeclSep        ::=  PEReference | S     [WFC: PE Between Declarations]
 * [28b] intSubset      ::=  (markupdecl | DeclSep)*
 * [29]  markupdecl     ::=  elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI | Comment
 *
 * External Subset
 * [30]  extSubset      ::=  TextDecl? extSubsetDecl
 * [31]  extSubsetDecl  ::=  ( markupdecl | conditionalSect | DeclSep)*
 *
 * Standalone Document Declaration
 * [32]  SDDecl         ::=  S 'standalone' Eq (("'" ('yes' | 'no') "'") | ('"' ('yes' | 'no') '"'))
 *
 * Element
 * [39]  element        ::=  EmptyElemTag
 *                        |  STag content ETag
 *
 * Start-tag
 * [40]  STag           ::=  '&lt;' Name (S Attribute)* S? '&gt;'
 * [41]  Attribute      ::=  Name Eq AttValue
 *
 * End-tag
 * [42]  ETag           ::=  '&lt;/' Name S? '&gt;'
 *
 * Content of Elements
 * [43]  content        ::=  CharData? ((element | Reference | CDSect | PI | Comment) CharData?)*
 *
 * Tags for Empty Elements
 * [44]  EmptyElemTag   ::=  '&lt;' Name (S Attribute)* S? '/&gt;'    [WFC: Unique Att Spec]
 *
 * Element Type Declaration
 * [45]  elementdecl    ::=  '&lt;!ELEMENT' S Name S contentspec S? '&gt;'    [VC: Unique Element Type Declaration]
 * [46]  contentspec    ::=  'EMPTY' | 'ANY' | Mixed | children
 *
 * Element-content Models
 * [47]  children       ::=  (choice | seq) ('?' | '*' | '+')?
 * [48]  cp             ::=  (Name | choice | seq) ('?' | '*' | '+')?
 * [49]  choice         ::=  '(' S? cp ( S? '|' S? cp )+ S? ')'    [VC: Proper Group/PE Nesting]
 * [50]  seq            ::=  '(' S? cp ( S? ',' S? cp )* S? ')'
 *
 * Mixed-content Declaration
 * [51]  Mixed          ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*'
 *                        | '(' S? '#PCDATA' S? ')'
 *
 * Attribute-list Declaration
 * [52]  AttlistDecl    ::=  '&lt;!ATTLIST' S Name AttDef* S? '&gt;'
 * [53]  AttDef         ::=  S Name S AttType S DefaultDecl
 *
 * Attribute Types
 * [54]  AttType        ::=  StringType | TokenizedType | EnumeratedType
 * [55]  StringType     ::=  'CDATA'
 * [56]  TokenizedType  ::=  'ID'
 *                        |  'IDREF'
 *                        |  'IDREFS'
 *                        |  'ENTITY'
 *                        |  'ENTITIES'
 *                        |  'NMTOKEN'
 *                        |  'NMTOKENS'
 *
 *
 * Enumerated Attribute Types
 * [57]  EnumeratedType ::=  NotationType | Enumeration
 * [58]  NotationType   ::=  'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'
 *
 * [59]  Enumeration    ::=  '(' S? Nmtoken (S? '|' S? Nmtoken)* S? ')'
 *
 * Attribute Defaults
 * [60] DefaultDecl     ::=  '#REQUIRED' | '#IMPLIED'
 *                        |  (('#FIXED' S)? AttValue)
 *
 * Conditional Section
 * [61]  conditionalSect ::= includeSect | ignoreSect
 * [62]  includeSect    ::=  '&lt;![' S? 'INCLUDE' S? '[' extSubsetDecl ']]&gt;'
 * [63]  ignoreSect     ::=  '&lt;![' S? 'IGNORE' S? '[' ignoreSectContents* ']]&gt;'
 * [64]  ignoreSectContents ::= Ignore ('&lt;![' ignoreSectContents ']]&gt;' Ignore)*
 * [65]  Ignore         ::=  Char* - (Char* ('&lt;![' | ']]&gt;') Char*)
 *
 * Character Reference
 * [66]  CharRef        ::=  '&amp;' '#' [0-9]+ ';'
 *                        |  '&amp;' '#' 'x' [0-9a-fA-F]+ ';'
 *
 * Entity Reference
 * [67]  Reference      ::=  EntityRef | CharRef
 * [68]  EntityRef      ::=  '&amp;' Name ';'
 * [69]  PEReference    ::=  '%' Name ';'
 *
 * Entity Declaration
 * [70]  EntityDecl     ::=  GEDecl | PEDecl
 * [71]  GEDecl         ::=  '&lt;!ENTITY' S Name S EntityDef S? '&gt;'
 * [72]  PEDecl         ::=  '&lt;!ENTITY' S '%' S Name S PEDef S? '&gt;'
 * [73]  EntityDef      ::=  EntityValue | (ExternalID NDataDecl?)
 * [74]  PEDef          ::=  EntityValue | ExternalID
 *
 * External Entity Declaration
 * [75]  ExternalID     ::=  'SYSTEM' S SystemLiteral
 *                        |  'PUBLIC' S PubidLiteral S SystemLiteral
 * [76]  NDataDecl      ::=  S 'NDATA' S Name
 *
 *
 * Text Declaration
 * [77]  TextDecl       ::=  '&lt;?xml' VersionInfo? EncodingDecl S? '?&gt;'
 *
 * Well-Formed External Parsed Entity
 * [78]  extParsedEnt   ::=  TextDecl? content
 *
 * Encoding Declaration
 * [80]  EncodingDecl   ::=  S 'encoding' Eq ('"' EncName '"' | "'" EncName "'" )
 * [81]  EncName        ::=  [A-Za-z] ([A-Za-z0-9._] | '-')*
 *
 * Notation Declarations
 * [82]  NotationDecl   ::=  '&lt;!NOTATION' S Name S (ExternalID | PublicID) S? '&gt;'
 * [83]  PublicID       ::=  'PUBLIC' S PubidLiteral
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>Extensible Markup Language (XML) 1.0 (Fifth Edition)</dt>
 *     <dd><a href="https://www.w3.org/TR/xml/">w3.org</a></dd>
 * </dl>
 * <dl>
 *     <dt>Canonical XML Version 1.1</dt>
 *     <dd><a href="https://www.w3.org/TR/xml-c14n11/">w3.org</a></dd>
 * </dl>
 * <dl>
 *     <dt>Extensible Markup Language (XML) 1.0 (Fifth Edition),
 *     2.10 White Space Handling, xml:space='preserve'</dt>
 *     <dd><a href="https://www.w3.org/TR/xml/#sec-white-space">w3.org</a></dd>
 * </dl>
 * <dl>
 *     <dt>Understanding xml:space</dt>
 *     <dd><a href="http://www.xmlplease.com/xml/xmlspace/">xmlplease.com</a></dd>
 * </dl>
 */
public class IndentingXMLStreamWriter implements XMLStreamWriter, AutoCloseable {
    public static final @NonNull String DEFAULT_NAMESPACE = "";
    public static final @NonNull String DEFAULT_PREFIX = "";
    public static final @NonNull String END_CHAR_REF = ";";
    public static final @NonNull String END_ENTITY_REF = ";";
    public static final @NonNull String END_PROCESSING_INSTRUCTION = "?>";
    public static final @NonNull String START_CHAR_REF = "&#x";
    public static final @NonNull String START_ENTITY_REF = "&";
    public static final @NonNull String START_PROCESSING_INSTRUCTION = "<?";
    private static final @NonNull String CLOSE_EMPTY_ELEMENT = "/>";
    private static final @NonNull String CLOSE_END_TAG = ">";
    private static final @NonNull String CLOSE_START_TAG = ">";
    private static final @NonNull String DEFAULT_XML_VERSION = "1.0";
    private static final @NonNull String END_ATTRIBUTE_VALUE = "\"";
    private static final @NonNull String END_CDATA = "]]>";
    private static final @NonNull String END_COMMENT = "-->";
    private static final @NonNull String END_ENCODING = "\"";
    private static final @NonNull String END_VERSION = "\"";
    private static final @NonNull String END_XML_DECLARATION = "?>";
    private static final @NonNull String OPEN_END_TAG = "</";
    private static final @NonNull String OPEN_START_TAG = "<";
    private static final @NonNull String PREFIX_SEPARATOR = ":";
    private static final @NonNull String SPACE = " ";
    private static final @NonNull String STANDALONE = " standalone=\"no\"";
    private static final @NonNull String START_ATTRIBUTE_VALUE = "=\"";
    private static final @NonNull String START_CDATA = "<![CDATA[";
    private static final @NonNull String START_COMMENT = "<!--";
    private static final @NonNull String START_ENCODING = " encoding=\"";
    private static final @NonNull String START_VERSION = " version=\"";
    private static final @NonNull String START_XML_DECLARATION = "<?xml";
    private static final @NonNull String XMLNS_NAMESPACE = "https://www.w3.org/TR/REC-xml-names/";
    private static final @NonNull String XMLNS_PREFIX = "xmlns";
    private static final @NonNull String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    /**
     * The local name of the special {@code xml:space} attribute.
     */
    private static final @NonNull String XML_SPACE_ATTRIBUTE = "space";
    /**
     * The value of the special {@code xml:space="preserve"} attribute.
     */
    private static final @NonNull String XML_SPACE_PRESERVE_VALUE = "preserve";
    private final @NonNull StringBuffer charBuffer = new StringBuffer();
    private final @NonNull CharsetEncoder encoder;
    /**
     * Invariant: this stack always contains at least the root element.
     */
    private final @NonNull Deque<Element> stack = new ArrayDeque<>();
    private final @NonNull Writer w;
    private Set<Attribute> attributes = new TreeSet<>(Comparator.comparing(Attribute::namespace).thenComparing(Attribute::localName));
    private boolean escapeClosingAngleBracket = true;
    private boolean escapeLineBreak = true;
    private boolean hasContent;
    private String indentation = "  ";
    private boolean isFirstWrite = true;
    private boolean isStartTagOpen = false;
    private String lineSeparator = "\n";

    public IndentingXMLStreamWriter(@NonNull Writer w) {
        this.w = w;
        this.encoder = StandardCharsets.UTF_8.newEncoder();
        stack.push(new Element("", "", "<root>", false));
    }

    public IndentingXMLStreamWriter(OutputStream out) {
        this(out, StandardCharsets.UTF_8);
    }

    public IndentingXMLStreamWriter(OutputStream out, Charset charset) {
        this.w = new BufferedWriter(new OutputStreamWriter(out, charset));
        this.encoder = charset.newEncoder();
        stack.push(new Element("", "", "<root>", false));
    }

    @Override
    public void close() throws XMLStreamException {
        try {
            w.flush();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private void closeStartTagOrCloseEmptyElemTag() throws XMLStreamException {
        charBuffer.setLength(0);
        if (isStartTagOpen) {
            isStartTagOpen = false;
            doWriteAttributes();
            Element peeked = stack.peek();
            if (peeked != null) {
                if (peeked.isEmpty()) {
                    write(CLOSE_EMPTY_ELEMENT);
                    stack.pop();
                } else {
                    write(CLOSE_START_TAG);
                }
            }
        }
    }

    private void doWriteAttribute(@NonNull Attribute attribute) throws XMLStreamException {
        write(" ");
        String prefix = attribute.prefix() == null ? getPrefixNonNull(attribute.namespace()) : attribute.prefix();
        if (!prefix.equals(DEFAULT_PREFIX)) {
            write(prefix);
            write(PREFIX_SEPARATOR);
        }
        write(attribute.localName());
        write(START_ATTRIBUTE_VALUE);
        writeXmlContent(attribute.value, true, false);
        write(END_ATTRIBUTE_VALUE);
    }

    private void doWriteAttributes() throws XMLStreamException {
        for (Attribute attribute : attributes) {
            doWriteAttribute(attribute);
        }
        attributes.clear();
    }

    @Override
    public void flush() throws XMLStreamException {
        try {
            w.flush();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public String getIndentation() {
        return indentation;
    }

    public void setIndentation(String indentation) {
        this.indentation = indentation;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return stack.getLast().namespaceContext;
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) {
        stack.getLast().namespaceContext = context;
    }

    private MyNamespaceContext getOrCreateNamespaceContext() {
        Element element = stack.getFirst();
        MyNamespaceContext ctx;
        if (element.namespaceContext instanceof MyNamespaceContext) {
            ctx = (MyNamespaceContext) element.namespaceContext;
        } else {
            ctx = new MyNamespaceContext();
            element.namespaceContext = ctx;
        }
        return ctx;
    }

    @Override
    public @Nullable String getPrefix(@NonNull String uri) {
        Objects.requireNonNull(uri, "uri");
        for (Element element : stack) {
            if (element.namespaceContext != null) {
                String prefix = element.namespaceContext.getPrefix(uri);
                if (prefix != null) {
                    return prefix;
                }
            }
        }
        return null;
    }

    private @NonNull String getPrefixNonNull(@NonNull String uri) {
        String prefix = getPrefix(uri);
        return prefix == null ? DEFAULT_PREFIX : prefix;
    }

    @Override
    public @Nullable Object getProperty(@NonNull String name) throws IllegalArgumentException {
        Objects.requireNonNull(name, "name");
        throw new IllegalArgumentException("unsupported property: " + name);
    }

    private boolean hasContent() {
        return hasContent;
    }

    private boolean isBlank(char[] text, int start, int length) {
        int left = 0;
        while (left < length) {
            char ch = text[start + left];
            if (!Character.isWhitespace(ch)) {
                break;
            }
            left++;
        }
        return left == length;
    }

    public boolean isEscapeClosingAngleBracket() {
        return escapeClosingAngleBracket;
    }

    /**
     * Whether to replace {@literal '<'} and {@literal '>} characters by
     * entity references.
     * <p>
     * These characters should always be replaced by entity references,
     * but some non-conforming parsers can not handle the entities.
     *
     * @param b true if less-than sign and greater-than sign shall be replaced
     */
    public void setEscapeClosingAngleBracket(boolean b) {
        escapeClosingAngleBracket = b;
    }

    public boolean isEscapeLineBreak() {
        return escapeLineBreak;
    }

    /**
     * Whether to replace the {@literal '\n'} character by an
     * entity references.
     * <p>
     * This character should always be replaced by an entity reference,
     * but some non-conforming parsers can not handle the entities.
     *
     * @param escapeLineBreak true if line break shall be replaced
     */
    public void setEscapeLineBreak(boolean escapeLineBreak) {
        this.escapeLineBreak = escapeLineBreak;
    }

    public boolean isPreserveSpace() {
        return stack.getFirst().isPreserveSpace();
    }

    public void setPreserveSpace(boolean preserveSpace) {
        stack.getFirst().setPreserveSpace(preserveSpace);
    }

    public boolean isSortAttributes() {
        return attributes instanceof SortedSet<Attribute>;
    }

    public void setSortAttributes(boolean b) {
        attributes = b ? new TreeSet<>(Comparator.comparing(Attribute::namespace).thenComparing(Attribute::localName))
                : new LinkedHashSet<>();
    }

    private void requireStartTagOpened() {
        if (!isStartTagOpen) {
            throw new IllegalStateException("There is currently no open start tag.");
        }
    }

    @Override
    public void setDefaultNamespace(String uri) {
        getOrCreateNamespaceContext().setNamespace(uri, DEFAULT_PREFIX);
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    @Override
    public void setPrefix(String prefix, String uri) {
        requireStartTagOpened();
        getOrCreateNamespaceContext().setNamespace(uri, prefix);
    }

    private void write(String str) throws XMLStreamException {
        try {
            isFirstWrite = false;
            w.write(str);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private void write(char ch) throws XMLStreamException {
        try {
            isFirstWrite = false;
            w.write(ch);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    public void writeAttribute(String localName, String value) {
        requireStartTagOpened();
        attributes.add(new Attribute(DEFAULT_PREFIX, DEFAULT_NAMESPACE, localName, value));
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        requireStartTagOpened();
        if (!attributes.add(new Attribute(prefix, namespaceURI, localName, value))) {
            throw new XMLStreamException("Attribute already added: " + localName);
        }
        if (XML_NAMESPACE.equals(namespaceURI) && XML_SPACE_ATTRIBUTE.equals(localName)) {
            setPreserveSpace(XML_SPACE_PRESERVE_VALUE.equals(value));
        }
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        requireStartTagOpened();
        if (!attributes.add(new Attribute(DEFAULT_PREFIX, namespaceURI, localName, value))) {
            throw new XMLStreamException("Attribute already added: " + localName);
        }
        if (XML_NAMESPACE.equals(namespaceURI) && XML_SPACE_ATTRIBUTE.equals(localName)) {
            setPreserveSpace(XML_SPACE_PRESERVE_VALUE.equals(value));
        }
    }

    @Override
    public void writeCData(@NonNull String data) throws XMLStreamException {
        Objects.requireNonNull(data, "data");
        if (data.contains(END_CDATA)) {
            throw new XMLStreamException("CData must not contain \"" + END_CDATA + "\", CData: " + data);
        }
        setHasContent(true);
        closeStartTagOrCloseEmptyElemTag();
        write(START_CDATA);
        write(data);
        write(END_CDATA);
    }

    /**
     * Writes character reference in upper case hex format.
     * <p>
     * We use upper case here, because Canonical XML uses upper case hex characters.
     */
    private void writeCharRef(int codePoint) throws XMLStreamException {
        write(START_CHAR_REF);
        write(Integer.toHexString(codePoint).toUpperCase());
        write(END_CHAR_REF);
    }

    @Override
    public void writeCharacters(@NonNull String text) throws XMLStreamException {
        Objects.requireNonNull(text, "text");
        closeStartTagOrCloseEmptyElemTag();
        if (!isPreserveSpace() && !hasContent() && text.trim().isEmpty()) {
            charBuffer.append(text);
            return;
        } else {
            setHasContent(true);
            if (!charBuffer.isEmpty()) {
                writeXmlContent(charBuffer.toString(), false, false);
                charBuffer.setLength(0);
            }
        }
        writeXmlContent(text, false, false);
    }

    @Override
    public void writeCharacters(char @NonNull [] text, int start, int len) throws XMLStreamException {
        Objects.requireNonNull(text, "text");
        Objects.checkFromIndexSize(start, len, text.length);
        closeStartTagOrCloseEmptyElemTag();

        if (!isPreserveSpace() && !hasContent() && isBlank(text, start, len)) {
            charBuffer.append(text, start, len);
            return;
        } else {
            setHasContent(true);
            if (!charBuffer.isEmpty()) {
                writeXmlContent(charBuffer.toString(), false, false);
                charBuffer.setLength(0);
            }
        }
        writeXmlContent(text, start, len, false, false);
    }

    @Override
    public void writeComment(@NonNull String data) throws XMLStreamException {
        Objects.requireNonNull(data, "data");

        // Write line breaks before comment
        for (int i = 0, n = charBuffer.length(); i < n; i++) {
            if (charBuffer.charAt(i) == '\n') {
                write('\n');
            }
        }
        charBuffer.setLength(0);

        closeStartTagOrCloseEmptyElemTag();
        stack.push(new Element(DEFAULT_PREFIX, DEFAULT_NAMESPACE, START_COMMENT, true));
        if (!hasContent && !isFirstWrite) {
            writeLineBreakAndIndentation();
        }
        write(START_COMMENT);
        writeXmlContent(data, false, true);
        write(END_COMMENT);
        setHasContent(false);
        stack.pop();
    }

    @Override
    public void writeDTD(@NonNull String dtd) throws XMLStreamException {
        Objects.requireNonNull(dtd, "dtd");
        closeStartTagOrCloseEmptyElemTag();
        write(dtd);
    }

    @Override
    public void writeDefaultNamespace(@Nullable String namespaceURI) {
        requireStartTagOpened();
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            setPrefix(DEFAULT_PREFIX, DEFAULT_NAMESPACE);
        } else {
            setPrefix(DEFAULT_PREFIX, namespaceURI);
            attributes.add(new Attribute(DEFAULT_PREFIX, XMLNS_NAMESPACE, XMLNS_PREFIX, namespaceURI));
        }
    }

    @Override
    public void writeEmptyElement(@NonNull String namespaceURI, @NonNull String localName) throws XMLStreamException {
        writeEmptyElement(getPrefixNonNull(namespaceURI), localName, namespaceURI);

    }

    @Override
    public void writeEmptyElement(@NonNull String prefix, @NonNull String localName, @NonNull String namespaceURI) throws XMLStreamException {
        closeStartTagOrCloseEmptyElemTag();
        Element e = new Element(prefix, namespaceURI, localName, true);
        stack.push(e);
        isStartTagOpen = true;
        setPrefix(prefix, namespaceURI);
        writeLineBreakAndIndentation();
        write(OPEN_START_TAG);
        if (!prefix.equals(DEFAULT_PREFIX)) {
            write(prefix);
            write(PREFIX_SEPARATOR);
        }
        write(localName);
    }

    @Override
    public void writeEmptyElement(@NonNull String localName) throws XMLStreamException {
        writeEmptyElement(DEFAULT_PREFIX, localName, DEFAULT_NAMESPACE);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        closeStartTagOrCloseEmptyElemTag();
        while (stack.size() > 1) {
            writeEndElement();
        }
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        if (stack.size() <= 1) {
            throw new XMLStreamException("no such element");
        }
        charBuffer.setLength(0);
        Element element = stack.pop();
        if (element.isEmpty()) {
            write(CLOSE_EMPTY_ELEMENT);
            element = stack.pop();
        }

        if (isStartTagOpen) {
            doWriteAttributes();
            write(CLOSE_EMPTY_ELEMENT);
            isStartTagOpen = false;
        } else {
            if (!hasContent) {
                writeEndElementLineBreakAndIndentation();
            }
            write(OPEN_END_TAG);
            String prefix = element.getPrefix();
            if (!prefix.isEmpty()) {
                write(prefix);
                write(PREFIX_SEPARATOR);
            }
            write(element.getLocalName());
            write(CLOSE_END_TAG);
        }
        hasContent = false;
    }

    private void writeEndElementLineBreakAndIndentation() throws XMLStreamException {
        write(lineSeparator);
        for (int i = stack.size() - 2; i >= 0; i--) {
            write(indentation);
        }
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        Objects.requireNonNull(name, "name");
        closeStartTagOrCloseEmptyElemTag();
        write(START_ENTITY_REF);
        write(name);
        write(END_ENTITY_REF);
    }

    private void writeLineBreakAndIndentation() throws XMLStreamException {
        write(lineSeparator);
        for (int i = stack.size() - 3; i >= 0; i--) {
            write(indentation);
        }
    }

    @Override
    public void writeNamespace(@NonNull String prefix, @NonNull String namespaceURI) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(namespaceURI, "namespaceURI");
        requireStartTagOpened();
        attributes.add(new Attribute(prefix.isEmpty() || XMLNS_PREFIX.equals(prefix) ? "" : XMLNS_PREFIX,
                XMLNS_NAMESPACE, prefix.isEmpty() ? XMLNS_PREFIX : prefix, namespaceURI));
    }

    @Override
    public void writeProcessingInstruction(@NonNull String target) throws XMLStreamException {
        Objects.requireNonNull(target, "target");
        closeStartTagOrCloseEmptyElemTag();
        write(START_PROCESSING_INSTRUCTION);
        write(target);
        write(END_PROCESSING_INSTRUCTION);
    }

    @Override
    public void writeProcessingInstruction(@NonNull String target, @NonNull String data) throws XMLStreamException {
        Objects.requireNonNull(target, "target");
        closeStartTagOrCloseEmptyElemTag();
        write(START_PROCESSING_INSTRUCTION);
        write(target);
        write(SPACE);
        write(data);
        write(END_PROCESSING_INSTRUCTION);

    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        writeStartDocument(encoder.charset().name(), DEFAULT_XML_VERSION);
    }

    @Override
    public void writeStartDocument(@NonNull String version) throws XMLStreamException {
        writeStartDocument(encoder.charset().name(), version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        write(START_XML_DECLARATION);
        write(START_VERSION);
        write(version);
        write(END_VERSION);
        write(START_ENCODING);
        write(encoding);
        write(END_ENCODING);
        write(STANDALONE);
        write(END_XML_DECLARATION);
    }

    @Override
    public void writeStartElement(@NonNull String localName) throws XMLStreamException {
        writeStartElement(DEFAULT_PREFIX, localName, DEFAULT_NAMESPACE);
    }

    @Override
    public void writeStartElement(@NonNull String namespaceURI, @NonNull String localName) throws XMLStreamException {
        writeStartElement(getPrefixNonNull(namespaceURI), localName, namespaceURI);
    }

    @Override
    public void writeStartElement(@NonNull String prefix, @NonNull String localName, @NonNull String namespaceURI) throws XMLStreamException {
        closeStartTagOrCloseEmptyElemTag();
        Element e = new Element(prefix, namespaceURI, localName, false);
        e.setPreserveSpace(stack.getFirst().isPreserveSpace());
        stack.push(e);
        isStartTagOpen = true;
        setPrefix(prefix, namespaceURI);
        if (!isPreserveSpace() && !hasContent) {
            writeLineBreakAndIndentation();
        }
        write(OPEN_START_TAG);
        if (!prefix.equals(DEFAULT_PREFIX)) {
            write(prefix);
            write(PREFIX_SEPARATOR);
        }
        write(localName);
        hasContent = false;
    }

    private void writeXmlContent(char[] data, int start, int len, boolean isDoubleQuoted, boolean isComment) throws XMLStreamException {
        writeXmlContent(new String(data, start, len), isDoubleQuoted, isComment);
    }

    private void writeXmlContent(String content, boolean isDoubleQuoted, boolean isComment) throws XMLStreamException {
        for (int index = 0, end = content.length(); index < end; index++) {
            char ch = content.charAt(index);

            if (!encoder.canEncode(ch)) {
                if (index != end - 1 && Character.isSurrogatePair(ch, content.charAt(index + 1))) {
                    writeCharRef(Character.toCodePoint(ch, content.charAt(index + 1)));
                    index++;
                } else {
                    writeCharRef(ch);
                }
                continue;
            }
            if (isDoubleQuoted &&
                ((Character.isWhitespace(ch) && ch != ' ')
                 || Character.isISOControl(ch)
                 || index != end - 1
                    && Character.isSurrogatePair(ch, content.charAt(index + 1))
                    && Character.isISOControl(Character.toCodePoint(ch, content.charAt(index + 1))))
            ) {
                if (escapeLineBreak || ch != '\n') {
                    writeCharRef(ch);
                    continue;
                }
            }

            switch (ch) {
                case '\r':
                    write("&xD;");// makes carriage return visible in the output!
                    break;

                case '\n':
                    if (isComment) {
                        writeLineBreakAndIndentation();
                        while (index < end - 1
                               && '\n' != content.charAt(index + 1)
                               && Character.isWhitespace(content.charAt(index + 1))) {
                            index++;
                        }
                    } else {
                        write('\n');
                    }
                    break;

                case '<':
                    if (isComment) {
                        write('<');
                    } else {
                        write("&lt;");
                    }
                    break;

                case '&':
                    if (isComment) {
                        write('&');
                    } else {
                        write("&amp;");
                    }
                    break;


                case '-':
                    // In a comment, we must escape double dashes
                    if (isComment && index < end - 1 && content.charAt(index + 1) == '-') {
                        index++;
                        writeCharRef('-');
                        writeCharRef('-');
                    } else {
                        write(ch);
                    }
                    break;
                case '>':
                    // Canonical XML replaces closing angle brackets only inside
                    // text nodes.
                    if (isComment || !escapeClosingAngleBracket || isDoubleQuoted) {
                        write('>');
                    } else {
                        write("&gt;");
                    }
                    break;
                case '"':
                    if (isDoubleQuoted) {
                        write("&quot;");
                    } else {
                        write(ch);
                    }
                    break;
                default:
                    write(ch);
                    break;
            }
        }
    }

    private static class MyNamespaceContext implements NamespaceContext {
        private final @NonNull Map<String, List<String>> nsToPrefix = new HashMap<>();
        private final @NonNull Map<String, String> prefixToNs = new HashMap<>();

        @Override
        public String getNamespaceURI(@NonNull String prefix) {
            return prefixToNs.get(prefix);
        }

        @Override
        public @NonNull String getPrefix(@NonNull String namespaceURI) {
            Objects.requireNonNull(namespaceURI, "namespaceURI");
            List<String> prefixes = nsToPrefix.get(namespaceURI);
            return prefixes == null || prefixes.isEmpty() ? XMLConstants.DEFAULT_NS_PREFIX : prefixes.getFirst();
        }

        @Override
        public Iterator<String> getPrefixes(@NonNull String namespaceURI) {
            Objects.requireNonNull(namespaceURI, "namespaceURI");
            List<String> prefixes = nsToPrefix.get(namespaceURI);
            return prefixes == null ? Collections.emptyIterator() : prefixes.iterator();
        }

        public void setNamespace(String uri, String prefix) {
            String oldNs = prefixToNs.put(prefix, uri);
            if (oldNs != null) {
                nsToPrefix.get(oldNs).removeIf(prefix::equals);
            }
            if (DEFAULT_PREFIX.equals(prefix)) {
                nsToPrefix.computeIfAbsent(uri, k -> new ArrayList<>()).addFirst(prefix);
            } else {
                nsToPrefix.computeIfAbsent(uri, k -> new ArrayList<>()).add(prefix);
            }
        }
    }

    private static class Element {
        private final boolean isEmpty;
        private final @NonNull String localName;
        private final @NonNull String namespaceUri;
        private final @NonNull String prefix;
        private NamespaceContext namespaceContext = new MyNamespaceContext();
        private boolean preserveSpace;

        public Element(@NonNull String prefix, @NonNull String namespaceUri, @NonNull String localName, boolean isEmpty) {
            this.prefix = prefix;
            this.namespaceUri = namespaceUri;
            this.localName = localName;
            this.isEmpty = isEmpty;
        }

        public @NonNull String getLocalName() {
            return localName;
        }

        public @NonNull String getPrefix() {
            return prefix;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public boolean isPreserveSpace() {
            return preserveSpace;
        }

        public void setPreserveSpace(boolean preserveSpace) {
            this.preserveSpace = preserveSpace;
        }
    }

    /**
     * In a valid XML there can only be one attribute with the same local name
     * in the same name space.
     */
    private record Attribute(@Nullable String prefix, @NonNull String namespace, @NonNull String localName,
                             @NonNull String value) {

    }
}
