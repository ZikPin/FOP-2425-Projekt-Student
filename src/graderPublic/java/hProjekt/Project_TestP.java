package hProjekt;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertNotNull;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertTrue;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.emptyContext;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions4.assertIsNotRecursively;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;
import org.sourcegrade.jagr.api.testing.extension.TestCycleResolver;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.reflections.BasicMethodLink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import hProjekt.mocking.ReflectionUtilsP;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtWhile;

public class Project_TestP {

    public static <T> void assertContainsAll(List<T> expected, List<T> actual, Context context) {
        assertContainsAll(expected, actual, ReflectionUtilsP::equalsForMocks, context);
    }

    public static <T> void assertContainsAll(List<T> expected, List<T> actual, BiPredicate<T, T> matcher,
            Context context) {
        assertNotNull(actual, context, r -> "The returned List is null and should not have been");

        Context contextComplete = contextBuilder()
                .add(context)
                .add("actual", actual)
                .add("expected", expected)
                .build();

        assertEquals(expected.size(), actual.size(), contextComplete,
                r -> "List does not contain same amount of items.");

        for (int i = 0; i < expected.size(); i++) {
            int finalI = i;

            assertTrue(
                    expected.stream().anyMatch(
                            exp -> {
                                if (exp == null) {
                                    return actual.get(finalI) == null;
                                }
                                return matcher.test(exp, actual.get(finalI));
                            }),
                    contextComplete,
                    r -> "Actual List does not contain all expected Elements. Actual "
                            + finalI
                            + " was not found in expected elements!");
        }
    }

    public static <T> void assertSetEquals(Set<T> expected, Set<T> actual, Context context) {
        assertSetEquals(expected, actual, ReflectionUtilsP::equalsForMocks, context);
    }

    public static <T> void assertSetEquals(Set<T> expected, Set<T> actual, BiPredicate<T, T> matcher, Context context) {
        assertNotNull(actual, context, r -> "The returned Set is null and should not have been");

        Context contextComplete = contextBuilder()
                .add(context)
                .add("actual", actual)
                .add("expected", expected)
                .build();

        assertEquals(expected.size(), actual.size(), contextComplete,
                r -> "Set does not contain same amount of items.");

        List<T> actualList = new ArrayList<>(actual);

        for (int i = 0; i < expected.size(); i++) {
            int finalI = i;

            assertTrue(
                    expected.stream().anyMatch(
                            exp -> {
                                if (exp == null) {
                                    return actualList.get(finalI) == null;
                                }
                                return matcher.test(exp, actualList.get(finalI));
                            }),
                    contextComplete,
                    r -> "Actual Set does not contain all expected Elements. Actual "
                            + finalI
                            + " was not found in expected elements!");
        }
    }

    public static <K, V> void assertContainsAll(Map<K, V> expected, Map<K, V> actual, Context context) {
        assertContainsAll(expected, actual, ReflectionUtilsP::equalsForMocks, ReflectionUtilsP::equalsForMocks,
                context);
    }

    public static <K, V> void assertContainsAll(Map<K, V> expected, Map<K, V> actual, BiPredicate<K, K> keyMatcher,
            BiPredicate<V, V> valueMatcher, Context context) {
        assertNotNull(actual, context, r -> "The returned Map is null and should not have been");

        Context contextComplete = contextBuilder()
                .add(context)
                .add("actual", actual)
                .add("expected", expected)
                .build();

        assertEquals(expected.size(), actual.size(), contextComplete,
                r -> "Map does not contain same amount of items.");

        List<Map.Entry<K, V>> actualEntrys = new ArrayList<>(actual.entrySet());
        for (int i = 0; i < expected.size(); i++) {
            int finalI = i;

            Map.Entry<K, V> actualEntry = actualEntrys.get(i);

            assertTrue(
                    expected.entrySet().stream().anyMatch(
                            e -> {
                                K key = e.getKey();
                                V value = e.getValue();

                                if (!keyMatcher.test(key, actualEntry.getKey())/* key.equals(actualEntry.getKey()) */) {
                                    return false;
                                }
                                if (value == null) {
                                    return actualEntry.getValue() == null;
                                } else {
                                    return valueMatcher.test(value, actualEntry.getValue());// value.equals(actualEntry.getValue());
                                }
                            }),
                    contextComplete,
                    r -> "Actual Map does not contain all expected Elements. Actual " + finalI + " not found!");
        }
    }

    public static <T> void assertListEquals(List<T> expected, List<T> actual, Context context) {
        assertListEquals(expected, actual, ReflectionUtilsP::equalsForMocks, context);
    }

    public static <T> void assertListEquals(List<T> expected, List<T> actual, BiPredicate<T, T> matcher,
            Context context) {
        assertNotNull(actual, context, r -> "The returned List is null and should not have been");

        Context contextComplete = contextBuilder()
                .add(context)
                .add("actual", actual)
                .add("expected", expected)
                .build();

        assertEquals(expected.size(), actual.size(), contextComplete,
                r -> "List does not contain same amount of items.");

        for (int i = 0; i < expected.size(); i++) {
            int finalI = i;

            boolean equals;
            if (expected.get(i) == null) {
                equals = actual.get(finalI) == null;
            } else {
                equals = matcher.test(expected.get(i), actual.get(i));
            }

            assertTrue(
                    equals,
                    contextComplete,
                    r -> "Actual List does not match expected Elements. Actual at " + finalI
                            + " is not the same as expected!");
        }
    }

    public static <T> void assertEqualsWithMatcher(T expected, T actual, BiPredicate<T, T> matcher, Context context) {
        Context contextComplete = contextBuilder()
                .add(context)
                .add("actual", actual)
                .add("expected", expected)
                .build();

        assertTrue(matcher.test(expected, actual), contextComplete, r -> "Actual Object does not match expected.");
    }

    public static void assertNoLoopOrRecursion(Method methodToCheck) {
        assertIsNotRecursively(
                BasicMethodLink.of(methodToCheck).getCtElement(),
                emptyContext(),
                r -> "Method %s uses recursion.".formatted(methodToCheck.getName()));
        if (BasicMethodLink.of(methodToCheck).getCtElement().getElements(e -> e instanceof CtFor
                || e instanceof CtForEach
                || e instanceof CtWhile
                || e instanceof CtDo).isEmpty()) {
            return;
        }
        Assertions2.fail(
                emptyContext(),
                r -> "Method %s uses loops.".formatted(methodToCheck.getName()));
    }

    public static Stream<Arguments> parseJsonFile(String filename) {
        final ArrayNode rootNode;
        if (TestCycleResolver.getTestCycle() != null) {

            URL url = Project_TestP.class.getResource(
                    filename.replaceFirst(ReflectionUtilsP.getExercisePrefix(Project_TestP.class) + "/", ""));
            Assertions.assertNotNull(url, "Could not find JSON file: " + filename);
            try {
                rootNode = (ArrayNode) new ObjectMapper().readTree(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                rootNode = (ArrayNode) new ObjectMapper()
                        .readTree(Files.readString(Path.of("src/graderPublic/resources/" + filename)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Streams.of(rootNode).map(Arguments::of);

    }
}
