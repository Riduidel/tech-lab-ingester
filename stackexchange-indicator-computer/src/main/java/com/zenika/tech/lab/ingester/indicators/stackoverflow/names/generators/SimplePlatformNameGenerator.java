package com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.PlatformNameGenerator;

public class SimplePlatformNameGenerator implements PlatformNameGenerator {
	private NameConfiguration configuration;

	public SimplePlatformNameGenerator(NameConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Collection<String> transform(String technologyName) {
		List<String> names = Arrays.asList(technologyName);
		Collection<String> returned = new TreeSet<String>();
		returned.addAll(names);
		returned.addAll(processEndings(names, this::processPrefixesOf));
		returned.addAll(processEndings(names, this::processSuffixesOf));
		return returned;
	}
	private Collection<String> processEndings(Collection<String> names, Function<String,Collection<String>> processOneEnding) {
		return names.stream()
				.map(processOneEnding)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> new TreeSet<String>()));
	}


	private Collection<String> processPrefixesOf(String name) {
		return processEndingsOf(name, configuration.prefixes(), 
				(prefix, n) -> n.startsWith(prefix),
				(prefix, n) -> prefix.concat(n)
				);
	}

	private Collection<String> processSuffixesOf(String name) {
		return processEndingsOf(name, configuration.suffixes(), 
				(suffix, n) -> n.endsWith(suffix),
				(suffix, n) -> n.concat(suffix)
				);
	}
	
	private Collection<String> processEndingsOf(String name, 
			Collection<String> endings, 
			BiFunction<String, String, Boolean> hasExtremity,
			BiFunction<String, String, String> buildExtremity) {
		if(endings==null) {
			return Arrays.asList(name);
		} else {
			return endings.stream()
					.filter(p -> !hasExtremity.apply(p, name))
					.map(p -> buildExtremity.apply(p, name))
					.collect(Collectors.toCollection(() -> new TreeSet<String>()));
		}
	}

}
