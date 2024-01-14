/*
 * @(#)ChampMapGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.guava;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jhotdraw8.icollection.SimpleMutableMap;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Tests {@link SimpleMutableMap} with the Guava test suite.
 */
public class MutableSimpleMapGuavaTests {

    public static Test suite() {
        return new MutableSimpleMapGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite(SimpleMutableMap.class.getSimpleName());
        suite.addTest(testsForTrieMap());
        return suite;
    }

    public Test testsForTrieMap() {
        return MapTestSuiteBuilder.using(
                        new TestStringMapGenerator() {
                            @Override
                            protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                                return new SimpleMutableMap<>(Arrays.asList(entries));
                            }
                        })
                .named(SimpleMutableMap.class.getSimpleName())
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


}
