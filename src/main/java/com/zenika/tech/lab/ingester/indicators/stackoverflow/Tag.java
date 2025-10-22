package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.List;
import java.util.Objects;

import org.jilt.Builder;

import com.zenika.tech.lab.ingester.model.Technology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@Override
	public int hashCode() {
		return Objects.hash(id, name, site);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		return Objects.equals(id, other.id) && Objects.equals(name, other.name) && Objects.equals(site, other.site);
	}

	@Override
	public String toString() {
		return "Tag [" + (site != null ? "site=" + site + ", " : "") + (name != null ? "name=" + name + ", " : "")
				 + "]";
	}
}
