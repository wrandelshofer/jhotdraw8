/*
 * @(#)FXSvgTinyWriterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package io;

import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jhotdraw8.svg.io.FXSvgTinyWriter;
import org.jhotdraw8.xml.XmlUtil;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class FXSvgTinyWriterTest {


    @TestFactory
    public List<DynamicTest> dynamicTestsExportToWriter() {
        return Arrays.asList(
                dynamicTest("rect", () -> testExportToWriter(new Rectangle(10, 20, 100, 200),
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <svg baseProfile="tiny" version="1.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
                                  <rect fill="#000000" height="200" width="100" x="10" y="20"/>
                                </svg>""")),
                dynamicTest("text", () -> {
                    Text text = new Text(10, 20, "Hello");
                    text.setFont(Font.font("System", 13));
                    testExportToWriter(text,
                            """
                                    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                    <svg baseProfile="tiny" version="1.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
                                      <text fill="#000000" font-family="'System Regular', 'System'" font-size="13" x="10" y="20">Hello</text>
                                    </svg>""");
                }),
                dynamicTest("text escape", () -> {
                    Text text = new Text(10, 20, "&<>\"");
                    text.setFont(Font.font("System", 13));
                    testExportToWriter(text,
                            """
                                    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                    <svg baseProfile="tiny" version="1.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
                                      <text fill="#000000" font-family="'System Regular', 'System'" font-size="13" x="10" y="20">&amp;&lt;&gt;"</text>
                                    </svg>""");
                })
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsExportToDOM() {
        return Arrays.asList(
                dynamicTest("rect", () -> testExportToDOM(new Rectangle(10, 20, 100, 200),
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                <svg baseProfile="tiny" version="1.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
                                  <rect fill="#000000" height="200" width="100" x="10" y="20"/>
                                </svg>""")),
                dynamicTest("text", () -> {
                    Text text = new Text(10, 20, "Hello");
                    text.setFont(Font.font("System", 13));
                    testExportToDOM(text,
                            """
                                    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                    <svg baseProfile="tiny" version="1.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
                                      <text fill="#000000" font-family="'System Regular', 'System'" font-size="13" x="10" y="20">Hello</text>
                                    </svg>""");
                }),
                dynamicTest("text escape", () -> {
                    Text text = new Text(10, 20, "&<>\"");
                    text.setFont(Font.font("System", 13));
                    testExportToWriter(text,
                            """
                                    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                    <svg baseProfile="tiny" version="1.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
                                      <text fill="#000000" font-family="'System Regular', 'System'" font-size="13" x="10" y="20">&amp;&lt;&gt;"</text>
                                    </svg>""");
                })
        );
    }


    private void testExportToDOM(Node node, String expected) throws IOException {
        FXSvgTinyWriter instance = new FXSvgTinyWriter(null, null);
        Document document = instance.toDocument(node, null, null);
        StringWriter w = new StringWriter();
        XmlUtil.write(w, document);
        String actual = w.toString();
        assertEquals(expected, actual);
    }

    private void testExportToWriter(Node node, String expected) throws IOException {
        StringWriter w = new StringWriter();
        FXSvgTinyWriter instance = new FXSvgTinyWriter(null, null);
        instance.write(w, node, null, null);
        String actual = w.toString();
        assertEquals(expected, actual);
    }
}