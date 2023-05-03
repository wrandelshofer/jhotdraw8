/*
 * @(#)Stylesheet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.readonly.ReadOnlyList;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A stylesheet is a list of rules.
 * <p>
 * A stylesheet is identified by a {@link URI}.
 *
 * @author Werner Randelshofer
 */
public class Stylesheet extends AbstractSyntaxTree {


    private final @Nullable URI uri;
    private final @NonNull ImmutableList<Rule> rules;
    private final @NonNull ImmutableList<StyleRule> styleRules;

    public Stylesheet(@Nullable URI uri, @NonNull List<Rule> rules) {
        super(new SourceLocator(0, 0, uri));
        this.uri = uri;
        this.rules = VectorList.copyOf(rules);
        this.styleRules = VectorList.copyOf(
                rules.stream()
                        .filter(r -> r instanceof StyleRule)
                        .map(r -> (StyleRule) r)
                        .collect(Collectors.toList()));
    }

    /**
     * Gets the URI that identifies this stylesheet.
     *
     * @return an URI
     */
    public @Nullable URI getUri() {
        return uri;
    }

    /**
     * Returns only the style rules in the stylesheet.
     *
     * @return the rules
     */
    public @NonNull ReadOnlyList<StyleRule> getStyleRules() {
        return styleRules;
    }

    /**
     * Returns rules in the stylesheet.
     *
     * @return the rules
     */
    public @NonNull ReadOnlyList<Rule> getRules() {
        return rules;
    }

    @Override
    public @NonNull String toString() {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Rule r : rules) {
            if (first) {
                first = false;
            } else {
                buf.append('\n');
            }
            buf.append(r.toString());
        }
        return buf.toString();
    }
}
