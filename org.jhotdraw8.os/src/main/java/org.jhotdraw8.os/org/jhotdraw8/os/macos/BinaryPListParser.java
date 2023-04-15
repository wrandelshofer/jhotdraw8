/*
 * @(#)BinaryPListParser.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.os.macos;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Reads a binary PList file and returns it as a DOM.
 * <p>
 * The DOM returned by this reader is equivalent to the
 * DOM returned, if a PList file in XML format is parsed with
 * a standard {@link javax.xml.parsers.DocumentBuilder}.
 * <p>
 * Description about property list taken from <a href="http://developer.apple.com/documentation/Cocoa/Conceptual/PropertyLists/index.html#//apple_ref/doc/uid/10000048i">
 * Apple's online documentation</a>:
 * <p>
 * "A property list is a data representation used by Mac OS X Cocoa and Core
 * Foundation as a convenient way to store, organize, and access standard object
 * types. Frequently called a plist, a property list is an object of one of
 * several certain Cocoa or Core Foundation types, including  arrays,
 * dictionaries, strings, binary data, numbers, dates, and Boolean values. If
 * the object is a container (an array or dictionary), all objects contained
 * within it must also be supported property list objects. (Arrays and
 * dictionaries can contain objects not supported by the architecture, but are
 * then not property lists, and cannot be saved and restored with the various
 * property list methods.)"
 *
 * @author Werner Randelshofer
 */
public class BinaryPListParser {

    private static final boolean DEBUG = false;
    /**
     * Time interval based dates are measured in seconds from 2001-01-01.
     */
    private static final long TIMER_INTERVAL_TIMEBASE = new GregorianCalendar(2001,
            Calendar.JANUARY, 1, 1, 0, 0).getTimeInMillis();
    /**
     * Factory for generating XML data types.
     */
    private static DatatypeFactory datatypeFactory;

    /* Description of the binary plist format derived from
     * http://opensource.apple.com/source/CF/CF-635/CFBinaryPList.c
     *
     * EBNF description of the file format:
     * <pre>
     * bplist ::= header objectTable offsetTable trailer
     *
     * header ::= magicNumber fileFormatVersion
     * magicNumber ::= "bplist"
     * fileFormatVersion ::= "00"
     *
     * objectTable ::= { null | bool | fill | number | date | data |
     *                 string | uid | array | dict }
     *
     * null  ::= 0b0000 0b0000
     *
     * bool  ::= false | true
     * false ::= 0b0000 0b1000
     * true  ::= 0b0000 0b1001
     *
     * fill  ::= 0b0000 0b1111         // fill byte
     *
     * number ::= int | real
     * int    ::= 0b0001 0bnnnn byte*(2^nnnn)  // 2^nnnn big-endian bytes
     * real   ::= 0b0010 0bnnnn byte*(2^nnnn)  // 2^nnnn big-endian bytes
     *
     * unknown::= 0b0011 0b0000 byte*8       // 8 byte float big-endian bytes ?
     *
     * date   ::= 0b0011 0b0011 byte*8       // 8 byte float big-endian bytes
     *
     * data   ::= 0b0100 0bnnnn [int] byte*  // nnnn is number of bytes
     *                                       // unless 0b1111 then a int
     *                                       // variable-sized object follows
     *                                       // to indicate the number of bytes
     *
     * string ::= asciiString | unicodeString
     * asciiString   ::= 0b0101 0bnnnn [int] byte*
     * unicodeString ::= 0b0110 0bnnnn [int] short*
     *                                       // nnnn is number of bytes
     *                                       // unless 0b1111 then a int
     *                                       // variable-sized object follows
     *                                       // to indicate the number of bytes
     *
     * uid ::= 0b1000 0bnnnn byte*           // nnnn+1 is # of bytes
     *
     * array ::= 0b1010 0bnnnn [int] objref* //
     *                                       // nnnn is number of objref
     *                                       // unless 0b1111 then a int
     *                                       // variable-sized object follows
     *                                       // to indicate the number of objref
     *
     * dict ::= 0b1010 0bnnnn [int] keyref* objref*
     *                                       // nnnn is number of keyref and
     *                                       // objref pairs
     *                                       // unless 0b1111 then a int
     *                                       // variable-sized object follows
     *                                       // to indicate the number of pairs
     *
     * objref = byte | short                 // if refCount
     *                                       // is less than 256 then objref is
     *                                       // an unsigned byte, otherwise it
     *                                       // is an unsigned big-endian short
     *
     * keyref = byte | short                 // if refCount
     *                                       // is less than 256 then objref is
     *                                       // an unsigned byte, otherwise it
     *                                       // is an unsigned big-endian short
     *
     * unused ::= 0b0111 0bxxxx | 0b1001 0bxxxx |
     *            0b1011 0bxxxx | 0b1100 0bxxxx |
     *            0b1110 0bxxxx | 0b1111 0bxxxx
     *
     *
     * offsetTable ::= { int }               // List of ints, byte size of which
     *                                       // is given in trailer
     *                                       // These are the byte offsets into
     *                                       // the file.
     *                                       // The number of the ffsets is given
     *                                       // in the trailer.
     *
     * trailer ::= refCount offsetCount objectCount topLevelOffset
     *
     * refCount ::= byte*8                  // unsigned big-endian long
     * offsetCount ::= byte*8               // unsigned big-endian long
     * objectCount ::= byte*8               // unsigned big-endian long
     * topLevelOffset ::= byte*8            // unsigned big-endian long
     * </pre>
     */
    /**
     * Total count of objrefs and keyrefs.
     */
    private int refCount;
    /**
     * Total count of ofsets.
     */
    private int offsetCount;
    /**
     * Total count of objects.
     */
    private int objectCount;
    /**
     * Offset in file of top level offset in offset table.
     */
    private int topLevelOffset;
    /**
     * Object table.
     * We gradually fill in objects from the binary PList object table into
     * this list.
     */
    private ArrayList<Object> objectTable;

    /**
     * Holder for a binary PList Uid element.
     */
    private static class BPLUid {

        private final int number;

        public BPLUid(int number) {
            super();
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }

    /**
     * Holder for a binary PList array element.
     */
    private static class BPLArray {

        ArrayList<Object> objectTable;
        int[] objref;

        public Object getValue(int i) {
            return objectTable.get(objref[i]);
        }

        @Override
        public @NonNull String toString() {
            StringBuilder buf = new StringBuilder("Array{");
            for (int i = 0; i < objref.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                if (objectTable.size() > objref[i]
                        && objectTable.get(objref[i]) != this) {
                    buf.append(objectTable.get(objref[i]));
                } else {
                    buf.append("*").append(objref[i]);
                }
            }
            buf.append('}');
            return buf.toString();
        }
    }

    /**
     * Holder for a binary PList dict element.
     */
    private static class BPLDict {

        ArrayList<Object> objectTable;
        int[] keyref;
        int[] objref;

        public String getKey(int i) {
            return objectTable.get(keyref[i]).toString();
        }

        public Object getValue(int i) {
            return objectTable.get(objref[i]);
        }

        @Override
        public @NonNull String toString() {
            StringBuilder buf = new StringBuilder("BPLDict{");
            for (int i = 0; i < keyref.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                if (keyref[i] < 0 || keyref[i] >= objectTable.size()) {
                    buf.append("#").append(keyref[i]);
                } else if (objectTable.get(keyref[i]) == this) {
                    buf.append("*").append(keyref[i]);
                } else {
                    buf.append(objectTable.get(keyref[i]));
                    //buf.append(keyref[i]);
                }
                buf.append(":");
                if (objref[i] < 0 || objref[i] >= objectTable.size()) {
                    buf.append("#").append(objref[i]);
                } else if (objectTable.get(objref[i]) == this) {
                    buf.append("*").append(objref[i]);
                } else {
                    buf.append(objectTable.get(objref[i]));
                    //buf.append(objref[i]);
                }
            }
            buf.append('}');
            return buf.toString();
        }
    }

    /**
     * Creates a new instance.
     */
    public BinaryPListParser() {
    }

    /**
     * Parses a binary PList file and turns it into a DOM.
     * The Document is equivalent with a XML PList file parsed using
     * a standard {@link javax.xml.parsers.DocumentBuilder}.
     *
     * @param file A file containing a binary PList.
     * @return Returns the parsed Element.
     */
    public Document parse(@NonNull File file) throws IOException {
        long fileLength;
        byte[] buf;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            fileLength = raf.length();

            // Parse the HEADER
            // ----------------
            //  magic number ("bplist")
            //  file format version ("00")
            int bpli = raf.readInt();
            int st00 = raf.readInt();
            if (bpli != 0x62706c69 || st00 != 0x73743030) {
                throw new IOException("parseHeader: File does not start with 'bplist00' magic.");
            }

            // Parse the TRAILER
            // ----------------
            //	byte size of offset ints in offset table
            //      byte size of object refs in arrays and dicts
            //      number of offsets in offset table (also is number of objects)
            //      element # in offset table which is top level object
            raf.seek(fileLength - 32);
            //	count of offset ints in offset table
            offsetCount = (int) raf.readLong();
            //  count of object refs in arrays and dicts
            refCount = (int) raf.readLong();
            //  count of offsets in offset table (also is number of objects)
            objectCount = (int) raf.readLong();
            //  element # in offset table which is top level object
            topLevelOffset = (int) raf.readLong();

            if (offsetCount < 0 || refCount < 0 || objectCount < 0 || topLevelOffset < 0) {
                throw new IOException("file is too large");
            }

            buf = new byte[topLevelOffset - 8];
            raf.seek(8);
            raf.readFully(buf);
        }

        // Parse the OBJECT TABLE
        // ----------------------
        objectTable = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(
                pos = new PosByteArrayInputStream(buf))) {
            parseObjectTable(in);
        }

        // Convert the object table to XML and return it
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Cannot create document builder", e);
        }
        // We do not want that the reader creates a socket connection!
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        Document doc = builder.newDocument();

        Element root = doc.createElement("plist");
        doc.appendChild(root);
        root.setAttribute("version", "1.0");
        convertObjectTableToXML(doc, root, objectTable.get(0), (int) fileLength);

        return doc;
    }

    private long getPosition() {
        return pos.getPos() + 8;
    }

    private @Nullable PosByteArrayInputStream pos;

    private static class PosByteArrayInputStream extends ByteArrayInputStream {

        public PosByteArrayInputStream(byte @NonNull [] buf) {
            super(buf);
        }

        public int getPos() {
            return pos;
        }

    }

    /**
     * Converts the object table in the binary PList into an Element.
     *
     * @param doc                the document to which this method adds elements
     * @param parent             the parent element
     * @param object             the current object table
     * @param remainingRecursion the remaining number of recursions that
     *                           this method may perform. Since every
     *                           element in the binary PList is at least
     *                           one byte long, there can be no more
     *                           recursions than the length of the file..
     * @throws IOException
     */
    private void convertObjectTableToXML(@NonNull Document doc, @NonNull Element parent, Object object, int remainingRecursion)
            throws IOException {
        if (remainingRecursion < 0) {
            throw new IOException("recursion limit reached");
        }
        Element elem;
        if (object instanceof BPLDict) {
            BPLDict dict = (BPLDict) object;
            elem = doc.createElement("dict");
            for (int i = 0; i < dict.keyref.length; i++) {
                Element key = doc.createElement("key");
                parent.appendChild(key);
                key.appendChild(doc.createTextNode(dict.getKey(i)));
                elem.appendChild(key);
                convertObjectTableToXML(doc, elem, dict.getValue(i), remainingRecursion - 1);
            }
        } else if (object instanceof BPLArray) {
            BPLArray arr = (BPLArray) object;
            elem = doc.createElement("array");
            for (int i = 0; i < arr.objref.length; i++) {
                convertObjectTableToXML(doc, elem, arr.getValue(i), remainingRecursion - 1);
            }

        } else if (object instanceof String) {
            elem = doc.createElement("string");
            elem.appendChild(doc.createTextNode((String) object));
        } else if (object instanceof Integer) {
            elem = doc.createElement("integer");
            elem.appendChild(doc.createTextNode(object.toString()));
        } else if (object instanceof Long) {
            elem = doc.createElement("integer");
            elem.appendChild(doc.createTextNode(object.toString()));
        } else if (object instanceof Float) {
            elem = doc.createElement("real");
            elem.appendChild(doc.createTextNode(object.toString()));
        } else if (object instanceof Double) {
            elem = doc.createElement("real");
            elem.appendChild(doc.createTextNode(object.toString()));
        } else if (object instanceof Boolean) {
            boolean b = (Boolean) object;
            elem = doc.createElement(b ? "true" : "false");
        } else if (object instanceof byte[]) {
            elem = doc.createElement("data");
            elem.appendChild(doc.createTextNode(Base64.getEncoder().encodeToString((byte[]) object)));
        } else if (object instanceof XMLGregorianCalendar) {
            elem = doc.createElement("date");
            elem.appendChild(doc.createTextNode(((XMLGregorianCalendar) object).toXMLFormat() + "Z"));
        } else if (object instanceof BPLUid) {
            elem = doc.createElement("UID");
            elem.appendChild(doc.createTextNode(Integer.toString(((BPLUid) object).getNumber())));
        } else {
            elem = doc.createElement("unsupported");
            elem.appendChild(doc.createTextNode(object.toString()));
        }

        parent.appendChild(elem);
    }

    /**
     * Object Formats (marker byte followed by additional info in some cases)
     * <pre>
     * null	0000 0000
     * bool	0000 1000			// false
     * bool	0000 1001			// true
     * fill	0000 1111			// fill byte
     * int	0001 nnnn	...		// # of bytes is 2^nnnn, big-endian bytes
     * real	0010 nnnn	...		// # of bytes is 2^nnnn, big-endian bytes
     * date	0011 0011	...		// 8 byte float follows, big-endian bytes
     * data	0100 nnnn	[int]	...	// nnnn is number of bytes unless 1111 then int count follows, followed by bytes
     * string	0101 nnnn	[int]	...	// ASCII string, nnnn is # of chars, else 1111 then int count, then bytes
     * string	0110 nnnn	[int]	...	// Unicode string, nnnn is # of chars, else 1111 then int count, then big-endian 2-byte shorts
     * 0111 xxxx			// unused
     * uid	1000 nnnn	...		// nnnn+1 is # of bytes
     * 1001 xxxx			// unused
     * array	1010 nnnn	[int]	objref*	// nnnn is count, unless '1111', then int count follows
     * 1011 xxxx			// unused
     * 1100 xxxx			// unused
     * dict	1101 nnnn	[int]	keyref* objref*	// nnnn is count, unless '1111', then int count follows
     * 1110 xxxx			// unused
     * 1111 xxxx			// unused
     * </pre>
     */
    private void parseObjectTable(@NonNull DataInputStream in) throws IOException {
        int marker;
        while ((marker = in.read()) != -1) {
            //System.err.println("parseObjectTable marker=" + Integer.toBinaryString(marker) + " 0x" + Integer.toHexString(marker) + " @0x" + Long.toHexString(getPosition()));
            switch ((marker & 0xf0) >> 4) {
            case 0: {
                parsePrimitive(in, marker & 0xf);
                break;
            }
            case 1: {
                int count = 1 << (marker & 0xf);
                parseInteger(in, count);
                break;
            }
            case 2: {
                int count = 1 << (marker & 0xf);
                parseReal(in, count);
                break;
            }
            case 3: {
                switch (marker & 0xf) {
                case 3:
                    parseDate(in);
                    break;
                default:
                    throw new IOException("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                }
                break;
            }
            case 4: {
                int count = marker & 0xf;
                if (count == 15) {
                    count = readCount(in);
                }
                parseData(in, count);
                break;
            }
            case 5: {
                int count = marker & 0xf;
                if (count == 15) {
                    count = readCount(in);
                }
                parseAsciiString(in, count);
                break;
            }
            case 6: {
                int count = marker & 0xf;
                if (count == 15) {
                    count = readCount(in);
                }
                parseUnicodeString(in, count);
                break;
            }
            case 7: {
                if (DEBUG) {
                    System.out.println("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                }
                return;
                // throw new IOException("parseObjectTable: illegal marker "+Integer.toBinaryString(marker));
                //break;
            }
            case 8: {
                int count = (marker & 0xf) + 1;
                if (DEBUG) {
                    System.out.println("uid " + count);
                }
                parseUID(in, count);
                break;
            }
            case 9: {
                throw new IOException("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                //break;
            }
            case 10: {
                int count = marker & 0xf;
                if (count == 15) {
                    count = readCount(in);
                }
                if (refCount > 255) {
                    parseShortArray(in, count);
                } else {
                    parseByteArray(in, count);
                }
                break;
            }
            case 11: {
                throw new IOException("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                //break;
            }
            case 12: {
                throw new IOException("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                //break;
            }
            case 13: {
                int count = marker & 0xf;
                if (count == 15) {
                    count = readCount(in);
                }
                if (refCount > 255) {
                    parseShortDict(in, count);
                } else {
                    parseByteDict(in, count);
                }
                break;
            }
            case 14: {
                throw new IOException("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                //break;
            }
            case 15: {
                throw new IOException("parseObjectTable: illegal marker " + Integer.toBinaryString(marker));
                //break;
            }
            }
        }
    }

    /**
     * Reads a count value from the object table. Count values are encoded
     * using the following scheme:
     * <p>
     * int	0001 nnnn   ...     // # of bytes is 2^nnnn, big-endian bytes
     */
    private int readCount(@NonNull DataInputStream in) throws IOException {
        int marker = in.read();
        if (marker == -1) {
            throw new IOException("variableLengthInt: Illegal EOF in marker");
        }
        if (((marker & 0xf0) >> 4) != 1) {
            throw new IOException("variableLengthInt: Illegal marker " + Integer.toBinaryString(marker));
        }
        int count = 1 << (marker & 0xf);
        int value = 0;
        for (int i = 0; i < count; i++) {
            int b = in.read();
            if (b == -1) {
                throw new IOException("variableLengthInt: Illegal EOF in value");
            }
            value = (value << 8) | b;
        }
        return value;
    }

    /**
     * null	0000 0000
     * bool	0000 1000			// false
     * bool	0000 1001			// true
     * fill	0000 1111			// fill byte
     */
    private void parsePrimitive(DataInputStream in, int primitive) throws IOException {
        switch (primitive) {
        case 0:
            objectTable.add(null);
            break;
        case 8:
            objectTable.add(Boolean.FALSE);
            break;
        case 9:
            objectTable.add(Boolean.TRUE);
            break;
        case 15:
            // fill byte: don't add to object table
            break;
        default:
            throw new IOException("parsePrimitive: illegal primitive " + Integer.toBinaryString(primitive));
        }
    }

    /**
     * array	1010 nnnn	[int]	objref*	// nnnn is count, unless '1111', then int count follows
     */
    private void parseByteArray(@NonNull DataInputStream in, int count) throws IOException {
        BPLArray arr = new BPLArray();
        arr.objectTable = objectTable;
        arr.objref = new int[count];

        for (int i = 0; i < count; i++) {
            arr.objref[i] = in.readByte() & 0xff;
            if (arr.objref[i] == -1) {
                throw new IOException("parseByteArray: illegal EOF in objref*");
            }
        }

        objectTable.add(arr);
    }

    /**
     * array	1010 nnnn	[int]	objref*	// nnnn is count, unless '1111', then int count follows
     */
    private void parseShortArray(@NonNull DataInputStream in, int count) throws IOException {
        BPLArray arr = new BPLArray();
        arr.objectTable = objectTable;
        arr.objref = new int[count];

        for (int i = 0; i < count; i++) {
            arr.objref[i] = in.readShort() & 0xffff;
            if (arr.objref[i] == -1) {
                throw new IOException("parseShortArray: illegal EOF in objref*");
            }
        }

        objectTable.add(arr);
    }
    /*
     * data	0100 nnnn	[int]	...	// nnnn is number of bytes unless 1111 then int count follows, followed by bytes
     */

    private void parseData(@NonNull DataInputStream in, int count) throws IOException {
        byte[] data = new byte[count];
        in.readFully(data);
        objectTable.add(data);
    }

    /**
     * byte dict	1101 nnnn keyref* objref*	// nnnn is less than '1111'
     */
    private void parseByteDict(@NonNull DataInputStream in, int count) throws IOException {
        BPLDict dict = new BPLDict();
        dict.objectTable = objectTable;
        dict.keyref = new int[count];
        dict.objref = new int[count];

        for (int i = 0; i < count; i++) {
            dict.keyref[i] = in.readByte() & 0xff;
        }
        for (int i = 0; i < count; i++) {
            dict.objref[i] = in.readByte() & 0xff;
        }
        objectTable.add(dict);
    }

    /**
     * <pre>
     * short dict	1101 ffff int keyref* objref*	// int is count
     * </pre>
     */
    private void parseShortDict(@NonNull DataInputStream in, int count) throws IOException {
        BPLDict dict = new BPLDict();
        dict.objectTable = objectTable;
        dict.keyref = new int[count];
        dict.objref = new int[count];

        for (int i = 0; i < count; i++) {
            dict.keyref[i] = in.readShort() & 0xffff;
        }
        for (int i = 0; i < count; i++) {
            dict.objref[i] = in.readShort() & 0xffff;
        }
        objectTable.add(dict);
    }

    /**
     * string	0101 nnnn	[int]	...	// ASCII string, nnnn is # of chars, else 1111 then int count, then bytes
     */
    private void parseAsciiString(@NonNull DataInputStream in, int count) throws IOException {
        byte[] buf = new byte[count];
        in.readFully(buf);
        String str = new String(buf, StandardCharsets.US_ASCII);
        objectTable.add(str);
    }

    private void parseUID(@NonNull DataInputStream in, int count) throws IOException {
        if (count > 4) {
            throw new IOException("parseUID: unsupported byte count: " + count);
        }
        byte[] uid = new byte[count];
        in.readFully(uid);
        objectTable.add(new BPLUid(new BigInteger(uid).intValue()));
    }

    /**
     * int	0001 nnnn	...		// # of bytes is 2^nnnn, big-endian bytes
     */
    private void parseInteger(@NonNull DataInputStream in, int count) throws IOException {
        byte[] bytes = new byte[count];
        in.readFully(bytes);
        BigInteger bigInteger = new BigInteger(bytes);
        if (bigInteger.bitLength() < 32) {
            objectTable.add(bigInteger.intValue());
        } else if (bigInteger.bitLength() < 64) {
            objectTable.add(bigInteger.longValue());
        } else {
            objectTable.add(bigInteger);
        }
    }

    /**
     * real	0010 nnnn	...		// # of bytes is 2^nnnn, big-endian bytes
     */
    private void parseReal(@NonNull DataInputStream in, int count) throws IOException {
        switch (count) {
        case 4:
            objectTable.add(in.readFloat());
            break;
        case 8:
            objectTable.add(in.readDouble());
            break;
        default:
            throw new IOException("parseReal: unsupported byte count:" + count);
        }
    }

    /**
     * unknown	0011 0000	...		// 8 byte float follows, big-endian bytes
     */
    private void parseUnknown(@NonNull DataInputStream in) throws IOException {
        in.skipBytes(1);
        objectTable.add("unknown");
    }

    /**
     * date	0011 0011	...		// 8 byte float follows, big-endian bytes
     */
    private void parseDate(@NonNull DataInputStream in) throws IOException {
        objectTable.add(fromTimerInterval(in.readDouble()));
    }

    /**
     * string	0110 nnnn	[int]	...	// Unicode string, nnnn is # of chars, else 1111 then int count, then big-endian 2-byte shorts
     */
    private void parseUnicodeString(@NonNull DataInputStream in, int count) throws IOException {
        char[] buf = new char[count];
        for (int i = 0; i < count; i++) {
            buf[i] = in.readChar();
        }
        String str = new String(buf);
        objectTable.add(str);
    }

    //

    /**
     * Timer interval based dates are measured in seconds from 1/1/2001.
     * Timer intervals have no time zone.
     */
    private static XMLGregorianCalendar fromTimerInterval(double timerInterval) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date(TIMER_INTERVAL_TIMEBASE + (long) timerInterval * 1000L));
        XMLGregorianCalendar xmlgc = getDatatypeFactory().newXMLGregorianCalendar(gc);
        xmlgc.setFractionalSecond(null);
        xmlgc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        return xmlgc;
    }

    /**
     * Gets the factory for XML data types.
     */
    private static DatatypeFactory getDatatypeFactory() {
        if (datatypeFactory == null) {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException ex) {
                throw new InternalError("Can't create XML datatype factory.", ex);
            }
        }
        return datatypeFactory;
    }
}
