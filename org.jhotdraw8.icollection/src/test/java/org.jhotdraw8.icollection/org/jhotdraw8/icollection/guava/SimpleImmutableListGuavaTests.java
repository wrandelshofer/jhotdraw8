/*
 * @(#)SimpleImmutableSetGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.guava;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jhotdraw8.icollection.SimpleImmutableList;
import org.jhotdraw8.icollection.SimpleMutableList;
import org.jhotdraw8.icollection.facade.MutableListFacade;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Tests {@link SimpleImmutableList} with the Guava test suite.
 */

public class SimpleImmutableListGuavaTests {

    public static Test suite() {
        return new SimpleImmutableListGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(SimpleMutableList.class.getSimpleName());
        suite.addTest(testsForTrieList());
        return suite;
    }

    public Test testsForTrieList() {
        return ListTestSuiteBuilder.using(
                        new TestStringListGenerator() {
                            @Override
                            public List<String> create(String[] elements) {
                                return new MutableListFacade<>(SimpleImmutableList.copyOf(MinimalCollection.of(elements)));
                            }
                        })
                .named(SimpleMutableList.class.getSimpleName())
                .withFeatures(
                        ListFeature.GENERAL_PURPOSE,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        // CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .suppressing(suppressForTrieSet())
                .createTestSuite();
    }

    protected Collection<Method> suppressForTrieSet() {
        return Collections.emptySet();
    }

}
