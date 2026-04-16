package com.zenika.tech.lab.ingester.indicators.stackoverflow.dump;

import java.sql.Date;

import org.jilt.Builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="posts")
@Builder
public class Post {
	public static enum PostType {
		QUESTION, ANSWER
	}
	@Id @GeneratedValue long id;
	@Column(name = "PostTypeId")
	PostType type;
	@ManyToOne
	@JoinColumn(name = "parentId")
	Post parent;
	@OneToOne
	@JoinColumn(name = "AcceptedAnswerId")
	Post acceptedAnswer;
	Date creationDate;
	int score;
	String body;
//	@OneToOne
//	String ownerUserId;
	String title;
	@Column(name="Tags")
	String tasgIds;
	
	public Post() {}

	public Post(long id, PostType type, Post parent, Post acceptedAnswer, Date creationDate, int score, String body,
			String title, String tasgIds) {
		super();
		this.id = id;
		this.type = type;
		this.parent = parent;
		this.acceptedAnswer = acceptedAnswer;
		this.creationDate = creationDate;
		this.score = score;
		this.body = body;
		this.title = title;
		this.tasgIds = tasgIds;
	}
}
