package com.job.interview.blog.utils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

public abstract class SortUtil {
    public static <T> Collection<T> sortByCreatedAt(Collection<T> unsorted) {
        Set<T> sortedSet = new TreeSet<>(Comparator.comparing(SortUtil::extractCreatedAt, Collections.reverseOrder()));
        sortedSet.addAll(unsorted);
        return sortedSet;
    }

    private static <T> LocalDateTime extractCreatedAt(T obj) {
        try {
            Method method = obj.getClass().getMethod("getCreatedAt");
            return (LocalDateTime) method.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException("Class " + obj.getClass().getName() + " must have a getCreatedAt() method returning LocalDateTime.", e);
        }
    }
}