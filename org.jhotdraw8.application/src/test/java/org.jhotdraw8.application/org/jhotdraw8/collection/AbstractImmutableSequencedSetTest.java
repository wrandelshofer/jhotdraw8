/*
 * @(#)AbstractPersistentSequencedSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

@Deprecated
public abstract class AbstractImmutableSequencedSetTest extends AbstractImmutableSetTest {
    /*
    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public void doTestIterationSequence(int mask, int... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Add all in order
        List<HashCollider> list = new ArrayList<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            list.add(e1);
        }

        ImmutableSet<HashCollider> instance = copyOf(list);
        assertEquals(list, new ArrayList<>(instance.asCollection()));

        // Remove one element in the middle
        HashCollider middle = list.get(list.size() / 2);
        list.remove(list.size() / 2);
        instance = instance.copyRemove(middle);
        assertEquals(list, new ArrayList<>(instance.asSet()));

        // Add the removed element
        list.add(middle);
        instance = instance.copyAdd(middle);
        assertEquals(list, new ArrayList<>(instance.asCollection()));

        // Get another element from the middle
        // Add the element from the middle - this must not reorder the instance,
        // because the element is already present
        middle = list.get(list.size() / 2);
        instance = instance.copyAdd(middle);
        assertEquals(list, new ArrayList<>(instance.asCollection()));
    }

*/
}
