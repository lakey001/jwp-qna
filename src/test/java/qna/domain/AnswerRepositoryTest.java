package qna.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class AnswerRepositoryTest {
    @Autowired
    AnswerRepository answerRepository;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    UserRepository userRepository;

    private Question question;
    private User user;

    @BeforeEach
    void initialize(){
        user = userRepository.save(UserTest.JAVAJIGI);
        question = questionRepository.save(new Question("title1", "contents1").writeBy(user));
    }

    @Test
    @DisplayName("Answer 저장")
    void save(){
        Answer saved = answerRepository.save(new Answer(user, question, "Answers Contents1"));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Answer 조회: by QuestionId, DeletedFalse")
    void Answer_조회_by_QuestionId_DeletedFalse(){
        answerRepository.save(AnswerTest.generateAnswer(user, question, true));
        Answer answerDeletedFalse = answerRepository.save(AnswerTest.generateAnswer(user, question, false));
        List<Answer> answers = answerRepository.findByQuestionIdAndDeletedFalse(question.getId());
        assertThat(answers).containsExactly(answerDeletedFalse);
    }

    @ParameterizedTest(name = "삭제되지 않은 Answer 조회: deleted = {0}, 조회값 존재 = {1}")
    @DisplayName("Answer 조회: by Id, DeletedFalse")
    @MethodSource("provideBooleansForAnswerFind")
    void Answer_조회_by_Id_DeletedFalse(boolean deleted, boolean resultPresent){
        Answer answer = answerRepository.save(AnswerTest.generateAnswer(user, question, deleted));
        assertThat(answerRepository.findByIdAndDeletedFalse(answer.getId()).isPresent()).isEqualTo(resultPresent);
    }

    private static Stream<Arguments> provideBooleansForAnswerFind() {
        return Stream.of(
                Arguments.of(true, false),
                Arguments.of(false, true)
        );
    }
}
