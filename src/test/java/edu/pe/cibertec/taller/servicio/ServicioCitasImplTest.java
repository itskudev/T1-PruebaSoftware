package edu.pe.cibertec.taller.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.pe.cibertec.taller.excepcion.CitaNoCancelableException;
import edu.pe.cibertec.taller.excepcion.CitaNoEncontradaException;
import edu.pe.cibertec.taller.excepcion.FechaInvalidaException;
import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.excepcion.SinDisponibilidadException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicioCitasImplTest {

	@Mock
	private RepositorioMecanicos repositorioMecanicos;

	@Mock
	private RepositorioCitas repositorioCitas;

	@Mock
	private ProveedorFechaHora proveedorFechaHora;

	@Mock
	private ServicioNotificaciones servicioNotificaciones;

	private ServicioCitasImpl servicioCitas;

	@BeforeEach
	void inicializar() {
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
	}

	@Test
	@DisplayName("Agendar una cita valida la guarda, notifica y la retorna en estado PROGRAMADA")
	void agendarCitaExitosa() {
		LocalDateTime relojAmbar = LocalDateTime.of(2026, 9, 13, 10, 0);
		LocalDateTime fechaCita = relojAmbar.plusDays(1);
		Mecanico mecanico = new Mecanico(1L, "Diego Cabrera", TipoServicio.CAMBIO_ACEITE);

		when(repositorioMecanicos.findById(1L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(relojAmbar);
		when(repositorioCitas.findByMecanicoIdAndEstado(1L, EstadoCita.PROGRAMADA))
				.thenReturn(Collections.emptyList());
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

		Cita resultado = servicioCitas.agendarCita(1L, "CAB-003", TipoServicio.CAMBIO_ACEITE, fechaCita);

		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
		assertEquals(TipoServicio.CAMBIO_ACEITE.getDuracionHoras(), resultado.getDuracionHoras());
		verify(servicioNotificaciones, times(1)).notificarCitaAgendada(resultado);
	}

	@Test
	@DisplayName("Agendar con fecha igual a la hora del reloj simulado lanza FechaInvalidaException")
	void agendarConFechaIgualAlReloj() {
		LocalDateTime horaAmbar = LocalDateTime.of(2026, 9, 13, 10, 0);
		Mecanico mecanico = new Mecanico(1L, "Diego Cabrera", TipoServicio.CAMBIO_ACEITE);

		when(repositorioMecanicos.findById(1L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(horaAmbar);

		assertThrows(FechaInvalidaException.class, () ->
				servicioCitas.agendarCita(1L, "CAB-003", TipoServicio.CAMBIO_ACEITE, horaAmbar));
	}

	@Test
	@DisplayName("Agendar con un mecanico inexistente lanza MecanicoNoEncontradoException")
	void agendarConMecanicoInexistente() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Agendar cuando la especialidad no coincide lanza EspecialidadIncorrectaException")
	void agendarConEspecialidadIncorrecta() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Un servicio pesado a las 15:00 se rechaza con HorarioNoPermitidoException")
	void agendarServicioPesadoEnLaTarde() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Un servicio pesado a las 09:00 se acepta y se guarda")
	void agendarServicioPesadoEnLaManana() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Agendar en una fecha del pasado lanza FechaInvalidaException")
	void agendarConFechaEnElPasado() {
		LocalDateTime fechaAmbar = LocalDateTime.of(2026, 9, 13, 10, 0); // DIA a las 10:00
		LocalDateTime relojSimulado = fechaAmbar.plusDays(1);
		Mecanico mecanico = new Mecanico(1L, "Diego Cabrera", TipoServicio.CAMBIO_ACEITE);

		when(repositorioMecanicos.findById(1L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(relojSimulado);

		assertThrows(FechaInvalidaException.class, () ->
				servicioCitas.agendarCita(1L, "CAB-003", TipoServicio.CAMBIO_ACEITE, fechaAmbar));

		verify(repositorioCitas, never()).save(any(Cita.class));
	}

	@Test
	@DisplayName("Agendar sobre una cita ya programada se rechaza con HorarioOcupadoException")
	void agendarConSuperposicion() {
		LocalDateTime relojAmbar = LocalDateTime.of(2026, 9, 12, 8, 0);
		LocalDateTime inicioExistente = LocalDateTime.of(2026, 9, 13, 10, 0);
		Mecanico mecanico = new Mecanico(2L, "Diego Cabrera", TipoServicio.MANTENIMIENTO_LIGERO);

		Cita citaExistente = new Cita();
		citaExistente.setMecanico(mecanico);
		citaExistente.setFechaHoraInicio(inicioExistente);
		citaExistente.setDuracionHoras(TipoServicio.MANTENIMIENTO_LIGERO.getDuracionHoras());
		citaExistente.setEstado(EstadoCita.PROGRAMADA);

		LocalDateTime nuevaInicio = LocalDateTime.of(2026, 9, 13, 11, 0);

		when(repositorioMecanicos.findById(2L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(relojAmbar);
		when(repositorioCitas.findByMecanicoIdAndEstado(2L, EstadoCita.PROGRAMADA))
				.thenReturn(List.of(citaExistente));

		assertThrows(HorarioOcupadoException.class, () ->
				servicioCitas.agendarCita(2L, "CAB-003", TipoServicio.MANTENIMIENTO_LIGERO, nuevaInicio));
	}

	@Test
	@DisplayName("Una cita que empieza justo cuando termina otra se acepta")
	void agendarCitaContigua() {
		LocalDateTime relojAmbar = LocalDateTime.of(2026, 9, 12, 8, 0);
		LocalDateTime inicioExistente = LocalDateTime.of(2026, 9, 13, 10, 0);
		Mecanico mecanico = new Mecanico(2L, "Diego Cabrera", TipoServicio.MANTENIMIENTO_LIGERO);

		Cita citaExistente = new Cita();
		citaExistente.setMecanico(mecanico);
		citaExistente.setFechaHoraInicio(inicioExistente);
		citaExistente.setDuracionHoras(TipoServicio.MANTENIMIENTO_LIGERO.getDuracionHoras());
		citaExistente.setEstado(EstadoCita.PROGRAMADA);

		LocalDateTime nuevaInicio = LocalDateTime.of(2026, 9, 13, 12, 0);

		when(repositorioMecanicos.findById(2L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(relojAmbar);
		when(repositorioCitas.findByMecanicoIdAndEstado(2L, EstadoCita.PROGRAMADA))
				.thenReturn(List.of(citaExistente));
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

		Cita resultado = servicioCitas.agendarCita(2L, "CAB-003", TipoServicio.MANTENIMIENTO_LIGERO, nuevaInicio);

		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	@Test
	@DisplayName("Una cita agendada el dia siguiente no se superpone y se acepta")
	void agendarCitaDiaSiguienteSinSuperposicion() {
		LocalDateTime relojAmbar = LocalDateTime.of(2026, 9, 12, 8, 0);
		LocalDateTime inicioExistente = LocalDateTime.of(2026, 9, 13, 10, 0);
		Mecanico mecanico = new Mecanico(2L, "Diego Cabrera", TipoServicio.MANTENIMIENTO_LIGERO);

		Cita citaExistente = new Cita();
		citaExistente.setMecanico(mecanico);
		citaExistente.setFechaHoraInicio(inicioExistente);
		citaExistente.setDuracionHoras(TipoServicio.MANTENIMIENTO_LIGERO.getDuracionHoras());
		citaExistente.setEstado(EstadoCita.PROGRAMADA);

		LocalDateTime nuevaInicio = LocalDateTime.of(2026, 9, 14, 11, 0);

		when(repositorioMecanicos.findById(2L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(relojAmbar);
		when(repositorioCitas.findByMecanicoIdAndEstado(2L, EstadoCita.PROGRAMADA))
				.thenReturn(List.of(citaExistente));
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

		Cita resultado = servicioCitas.agendarCita(2L, "CAB-003", TipoServicio.MANTENIMIENTO_LIGERO, nuevaInicio);

		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	@Test
	@DisplayName("Cancelar con 24 horas o mas de anticipacion no genera penalidad")
	void cancelarConAnticipacionSuficiente() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO: penalidad 0, estado CANCELADA, notificacion
	}

	@Test
	@DisplayName("Cancelar con menos de 24 horas aplica una penalidad de 50.00")
	void cancelarConAvisoTardio() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar una cita inexistente lanza CitaNoEncontradaException")
	void cancelarCitaInexistente() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar una cita que ya fue cancelada lanza CitaNoCancelableException")
	void cancelarCitaYaCancelada() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Buscar mecanico disponible retorna el primero sin citas superpuestas")
	void buscarMecanicoDisponibleRetornaPrimeroLibre() {
		// Arrange
		// TODO: dos mecanicos de la misma especialidad, el primero ocupado

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Buscar mecanico cuando ninguno esta libre lanza SinDisponibilidadException")
	void buscarMecanicoSinDisponibilidad() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}
}
