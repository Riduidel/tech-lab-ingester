package com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.PlatformNameGenerator;

public abstract class HierarchicalPlatformNameGenerator implements PlatformNameGenerator {

	private SimplePlatformNameGenerator delegate;

	private HierarchicalConfiguration configuration;

	public HierarchicalPlatformNameGenerator(HierarchicalConfiguration configuration) {
		this.configuration = configuration;
		this.delegate = new SimplePlatformNameGenerator(configuration.name());
	}


	@Override
	public Collection<String> transform(String technologyName) {
		List<String> separators = configuration.separators();
		List<String> returned = new ArrayList<String>();
		returned.addAll(
			separators.stream()
				.filter(s -> technologyName.contains(s))
				.filter(s -> technologyName.indexOf(s)<technologyName.length())
				.map(s -> extractLastFragmentOf(technologyName, s))
				.flatMap(name -> transformNameFragment(Arrays.asList(name)).stream())
				.collect(Collectors.toList()))
				;
		if(configuration.supportOptionalSeparator())
			returned.addAll(transformNameFragment(Arrays.asList(technologyName)));
		return returned;
	}
	
	protected String extractLastFragmentOf(String name, String separator) {
		String returned = name.substring(name.indexOf(separator)+1);
		return returned;
	}
	
	protected Collection<String> transformNameFragment(List<String> names) {
		return names.stream()
				.map(delegate::transform)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> new TreeSet<String>()));
	}

}
