package com.redmuqui.platform.ia.client;

/**
 * Abstracción proveedor-agnóstica de un modelo de lenguaje (LLM).
 *
 * El módulo de resumen ejecutivo depende de esta interfaz, no de un proveedor
 * concreto, para poder cambiar de Gemini a otro proveedor sin tocar la lógica
 * de negocio. La implementación por defecto es {@link GeminiLlmClient}.
 */
public interface LlmClient {

    /** {@code true} si el proveedor tiene credenciales configuradas y puede usarse. */
    boolean estaConfigurado();

    /** Identificador del modelo en uso (p.ej. {@code gemini-2.0-flash}), para trazabilidad. */
    String modelo();

    /**
     * Genera texto a partir de una instrucción de sistema y un prompt de usuario.
     *
     * @param instruccionSistema reglas/rol que debe seguir el modelo
     * @param prompt             contenido sobre el que redactar (la ficha del proyecto)
     * @return el texto generado por el modelo
     * @throws RuntimeException si la llamada al proveedor falla
     */
    String generar(String instruccionSistema, String prompt);
}
