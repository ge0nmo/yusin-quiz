package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberWithdrawalRepository;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberWithdrawalRepositoryImpl implements MemberWithdrawalRepository {
    private final EntityManager entityManager;

    @Override
    public void withdraw(long memberId, Member withdrawnAuthor) {
        entityManager.flush();

        anonymizePublicContent(memberId, withdrawnAuthor);
        deleteBookmarks(memberId);
        deleteStudyData(memberId);
        deleteWordPracticeData(memberId);
        deleteMember(memberId);

        entityManager.clear();
    }

    private void anonymizePublicContent(long memberId, Member withdrawnAuthor) {
        entityManager.createQuery("""
                        update Question q
                        set q.member = :withdrawnAuthor
                        where q.member.id = :memberId
                        """)
                .setParameter("withdrawnAuthor", withdrawnAuthor)
                .setParameter("memberId", memberId)
                .executeUpdate();

        entityManager.createQuery("""
                        update Answer a
                        set a.member = :withdrawnAuthor
                        where a.member.id = :memberId
                        """)
                .setParameter("withdrawnAuthor", withdrawnAuthor)
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

    private void deleteBookmarks(long memberId) {
        entityManager.createQuery("delete from Bookmark b where b.member.id = :memberId")
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

    private void deleteStudyData(long memberId) {
        entityManager.createQuery("""
                        delete from SubmittedAnswer a
                        where a.studySession.id in (
                            select s.id from StudySession s where s.member.id = :memberId
                        )
                        """)
                .setParameter("memberId", memberId)
                .executeUpdate();

        entityManager.createQuery("delete from StudySession s where s.member.id = :memberId")
                .setParameter("memberId", memberId)
                .executeUpdate();

        entityManager.createQuery("delete from DailyStudyLog l where l.member.id = :memberId")
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

    private void deleteWordPracticeData(long memberId) {
        String ownerKey = String.valueOf(memberId);

        entityManager.createQuery("""
                        delete from WordPracticeAnswer a
                        where a.cycle.id in (
                            select c.id from WordPracticeCycle c
                            where c.participant.type = :type and c.participant.ownerKey = :ownerKey
                        )
                        """)
                .setParameter("type", WordPracticeParticipantType.MEMBER)
                .setParameter("ownerKey", ownerKey)
                .executeUpdate();

        entityManager.createQuery("""
                        delete from WordPracticeCycle c
                        where c.participant.id in (
                            select p.id from WordPracticeParticipant p
                            where p.type = :type and p.ownerKey = :ownerKey
                        )
                        """)
                .setParameter("type", WordPracticeParticipantType.MEMBER)
                .setParameter("ownerKey", ownerKey)
                .executeUpdate();

        entityManager.createQuery("""
                        delete from WordPracticeParticipant p
                        where p.type = :type and p.ownerKey = :ownerKey
                        """)
                .setParameter("type", WordPracticeParticipantType.MEMBER)
                .setParameter("ownerKey", ownerKey)
                .executeUpdate();
    }

    private void deleteMember(long memberId) {
        entityManager.createQuery("delete from Member m where m.id = :memberId")
                .setParameter("memberId", memberId)
                .executeUpdate();
    }
}
