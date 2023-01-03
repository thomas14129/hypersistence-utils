package io.hypersistence.utils.hibernate.type.basic;

import io.hypersistence.utils.hibernate.util.AbstractPostgreSQLIntegrationTest;
import io.hypersistence.utils.hibernate.query.SQLExtractor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.type.CustomType;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLEnumTest extends AbstractPostgreSQLIntegrationTest {

    public static final CustomType POST_STATUS_TYPE = new CustomType(new PostgreSQLEnumType(PostStatus.class));

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
        };
    }

    public void init() {
        DataSource dataSource = newDataSource();
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try {
                    statement.executeUpdate(
                            "DROP TYPE post_status_info CASCADE"
                    );
                } catch (SQLException ignore) {
                }
                statement.executeUpdate(
                        "CREATE TYPE post_status_info AS ENUM ('PENDING', 'APPROVED', 'SPAM')"
                );
            }
        } catch (SQLException e) {
            fail(e.getMessage());
        }
        super.init();
    }

    @Before
    public void setUp() {
        doInJPA(entityManager -> {
            Post post = new Post();
            post.setId(1L);
            post.setTitle("High-Performance Java Persistence");
            post.setStatus(PostStatus.PENDING);
            entityManager.persist(post);
        });
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            Post post = entityManager.find(Post.class, 1L);
            assertEquals(PostStatus.PENDING, post.getStatus());
        });
    }
    
    @Test
    public void testCriteriaAPI() {
      doInJPA(entityManager -> {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<PostDto> criteria = builder.createQuery(PostDto.class);

        Root<Post> postComment = criteria.from(Post.class);

        criteria.select(builder.construct(PostDto.class, postComment.get("status")));

        TypedQuery<PostDto> criteriaQuery = entityManager.createQuery(criteria);

        List<PostDto> p = criteriaQuery.getResultList();

        assertFalse(p.isEmpty());

        String sql = SQLExtractor.from(criteriaQuery);

        assertNotNull(sql);

        LOGGER.info("The Criteria API query: [\n{}\n]\ngenerates the following SQL query: [\n{}\n]",
            criteriaQuery.unwrap(org.hibernate.query.Query.class).getQueryString(), sql);
      });
    }

    @Test
    public void testTypedParameterValue() {
        doInJPA(entityManager -> {
            entityManager.createQuery("SELECT a FROM Post a WHERE a.status = :paramValue", Post.class)
                   .setParameter("paramValue", new TypedParameterValue(POST_STATUS_TYPE, PostStatus.APPROVED))
                   .getResultList();
        });
    }

    public enum PostStatus {
        PENDING,
        APPROVED,
        SPAM;

        @Override
        public String toString() {
            return String.format("The %s enum is mapped to ordinal: %d", name(), ordinal());
        }
    }

    @Entity(name = "Post")
    @Table(name = "post")
    @TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
    public static class Post {

        @Id
        private Long id;

        private String title;

        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "post_status_info")
        @Type(type = "pgsql_enum")
        private PostStatus status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public PostStatus getStatus() {
            return status;
        }

        public void setStatus(PostStatus status) {
            this.status = status;
        }
    }
    
    public static class PostDto {

        private PostStatus status;

        public PostStatus getStatus() {
          return status;
        }

        public void setStatus(PostStatus status) {
          this.status = status;
        }

        public PostDto(PostStatus status) {
          super();
          this.status = status;
        }

        public PostDto(String status) {
          System.out.println("goo");
          this.status = PostStatus.valueOf(status);
        }

        public PostDto() {
          super();
        }

      }
}
