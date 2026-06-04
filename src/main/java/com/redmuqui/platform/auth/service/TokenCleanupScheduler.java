package com.redmuqui.platform.auth.service;

import com.redmuqui.platform.auth.repository.RefreshTokenRevocadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Limpieza periódica de refresh tokens revocados ya expirados.
 *
 * Los tokens se revocan en la tabla refresh_tokens_revocados al hacer logout.
 * Una vez pasada su fecha de expiración natural, ya no pueden usarse de todas
 * formas (el JWT mismo estaría expirado), por lo que se pueden borrar sin riesgo.
 *
 * Se ejecuta cada hora (cron: minuto 0 de cada hora).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRevocadoRepository refreshTokenRevocadoRepository;

    @Scheduled(cron = "0 0 * * * *")  // cada hora, en punto
    @Transactional
    public void limpiarTokensExpirados() {
        int eliminados = refreshTokenRevocadoRepository.deleteExpirados(LocalDateTime.now());
        if (eliminados > 0) {
            log.info("TokenCleanup: {} refresh token(s) expirado(s) eliminado(s) de la lista negra.", eliminados);
        }
    }
}
