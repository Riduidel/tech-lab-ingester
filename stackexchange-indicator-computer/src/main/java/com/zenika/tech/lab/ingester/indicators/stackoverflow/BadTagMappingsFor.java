package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnology;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.model.Technology;

public class BadTagMappingsFor extends RuntimeException {

	private KnownTechnology known;
	private Set<Tag> tags;

	public BadTagMappingsFor(KnownTechnology known, Set<Tag> linkedTags) {
		this.known = known;
		this.tags = linkedTags;
	}

	public String toLog() {
		if(tags.isEmpty()) {
			return String.format("Found no tags for %s.", known.id.technology, tags);
		} else {
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
		Map<String, Object> parent = returned;
		List<String> fragments = new ArrayList<>(Arrays.asList(path.split("\\.")));
		fragments.add(technology.name);
		ListIterator<String> iterator = fragments.listIterator();
		while (iterator.hasNext()) {
			String p = (String) iterator.next();
			current.put(p, new HashMap<>());
			parent = current;
			current = (Map<String, Object>) current.get(p);
			
		}
		if(tags.isEmpty()) {
			parent.put(parent.keySet().iterator().next(), Collections.emptyList());
			return returned;
		} else {
			// Now we're at the terminal node, so let's do some mapping
			current.put("select the correct mappings in this list ", tags.stream()
					.map(t -> String.format("%s:%s", t.site, t.name))
					.collect(Collectors.toList()));
			return returned;
		}
	}
	
	public String getFileName() {
		return String.format("%s___%s.yaml", 
				known.getTechnologyPlatform(),
				known.getTechnologyName().replaceAll("[/@]", "_"));
	}
}
