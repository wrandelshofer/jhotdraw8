/*
 * @(#)SeqChampMapGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.guava;

import org.jhotdraw8.collection.champ.SequencedChampMap;
import org.junit.jupiter.api.Disabled;

/**
 * Tests {@link SequencedChampMap} with the Guava test suite.
 */
@Disabled
public class SequencedChampMapGuavaTests {
    /*
    public static Test suite() {
        return new SequencedChampMapGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(SequencedChampMap.class.getSimpleName());
        suite.addTest(testsForLinkedTrieMap());
        return suite;
    }

    public Test testsForLinkedTrieMap() {
        return MapTestSuiteBuilder.using(
                        new TestStringMapGenerator() {
                            @Override
                            protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                                return new SequencedChampMap<String, String>(Arrays.asList(entries));
                            }
                        })
                .named(SequencedChampMap.class.getSimpleName())
                .withFeatures(
                        MapFeature.GENERAL_PURPOSE,
                        MapFeature.ALLOWS_NULL_KEYS,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.ALLOWS_ANY_NULL_QUERIES,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SERIALIZABLE,
                        CollectionSize.ANY)
                .suppressing(suppressForRobinHoodHashMap())
                .createTestSuite();
    }

    protected Collection<Method> suppressForRobinHoodHashMap() {
        return Collections.emptySet();
    }
*/

}
