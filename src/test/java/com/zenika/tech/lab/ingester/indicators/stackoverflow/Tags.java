package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;

public interface Tags {
	public static interface StackOverflow {
		static Tag react = TagBuilder.tag()
				.site("stackoverflow")
				.name("reactjs")
				.synonmyms(Arrays.asList("react-component",
						"react.js",
						"react",
						"react-jsx"))
				.excerpt("React is a JavaScript library for building user interfaces. It uses a declarative, component-based paradigm and aims to be efficient and flexible.")
				.build();
		static Tag jest = TagBuilder.tag()
				.site("stackoverflow")
				.name("jestjs")
				.synonmyms(Arrays.asList("jest"))
				.excerpt("Jest is a JavaScript unit testing framework made by Facebook based on Jasmine and provides automated mock creation and a jsdom environment. It&#39;s often used for testing React components.")
				.build();
		static Tag nextjs = TagBuilder.tag()
				.site("stackoverflow")
				.name("next.js")
				.synonmyms(Arrays.asList("nextjs"))
				.excerpt("Next.js is a minimalistic framework for server-rendered React applications as well as statically exported React apps.")
				.build();
		static Tag clipboardjs = TagBuilder.tag()
				.site("stackoverflow")
				.name("clipboard.js")
				.synonmyms(Arrays.asList(""))
				.excerpt("Clipboard.js is a modern approach to copy text to clipboard. It doesn&#39;t depend on Flash. It has no dependencies. And it&#39;s just 2kb gzipped.")
				.build();
		static Tag flambo = TagBuilder.tag()
				.site("stackoverflow")
				.name("flambo")
				.synonmyms(Arrays.asList(""))
				.excerpt("TODO")
				.build();
		static Tag enzyme = TagBuilder.tag()
				.site("stackoverflow")
				.name("enzyme")
				.synonmyms(Arrays.asList(""))
				.excerpt("Unit test library for React. It is developed by Airbnb. It can be used with other JavaScript testing frameworks like Mocha, Jest, Karma, etc. ")
				.build();
		static Tag vue = TagBuilder.tag()
				.site("stackoverflow")
				.name("vue.js")
				.synonmyms(Arrays.asList("vue-js", "vue", "vuejs"))
				.excerpt("Vue.js is an open-source, progressive JavaScript framework for building user interfaces that aims to be incrementally adoptable. Vue.js is mainly used for front-end development and requires an intermediate level of HTML and CSS. Vue.js questions are highly version specific and should always be tagged with [vuejs2] or [vuejs3] in addition to this tag.")
				.build();
		static Tag angular = TagBuilder.tag()
				.site("stackoverflow")
				.name("angular")
				.synonmyms(Arrays.asList("angularjs2", "angular2", "angular4", "angular4.x"))
				.excerpt("Questions about Angular (not to be confused with AngularJS), the web framework from Google. Use this tag for Angular questions which are not specific to an individual version. For the older AngularJS (1.x) web framework, use the AngularJS tag.")
				.build();
		
	}
}