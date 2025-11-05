package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.MembresiaRequestDTO;
import com.recolectaedu.dto.response.MembresiaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Membresia;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.MembresiaStatus;
import com.recolectaedu.model.enums.Plan;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.repository.MembresiaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import com.recolectaedu.service.MembresiaService;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class MembresiaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private MembresiaService membresiaService;

    // US-23 - Crear membresía - éxito
    @Test
    @DisplayName("Debe crear una membresía ACTIVE cuando el usuario no tiene membresías vigentes")
    void createMembresia_usuarioSinActivas_ok() {
        // GIVEN
        Integer idUsuario = 1;

        Usuario user = new Usuario();
        user.setId_usuario(idUsuario);
        user.setEmail("membresia@test.com");
        user.setRolTipo(RolTipo.ROLE_FREE);

        MembresiaRequestDTO req = new MembresiaRequestDTO(Plan.MONTHLY, true);

        // el usuario existe
        given(usuarioRepository.findById(idUsuario)).willReturn(Optional.of(user));

        // "usuario autenticado" = el mismo user
        given(usuarioService.getAuthenticatedUsuario()).willReturn(user);

        // no tiene membresías activas vigentes
        given(membresiaRepository.hasAnyActiveNow(eq(idUsuario), any(Instant.class))).willReturn(false);

        // simulamos lo que devuelve el save
        Membresia saved = new Membresia();
        saved.setId(10);
        saved.setUsuario(user);
        saved.setPlan(Plan.MONTHLY);
        saved.setStatus(MembresiaStatus.ACTIVE);
        saved.setAutoRenew(true);
        saved.setStartsAt(Instant.now());
        saved.setEndsAt(Instant.now().plusSeconds(3600));

        given(membresiaRepository.save(any(Membresia.class)))
                .willReturn(saved);

        // WHEN
        MembresiaResponseDTO resp = membresiaService.create(idUsuario, req);

        // THEN
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(10);
        assertThat(resp.plan()).isEqualTo(Plan.MONTHLY);
        assertThat(resp.status()).isEqualTo(MembresiaStatus.ACTIVE);

        then(usuarioRepository).should().findById(idUsuario);
        then(membresiaRepository).should().hasAnyActiveNow(eq(idUsuario), any(Instant.class));
        then(membresiaRepository).should().save(any(Membresia.class));
    }

    // US-23 - Cancelar membresía activa - éxito
    @Test
    @DisplayName("Debe cancelar la membresía activa más reciente del propio usuario")
    void cancelActive_usuarioConMembresiaActiva_ok() {
        // GIVEN
        Integer idUsuario = 1;

        Usuario user = new Usuario();
        user.setId_usuario(idUsuario);
        user.setEmail("membresia@test.com");
        user.setRolTipo(RolTipo.ROLE_FREE);

        Membresia m = new Membresia();
        m.setId(10);
        m.setUsuario(user);
        m.setPlan(Plan.MONTHLY);
        m.setStatus(MembresiaStatus.ACTIVE);
        m.setAutoRenew(true);
        m.setStartsAt(Instant.parse("2025-01-01T00:00:00Z"));
        m.setEndsAt(Instant.parse("2025-02-01T00:00:00Z"));

        // usuario existe en BD
        given(usuarioRepository.findById(idUsuario)).willReturn(Optional.of(user));

        // usuario autenticado = el mismo (para validarPropietarioOAdmin)
        given(usuarioService.getAuthenticatedUsuario()).willReturn(user);

        // tiene una membresía activa
        given(membresiaRepository.findAllActiveNowOrderByStartsDesc(eq(idUsuario), any(Instant.class))).willReturn(List.of(m));

        given(membresiaRepository.save(any(Membresia.class))).willAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        MembresiaResponseDTO resp = membresiaService.cancelActive(idUsuario);

        // THEN
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(10);
        assertThat(resp.status()).isEqualTo(MembresiaStatus.CANCELED);
        assertThat(resp.autoRenew()).isFalse();

        then(usuarioRepository).should().findById(idUsuario);
        then(membresiaRepository).should().findAllActiveNowOrderByStartsDesc(eq(idUsuario), any(Instant.class));
        then(membresiaRepository).should().save(any(Membresia.class));
    }

    // US-23 - Cancelar membresía activa - error usuario sin membresías activas
    @Test
    @DisplayName("Debe lanzar BusinessRuleException si el usuario no tiene membresía activa vigente")
    void cancelActive_sinMembresiasActivas_lanzaBusinessRuleException() {
        // GIVEN
        Integer idUsuario = 1;

        Usuario user = new Usuario();
        user.setId_usuario(idUsuario);
        user.setEmail("nomem@test.com");
        user.setRolTipo(RolTipo.ROLE_FREE);

        given(usuarioRepository.findById(idUsuario))
                .willReturn(Optional.of(user));

        given(usuarioService.getAuthenticatedUsuario())
                .willReturn(user);

        given(membresiaRepository.findAllActiveNowOrderByStartsDesc(eq(idUsuario), any(Instant.class)))
                .willReturn(List.of());

        // WHEN - THEN
        var ex = assertThrows(BusinessRuleException.class, () -> membresiaService.cancelActive(idUsuario));
        assertThat(ex.getMessage()).containsIgnoringCase("no tiene una membresía activa");


        then(usuarioRepository).should().findById(idUsuario);
        then(membresiaRepository).should()
                .findAllActiveNowOrderByStartsDesc(eq(idUsuario), any(Instant.class));
    }

    // US-23 - Listar membresías de un usuario - éxito
    @Test
    @DisplayName("Debe listar las membresías de un usuario propietario")
    void list_usuarioPropietarioConMembresias_ok() {
        // GIVEN
        Integer idUsuario = 1;

        Usuario user = new Usuario();
        user.setId_usuario(idUsuario);
        user.setEmail("user@test.com");
        user.setRolTipo(RolTipo.ROLE_FREE);

        Membresia m1 = new Membresia();
        m1.setId(10);
        m1.setUsuario(user);
        m1.setPlan(Plan.MONTHLY);
        m1.setStatus(MembresiaStatus.ACTIVE);

        Membresia m2 = new Membresia();
        m2.setId(11);
        m2.setUsuario(user);
        m2.setPlan(Plan.ANNUAL);
        m2.setStatus(MembresiaStatus.CANCELED);

        // usuario existe
        given(usuarioRepository.findById(idUsuario))
                .willReturn(java.util.Optional.of(user));

        // usuario autenticado = el mismo (validarPropietarioOAdmin)
        given(usuarioService.getAuthenticatedUsuario())
                .willReturn(user);

        // membresías del usuario
        given(membresiaRepository.findAllByUsuarioOrderByStartsDesc(idUsuario))
                .willReturn(java.util.List.of(m1, m2));

        // WHEN
        var resp = membresiaService.list(idUsuario);

        // THEN
        assertThat(resp).hasSize(2);
        assertThat(resp.get(0).id()).isEqualTo(10);
        assertThat(resp.get(1).id()).isEqualTo(11);

        then(usuarioRepository).should().findById(idUsuario);
        then(membresiaRepository).should().findAllByUsuarioOrderByStartsDesc(idUsuario);
    }

    // US-23 - Listar membresías de un usuario - error usuario no existe
    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe al listar sus membresías")
    void list_usuarioNoExiste_lanzaResourceNotFound() {
        // GIVEN
        Integer idUsuario = 99;

        given(usuarioRepository.findById(idUsuario))
                .willReturn(java.util.Optional.empty());

        // WHEN - THEN
        assertThrows(ResourceNotFoundException.class,
                () -> membresiaService.list(idUsuario));

        then(usuarioRepository).should().findById(idUsuario);
        // No debe llamar al repositorio de membresías
        then(membresiaRepository).shouldHaveNoInteractions();
    }

}
