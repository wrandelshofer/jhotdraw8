/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 * <p>
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public class PersistentTrieMap<K, V> implements PersistentMap<K, V> {

  private static final CompactMapNode<?, ?> EMPTY_NODE = new BitmapIndexedMapNode<>(null, (0), (0), new Object[]{});

  private static final PersistentTrieMap<?, ?> EMPTY_MAP = new PersistentTrieMap<>(EMPTY_NODE, 0, 0);

  private final @NonNull AbstractMapNode<K, V> root;
  private final int hashCode;
  private final int size;

  PersistentTrieMap(@NonNull AbstractMapNode<K, V> root, int hashCode, int size) {
    this.root = root;
    this.hashCode = hashCode;
    this.size = size;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> @NonNull PersistentTrieMap<K, V> of() {
    return (PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY_MAP;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> @NonNull PersistentTrieMap<K, V> of(@NonNull Object... keyValuePairs) {
    if (keyValuePairs.length % 2 != 0) {
      throw new IllegalArgumentException("Length of argument list is uneven: no key/value pairs.");
    }

    TransientTrieMap<K, V> result = PersistentTrieMap.<K, V>of().asTransient();
    for (int i = 0; i < keyValuePairs.length; i += 2) {
      final K key = (K) keyValuePairs[i];
      final V val = (V) keyValuePairs[i + 1];
      result.__put(key, val);
    }
    return result.freeze();
  }

  @Override
  public boolean containsKey(final @NonNull Object o) {
    try {
      @SuppressWarnings("unchecked") final K key = (K) o;
      return root.findByKey(key, key.hashCode(), 0).exists();
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public boolean containsValue(final Object o) {
    for (Iterator<V> iterator = valueIterator(); iterator.hasNext(); ) {
      if (Objects.equals(iterator.next(), o)) {
        return true;
      }
    }
    return false;
  }


  @Override
  public V get(final @NonNull Object o) {
    try {
      @SuppressWarnings("unchecked") final K key = (K) o;
      final ResultValue<V> result = root.findByKey(key, key.hashCode(), 0);
      return result.orElse(null);
    } catch (ClassCastException unused) {
      return null;
    }
  }

  @Override
  public @NonNull Iterator<Map.Entry<K, V>> entries() {
    return entryIterator();
  }

  @Override
  public @NonNull Iterator<K> keys() {
    return keyIterator();
  }


  public PersistentTrieMap<K, V> withPut(@NonNull K key, @Nullable V value) {
    return __putEquivalent(key, value);
  }


  public PersistentTrieMap<K, V> __putEquivalent(final K key, final V val) {
    final int keyHash = key.hashCode();
    final MapNodeResult<K, V> details = MapNodeResult.unchanged();

    final AbstractMapNode<K, V> newRootNode = root.updated(null, key, val,
            keyHash, 0, details);

    if (details.isModified()) {
      if (details.hasReplacedValue()) {
        final int valHashOld = details.getReplacedValue().hashCode();
        final int valHashNew = val.hashCode();

        return new PersistentTrieMap<K, V>(newRootNode,
                hashCode + ((keyHash ^ valHashNew)) - ((keyHash ^ valHashOld)), size);
      }

      final int valHash = val.hashCode();
      return new PersistentTrieMap<K, V>(newRootNode, hashCode + ((keyHash ^ valHash)),
              size + 1);
    }

    return this;
  }


  public PersistentTrieMap<K, V> withPutAll(@NonNull Map<? extends K, ? extends V> map) {
    return __putAllEquivalent(map);
  }

  public PersistentTrieMap<K, V> __putAllEquivalent(
          final Map<? extends K, ? extends V> map) {
    final TransientTrieMap<K, V> tmpTransient = this.asTransient();
    tmpTransient.__putAll(map);
    return tmpTransient.freeze();
  }


  public PersistentTrieMap<K, V> withRemove(@NonNull K key) {
    return __removeEquivalent(key);
  }


  public PersistentTrieMap<K, V> __removeEquivalent(final K key) {
    final int keyHash = key.hashCode();
    final MapNodeResult<K, V> details = MapNodeResult.unchanged();

    final AbstractMapNode<K, V> newRootNode = root.removed(null, key,
            keyHash, 0, details);

    if (details.isModified()) {
      assert details.hasReplacedValue();
      final int valHash = details.getReplacedValue().hashCode();
      return new PersistentTrieMap<K, V>(newRootNode, hashCode - ((keyHash ^ valHash)),
              size - 1);
    }

    return this;
  }


  @Override
  public @NonNull PersistentSet<K> withRemoveAll(@NonNull Iterable<? extends K> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }


  public Iterator<K> keyIterator() {
    return new MapKeyIterator<>(root);
  }


  public Iterator<V> valueIterator() {
    return new MapValueIterator<>(root);
  }

  public Iterator<Map.Entry<K, V>> entryIterator() {
    return new MapEntryIterator<>(root);
  }

  public Set<K> keySet() {
    Set<K> keySet = null;

    if (keySet == null) {
      keySet = new AbstractSet<K>() {
        @Override
        public Iterator<K> iterator() {
          return PersistentTrieMap.this.keyIterator();
        }

        @Override
        public int size() {
          return PersistentTrieMap.this.size();
        }

        @Override
        public boolean isEmpty() {
          return PersistentTrieMap.this.isEmpty();
        }

        @Override
        public void clear() {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object k) {
          return PersistentTrieMap.this.containsKey(k);
        }
      };
    }

    return keySet;
  }

  public Collection<V> values() {
    Collection<V> values = null;

    if (values == null) {
      values = new AbstractCollection<V>() {
        @Override
        public Iterator<V> iterator() {
          return PersistentTrieMap.this.valueIterator();
        }

        @Override
        public int size() {
          return PersistentTrieMap.this.size();
        }

        @Override
        public boolean isEmpty() {
          return PersistentTrieMap.this.isEmpty();
        }

        @Override
        public void clear() {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object v) {
          return PersistentTrieMap.this.containsValue(v);
        }
      };
    }

    return values;
  }


  @Override
  public boolean equals(final Object other) {
    return equivalent(other);
  }


  public boolean equivalent(final Object other) {
    if (other == this) {
      return true;
    }
    if (other == null) {
      return false;
    }

    if (other instanceof PersistentTrieMap) {
      PersistentTrieMap<?, ?> that = (PersistentTrieMap<?, ?>) other;

      if (this.size != that.size) {
        return false;
      }

      if (this.hashCode != that.hashCode) {
        return false;
      }

      return root.equivalent(that.root);
    } else if (other instanceof Map) {
      Map that = (Map) other;

      if (this.size() != that.size()) {
        return false;
      }

      for (
              Iterator<Map.Entry> it = that.entrySet().iterator(); it.hasNext(); ) {
        Map.Entry entry = it.next();

        try {
          final K key = (K) entry.getKey();
          final ResultValue<V> result = root
                  .findByKey(key, key.hashCode(), 0);

          if (!result.exists()) {
            return false;
          } else {
            final V val = (V) entry.getValue();

            if (!Objects.equals(result.get(), val)) {
              return false;
            }
          }
        } catch (ClassCastException unused) {
          return false;
        }
      }

      return true;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    String body =
            readOnlyEntrySet().stream().map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
                    .reduce((o1, o2) -> String.join(", ", o1, o2)).orElse("");
    return String.format("{%s}", body);
  }


  private TransientTrieMap<K, V> asTransient() {
    return new TransientTrieMap<K, V>(this);
  }

  /*
   * For analysis purposes only.
   */
  protected AbstractMapNode<K, V> getRoot() {
    return root;
  }

  /*
   * For analysis purposes only.
   */
  protected Iterator<AbstractMapNode<K, V>> nodeIterator() {
    return new TrieMapNodeIterator<>(root);
  }

  /*
   * For analysis purposes only.
   */
  protected int getNodeCount() {
    final Iterator<AbstractMapNode<K, V>> it = nodeIterator();
    int sumNodes = 0;

    for (; it.hasNext(); it.next()) {
      sumNodes += 1;
    }

    return sumNodes;
  }

  /*
   * For analysis purposes only. Payload X Node
   */
  protected int[][] arityCombinationsHistogram() {
    final Iterator<AbstractMapNode<K, V>> it = nodeIterator();
    final int[][] sumArityCombinations = new int[33][33];

    while (it.hasNext()) {
      final AbstractMapNode<K, V> node = it.next();
      sumArityCombinations[node.payloadArity()][node.nodeArity()] += 1;
    }

    return sumArityCombinations;
  }

  /*
   * For analysis purposes only.
   */
  protected int[] arityHistogram() {
    final int[][] sumArityCombinations = arityCombinationsHistogram();
    final int[] sumArity = new int[33];

    final int maxArity = 32; // TODO: factor out constant

    for (int j = 0; j <= maxArity; j++) {
      for (int maxRestArity = maxArity - j, k = 0; k <= maxRestArity - j; k++) {
        sumArity[j + k] += sumArityCombinations[j][k];
      }
    }

    return sumArity;
  }

  @Override
  public @NonNull PersistentSet<K> withRetainAll(@NonNull Collection<? extends K> c) {
    throw new UnsupportedOperationException();
  }

  // TODO: support {@code Iterable} interface like AbstractSetNode
  protected static abstract class AbstractMapNode<K, V> implements
          MapNode<K, V, AbstractMapNode<K, V>>,
          java.io.Serializable {

    private static final long serialVersionUID = 42L;

    static final int TUPLE_LENGTH = 2;

    static final <T> boolean isAllowedToEdit(AtomicReference<?> x, AtomicReference<?> y) {
      return x != null && y != null && (x == y || x.get() == y.get());
    }

    abstract boolean hasNodes();

    abstract int nodeArity();

    abstract AbstractMapNode<K, V> getNode(final int index);

    @Deprecated
    Iterator<? extends AbstractMapNode<K, V>> nodeIterator() {
      return new Iterator<AbstractMapNode<K, V>>() {

        int nextIndex = 0;
        final int nodeArity = AbstractMapNode.this.nodeArity();

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

        @Override
        public AbstractMapNode<K, V> next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          return AbstractMapNode.this.getNode(nextIndex++);
        }

        @Override
        public boolean hasNext() {
          return nextIndex < nodeArity;
        }
      };
    }

    abstract boolean hasPayload();

    abstract int payloadArity();

    abstract K getKey(final int index);

    abstract V getValue(final int index);

    abstract Map.Entry<K, V> getKeyValueEntry(final int index);

    @Deprecated
    abstract boolean hasSlots();

    abstract int slotArity();

    abstract Object getSlot(final int index);

    /**
     * The arity of this trie node (i.e. number of values and nodes stored on this level).
     *
     * @return sum of nodes and values stored within
     */

    int arity() {
      return payloadArity() + nodeArity();
    }

    int size() {
      final Iterator<K> it = new MapKeyIterator<>(this);

      int size = 0;
      while (it.hasNext()) {
        size += 1;
        it.next();
      }

      return size;
    }
  }

  protected static abstract class CompactMapNode<K, V> extends AbstractMapNode<K, V> {

    static final int HASH_CODE_LENGTH = 32;

    static final int BIT_PARTITION_SIZE = 5;
    static final int BIT_PARTITION_MASK = 0b11111;

    static final int mask(final int keyHash, final int shift) {
      return (keyHash >>> shift) & BIT_PARTITION_MASK;
    }

    static final int bitpos(final int mask) {
      return 1 << mask;
    }

    abstract int nodeMap();

    abstract int dataMap();

    @Override
    abstract CompactMapNode<K, V> getNode(final int index);

    boolean nodeInvariant() {
      boolean inv1 = (size() - payloadArity() >= 2 * (arity() - payloadArity()));
      boolean inv2 = (this.arity() == 0) ? sizePredicate() == SIZE_EMPTY : true;
      boolean inv3 =
              (this.arity() == 1 && payloadArity() == 1) ? sizePredicate() == SIZE_ONE : true;
      boolean inv4 = (this.arity() >= 2) ? sizePredicate() == SIZE_MORE_THAN_ONE : true;

      boolean inv5 = (this.nodeArity() >= 0) && (this.payloadArity() >= 0)
              && ((this.payloadArity() + this.nodeArity()) == this.arity());

      return inv1 && inv2 && inv3 && inv4 && inv5;
    }

    abstract CompactMapNode<K, V> copyAndSetValue(final AtomicReference<Thread> mutator,
                                                  final int bitpos, final V val);

    abstract CompactMapNode<K, V> copyAndInsertValue(final AtomicReference<Thread> mutator,
                                                     final int bitpos, final K key, final V val);

    abstract CompactMapNode<K, V> copyAndRemoveValue(final AtomicReference<Thread> mutator,
                                                     final int bitpos);

    abstract CompactMapNode<K, V> copyAndSetNode(final AtomicReference<Thread> mutator,
                                                 final int bitpos, final AbstractMapNode<K, V> node);

    abstract CompactMapNode<K, V> copyAndMigrateFromInlineToNode(
            final AtomicReference<Thread> mutator, final int bitpos, final AbstractMapNode<K, V> node);

    abstract CompactMapNode<K, V> copyAndMigrateFromNodeToInline(
            final AtomicReference<Thread> mutator, final int bitpos, final AbstractMapNode<K, V> node);

    static final <K, V> CompactMapNode<K, V> mergeTwoKeyValPairs(final K key0, final V val0,
                                                                 final int keyHash0, final K key1, final V val1, final int keyHash1, final int shift) {
      assert !(key0.equals(key1));

      if (shift >= HASH_CODE_LENGTH) {
        // throw new
        // IllegalStateException("Hash collision not yet fixed.");
        return new HashCollisionMapNode<>(keyHash0, (K[]) new Object[]{key0, key1},
                (V[]) new Object[]{val0, val1});
      }

      final int mask0 = mask(keyHash0, shift);
      final int mask1 = mask(keyHash1, shift);

      if (mask0 != mask1) {
        // both nodes fit on same level
        final int dataMap = bitpos(mask0) | bitpos(mask1);

        if (mask0 < mask1) {
          return nodeOf(null, (0), dataMap, new Object[]{key0, val0, key1, val1});
        } else {
          return nodeOf(null, (0), dataMap, new Object[]{key1, val1, key0, val0});
        }
      } else {
        final CompactMapNode<K, V> node = mergeTwoKeyValPairs(key0, val0, keyHash0, key1, val1,
                keyHash1, shift + BIT_PARTITION_SIZE);
        // values fit on next level

        final int nodeMap = bitpos(mask0);
        return nodeOf(null, nodeMap, (0), new Object[]{node});
      }
    }

    static final <K, V> CompactMapNode<K, V> nodeOf(final AtomicReference<Thread> mutator,
                                                    final int nodeMap, final int dataMap, final Object[] nodes) {
      return new BitmapIndexedMapNode<>(mutator, nodeMap, dataMap, nodes);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> PersistentTrieMap.CompactMapNode<K, V> emptyNode() {
      return (PersistentTrieMap.CompactMapNode<K, V>) PersistentTrieMap.EMPTY_NODE;
    }

    static final <K, V> CompactMapNode<K, V> nodeOf(AtomicReference<Thread> mutator) {
      return emptyNode();
    }

    static final <K, V> CompactMapNode<K, V> nodeOf(AtomicReference<Thread> mutator,
                                                    final int nodeMap, final int dataMap, final K key, final V val) {
      assert nodeMap == 0;
      return nodeOf(mutator, (0), dataMap, new Object[]{key, val});
    }

    static final int index(final int bitmap, final int bitpos) {
      return Integer.bitCount(bitmap & (bitpos - 1));
    }

    static final int index(final int bitmap, final int mask, final int bitpos) {
      return (bitmap == -1) ? mask : index(bitmap, bitpos);
    }

    int dataIndex(final int bitpos) {
      return Integer.bitCount(dataMap() & (bitpos - 1));
    }

    int nodeIndex(final int bitpos) {
      return Integer.bitCount(nodeMap() & (bitpos - 1));
    }

    CompactMapNode<K, V> nodeAt(final int bitpos) {
      return getNode(nodeIndex(bitpos));
    }

    @Override
    public ResultValue<V> findByKey(final K key, final int keyHash, final int shift) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      if ((dataMap() & bitpos) != 0) { // inplace value
        final int index = dataIndex(bitpos);
        if (Objects.equals(getKey(index), key)) {
          final V result = getValue(index);

          return ResultValue.of(result);
        }

        return ResultValue.empty();
      }

      if ((nodeMap() & bitpos) != 0) { // node (not value)
        final AbstractMapNode<K, V> subNode = nodeAt(bitpos);

        return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE);
      }

      return ResultValue.empty();
    }

    @Override
    public AbstractMapNode<K, V> updated(final AtomicReference<Thread> mutator, final K key, final V val,
                                         final int keyHash, final int shift, final MapNodeResult<K, V> details) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      if ((dataMap() & bitpos) != 0) { // inplace value
        final int dataIndex = dataIndex(bitpos);
        final K currentKey = getKey(dataIndex);

        if (Objects.equals(currentKey, key)) {
          final V currentVal = getValue(dataIndex);

          // update mapping
          details.updated(currentVal);
          return copyAndSetValue(mutator, bitpos, val);
        } else {
          final V currentVal = getValue(dataIndex);
          final AbstractMapNode<K, V> subNodeNew =
                  mergeTwoKeyValPairs(currentKey, currentVal, currentKey.hashCode(),
                          key, val, keyHash, shift + BIT_PARTITION_SIZE);

          details.modified();
          return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);
        }
      } else if ((nodeMap() & bitpos) != 0) { // node (not value)
        final AbstractMapNode<K, V> subNode = nodeAt(bitpos);
        final AbstractMapNode<K, V> subNodeNew =
                subNode.updated(mutator, key, val, keyHash, shift + BIT_PARTITION_SIZE, details);

        if (details.isModified()) {
          return copyAndSetNode(mutator, bitpos, subNodeNew);
        } else {
          return this;
        }
      } else {
        // no value
        details.modified();
        return copyAndInsertValue(mutator, bitpos, key, val);
      }
    }

    @Override
    public AbstractMapNode<K, V> removed(final AtomicReference<Thread> mutator, final K key,
                                         final int keyHash, final int shift, final MapNodeResult<K, V> details) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      if ((dataMap() & bitpos) != 0) { // inplace value
        final int dataIndex = dataIndex(bitpos);

        if (Objects.equals(getKey(dataIndex), key)) {
          final V currentVal = getValue(dataIndex);
          details.updated(currentVal);

          if (this.payloadArity() == 2 && this.nodeArity() == 0) {
            /*
             * Create new node with remaining pair. The new node will a) either become the new root
             * returned, or b) unwrapped and inlined during returning.
             */
            final int newDataMap =
                    (shift == 0) ? (int) (dataMap() ^ bitpos) : bitpos(mask(keyHash, 0));

            if (dataIndex == 0) {
              return CompactMapNode.<K, V>nodeOf(mutator, 0, newDataMap, getKey(1), getValue(1));
            } else {
              return CompactMapNode.<K, V>nodeOf(mutator, 0, newDataMap, getKey(0), getValue(0));
            }
          } else {
            return copyAndRemoveValue(mutator, bitpos);
          }
        } else {
          return this;
        }
      } else if ((nodeMap() & bitpos) != 0) { // node (not value)
        final AbstractMapNode<K, V> subNode = nodeAt(bitpos);
        final AbstractMapNode<K, V> subNodeNew =
                subNode.removed(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details);

        if (!details.isModified()) {
          return this;
        }

        switch (subNodeNew.sizePredicate()) {
        case 0: {
          throw new IllegalStateException("Sub-node must have at least one element.");
        }
        case 1: {
          if (this.payloadArity() == 0 && this.nodeArity() == 1) {
            // escalate (singleton or empty) result
            return subNodeNew;
          } else {
            // inline value (move to front)
            return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
          }
        }
        default: {
          // modify current node (set replacement node)
          return copyAndSetNode(mutator, bitpos, subNodeNew);
        }
        }
      }

      return this;
    }

    /**
     * @return 0 <= mask <= 2^BIT_PARTITION_SIZE - 1
     */
    static byte recoverMask(int map, byte i_th) {
      assert 1 <= i_th && i_th <= 32;

      byte cnt1 = 0;
      byte mask = 0;

      while (mask < 32) {
        if ((map & 0x01) == 0x01) {
          cnt1 += 1;

          if (cnt1 == i_th) {
            return mask;
          }
        }

        map = map >> 1;
        mask += 1;
      }

      assert cnt1 != i_th;
      throw new RuntimeException("Called with invalid arguments.");
    }

    @Override
    public String toString() {
      final StringBuilder bldr = new StringBuilder();
      bldr.append('[');

      for (byte i = 0; i < payloadArity(); i++) {
        final byte pos = recoverMask(dataMap(), (byte) (i + 1));
        bldr.append(String.format("@%d<#%d,#%d>", pos, Objects.hashCode(getKey(i)),
                Objects.hashCode(getValue(i))));

        if (!((i + 1) == payloadArity())) {
          bldr.append(", ");
        }
      }

      if (payloadArity() > 0 && nodeArity() > 0) {
        bldr.append(", ");
      }

      for (byte i = 0; i < nodeArity(); i++) {
        final byte pos = recoverMask(nodeMap(), (byte) (i + 1));
        bldr.append(String.format("@%d: %s", pos, getNode(i)));

        if (!((i + 1) == nodeArity())) {
          bldr.append(", ");
        }
      }

      bldr.append(']');
      return bldr.toString();
    }

  }

  protected static abstract class CompactMixedMapNode<K, V> extends CompactMapNode<K, V> {

    private final int nodeMap;
    private final int dataMap;

    CompactMixedMapNode(final int nodeMap,
                        final int dataMap) {
      this.nodeMap = nodeMap;
      this.dataMap = dataMap;
    }

    @Override
    public int nodeMap() {
      return nodeMap;
    }

    @Override
    public int dataMap() {
      return dataMap;
    }

  }

  private static final class BitmapIndexedMapNode<K, V> extends CompactMixedMapNode<K, V> {

    transient final AtomicReference<Thread> mutator;
    final Object[] nodes;

    private BitmapIndexedMapNode(final AtomicReference<Thread> mutator, final int nodeMap,
                                 final int dataMap, final Object[] nodes) {
      super(nodeMap, dataMap);

      this.mutator = mutator;
      this.nodes = nodes;

    }

    @Override
    K getKey(final int index) {
      return (K) nodes[TUPLE_LENGTH * index];
    }

    @Override
    V getValue(final int index) {
      return (V) nodes[TUPLE_LENGTH * index + 1];
    }

    @Override
    Map.Entry<K, V> getKeyValueEntry(final int index) {
      return entryOf((K) nodes[TUPLE_LENGTH * index], (V) nodes[TUPLE_LENGTH * index + 1]);
    }

    @Override
    CompactMapNode<K, V> getNode(final int index) {
      return (CompactMapNode<K, V>) nodes[nodes.length - 1 - index];
    }

    @Override
    boolean hasPayload() {
      return dataMap() != 0;
    }

    @Override
    int payloadArity() {
      return Integer.bitCount(dataMap());
    }

    @Override
    boolean hasNodes() {
      return nodeMap() != 0;
    }

    @Override
    int nodeArity() {
      return Integer.bitCount(nodeMap());
    }

    @Override
    Object getSlot(final int index) {
      return nodes[index];
    }

    @Override
    boolean hasSlots() {
      return nodes.length != 0;
    }

    @Override
    int slotArity() {
      return nodes.length;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 0;
      result = prime * result + (nodeMap());
      result = prime * result + (dataMap());
      result = prime * result + Arrays.hashCode(nodes);
      return result;
    }

    @Override
    public boolean equals(final Object other) {
      return equivalent(other);
    }

    @Override
    public boolean equivalent(final Object other) {
      if (null == other) {
        return false;
      }
      if (this == other) {
        return true;
      }
      if (getClass() != other.getClass()) {
        return false;
      }
      BitmapIndexedMapNode<?, ?> that = (BitmapIndexedMapNode<?, ?>) other;
      if (nodeMap() != that.nodeMap()) {
        return false;
      }
      if (dataMap() != that.dataMap()) {
        return false;
      }
      if (!deepContentEquality(nodes, that.nodes, 2 * payloadArity(), slotArity())) {
        return false;
      }
      return true;
    }

    private final boolean deepContentEquality(
            /* @NotNull */ Object[] a1, /* @NotNull */ Object[] a2, int splitAt, int length) {

//      assert a1 != null && a2 != null;
//      assert a1.length == a2.length;

      if (a1 == a2) {
        return true;
      }

      // compare local payload
      for (int i = 0; i < splitAt; i++) {
        Object o1 = a1[i];
        Object o2 = a2[i];

        if (!Objects.equals(o1, o2)) {
          return false;
        }
      }

      // recursively compare nested nodes
      for (int i = splitAt; i < length; i++) {
        AbstractMapNode o1 = (AbstractMapNode) a1[i];
        AbstractMapNode o2 = (AbstractMapNode) a2[i];

        if (!o1.equivalent(o2)) {
          return false;
        }
      }

      return true;
    }

    @Override
    public byte sizePredicate() {
      if (this.nodeArity() == 0) {
        switch (this.payloadArity()) {
        case 0:
          return SIZE_EMPTY;
        case 1:
          return SIZE_ONE;
        default:
          return SIZE_MORE_THAN_ONE;
        }
      } else {
        return SIZE_MORE_THAN_ONE;
      }
    }

    @Override
    CompactMapNode<K, V> copyAndSetValue(final AtomicReference<Thread> mutator, final int bitpos,
                                         final V val) {
      final int idx = TUPLE_LENGTH * dataIndex(bitpos) + 1;

      if (isAllowedToEdit(this.mutator, mutator)) {
        // no copying if already editable
        this.nodes[idx] = val;
        return this;
      } else {
        final Object[] src = this.nodes;
        final Object[] dst = new Object[src.length];

        // copy 'src' and set 1 element(s) at position 'idx'
        System.arraycopy(src, 0, dst, 0, src.length);
        dst[idx + 0] = val;

        return nodeOf(mutator, nodeMap(), dataMap(), dst);
      }
    }

    @Override
    CompactMapNode<K, V> copyAndSetNode(final AtomicReference<Thread> mutator, final int bitpos,
                                        final AbstractMapNode<K, V> node) {

      final int idx = this.nodes.length - 1 - nodeIndex(bitpos);

      if (isAllowedToEdit(this.mutator, mutator)) {
        // no copying if already editable
        this.nodes[idx] = node;
        return this;
      } else {
        final Object[] src = this.nodes;
        final Object[] dst = new Object[src.length];

        // copy 'src' and set 1 element(s) at position 'idx'
        System.arraycopy(src, 0, dst, 0, src.length);
        dst[idx + 0] = node;

        return nodeOf(mutator, nodeMap(), dataMap(), dst);
      }
    }

    @Override
    CompactMapNode<K, V> copyAndInsertValue(final AtomicReference<Thread> mutator, final int bitpos,
                                            final K key, final V val) {
      final int idx = TUPLE_LENGTH * dataIndex(bitpos);

      final Object[] src = this.nodes;
      final Object[] dst = new Object[src.length + 2];

      // copy 'src' and insert 2 element(s) at position 'idx'
      System.arraycopy(src, 0, dst, 0, idx);
      dst[idx + 0] = key;
      dst[idx + 1] = val;
      System.arraycopy(src, idx, dst, idx + 2, src.length - idx);

      return nodeOf(mutator, nodeMap(), dataMap() | bitpos, dst);
    }

    @Override
    CompactMapNode<K, V> copyAndRemoveValue(final AtomicReference<Thread> mutator,
                                            final int bitpos) {
      final int idx = TUPLE_LENGTH * dataIndex(bitpos);

      final Object[] src = this.nodes;
      final Object[] dst = new Object[src.length - 2];

      // copy 'src' and remove 2 element(s) at position 'idx'
      System.arraycopy(src, 0, dst, 0, idx);
      System.arraycopy(src, idx + 2, dst, idx, src.length - idx - 2);

      return nodeOf(mutator, nodeMap(), dataMap() ^ bitpos, dst);
    }

    @Override
    CompactMapNode<K, V> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
                                                        final int bitpos, final AbstractMapNode<K, V> node) {

      final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
      final int idxNew = this.nodes.length - TUPLE_LENGTH - nodeIndex(bitpos);

      final Object[] src = this.nodes;
      final Object[] dst = new Object[src.length - 2 + 1];

      // copy 'src' and remove 2 element(s) at position 'idxOld' and
      // insert 1 element(s) at position 'idxNew' (TODO: carefully test)
      assert idxOld <= idxNew;
      System.arraycopy(src, 0, dst, 0, idxOld);
      System.arraycopy(src, idxOld + 2, dst, idxOld, idxNew - idxOld);
      dst[idxNew + 0] = node;
      System.arraycopy(src, idxNew + 2, dst, idxNew + 1, src.length - idxNew - 2);

      return nodeOf(mutator, nodeMap() | bitpos, dataMap() ^ bitpos, dst);
    }

    @Override
    CompactMapNode<K, V> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
                                                        final int bitpos, final AbstractMapNode<K, V> node) {

      final int idxOld = this.nodes.length - 1 - nodeIndex(bitpos);
      final int idxNew = TUPLE_LENGTH * dataIndex(bitpos);

      final Object[] src = this.nodes;
      final Object[] dst = new Object[src.length - 1 + 2];

      // copy 'src' and remove 1 element(s) at position 'idxOld' and
      // insert 2 element(s) at position 'idxNew' (TODO: carefully test)
      assert idxOld >= idxNew;
      System.arraycopy(src, 0, dst, 0, idxNew);
      dst[idxNew + 0] = node.getKey(0);
      dst[idxNew + 1] = node.getValue(0);
      System.arraycopy(src, idxNew, dst, idxNew + 2, idxOld - idxNew);
      System.arraycopy(src, idxOld + 1, dst, idxOld + 2, src.length - idxOld - 1);

      return nodeOf(mutator, nodeMap() ^ bitpos, dataMap() | bitpos, dst);
    }

  }

  private static final class HashCollisionMapNode<K, V> extends CompactMapNode<K, V> {

    private final K[] keys;
    private final V[] vals;
    private final int hash;

    HashCollisionMapNode(final int hash, final K[] keys, final V[] vals) {
      this.keys = keys;
      this.vals = vals;
      this.hash = hash;

      assert payloadArity() >= 2;
    }

    @Override
    public ResultValue<V> findByKey(final K key, final int keyHash, final int shift) {
      for (int i = 0; i < keys.length; i++) {
        final K _key = keys[i];
        if (Objects.equals(key, _key)) {
          final V val = vals[i];
          return ResultValue.of(val);
        }
      }
      return ResultValue.empty();
    }

    @Override
    public AbstractMapNode<K, V> updated(final AtomicReference<Thread> mutator, final K key, final V val,
                                         final int keyHash, final int shift, final MapNodeResult<K, V> details) {
      assert this.hash == keyHash;

      for (int idx = 0; idx < keys.length; idx++) {
        if (Objects.equals(keys[idx], key)) {
          final V currentVal = vals[idx];

          if (Objects.equals(currentVal, val)) {
            return this;
          } else {
            // add new mapping
            final V[] src = this.vals;
            final V[] dst = (V[]) new Object[src.length];

            // copy 'src' and set 1 element(s) at position 'idx'
            System.arraycopy(src, 0, dst, 0, src.length);
            dst[idx + 0] = val;

            final CompactMapNode<K, V> thisNew =
                    new HashCollisionMapNode<>(this.hash, this.keys, dst);

            details.updated(currentVal);
            return thisNew;
          }
        }
      }

      final K[] keysNew = (K[]) new Object[this.keys.length + 1];

      // copy 'this.keys' and insert 1 element(s) at position
      // 'keys.length'
      System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
      keysNew[keys.length + 0] = key;
      System.arraycopy(this.keys, keys.length, keysNew, keys.length + 1,
              this.keys.length - keys.length);

      final V[] valsNew = (V[]) new Object[this.vals.length + 1];

      // copy 'this.vals' and insert 1 element(s) at position
      // 'vals.length'
      System.arraycopy(this.vals, 0, valsNew, 0, vals.length);
      valsNew[vals.length + 0] = val;
      System.arraycopy(this.vals, vals.length, valsNew, vals.length + 1,
              this.vals.length - vals.length);

      details.modified();
      return new HashCollisionMapNode<>(keyHash, keysNew, valsNew);
    }

    @Override
    public AbstractMapNode<K, V> removed(final AtomicReference<Thread> mutator, final K key,
                                         final int keyHash, final int shift, final MapNodeResult<K, V> details) {
      for (int idx = 0; idx < keys.length; idx++) {
        if (Objects.equals(keys[idx], key)) {
          final V currentVal = vals[idx];
          details.updated(currentVal);

          if (this.arity() == 1) {
            return nodeOf(mutator);
          } else if (this.arity() == 2) {
            /*
             * Create root node with singleton element. This node will be a) either be the new root
             * returned, or b) unwrapped and inlined.
             */
            final K theOtherKey = (idx == 0) ? keys[1] : keys[0];
            final V theOtherVal = (idx == 0) ? vals[1] : vals[0];
            return CompactMapNode.<K, V>nodeOf(mutator).updated(mutator, theOtherKey, theOtherVal,
                    keyHash, 0, details);
          } else {
            final K[] keysNew = (K[]) new Object[this.keys.length - 1];

            // copy 'this.keys' and remove 1 element(s) at position
            // 'idx'
            System.arraycopy(this.keys, 0, keysNew, 0, idx);
            System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx - 1);

            final V[] valsNew = (V[]) new Object[this.vals.length - 1];

            // copy 'this.vals' and remove 1 element(s) at position
            // 'idx'
            System.arraycopy(this.vals, 0, valsNew, 0, idx);
            System.arraycopy(this.vals, idx + 1, valsNew, idx, this.vals.length - idx - 1);

            return new HashCollisionMapNode<>(keyHash, keysNew, valsNew);
          }
        }
      }
      return this;
    }

    @Override
    boolean hasPayload() {
      return true;
    }

    @Override
    int payloadArity() {
      return keys.length;
    }

    @Override
    boolean hasNodes() {
      return false;
    }

    @Override
    int nodeArity() {
      return 0;
    }

    @Override
    int arity() {
      return payloadArity();
    }

    @Override
    public byte sizePredicate() {
      return SIZE_MORE_THAN_ONE;
    }

    @Override
    K getKey(final int index) {
      return keys[index];
    }

    @Override
    V getValue(final int index) {
      return vals[index];
    }

    @Override
    Map.Entry<K, V> getKeyValueEntry(final int index) {
      return entryOf(keys[index], vals[index]);
    }

    @Override
    public CompactMapNode<K, V> getNode(int index) {
      throw new IllegalStateException("Is leaf node.");
    }

    @Override
    Object getSlot(final int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    boolean hasSlots() {
      throw new UnsupportedOperationException();
    }

    @Override
    int slotArity() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 0;
      result = prime * result + hash;
      result = prime * result + Arrays.hashCode(keys);
      result = prime * result + Arrays.hashCode(vals);
      return result;
    }

    @Override
    public boolean equals(final Object other) {
      return equivalent(other);
    }

    @Override
    public boolean equivalent(Object other) {
      if (null == other) {
        return false;
      }
      if (this == other) {
        return true;
      }
      if (getClass() != other.getClass()) {
        return false;
      }

      HashCollisionMapNode<?, ?> that = (HashCollisionMapNode<?, ?>) other;

      if (hash != that.hash) {
        return false;
      }

      if (arity() != that.arity()) {
        return false;
      }

      /*
       * Linear scan for each key, because of arbitrary element order.
       */
      outerLoop:
      for (int i = 0; i < that.payloadArity(); i++) {
        final Object otherKey = that.getKey(i);
        final Object otherVal = that.getValue(i);

        for (int j = 0; j < keys.length; j++) {
          final K key = keys[j];
          final V val = vals[j];

          if (Objects.equals(key, otherKey) && Objects.equals(val, otherVal)) {
            continue outerLoop;
          }
        }
        return false;
      }

      return true;
    }

    @Override
    CompactMapNode<K, V> copyAndSetValue(final AtomicReference<Thread> mutator, final int bitpos,
                                         final V val) {
      throw new UnsupportedOperationException();
    }

    @Override
    CompactMapNode<K, V> copyAndInsertValue(final AtomicReference<Thread> mutator, final int bitpos,
                                            final K key, final V val) {
      throw new UnsupportedOperationException();
    }

    @Override
    CompactMapNode<K, V> copyAndRemoveValue(final AtomicReference<Thread> mutator,
                                            final int bitpos) {
      throw new UnsupportedOperationException();
    }

    @Override
    CompactMapNode<K, V> copyAndSetNode(final AtomicReference<Thread> mutator, final int bitpos,
                                        final AbstractMapNode<K, V> node) {
      throw new UnsupportedOperationException();
    }

    @Override
    CompactMapNode<K, V> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
                                                        final int bitpos, final AbstractMapNode<K, V> node) {
      throw new UnsupportedOperationException();
    }

    @Override
    CompactMapNode<K, V> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
                                                        final int bitpos, final AbstractMapNode<K, V> node) {
      throw new UnsupportedOperationException();
    }

    @Override
    int nodeMap() {
      throw new UnsupportedOperationException();
    }

    @Override
    int dataMap() {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * Iterator skeleton that uses a fixed stack in depth.
   */
  private static abstract class AbstractMapIterator<K, V> {

    private static final int MAX_DEPTH = 7;

    protected int currentValueCursor;
    protected int currentValueLength;
    protected AbstractMapNode<K, V> currentValueNode;

    private int currentStackLevel = -1;
    private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];

    AbstractMapNode<K, V>[] nodes = new AbstractMapNode[MAX_DEPTH];

    AbstractMapIterator(AbstractMapNode<K, V> rootNode) {
      if (rootNode.hasNodes()) {
        currentStackLevel = 0;

        nodes[0] = rootNode;
        nodeCursorsAndLengths[0] = 0;
        nodeCursorsAndLengths[1] = rootNode.nodeArity();
      }

      if (rootNode.hasPayload()) {
        currentValueNode = rootNode;
        currentValueCursor = 0;
        currentValueLength = rootNode.payloadArity();
      }
    }

    /*
     * search for next node that contains values
     */
    private boolean searchNextValueNode() {
      while (currentStackLevel >= 0) {
        final int currentCursorIndex = currentStackLevel * 2;
        final int currentLengthIndex = currentCursorIndex + 1;

        final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
        final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

        if (nodeCursor < nodeLength) {
          final AbstractMapNode<K, V> nextNode = nodes[currentStackLevel].getNode(nodeCursor);
          nodeCursorsAndLengths[currentCursorIndex]++;

          if (nextNode.hasNodes()) {
            /*
             * put node on next stack level for depth-first traversal
             */
            final int nextStackLevel = ++currentStackLevel;
            final int nextCursorIndex = nextStackLevel * 2;
            final int nextLengthIndex = nextCursorIndex + 1;

            nodes[nextStackLevel] = nextNode;
            nodeCursorsAndLengths[nextCursorIndex] = 0;
            nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
          }

          if (nextNode.hasPayload()) {
            /*
             * found next node that contains values
             */
            currentValueNode = nextNode;
            currentValueCursor = 0;
            currentValueLength = nextNode.payloadArity();
            return true;
          }
        } else {
          currentStackLevel--;
        }
      }

      return false;
    }

    public boolean hasNext() {
      if (currentValueCursor < currentValueLength) {
        return true;
      } else {
        return searchNextValueNode();
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  protected static class MapKeyIterator<K, V> extends AbstractMapIterator<K, V>
          implements Iterator<K> {

    MapKeyIterator(AbstractMapNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getKey(currentValueCursor++);
      }
    }

  }

  protected static class MapValueIterator<K, V> extends AbstractMapIterator<K, V>
          implements Iterator<V> {

    MapValueIterator(AbstractMapNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getValue(currentValueCursor++);
      }
    }

  }

  protected static class MapEntryIterator<K, V> extends AbstractMapIterator<K, V>
          implements Iterator<Map.Entry<K, V>> {

    MapEntryIterator(AbstractMapNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public Map.Entry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getKeyValueEntry(currentValueCursor++);
      }
    }

  }

  /**
   * Iterator that first iterates over inlined-values and then continues depth first recursively.
   */
  private static class TrieMapNodeIterator<K, V> implements Iterator<AbstractMapNode<K, V>> {

    final Deque<Iterator<? extends AbstractMapNode<K, V>>> nodeIteratorStack;

    TrieMapNodeIterator(AbstractMapNode<K, V> rootNode) {
      nodeIteratorStack = new ArrayDeque<>();
      nodeIteratorStack.push(Collections.singleton(rootNode).iterator());
    }

    @Override
    public boolean hasNext() {
      while (true) {
        if (nodeIteratorStack.isEmpty()) {
          return false;
        } else {
          if (nodeIteratorStack.peek().hasNext()) {
            return true;
          } else {
            nodeIteratorStack.pop();
            continue;
          }
        }
      }
    }

    @Override
    public AbstractMapNode<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      AbstractMapNode<K, V> innerNode = nodeIteratorStack.peek().next();

      if (innerNode.hasNodes()) {
        nodeIteratorStack.push(innerNode.nodeIterator());
      }

      return innerNode;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  static class ResultValue<O> {
    private final @Nullable O value;
    private final boolean exists;

    public ResultValue(@Nullable O value, boolean exists) {
      this.value = value;
      this.exists = exists;
    }

    public @Nullable O getValue() {
      return value;
    }

    public @Nullable O get() {
      if (!exists) {
        throw new NoSuchElementException();
      }
      return value;
    }

    public boolean exists() {
      return exists;
    }

    public @Nullable O orElse(@Nullable O elseValue) {
      return exists ? value : elseValue;
    }

    public static <OO> ResultValue<OO> of(@Nullable OO value) {
      return new ResultValue<>(value, true);
    }

    public static <OO> ResultValue<OO> empty() {
      return new ResultValue<>(null, false);
    }
  }

  static final class TransientTrieMap<K, V> {

    final private AtomicReference<Thread> mutator;
    private AbstractMapNode<K, V> root;
    private int hashCode;
    private int size;

    TransientTrieMap(PersistentTrieMap<K, V> trieMap) {
      this.mutator = new AtomicReference<Thread>(Thread.currentThread());
      this.root = trieMap.root;
      this.hashCode = trieMap.hashCode;
      this.size = trieMap.size;
    }

    public @NonNull PersistentTrieMap.ResultValue<V> __put(final K key, final V val) {
      if (mutator.get() == null) {
        throw new IllegalStateException("Transient already frozen.");
      }

      final int keyHash = key.hashCode();
      final MapNodeResult<K, V> details = MapNodeResult.unchanged();

      final AbstractMapNode<K, V> newRootNode =
              root.updated(mutator, key, val, keyHash, 0, details);

      if (details.isModified()) {
        if (details.hasReplacedValue()) {
          final V old = details.getReplacedValue();

          final int valHashOld = old.hashCode();
          final int valHashNew = val.hashCode();

          root = newRootNode;
          hashCode = hashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);

          return ResultValue.of(details.getReplacedValue());
        } else {
          final int valHashNew = val.hashCode();
          root = newRootNode;
          hashCode += (keyHash ^ valHashNew);
          size += 1;

          return ResultValue.empty();
        }
      }

      return null;
    }

    public boolean __putAll(final Map<? extends K, ? extends V> map) {
      boolean modified = false;

      for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        ResultValue<V> optional = this.__put(entry.getKey(), entry.getValue());
        final boolean isPresent = optional.exists();
        final V replaced = optional.orElse(null);

        if (!isPresent || replaced != null) {
          modified = true;
        }
      }

      return modified;
    }

    public V __remove(final K key) {
      if (mutator.get() == null) {
        throw new IllegalStateException("Transient already frozen.");
      }

      final int keyHash = key.hashCode();
      final MapNodeResult<K, V> details = MapNodeResult.unchanged();

      final AbstractMapNode<K, V> newRootNode = root.removed(mutator, key,
              keyHash, 0, details);

      if (details.isModified()) {
        assert details.hasReplacedValue();
        final int valHash = details.getReplacedValue().hashCode();

        root = newRootNode;
        hashCode = hashCode - (keyHash ^ valHash);
        size = size - 1;

        return details.getReplacedValue();
      }

      return null;
    }

    public PersistentTrieMap<K, V> freeze() {
      if (mutator.get() == null) {
        throw new IllegalStateException("Transient already frozen.");
      }

      mutator.set(null);
      return new PersistentTrieMap<K, V>(root, hashCode, size);
    }
  }

  interface MapNode<K, V, R extends MapNode<K, V, R>> extends Node {


    ResultValue<V> findByKey(final K key, final int keyHash, final int shift);

    R updated(final AtomicReference<Thread> mutator, final K key,
              final V val, final int keyHash, final int shift, final MapNodeResult<K, V> details);

    R removed(final AtomicReference<Thread> mutator, final K key,
              final int keyHash, final int shift, final MapNodeResult<K, V> details);

    // TODO: move to {@code Node} interface
    boolean equivalent(final Object other);

  }

  interface Node {

    byte SIZE_EMPTY = 0b00;
    byte SIZE_ONE = 0b01;
    byte SIZE_MORE_THAN_ONE = 0b10;

    /**
     * Abstract predicate over a node's size. Value can be either {@value #SIZE_EMPTY},
     * {@value #SIZE_ONE}, or {@value #SIZE_MORE_THAN_ONE}.
     *
     * @return size predicate
     */
    byte sizePredicate();


  }

  public static <K, V> Map.Entry<K, V> entryOf(final K key, final V val) {
    return new AbstractMap.SimpleImmutableEntry<K, V>(key, val);
  }

  private static class MapNodeResult<K, V> {

    private V replacedValue;
    private boolean isModified;
    private boolean isReplaced;

    // update: inserted/removed single element, element count changed
    public void modified() {
      this.isModified = true;
    }

    public void updated(V replacedValue) {
      this.replacedValue = replacedValue;
      this.isModified = true;
      this.isReplaced = true;
    }

    // update: neither element, nor element count changed
    public static <K, V> MapNodeResult<K, V> unchanged() {
      return new MapNodeResult<>();
    }

    private MapNodeResult() {
    }

    public boolean isModified() {
      return isModified;
    }

    public boolean hasReplacedValue() {
      return isReplaced;
    }

    public V getReplacedValue() {
      return replacedValue;
    }
  }
}
