package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.StackExchangeClient;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.StackExchangeList;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagWiki;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagDefinitionLoader {
	@ConfigProperty(name = "tech-lab-ingester.indicators.stackexchange.tags.per.page", defaultValue = "100")
	int tagsPerPage;
	@RestClient StackExchangeClient stackExchange;

	public List<TagDefinition> loadTagDefinitionsFrom(String site) {
		long count = stackExchange.getTagsCount(site);
		Log.infof("📏 There are %d tags to get from %s. Downloading them all", count, site);
		List<TagDefinition> loaded = new ArrayList<TagDefinition>();
		StackExchangeList<TagDefinition> result;
		// Strangely, StackExchange makes page start at 1
		int page = 1;
		do {
			result = stackExchange.getTags(site, page++, tagsPerPage);
			loaded.addAll(result.items());
			if(loaded.size()%1000==0) {
				Log.infof("Loaded %d/%d tags", loaded.size(), count);
			}
		} while(result.hasMore());
		return loaded;
	}

	public TagWiki loadWikiInfos(String site, TagDefinition tagdefinition) {
		Log.infof("Fetching wiki page for %s", tagdefinition);
		StackExchangeList<TagWiki> returned = stackExchange.getTagsWikis(site, tagdefinition.name(), 1, 1);
		return returned.items().getFirst();
	}

}
