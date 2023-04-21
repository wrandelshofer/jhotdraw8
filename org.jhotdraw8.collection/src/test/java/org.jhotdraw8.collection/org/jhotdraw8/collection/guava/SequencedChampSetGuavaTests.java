/*
 * @(#)ChampSetGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.guava;

import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jhotdraw8.collection.champ.SequencedChampSet;
import org.jhotdraw8.collection.facade.MutableSetFacade;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Tests {@link SequencedChampSet} with the Guava test suite.
 */

public class SequencedChampSetGuavaTests {

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
                                return new MutableSetFacade<>(
                                        SequencedChampSet.<String>copyOf(MinimalCollection.<String>of(elements)));
                            }
                        })
                .named(SequencedChampSet.class.getSimpleName())
                .withFeatures(
                        CollectionFeature.KNOWN_ORDER,
                        SetFeature.GENERAL_PURPOSE,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        //CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .suppressing(suppressForTrieSet())
                .createTestSuite();
    }

    protected Collection<Method> suppressForTrieSet() {
        return Collections.emptySet();
    }

}
