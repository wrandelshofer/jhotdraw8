package org.jhotdraw8.collection;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A simple implementation of {@link ReadOnlyOptionsMap}.
 */
public class SimpleOptionsMap extends HashMap<String, String> implements OptionsMap {
    public SimpleOptionsMap() {
    }

    public SimpleOptionsMap(ReadOnlyOptionsMap m) {
        for (Entry<String, String> e : readOnlyEntrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Iterator<Entry<String, String>> entries() {
        return entrySet().iterator();
    }

    @Override
    public Iterator<String> keys() {
        return keySet().iterator();
    }
}
