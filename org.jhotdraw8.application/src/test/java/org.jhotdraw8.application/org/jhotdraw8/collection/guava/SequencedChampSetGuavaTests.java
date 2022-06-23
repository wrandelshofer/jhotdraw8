/*
 * @(#)SeqChampSetGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.guava;

import org.jhotdraw8.collection.champ.ChampSequencedSet;
import org.junit.jupiter.api.Disabled;

/**
 * Tests {@link ChampSequencedSet} with the Guava test suite.
 */
@Disabled
public class SequencedChampSetGuavaTests {
    /*
    public static Test suite() {
        return new SequencedChampSetGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(SequencedChampSet.class.getSimpleName());
        suite.addTest(testsForTrieSet());
        return suite;
    }

    public Test testsForTrieSet() {
        return SetTestSuiteBuilder.using(
                        new TestStringSetGenerator() {
                            @Override
                            public Set<String> create(String[] elements) {
                                return new SequencedChampSet<>(MinimalCollection.of(elements));
                            }
                        })
                .named(SequencedChampSet.class.getSimpleName())
                .withFeatures(
                        SetFeature.GENERAL_PURPOSE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .suppressing(suppressForTrieSet())
                .createTestSuite();
    }

    protected Collection<Method> suppressForTrieSet() {
        return Collections.emptySet();
    }

     */
}
