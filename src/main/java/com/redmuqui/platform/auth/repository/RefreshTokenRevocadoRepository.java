package com.redmuqui.platform.auth.repository;

import com.redmuqui.platform.auth.entity.RefreshTokenRevocado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RefreshTokenRevocadoRepository extends JpaRepository<RefreshTokenRevocado, Long> {

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshTokenRevocado r WHERE r.expiresAt < :now")
    int deleteExpirados(@Param("now") LocalDateTime now);
}
