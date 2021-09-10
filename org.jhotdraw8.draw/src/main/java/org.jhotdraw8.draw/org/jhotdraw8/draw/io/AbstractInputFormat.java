package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ReadOnlyOptionsMap;
import org.jhotdraw8.collection.SimpleOptionsMap;

public abstract class AbstractInputFormat implements InputFormat {
    private @NonNull ReadOnlyOptionsMap options = new SimpleOptionsMap();

    @NonNull
    @Override
    public ReadOnlyOptionsMap getOptions() {
        return options;
    }

    @Override
    public void setOptions(@NonNull ReadOnlyOptionsMap options) {
        this.options = options;
    }
}
