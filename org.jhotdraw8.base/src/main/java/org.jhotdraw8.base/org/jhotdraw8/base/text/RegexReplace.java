/*
 * @(#)RegexReplace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.text;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Find - ReplaceAll regular expression.
 *
 */
public class RegexReplace {

    private final @Nullable String find;
    private final @Nullable String replace;
    private transient Pattern pattern;

    public RegexReplace() {
        this(null, null);
    }

    public RegexReplace(@Nullable String find, @Nullable String replace) {
        this.find = find;
        this.replace = replace;
    }

    public @Nullable String getFind() {
        return find;
    }

    public @Nullable String getReplace() {
        return replace;
    }

    @Override
    public String toString() {
        return "/" + escape(find) + "/" + escape(replace) + "/";
    }

    private String escape(@Nullable String str) {
        return str == null ? "" : str.replace("/", "\\/");
    }

    /**
     * Applies the regular expression to the string.
     *
     * @param str the string
     * @return the replaced string
     */
    public @Nullable String apply(@Nullable String str) {
        if (str == null) {
            return null;
        }
        if (find == null) {
            return replace == null ? str : replace;
        }
        if (pattern == null) {
            pattern = Pattern.compile(find);
        }

        Matcher m = pattern.matcher(str);
        try {
            return replace == null ? m.replaceAll("$0") : m.replaceAll(replace);
        } catch (IndexOutOfBoundsException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

            return str;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.find);
        hash = 53 * hash + Objects.hashCode(this.replace);
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegexReplace other = (RegexReplace) obj;
        if (!Objects.equals(this.find, other.find)) {
            return false;
        }
        return Objects.equals(this.replace, other.replace);
    }

}
