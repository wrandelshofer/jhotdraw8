package org.jhotdraw8.os.macos;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

class MacOsSystemPreferencesTest {
    @Test
    public void shouldPrintAllSystemPreferences() {
        LinkedHashMap<String, Object> prefs = new LinkedHashMap<>();
        MacOSPreferencesUtil.readPreferences(MacOSPreferencesUtil.GLOBAL_PREFERENCES, prefs);
        Map<String, Object> flattened = MacOSPreferencesUtil.flatten(prefs);
        flattened.entrySet().forEach(System.out::println);
    }

    @Test
    public void shouldLoadMacOsSystemPreferences() {
        MacOsSystemPreferences prefs = new MacOsSystemPreferences();
        prefs.load();
    }
}