package demo.com.example.myJavaApp.question;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business layer. The controller talks to this, never to the repository
 * directly, so transaction boundaries and rules live in one place.
 */
@Service
public class QuestionAnswerService {

    private final QuestionAnswerRepository repository;

    QuestionAnswerService(QuestionAnswerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<QuestionAnswer> findAll() {
        return repository.findAll(Sort.by("id"));
    }
}
