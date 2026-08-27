package demo.com.example.myJavaApp.question;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the question_answer table created by V1__create_question.sql.
 * Flyway owns the schema; this class only mirrors it (ddl-auto is "validate").
 */
@Entity
@Table(name = "question_answer")
public class QuestionAnswer {

    /** BIGSERIAL -> the database generates the id on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    /** Filled by the column's DEFAULT now(), so never written from Java. */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** JPA requires a no-arg constructor. */
    protected QuestionAnswer() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
