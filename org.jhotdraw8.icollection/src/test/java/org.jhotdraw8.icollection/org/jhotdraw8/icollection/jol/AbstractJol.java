package org.jhotdraw8.icollection.jol;

import org.jhotdraw8.icollection.jmh.Key;
import org.jhotdraw8.icollection.jmh.Value;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;

/**
 * Run the java command with the following option to prevent that the JVM hangs:
 * <pre>
 * {@value #REQUIRED_VM_OPTIONS}
 * </pre>
 */
public class AbstractJol {
    private final static String REQUIRED_VM_OPTIONS = "-Djdk.attach.allowAttachSelf -XX:+EnableDynamicAgentLoading --add-modules jol.core,jdk.attach --add-reads org.jhotdraw8.icollection=jol.core";

    private static final boolean PRINT = true;

    protected static Map<Key, Value> generateMap(int size, int mask, long bound) {
        Random rng = new Random(0);
        SequencedMap<Key, Value> map = new LinkedHashMap<>();
        Set<Integer> preventDuplicates = new HashSet<>();
        for (int i = 0; i < size; i++) {
            map.put(createKey(rng, preventDuplicates, mask, bound),
                    createValue(rng, preventDuplicates, mask, bound));
        }
        return map;
    }

    protected static Set<Key> generateSet(int size, int mask) {
        Random rng = new Random(0);
        SequencedSet<Key> set = new LinkedHashSet<>();
        Set<Integer> preventDuplicates = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(createKey(rng, preventDuplicates, mask, Integer.MAX_VALUE));
        }
        return set;
    }

    private static Key createKey(Random rng, Set<Integer> preventDuplicates, int mask, long bound) {
        int candidate = createInt(rng, bound);
        while (!preventDuplicates.add(candidate)) {
            candidate = createInt(rng, bound);
        }
        return new Key(candidate, mask);
    }

    private static int createInt(Random rng, long bound) {
        return bound <= 0 || bound > Integer.MAX_VALUE ? rng.nextInt() : rng.nextInt((int) bound);
    }

    private static Value createValue(Random rng, Set<Integer> preventDuplicates, int mask, long bound) {
        int candidate = createInt(rng, bound);
        while (!preventDuplicates.add(candidate)) {
            candidate = createInt(rng, bound);
        }
        return new Value(candidate, mask);
    }

    protected static void estimateMemoryUsage(Object collection, Map.Entry<?, ?> head, int size) {
        if (!System.getProperties().containsKey("jdk.attach.allowAttachSelf")) {
            throw new RuntimeException("VM must be started with the following options: " + REQUIRED_VM_OPTIONS);
        }
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
        if (!System.getProperties().containsKey("jdk.attach.allowAttachSelf")) {
            throw new RuntimeException("VM must be started with the following options: " + REQUIRED_VM_OPTIONS);
        }
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
