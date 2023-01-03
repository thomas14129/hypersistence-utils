package io.hypersistence.utils.hibernate.type.util.dto;

/**
 * @author Vlad Mihalcea
 */
public class PostDTO {

	private Long id;

	private String title;

	private PostStatus status;

	public PostDTO(Number id, String title, PostStatus status) {
		this.id = id.longValue();
		this.title = title;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public PostStatus getStatus() {
		return status;
	}
}
