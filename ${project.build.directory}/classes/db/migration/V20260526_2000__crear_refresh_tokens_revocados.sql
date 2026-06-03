-- Sprint 1 deuda técnica (RF-009 + RNF-006): tabla para invalidar refresh tokens al hacer logout.
-- Decisión arquitectónica documentada en docs/sprints/sprint-1/plan-tecnico.md §3:
-- "Híbrido pragmático: access token TTL 15 min + tabla refresh_tokens_revocados (insertar hash al hacer logout)".

CREATE TABLE refresh_tokens_revocados (
    id          BIGSERIAL PRIMARY KEY,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    revoked_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP   NOT NULL
);

CREATE INDEX idx_refresh_tokens_revocados_expires_at
    ON refresh_tokens_revocados (expires_at);
