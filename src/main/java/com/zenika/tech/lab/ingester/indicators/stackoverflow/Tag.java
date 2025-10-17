package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.List;

import org.jilt.Builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="STACKOVERFLOW_TAG")
@Builder
public class Tag {
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TECHNOLOGY_ID_SEQ")
	public Long id;
	
	/**
	 * Site in stachexchange network
	 */
	public String site;

	/**
	 * TagDefinition reference name
	 */
	public String name;
	/**
	 * List of tags synonyms. I don't yet know how useful it will be
	 */
	public List<String> synonmyms;
	/**
	 * Full wiki page as html 
	 */
	@Column(columnDefinition = "TEXT")
	public String wiki;
	/**
	 * TagDefinition excerpt usually shown 
	 */
	@Column(columnDefinition = "TEXT")
	public String excerpt;
	
	public Tag() {}

	public Tag(Long id, String site, String name, List<String> synonmyms, String wiki, String excerpt) {
		super();
		this.id = id;
		this.site = site;
		this.name = name;
		this.synonmyms = synonmyms;
		this.wiki = wiki;
		this.excerpt = excerpt;
	}
}
