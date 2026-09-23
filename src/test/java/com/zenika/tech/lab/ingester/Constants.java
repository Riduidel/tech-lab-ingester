package com.zenika.tech.lab.ingester;

import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyBuilder;

public interface Constants {

	public static interface Technologies {
		Technology react = TechnologyBuilder.technology()
//				.id(2l)
				.name("react")
				.platform("NPM")
				.description("React is a JavaScript library for building user interfaces.")
				.homepage("https://react.dev/")
				.packageManagerUrl("https://www.npmjs.com/package/react")
				.repositoryUrl("https://github.com/facebook/react")
				.build();
		Technology jest = TechnologyBuilder.technology()
				.name("jest")
				.platform("NPM")
				.description("Delightful JavaScript Testing.")
				.homepage("https://jestjs.io/")
				.packageManagerUrl("https://www.npmjs.com/package/jest")
				.repositoryUrl("https://github.com/jestjs/jest")
				.build();
		Technology next = TechnologyBuilder.technology()
				.name("next")
				.platform("NPM")
				.homepage("https://nextjs.org")
				.packageManagerUrl("https://www.npmjs.com/package/nextjs")
				.repositoryUrl("https://github.com/vercel/nextjs.js")
				.build();
		Technology clipboard = TechnologyBuilder.technology()
				.name("clipboard")
				.platform("NPM")
				.homepage("https://clipboardjs.com")
				.packageManagerUrl("https://www.npmjs.com/package/clipboard")
				.repositoryUrl("https://github.com/zenorocha/clipboard.js")
				.build();
		Technology flambo = TechnologyBuilder.technology()
				.name("yieldbot/flambo")
				.platform("Clojars")
				.packageManagerUrl("https://clojars.org/yieldbot/flambo")
				.repositoryUrl("https://github.com/yieldbot/flambo")
				.build();
		Technology enzyme = TechnologyBuilder.technology()
				.name("enzyme")
				.platform("NPM")
				.homepage("https://airbnb.io/enzyme/")
				.packageManagerUrl("https://www.npmjs.com/package/enzyme")
				.repositoryUrl("https://github.com/enzymejs/enzyme/")
				.build();
		Technology vue = TechnologyBuilder.technology()
				.name("vue")
//				.id(17l)
				.platform("NPM")
				.homepage("https://github.com/vuejs/core/tree/main/packages/vue#readme")
				.packageManagerUrl("https://www.npmjs.com/package/vue")
				.repositoryUrl("https://github.com/vuejs/core")
				.build()
				;
		Technology angular = TechnologyBuilder.technology()
				.name("@angular/core")
//				.id(2410l)
				.platform("NPM")
				.homepage("https://github.com/angular/angular#readme")
				.packageManagerUrl("https://www.npmjs.com/package/@angular/core")
				.repositoryUrl("https://github.com/angular/angular")
				.build()
				;
	}
}
