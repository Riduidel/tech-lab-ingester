package com.zenika.tech.lab.ingester.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public class MapUtils {

	/**
	 * Merge two maps recursively: when one map value is another map, 
	 * recursively calls the method on that second map
	 * @param first
	 * @param second
	 * @return
	 */
	public static  Map deepMerge(Map first, Map second) {
		return deepMerge(first, second, () -> new TreeMap());
	}

	private static  Map deepMerge(Map first, Map second, Supplier<Map> builder) {
		Map returned = builder.get();
		Set<Object> keys = new HashSet<>();
		keys.addAll(first.keySet());
		keys.addAll(second.keySet());
		for(Object k : keys) {
			Object firstValue = first.get(k);
			Object secondValue = second.get(k);
			if(firstValue==null) {
				returned.put(k, secondValue);
			} else if(secondValue==null) {
				returned.put(k, firstValue);
			} else {
				if(firstValue instanceof Map) {
					if(secondValue instanceof Map) {
						// Both values are maps
						returned.put(k, deepMerge((Map) firstValue, (Map) secondValue, builder));
					} else {
						// first value is a map, but second value is not
						throw new RuntimeException(String.format("Unable to merge values for key %s.\n"
							+ "first map value is %s\n"
							+ "second map value is %s\n"
							+ "We can't merge when values are not both maps.",
							k, firstValue, secondValue));
						
					}
				} else if(secondValue instanceof Map) {
					// second value is a map, but first value is not
					throw new RuntimeException(String.format("Unable to merge values for key %s.\n"
						+ "first map value is %s\n"
						+ "second map value is %s\n"
						+ "We can't merge when values are not both maps.",
						k, firstValue, secondValue));
				} else {
					// Both values are non null, and none is a map
					returned.put(k, firstValue==null ? secondValue : firstValue);
				}
			}
		}
		return returned;
	}

}
