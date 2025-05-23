package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FakeQuestionRepository implements QuestionRepository
{
    private final AtomicLong autoGeneratedId = new AtomicLong(1);
    private final List<Question> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Question save(Question question)
    {
        if(question.getId() == null || question.getId() == 0){
            Question newQuestion = Question.builder()
                    .id(autoGeneratedId.getAndIncrement())
                    .title(question.getTitle())
                    .content(question.getContent())
                    .password(question.getPassword())
                    .answerCount(question.getAnswerCount())
                    .problem(question.getProblem())
                    .build();

            data.add(newQuestion);
            return newQuestion;
        } else{
            data.removeIf(item -> Objects.equals(item.getId(), question.getId()));
            data.add(question);
        }
        return question;
    }

    @Override
    public Optional<Question> findById(long id)
    {
        return data.stream().filter(item -> Objects.equals(item.getId(), id)).findFirst();
    }

    @Override
    public Page<Question> findAllQuestions(Pageable pageable)
    {
        List<Question> response = data.stream()
                .limit(pageable.getPageSize())
                .sorted(Comparator.comparing(Question::getId).reversed())
                .toList();

        return new PageImpl<>(response, pageable, data.size());
    }

    @Override
    public Page<Question> findAllByProblemId(long problemId, Pageable pageable)
    {
        List<Question> questions = data.stream().filter(item -> Objects.equals(item.getProblem().getId(), problemId))
                .toList();

        return new PageImpl<>(questions, pageable, data.size());
    }

    @Override
    public void deleteById(long id) {
        data.removeIf(item -> Objects.equals(item.getId(), id));
    }
}
