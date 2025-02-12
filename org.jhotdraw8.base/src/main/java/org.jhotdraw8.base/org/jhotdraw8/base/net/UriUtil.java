/*
 * @(#)UriUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.net;

import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UriUtil.
 *
 */
public class UriUtil {

    /**
     * Don't let anyone instantiate this class.
     */
    private UriUtil() {
    }


    /**
     * Returns the name of an URI for display in the title bar of a window.
     * <p>
     * If the URI is a file URI, then displays the file name followed
     * by the path to the file. If the file is inside the user home
     * directory, only the relative path is shown.
     * <p>
     * Examples:
     * <pre>
     *     uri = /User/me/Desktop/Hello.txt
     *     ⊢ title = Hello.txt [~/Desktop]
     *
     *     uri = /User/notme/Desktop/Hello.txt
     *     ⊢ title = Hello.txt [/User/notme/Desktop]
     *
     *     uri = /Volumes/NetworkDrive/Hello.txt
     *     ⊢ title = Hello.txt [/Volumes/NetworkDrive]
     * </pre>
     *
     * @param uri the uri
     * @return the name
     */
    public static String getName(URI uri) {
        if (uri.getScheme() != null && "file".equals(uri.getScheme())) {
            Path file = Paths.get(clearQuery(uri));
            String userHome = System.getProperty("user.home");
            boolean isInsideHome = false;
            if (userHome != null) {
                Path userHomePath = Paths.get(userHome);
                if (file.startsWith(userHomePath)) {
                    file = userHomePath.relativize(file);
                    isInsideHome = true;
                }
            }
            return file.getFileName() + " ["
                    + (isInsideHome ? "~" + File.separatorChar : "")
                    + file.getParent() + "]";
        }
        return uri.toString();
    }

    /**
     * Adds a query. If a query is already present, adds it after a {@literal '&'}
     * character. Both, the key, and the value may not include the characters
     * {@literal '&'} and '='.
     *
     * @param uri   an uri
     * @param key   the key
     * @param value the value
     * @return the updated query
     */
    public static URI addQuery(URI uri, @Nullable String key, @Nullable String value) {
        if (key == null || value == null) {
            return uri;
        }
        if (key.indexOf('=') != -1 || key.indexOf('&') != -1) {
            throw new IllegalArgumentException("Can not add a key that contains the characters '&' or '='. key=\"" + key + "\".");
        }
        if (value.indexOf('=') != -1 || value.indexOf('&') != -1) {
            throw new IllegalArgumentException("Can not add a value that contains the characters '&' or '='. value=\"" + value + "\".");
        }

        return addQuery(uri, key + '=' + value);
    }

    /**
     * Adds a query. If a query is already present, adds it after a {@literal '&'}
     * character. The query may not include the characters {@literal '&'} or {@literal '='}.
     *
     * @param uri   an uri
     * @param query the query
     * @return the updated query
     */
    public static URI addQuery(URI uri, @Nullable String query) {
        if (query == null) {
            return uri;
        }
        if (query.indexOf('&') != -1 || query.indexOf('=') != -1) {
            throw new IllegalArgumentException("Can not add a query that contains the characters '&' or '='. query=\"" + query + "\".");
        }

        String oldQuery = uri.getQuery();
        String newQuery = oldQuery == null ? query : oldQuery + "&" + query;

        return setQuery(uri, newQuery);
    }

    /**
     * Sets the query on the specified URI. If a query is already present, it is
     * removed.
     *
     * @param uri   an uri
     * @param query the query
     * @return the update uri
     */
    public static URI setQuery(URI uri, String query) {
        URI u = uri;
        try {
            u = new URI(u.getScheme(),
                    u.getUserInfo(), u.getHost(), u.getPort(),
                    u.getPath(), query,
                    u.getFragment());
        } catch (URISyntaxException ex) {
            Logger.getLogger(UriUtil.class.getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

        }
        return u;
    }

    public static URI clearQuery(URI uri) {

        return setQuery(uri, null);
    }

    /**
     * Parses the query of the URI. Assumes that the query consists of
     * {@literal '&'}-separated, key '=' value pairs.
     *
     * @param uri an URI
     * @return a map
     */
    public static Map<String, String> parseQuery(URI uri) {
        String query = uri.getQuery();
        SequencedMap<String, String> map = new LinkedHashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                int p = pair.indexOf('=');
                String key = pair.substring(0, p);
                String value = pair.substring(p + 1);
                map.put(key, value);
            }
        }
        return map;
    }

    public static URI relativize(@Nullable URI base, URI uri) {
        URI relativized = uri;
        // Paths is better at relativizing URIs than URI.relativize().
        if (base == null) {
            return uri;
        }

        if ("file".equals(base.getScheme()) &&
                ("file".equals(relativized.getScheme()) || relativized.getScheme() == null)) {
            Path other = Paths.get(relativized);
            Path relativizedPath;
            if (other.isAbsolute()) {
                relativizedPath = Paths.get(base).relativize(other);
            } else {
                relativizedPath = other;
            }
            if (relativizedPath.isAbsolute()) {
                relativized = relativizedPath.toUri();
            } else {
                try {
                    relativized = new URI(null, null, relativizedPath.toString()
                            , null, null);
                } catch (URISyntaxException e) {
                    relativized = base;// we tried hard, but we failed
                }
            }
        } else {
            relativized = base.relativize(relativized);
        }
        return relativized;
    }

    /**
     * Absolutizes an URI.
     *
     * @param base the base URI
     * @param uri  an URI that is relative to the base URI
     * @return the absolutized URI
     */
    public static URI absolutize(@Nullable URI base, URI uri) {
        if (base == null) {
            return uri;
        }
        URI absolutized = uri;
        // Paths is better at resolving URIs than URI.resolve().
        if ("file".equals(base.getScheme()) &&
                ("file".equals(absolutized.getScheme()) || absolutized.getScheme() == null)) {

            String pathStr = absolutized.getPath();
            if (pathStr.startsWith("/") && pathStr.indexOf(':') == 2) {
                pathStr = pathStr.substring(1);
            }
            absolutized = Paths.get(base).resolve(Paths.get(pathStr)).normalize().toUri();
        } else if ("jar".equals(base.getScheme()) && null == uri.getScheme()) {
            final String baseStr = base.toString();
            final String uriStr = uri.toString();
            try {
                return new URI(baseStr + "/" + uriStr);
            } catch (URISyntaxException e) {
                return uri;
            }
        } else {
            absolutized = base.resolve(absolutized);
        }

        return absolutized;
    }

    public static URI getParent(URI uri) {
        if ("jar".equals(uri.getScheme())) {
            try {
                final String str = uri.toString();
                return new URI(str.substring(0, str.lastIndexOf('/')));
            } catch (final URISyntaxException e) {
                return uri;
            }
        } else {
            return uri.resolve(".");
        }
    }
}
