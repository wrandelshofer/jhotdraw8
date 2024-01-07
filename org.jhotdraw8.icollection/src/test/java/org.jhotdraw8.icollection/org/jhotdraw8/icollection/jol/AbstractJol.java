package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Run the java command with the following option to prevent that the JVM hangs:
 * <pre>
 * -ea  -Djdk.attach.allowAttachSelf -XX:+EnableDynamicAgentLoading --add-modules jol.core,jdk.attach --add-reads org.jhotdraw8.icollection=jol.core
 * </pre>
 */
public class AbstractJol {
    private static final boolean PRINT = true;

    protected static Map<Key, Key> generateMap(int size, int mask) {
        Random rng = new Random(0);
        Map<Key, Key> map = new LinkedHashMap<>();
        Set<Integer> preventDuplicates = new HashSet<>();
        for (int i = 0; i < size; i++) {
            map.put(createKey(rng, preventDuplicates, mask), createKey(rng, preventDuplicates, mask));
        }
        return map;
    }

    protected static Set<Key> generateSet(int size, int mask) {
        Random rng = new Random(0);
        Set<Key> set = new LinkedHashSet<>();
        Set<Integer> preventDuplicates = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(createKey(rng, preventDuplicates, mask));
        }
        return set;
    }

    private static Key createKey(Random rng, Set<Integer> preventDuplicates, int mask) {
        int candidate = rng.nextInt();
        while (!preventDuplicates.add(candidate)) {
            candidate = rng.nextInt();
        }
        return new Key(candidate, mask);
    }

    protected static void estimateMemoryUsage(Object collection, Map.Entry<?, ?> head, int size) {
        if (PRINT) {
            System.out.println(VM.current().details());
            GraphLayout graphLayout = GraphLayout.parseInstance(collection);
            long totalSize = graphLayout.totalSize();
            long elementSize = GraphLayout.parseInstance(head.getKey()).totalSize()
                    + GraphLayout.parseInstance(head.getValue()).totalSize();
            long dataSize = elementSize * size;
            long dataStructureSize = totalSize - dataSize;
            System.out.println(collection.getClass() + " with " + size + " elements.");
            System.out.println("total size              : " + totalSize);
            System.out.println("element size            : " + elementSize);
            System.out.println("data size               : " + dataSize + " " + (100 * dataSize / totalSize) + "%");
            System.out.println("data structure size     : " + dataStructureSize + " " + (100 * dataStructureSize / totalSize) + "%");
            System.out.println("overhead per element    : " + (float) dataStructureSize / size + " bytes");

            System.out.println("----footprint---");
            System.out.println(graphLayout.toFootprint());
            if (size <= 10) {
                System.out.println("----printable---");
                System.out.println(graphLayout.toPrintable());
            }
        }
    }

    protected static void estimateMemoryUsage(Object collection, Key head, int size) {
        System.out.println(VM.current().details());
        GraphLayout graphLayout = GraphLayout.parseInstance(collection);
        long totalSize = graphLayout.totalSize();
        long elementSize = GraphLayout.parseInstance(head).totalSize();
        long dataSize = elementSize * size;
        long dataStructureSize = totalSize - dataSize;
        System.out.println(collection.getClass() + " with " + size + " elements.");
        System.out.println("total size              : " + totalSize);
        System.out.println("element size            : " + elementSize);
        System.out.println("data size               : " + dataSize + " " + (100 * dataSize / totalSize) + "%");
        System.out.println("data structure size     : " + dataStructureSize + " " + (100 * dataStructureSize / totalSize) + "%");
        System.out.println("overhead per element    : " + dataStructureSize / (float) size + " bytes");
        System.out.println("----footprint---");
        System.out.println(graphLayout.toFootprint());
        if (size <= 10) {
            System.out.println("----printable---");
            System.out.println(graphLayout.toPrintable());

        }
    }
}
