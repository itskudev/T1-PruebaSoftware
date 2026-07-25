Feature: Gestion de citas del taller mecanico

  Scenario: Intento de reparacion de transmision fuera de horario permitido
    Given un mecanico de especialidad REPARACION_TRANSMISION disponible
    And la fecha y hora actual simulada es un dia antes del DIA a las 08:00
    When se intenta registrar una cita de REPARACION_TRANSMISION para el DIA a las 16:00
    Then el servicio rechaza el registro con HorarioNoPermitidoException

  Scenario: Cancelacion anticipada de una cita programada dentro de 3 dias
    Given una cita programada para dentro de 3 dias
    When se cancela la cita hoy
    Then la penalidad es 0.0
    And el estado final de la cita es CANCELADA