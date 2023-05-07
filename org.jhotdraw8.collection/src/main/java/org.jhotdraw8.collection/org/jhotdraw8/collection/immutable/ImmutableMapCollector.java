package org.jhotdraw8.collection.immutable;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Recursively builds a larger set from smaller instances of the set.
 * <p>
 * Use this collector for sets that can perform {@link ImmutableMap#putAll(Iterable)}
 * faster than {@link ImmutableMap#put}.
 * <p>
 * Usage with {@link Stream}:
 * <pre>{@literal
 *     Stream<E> stream = ...
 *     ChampSet result = stream.collect(new ImmutableMapCollector<>(ChampSet.of());
 * }</pre>
 * Usage with enhanced for-loop:
 * <pre>{@literal
 *     var c = new ImmutableMapCollector<E,ChampSet<E>>(ChampSet.of();
 *     for (E e : ...) {
 *      c.accept(e);
 *     }
 *     ChampSet result = c.build();
 * }</pre>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ImmutableMapCollector<K, V, S extends ImmutableMap<K, V>> implements Consumer<Map.Entry<? extends K, ? extends V>>,
        Collector<Map.Entry<K, V>, ImmutableMapCollector<K, V, S>, S> {
    private final Stack<ImmutableMap<K, V>> stack = new Stack<ImmutableMap<K, V>>();
    private ImmutableMap<K, V> s;
    private final Set<Characteristics> characteristics;

    public ImmutableMapCollector(ImmutableMap<K, V> s) {
        this.s = s;
        characteristics = s instanceof ImmutableSequencedMap<K, V>
                ? Set.of()
                : Set.of(Characteristics.UNORDERED);
    }

    @Override
    public void accept(Map.Entry<? extends K, ? extends V> t) {
        s = s.put(t.getKey(), t.getValue());
        if (s.size() >= 16384) {
            if (!stack.isEmpty()) {
                do {
                    s = stack.pop().putAll(s);
                } while (!stack.isEmpty() && stack.peek().size() <= s.size());
            }
            stack.push(s);
            s = s.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public S build() {
        while (!stack.isEmpty()) {
            s = stack.pop().putAll(s);
        }
        return (S) s;
    }

    @Override
    public Supplier<ImmutableMapCollector<K, V, S>> supplier() {
        return () -> new ImmutableMapCollector<>(s.clear());
    }

    @Override
    public BiConsumer<ImmutableMapCollector<K, V, S>, Map.Entry<K, V>> accumulator() {
        return ImmutableMapCollector::accept;
    }

    @Override
    public BinaryOperator<ImmutableMapCollector<K, V, S>> combiner() {
        return (a, b) -> new ImmutableMapCollector<>(a.build().putAll(b.build()));
    }

    @Override
    public Function<ImmutableMapCollector<K, V, S>, S> finisher() {
        return ImmutableMapCollector::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }
}
