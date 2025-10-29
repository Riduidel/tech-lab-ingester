package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jilt.Builder;

import com.zenika.tech.lab.ingester.model.Technology;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** 
 * Links technologies to tags (and make sure we know if this technology is already known)
 */
@Entity
@Table(name="STACKOVERFLOW_KNOWN_TECHNOLOGY")
public class KnownTechnology extends PanacheEntityBase  {
	@Embeddable
	public static class KnownTechnologyId implements Serializable {
	    @ManyToOne
	    @JoinColumn(name = "TECHNOLOGY_ID",insertable = false, updatable = false, foreignKey = @ForeignKey(name="fk_technology_id"))
		public Technology technology;

		@Override
		public String toString() {
			return "KnownTechnologyId [" + (technology != null ? "technology=" + technology : "") + "]";
		}
	}
	
	@EmbeddedId
	public KnownTechnologyId id;
	
	public boolean known;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(
			name = "STACKOVERFLOW_TECHNOLOGY_TO_TAG",
			joinColumns = @JoinColumn(name="TECHNOLOGY_ID"),
			/**
			 * Beware, because there is an hidden unicity constraint here : 
			 * one StackExchange tag cannot be used on more than one technology
			 */
			inverseJoinColumns = @JoinColumn(name="TAG_ID", 
				foreignKey = @ForeignKey(foreignKeyDefinition = "STACKOVERFLOW_TECHNOLOGY_TO_TAG_PK"))
	)
	public Set<Tag> linkedTags = new LinkedHashSet<Tag>();
	
	public KnownTechnology() {}
	
	@Builder
	public KnownTechnology(Technology technology, boolean known) {
		super();
		this.id= new KnownTechnologyId();
		this.id.technology = technology;
		this.known = known;
	}

	@Override
	public String toString() {
		return "KnownTechnology [known=" + known + ", " + (id != null ? "\n\tid=" + id + ", " : "")
				+ (linkedTags != null ? "\n\tlinkedTags=" + linkedTags : "") + "]";
	}
}
