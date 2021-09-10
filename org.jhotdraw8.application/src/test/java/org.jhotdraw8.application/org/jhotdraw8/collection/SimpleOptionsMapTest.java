package org.jhotdraw8.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleOptionsMapTest {
    @Test
    public void testReadWrite() {
        SimpleOptionsMap instance = new SimpleOptionsMap();
        instance.put("textWithSpaces", "Hello World  ");
        instance.put("textWithQuotes", "Hello \"World\"");
        instance.put("textWithNewline", "Hello\n World");
        instance.putBoolean("booleanTrue", true);
        instance.putBoolean("booleanFalse", false);
        instance.putByteArray("byteArrayCafeBabe", new byte[]{(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe});
        instance.putDouble("doubleValue", 42.0);
        instance.putFloat("floatValue", -42.0f);
        instance.putInt("intValue", 420);
        instance.putLong("longValue", 4200L);

        String str = instance.writeToString();
        System.out.println(str);

        SimpleOptionsMap actual = new SimpleOptionsMap();
        boolean success = actual.readFromString(str);
        assertTrue(success);
        assertEquals(instance, actual);

    }
}