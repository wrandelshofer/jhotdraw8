package org.jhotdraw8.collection.impl.champ;

public class BulkChangeEvent {
    public int inBoth;
    public boolean replaced;
    public int removed;

    public void reset() {
        inBoth = 0;
        replaced = false;
        removed = 0;
    }
}
