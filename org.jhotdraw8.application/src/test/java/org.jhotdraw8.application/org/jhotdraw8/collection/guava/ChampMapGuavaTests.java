/*
 * @(#)ChampMapGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.guava;

import org.jhotdraw8.collection.champ.ChampMap;
import org.junit.jupiter.api.Disabled;

/**
 * Tests {@link ChampMap} with the Guava test suite.
 */
@Disabled
public class ChampMapGuavaTests {
    /*
    public static Test suite() {
        return new ChampMapGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(ChampMap.class.getSimpleName());
        suite.addTest(testsForTrieMap());
        return suite;
    }

    public Test testsForTrieMap() {
        return MapTestSuiteBuilder.using(
                        new TestStringMapGenerator() {
                            @Override
                            protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                                return new ChampMap<String, String>(Arrays.asList(entries));
                            }
                        })
                .named(ChampMap.class.getSimpleName())
                .withFeatures(
                        MapFeature.GENERAL_PURPOSE,
                        MapFeature.ALLOWS_NULL_KEYS,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.ALLOWS_ANY_NULL_QUERIES,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
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
