/*
 * @(#)NodeReaderRegistry.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.spi;

import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class NodeReaderRegistry {
    private NodeReaderRegistry() {
    }

    public static List<NodeReader> getNodeReaders(URL url) {
        List<NodeReader> list = new ArrayList<>();
        for (NodeReaderProvider spi : ServiceLoader.load(NodeReaderProvider.class)) {
            if (spi.canDecodeInput(url)) {
                list.add(spi.createReader());
            }
        }
        return list;
    }

    public static List<NodeReader> getNodeReaders(String path) {
        List<NodeReader> list = new ArrayList<>();
        for (NodeReaderProvider spi : ServiceLoader.load(NodeReaderProvider.class)) {
            if (spi.canDecodeInput(path)) {
                list.add(spi.createReader());
            }
        }
        return list;
    }

    public static @Nullable NodeReader getNodeReader(URL url) {
        List<NodeReader> list = getNodeReaders(url);
        return list.isEmpty() ? null : list.getFirst();
    }

    public static @Nullable NodeReader getNodeReader(String path) {
        List<NodeReader> list = getNodeReaders(path);
        return list.isEmpty() ? null : list.getFirst();
    }
}
