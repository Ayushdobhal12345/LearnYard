package com.bruteforce.ila.subject;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByName(String name);

    boolean existsByName(String name);

    List<Subject> findByOwnerId(Long ownerId);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}