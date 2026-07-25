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
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Una cita que empieza justo cuando termina otra se acepta")
	void agendarCitaContigua() {
		// Arrange
		// TODO: una cita existente que termina a las 10:00 y la nueva que empieza a las 10:00

		// Act
		// TODO

		// Assert
		// TODO
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
