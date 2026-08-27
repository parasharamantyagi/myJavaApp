package demo.com.example.myJavaApp.question;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data generates the implementation at startup — no code needed here
 * for the standard finders (findAll, findById, save, ...).
 */
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
}
