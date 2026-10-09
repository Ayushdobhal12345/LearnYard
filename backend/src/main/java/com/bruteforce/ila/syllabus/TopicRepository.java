package com.bruteforce.ila.syllabus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByUnitId(Long unitId);

    @Query("select t.unit.id from Topic t where t.id = :topicId")
    Optional<Long> findUnitIdByTopicId(@Param("topicId") Long topicId);
}