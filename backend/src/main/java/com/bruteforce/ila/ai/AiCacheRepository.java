package com.bruteforce.ila.ai;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiCacheRepository extends JpaRepository<AiCache, Long> {

    Optional<AiCache> findByCacheKey(String cacheKey);
}
