package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.MentorshipSession;
import com.infosys.knowledgegap.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, Long> {

    @Query("SELECT s FROM MentorshipSession s WHERE (s.mentor.id = :userId OR s.mentee.id = :userId) " +
           "AND s.status = :status AND s.scheduledAt BETWEEN :from AND :to")
    List<MentorshipSession> findUpcomingForUser(@Param("userId") Long userId,
                                                 @Param("status") SessionStatus status,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    List<MentorshipSession> findByMentorIdOrMenteeIdOrderByScheduledAtAsc(Long mentorId, Long menteeId);
}
