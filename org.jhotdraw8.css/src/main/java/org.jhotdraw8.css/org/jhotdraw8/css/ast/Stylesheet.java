/*
 * @(#)Stylesheet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jspecify.annotations.Nullable;

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
    private final ImmutableList<Rule> rules;
    private final ImmutableList<StyleRule> styleRules;

    public Stylesheet(@Nullable URI uri, List<Rule> rules) {
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
    public ReadOnlyList<StyleRule> getStyleRules() {
        return styleRules;
    }

    /**
     * Returns rules in the stylesheet.
     *
     * @return the rules
     */
    public ReadOnlyList<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
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
