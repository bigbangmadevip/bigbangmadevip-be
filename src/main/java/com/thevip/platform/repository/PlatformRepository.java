package com.thevip.platform.repository;

import com.thevip.platform.entity.Platform;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    Optional<Platform> findByName(String name);
}
