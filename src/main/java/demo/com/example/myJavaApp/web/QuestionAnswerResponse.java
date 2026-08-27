package demo.com.example.myJavaApp.web;

import java.time.OffsetDateTime;

import demo.com.example.myJavaApp.question.QuestionAnswer;

/**
 * What the API returns. Kept separate from the entity so the table layout
 * and the JSON contract can change independently.
 */
public record QuestionAnswerResponse(Long id, String title, String body, OffsetDateTime createdAt) {

    public static QuestionAnswerResponse from(QuestionAnswer entity) {
        return new QuestionAnswerResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getBody(),
                entity.getCreatedAt());
    }
}
