package com.redmuqui.platform.auth.controller;

import com.redmuqui.platform.auth.dto.*;
import com.redmuqui.platform.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login, refresh y recuperación de cuenta")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión (RF-001, RF-003)")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token con refresh token (RF-003)")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (RF-009)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recover")
    @Operation(summary = "Solicitar recuperación de cuenta (RF-010)")
    public ResponseEntity<Void> recover(@Valid @RequestBody RecoverRequest request) {
        authService.requestRecovery(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Restablecer contraseña con token de correo (RF-011)")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetRequest request) {
        authService.resetPassword(request.token(), request.nuevaContrasenha());
        return ResponseEntity.noContent().build();
    }
}
