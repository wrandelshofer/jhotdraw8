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
import org.jhotdraw8.icollection.SimpleMutableNavigableSet;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * Tests {@link SimpleMutableNavigableSet} with the Guava test suite.
 */
public class SimpleMutableNavigableSetGuavaTests {

    public static Test suite() {
        return new SimpleMutableNavigableSetGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(SimpleMutableNavigableSet.class.getSimpleName());
        suite.addTest(testsForSet());
        suite.addTest(testsForReversedSet());
        return suite;
    }

    public Test testsForSet() {
        return SetTestSuiteBuilder.using(
                        new TestStringSetGenerator() {
                            @Override
                            public Set<String> create(String[] elements) {
                                return new SimpleMutableNavigableSet<>(MinimalCollection.of(elements));
                            }
                        })
                .named(SimpleMutableNavigableSet.class.getSimpleName())
                .withFeatures(
                        SetFeature.GENERAL_PURPOSE,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.SUBSET_VIEW,
                        //CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
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
                                return new SimpleMutableNavigableSet<>(MinimalCollection.of(elements)).reversed();
                            }
                        })
                .named(SimpleMutableNavigableSet.class.getSimpleName() + "Reversed")
                .withFeatures(
                        SetFeature.GENERAL_PURPOSE,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.SUBSET_VIEW,
                        //CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        //CollectionFeature.SERIALIZABLE,
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
