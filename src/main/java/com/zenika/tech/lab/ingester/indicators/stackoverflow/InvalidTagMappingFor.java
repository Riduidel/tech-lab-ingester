package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.TechLabIngesterException;
import com.zenika.tech.lab.ingester.model.Technology;

public class InvalidTagMappingFor extends TechLabIngesterException {

	private KnownTechnology known;
	private Set<Tag> tags;

	public InvalidTagMappingFor(KnownTechnology known, Set<Tag> linkedTags) {
		this.known = known;
		this.tags = linkedTags;
	}

	public String toLog() {
		String detailedError = TagService.groupedBySite(tags).entrySet().stream()
				.filter(entry -> entry.getValue().size()>1)
				.map(entry -> String.format("Site %s has %d tags found: %s", entry.getKey(), entry.getValue().size(), entry.getValue()))
				.collect(Collectors.joining("\n - ", " - ", "\n"));
		return String.format("Found tags for %s.\n"
				+ "They're not coherent: more than one tag per StackExchange site was detected:\n"
				+ "We found:\n"
				+ "%s\n"
				+ "You'll have to set that by hand in configuration file", known.id.technology, tags, detailedError);
	}

	/**
	 * Create the yaml structure we want to see
	 */
	public Object toYaml() {
		Technology technology = known.id.technology;
		String path = String.format("%s.%s", 
		TagService.PREFIX, technology.platform);
		Map<String, Object> returned = new HashMap<>();
		Map<String, Object> current = returned;
		List<String> fragments = new ArrayList<>(Arrays.asList(path.split("\\.")));
		fragments.add(technology.name);
		ListIterator<String> iterator = fragments.listIterator();
		while (iterator.hasNext()) {
			String p = (String) iterator.next();
			current.put(p, new HashMap<>());
			current = (Map<String, Object>) current.get(p);
			
		}
		// Now we're at the terminal node, so let's do some mapping
		current.put("select the correct mappings in this list ", tags.stream()
				.map(t -> String.format("%s:%s", t.site, t.name))
				.collect(Collectors.toList()));
		return returned;
	}
}
