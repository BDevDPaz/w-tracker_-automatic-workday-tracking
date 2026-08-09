package com.example.data.model

enum class SessionState(val label: String, val description: String) {
    NO_INICIADA("No iniciada", "Configura tu jornada de trabajo"),
    ESPERANDO("Esperando llegada", "Esperando arribo físico al área de trabajo"),
    ACTIVA("Jornada activa", "Trabajando en destino (Tiempo remunerado)"),
    PAUSADA("En almuerzo", "Almuerzo / Descanso supervisado"),
    TRASLADO("En traslado", "Transitando entre destinos (Traslado remunerado)"),
    SALIDA("Salida detectada", "Fuera del perímetro de geocerca"),
    CERRADA("Jornada cerrada", "Jornada completada")
}

enum class IntegrityType(val label: String) {
    VALID("Validada"),
    MANUAL("Ajuste manual"),
    AUTO("Auto cierre"),
    CONFLICT("Revisar")
}
