package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import com.cpa.yusin.quiz.subject.infrastructure.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FakeSubjectRepository implements SubjectRepository
{
    private final AtomicLong autoGeneratedId = new AtomicLong(1);
    private final List<SubjectDomain> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public SubjectDomain save(SubjectDomain subject)
    {
        if(subject.getId() == null || subject.getId() == 0){
            SubjectDomain newSubject = SubjectDomain.builder()
                    .id(autoGeneratedId.getAndIncrement())
                    .name(subject.getName())
                    .build();
            data.add(newSubject);
            return newSubject;
        } else{
          data.removeIf(item -> Objects.equals(item.getId(), subject.getId()));
          data.add(subject);
        }
        return subject;
    }

    @Override
    public Optional<SubjectDomain> findById(long id)
    {
        return data.stream()
                .filter(item -> item.getId().equals(id))
                .findAny();
    }

    @Override
    public List<SubjectDomain> findAll()
    {
        return data.stream()
                .toList();
    }

    @Override
    public boolean existsById(long id)
    {
        return data.stream()
                .anyMatch(item -> item.getId().equals(id));
    }

    @Override
    public void deleteById(long id)
    {
        data.removeIf(item -> item.getId().equals(id));
    }

    @Override
    public boolean existsByName(String name)
    {
        return data.stream()
                .anyMatch(item -> item.getName().equals(name));
    }
}
