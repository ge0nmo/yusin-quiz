package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FakeExamRepository implements ExamRepository
{
    private final AtomicLong autoGeneratedId = new AtomicLong(1);
    private final List<Exam> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Exam save(Exam exam)
    {
        if(exam.getId() == null || exam.getId() == 0){
            Exam newExam = Exam.builder()
                    .id(autoGeneratedId.getAndIncrement())
                    .name(exam.getName())
                    .year(exam.getYear())
                    .subjectId(exam.getSubjectId())
                    .build();
            data.add(newExam);
            return newExam;
        } else{
            data.removeIf(item -> Objects.equals(item.getId(), exam.getId()));
            data.add(exam);
        }

        return exam;
    }

    @Override
    public Optional<Exam> findById(long id)
    {
        return data.stream()
                .filter(item -> item.getId().equals(id))
                .findAny();
    }

    @Override
    public List<Exam> findAllBySubjectId(long subjectId, Integer year)
    {
        return data.stream()
                .filter(item -> item.getSubjectId().equals(subjectId) && item.getYear() == year)
                .toList();
    }

    @Override
    public List<Exam> findAllBySubjectId(long subjectId)
    {
        return data.stream()
                .filter(item -> item.getSubjectId().equals(subjectId))
                .toList();
    }

    @Override
    public void deleteById(long id)
    {
        data.removeIf(item -> item.getId().equals(id));
    }

    @Override
    public void deleteAllBySubjectId(long subjectId)
    {
        data.removeIf(item -> item.getSubjectId().equals(subjectId));
    }

    @Override
    public boolean existsById(long id)
    {
        return data.stream()
                .anyMatch(item -> item.getId().equals(id));
    }

    @Override
    public boolean existsBySubjectIdAndNameAndYear(long subjectId, String name, int year)
    {
        return data.stream()
                .anyMatch(item ->
                        item.getSubjectId().equals(subjectId)
                        && item.getName().equals(name)
                        && item.getYear() == year);
    }

    @Override
    public boolean existsByIdNotAndSubjectIdAndNameAndYear(long examId, long subjectId, String name, int year)
    {
        return data.stream()
                .anyMatch(item ->
                        !item.getId().equals(examId)
                                && item.getSubjectId().equals(subjectId)
                                && item.getName().equals(name)
                                && item.getYear() == year);
    }

    @Override
    public List<Integer> getYearsBySubjectId(long subjectId)
    {
        return data.stream()
                .filter(item -> item.getSubjectId().equals(subjectId))
                .map(Exam::getYear)
                .toList();
    }
}
