package edu.pe.cibertec.taller.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.pe.cibertec.taller.excepcion.HorarioNoPermitidoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.ResultadoCancelacion;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

public class GestionCitasSteps {

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;

	private static final Long ID_MECANICO_AMBAR = 1L;
	private static final Long ID_CITA_AMBAR = 1L;

	private Cita citaExistente;
	private ResultadoCancelacion resultadoCancelacion;
	private HorarioNoPermitidoException excepcionCapturada;

	@Before
	public void inicializar() {
		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
	}

	@Given("un mecanico de especialidad REPARACION_TRANSMISION disponible")
	public void unMecanicoDeEspecialidadReparacionTransmisionDisponible() {
		Mecanico mecanico = new Mecanico(ID_MECANICO_AMBAR, "Diego Cabrera", TipoServicio.REPARACION_TRANSMISION);
		when(repositorioMecanicos.findById(ID_MECANICO_AMBAR)).thenReturn(Optional.of(mecanico));
		when(repositorioCitas.findByMecanicoIdAndEstado(ID_MECANICO_AMBAR, EstadoCita.PROGRAMADA))
				.thenReturn(Collections.emptyList());
	}

	@Given("la fecha y hora actual simulada es un dia antes del DIA a las 08:00")
	public void laFechaYHoraActualSimuladaEsUnDiaAntesDelDiaALas0800() {
		LocalDateTime relojAmbar = LocalDateTime.of(2026, 9, 12, 8, 0);
		when(proveedorFechaHora.ahora()).thenReturn(relojAmbar);
	}

	@When("se intenta registrar una cita de REPARACION_TRANSMISION para el DIA a las 16:00")
	public void seIntentaRegistrarUnaCitaDeReparacionTransmisionParaElDiaALas1600() {
		LocalDateTime fechaAmbar = LocalDateTime.of(2026, 9, 13, 16, 0);
		excepcionCapturada = assertThrows(HorarioNoPermitidoException.class, () ->
				servicioCitas.agendarCita(ID_MECANICO_AMBAR, "CAB-003",
						TipoServicio.REPARACION_TRANSMISION, fechaAmbar));
	}

	@Then("el servicio rechaza el registro con HorarioNoPermitidoException")
	public void elServicioRechazaElRegistroConHorarioNoPermitidoException() {
		assertEquals(HorarioNoPermitidoException.class, excepcionCapturada.getClass());
	}

	@Given("una cita programada para dentro de 3 dias")
	public void unaCitaProgramadaParaDentroDe3Dias() {
		LocalDateTime ahoraAmbar = LocalDateTime.of(2026, 9, 13, 9, 0);
		when(proveedorFechaHora.ahora()).thenReturn(ahoraAmbar);

		citaExistente = new Cita();
		citaExistente.setId(ID_CITA_AMBAR);
		citaExistente.setFechaHoraInicio(ahoraAmbar.plusDays(3));
		citaExistente.setEstado(EstadoCita.PROGRAMADA);

		when(repositorioCitas.findById(ID_CITA_AMBAR)).thenReturn(Optional.of(citaExistente));
		when(repositorioCitas.save(citaExistente)).thenReturn(citaExistente);
	}

	@When("se cancela la cita hoy")
	public void seCancelaLaCitaHoy() {
		resultadoCancelacion = servicioCitas.cancelarCita(ID_CITA_AMBAR);
	}

	@Then("la penalidad es 0.0")
	public void laPenalidadEs00() {
		assertEquals(0.0, resultadoCancelacion.getMontoPenalidad());
	}

	@And("el estado final de la cita es CANCELADA")
	public void elEstadoFinalDeLaCitaEsCancelada() {
		assertEquals(EstadoCita.CANCELADA, citaExistente.getEstado());
	}
}