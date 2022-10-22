/*
 * @(#)MacOSPreferencesTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.os.macos;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.MapEntries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.xml.datatype.DatatypeFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacOSPreferencesTest {
    @NonNull
    @TestFactory
    public List<DynamicTest> dynamicTestsPreferences() {
        List<DynamicTest> list = new ArrayList<>();
        for (String file : Arrays.asList("SmallXmlPropertyList.plist",
                "SmallBinaryPropertyList.plist")) {
            list.addAll(Arrays.asList(
                    DynamicTest.dynamicTest("nonexistent key", () -> testPreferences(file, "key", null)),
                    DynamicTest.dynamicTest("array", () -> testPreferences(file, "a array", Arrays.asList("the item 0 value", "the item 1 value"))),
                    DynamicTest.dynamicTest("dict", () -> testPreferences(file, "a dict", MapEntries.linkedHashMap(MapEntries.of("a child 1", "the child 1 value", "a child 2", "the child 2 value")))),
                    DynamicTest.dynamicTest("sub-dict access", () -> testPreferences(file, "a dict\ta child 2", "the child 2 value")),

                    DynamicTest.dynamicTest("boolean false", () -> testPreferences(file, "a boolean false", false)),
                    DynamicTest.dynamicTest("boolean true", () -> testPreferences(file, "a boolean true", true)),
                    DynamicTest.dynamicTest("data", () -> testPreferences(file, "a data", new byte[]{(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe})),
                    DynamicTest.dynamicTest("date", () -> testPreferences(file, "a date", DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-11-09T11:39:03Z"))),
                    DynamicTest.dynamicTest("float", () -> testPreferences(file, "a float", 0.42)),
                    DynamicTest.dynamicTest("integer", () -> testPreferences(file, "a integer", 42L)),
                    DynamicTest.dynamicTest("long", () -> testPreferences(file, "a long", 4294967296L)),
                    DynamicTest.dynamicTest("string", () -> testPreferences(file, "a string", "The String Value"))
            ));
        }
        return list;

    }

    private void testPreferences(String filename, @NonNull String key, Object expectedValue) throws URISyntaxException {
        URL resource = getClass().getResource(filename);
        if (resource == null) {
            throw new IllegalArgumentException("Could not find resource with filename=\"" + filename + "\" for class=" + getClass() + ".");
        }
        File file = new File(resource.toURI());
        // System.out.println(filename + ", " + key.replaceAll("\t", "→") + " = " + expectedValue);
        final Object actualValue = MacOSPreferencesUtil.get(file, key);
        if (expectedValue instanceof byte[]) {
            Assertions.assertArrayEquals((byte[]) expectedValue, (byte[]) actualValue, "key=" + key);
        } else {
            Assertions.assertEquals(expectedValue, actualValue, "key=" + key);
        }
    }
}