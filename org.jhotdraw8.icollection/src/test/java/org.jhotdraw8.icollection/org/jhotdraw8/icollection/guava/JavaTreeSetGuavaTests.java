/*
 * @(#)SeqChampSetGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.guava;

import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import com.google.common.collect.testing.testers.CollectionSpliteratorTester;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * Tests {@link java.util.TreeSet} with the Guava test suite.
 */
public class JavaTreeSetGuavaTests {

    public static Test suite() {
        return new JavaTreeSetGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(java.util.TreeSet.class.getSimpleName());
        suite.addTest(testsForSet());
        suite.addTest(testsForReversedSet());
        return suite;
    }

    public Test testsForSet() {
        return SetTestSuiteBuilder.using(
                        new TestStringSetGenerator() {
                            @Override
                            public Set<String> create(String[] elements) {
                                return new java.util.TreeSet<>(MinimalCollection.of(elements));
                            }
                        })
                .named(java.util.TreeSet.class.getSimpleName())
                .withFeatures(
                        SetFeature.GENERAL_PURPOSE,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        //CollectionFeature.KNOWN_ORDER,
                        //CollectionFeature.ALLOWS_NULL_VALUES,
                        //CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .suppressing(suppressForSet())
                .createTestSuite();
    }

    public Test testsForReversedSet() {
        return SetTestSuiteBuilder.using(
                        new TestStringSetGenerator() {
                            @Override
                            public Set<String> create(String[] elements) {
                                return new java.util.TreeSet<>(MinimalCollection.of(elements)).reversed();
                            }
                        })
                .named(java.util.TreeSet.class.getSimpleName() + "Reversed")
                .withFeatures(
                        SetFeature.GENERAL_PURPOSE,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.REMOVE_OPERATIONS,
                        //CollectionFeature.KNOWN_ORDER,
                        //CollectionFeature.ALLOWS_NULL_VALUES,
                        //CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionFeature.GENERAL_PURPOSE,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .suppressing(suppressForSet())
                .createTestSuite();
    }

    protected Collection<Method> suppressForSet() {
        try {
            return Set.of(
                    CollectionSpliteratorTester.class.getMethod("testSpliteratorUnknownOrder")
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
