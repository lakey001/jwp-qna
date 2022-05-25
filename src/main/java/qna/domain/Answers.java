package qna.domain;

import qna.CannotDeleteException;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Embeddable
public class Answers {
    @OneToMany(mappedBy = "question")
    List<Answer> answers = new ArrayList<>();

    protected Answers() {
    }

    public Answers(List<Answer> answers){
        this.answers = answers;
    }

    public void deleteAnswers(User loginUser){
        for (Answer answer : answers) {
            answer.delete(loginUser);
        }
    }

    public void add(Answer answer) {
        answers.add(answer);
    }

    public List<Answer> getAnswers(){
        return Collections.unmodifiableList(answers);
    }
}
