/*
 * @(#)SeqTrieMapGuavaTests.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.guava;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jhotdraw8.collection.SeqChampMap;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Tests TrieMap with the Guava test suite.
 */
public class SeqChampMapGuavaTests {
    public static Test suite() {
        return new SeqChampMapGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite("SequencedTrieMap");
        suite.addTest(testsForLinkedTrieMap());
        return suite;
    }

    public Test testsForLinkedTrieMap() {
        return MapTestSuiteBuilder.using(
                        new TestStringMapGenerator() {
                            @Override
                            protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                                return new SeqChampMap<String, String>(Arrays.asList(entries));
                            }
                        })
                .named("SequencedTrieMap")
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


}
