package com.bruteforce.ila.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByStudentIdOrderByCreatedAtAsc(Long studentId);

    // A derived delete query must run inside a transaction, otherwise Spring Data throws and the API returns 500.
    @Transactional
    void deleteByStudentId(Long studentId);
}