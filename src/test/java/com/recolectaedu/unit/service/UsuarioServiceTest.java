package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.response.UsuarioStatsResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.*;
import com.recolectaedu.service.UsuarioService;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.UserResponseDTO;
import com.recolectaedu.model.enums.RolTipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private BibliotecaRecursoRepository bibliotecaRecursoRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private Usuario mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new Usuario();
        mockUser.setId_usuario(1);
    }

    // Renzo Tests -----------------------------------------------------------
    // US 01 - Registro de usuario: Caso exitoso
    @Test
    @DisplayName("Debe registrar usuario con email nuevo y rol FREE")
    void registrarUsuario_emailNuevo_ok() {
        // Arrange
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("random@gmail.com");
        req.setPassword("Contra123!");
        req.setRol("ROLE_FREE");
        req.setPerfil(null);


        when(usuarioRepository.existsByEmail("random@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("Contra123!")).thenReturn("$hash");

        Usuario u = new Usuario();
        u.setId_usuario(1);
        u.setEmail("random@gmail.com");
        u.setPassword_hash("$hash");
        u.setRolTipo(RolTipo.ROLE_FREE);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(u);

        // Act
        UserResponseDTO resp = usuarioService.registrarUsuario(req);

        // Assert
        assertThat(resp.getId_usuario()).isEqualTo(1);
        assertThat(resp.getEmail()).isEqualTo("random@gmail.com");
        assertThat(resp.getRole()).isEqualTo("ROLE_FREE");

        verify(usuarioRepository).existsByEmail("random@gmail.com");
        verify(passwordEncoder).encode("Contra123!");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // US01 - Registro de usuario: Error email duplicado
    @Test
    @DisplayName("Debe lanzar BusinessRuleException cuando el email ya está registrado")
    void registrarUsuario_emailDuplicado_error() {
        // Arrange
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("random@gmail.com");
        req.setPassword("Contra123!");
        req.setRol("ROLE_FREE");
        req.setPerfil(null);

        when(usuarioRepository.existsByEmail("random@gmail.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("email");

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    // US02 - Registro de usuario con perfil completo: Caso exitoso
    @Test
    @DisplayName("Debe registrar usuario con perfil completo (universidad, carrera, ciclo)")
    void registrarUsuario_conPerfil_ok() {
        // Arrange
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("perfil@gmail.com");
        req.setPassword("Contra123!");
        req.setRol("ROLE_FREE");

        PerfilRequestDTO perfilReq = new PerfilRequestDTO();
        perfilReq.setNombre("Renzo");
        perfilReq.setApellidos("Gutierrez");
        perfilReq.setUniversidad("UPC");
        perfilReq.setCarrera("Ciencias de la Computación");
        perfilReq.setCiclo((short) 6);
        req.setPerfil(perfilReq);

        when(usuarioRepository.existsByEmail("perfil@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("Contra123!")).thenReturn("$hash");

        // simulamos que la BD asigna ID al usuario
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId_usuario(10);
            return u;
        });

        // Act
        UserResponseDTO resp = usuarioService.registrarUsuario(req);

        // Assert sobre el DTO básico (sin perfil, porque el DTO no lo expone)
        assertThat(resp).isNotNull();
        assertThat(resp.getId_usuario()).isEqualTo(10);          // ajusta al getter real (getId() si fuera el caso)
        assertThat(resp.getEmail()).isEqualTo("perfil@gmail.com");
        assertThat(resp.getRole()).isEqualTo("ROLE_FREE");       // o getRol(), según tu DTO

        // Capturamos el Usuario que realmente se mandó a guardar
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioGuardado = usuarioCaptor.getValue();

        // verificamos que el PERFIL se construyó bien
        assertThat(usuarioGuardado.getPerfil()).isNotNull();
        assertThat(usuarioGuardado.getPerfil().getNombre()).isEqualTo("Renzo");
        assertThat(usuarioGuardado.getPerfil().getApellidos()).isEqualTo("Gutierrez");
        assertThat(usuarioGuardado.getPerfil().getUniversidad()).isEqualTo("UPC");
        assertThat(usuarioGuardado.getPerfil().getCarrera()).isEqualTo("Ciencias de la Computación");
        assertThat(usuarioGuardado.getPerfil().getCiclo()).isEqualTo((short) 6);
    }

    // US03 - Actualizar perfil de usuario: Caso exitoso
    @Test
    @DisplayName("Debe actualizar el perfil existente de un usuario")
    void actualizarPerfilDTO_usuarioConPerfil_ok() {
        // Arrange
        Integer idUsuario = 1;

        // Usuario existente con perfil viejo
        Usuario usuario = new Usuario();
        usuario.setId_usuario(idUsuario);
        usuario.setEmail("perfilold@gmail.com");
        usuario.setRolTipo(RolTipo.ROLE_FREE);


        Perfil perfilExistente = new Perfil();
        perfilExistente.setId_usuario(idUsuario);
        perfilExistente.setNombre("Nombre viejo");
        perfilExistente.setApellidos("Apellido viejo");
        perfilExistente.setUniversidad("UNI");
        perfilExistente.setCarrera("Ingeniería X");
        perfilExistente.setCiclo((short) 3);

        perfilExistente.setUsuario(usuario);
        usuario.setPerfil(perfilExistente);

        // DTO con nuevos datos de perfil
        PerfilRequestDTO dto = new PerfilRequestDTO();
        dto.setNombre("Renzo");
        dto.setApellidos("Gutierrez");
        dto.setUniversidad("UPC");
        dto.setCarrera("Ingeniería de Software");
        dto.setCiclo((short) 7);

        // mocks
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var resp = usuarioService.actualizarPerfilDTO(idUsuario, dto);

        // Assert: el servicio devuelve algo
        assertThat(resp).isNotNull();
        assertThat(resp.getId_usuario()).isEqualTo(idUsuario);

        // Y el perfil del usuario quedó actualizado en memoria
        Perfil perfilFinal = usuario.getPerfil();
        assertThat(perfilFinal).isNotNull();
        assertThat(perfilFinal.getNombre()).isEqualTo("Renzo");
        assertThat(perfilFinal.getApellidos()).isEqualTo("Gutierrez");
        assertThat(perfilFinal.getUniversidad()).isEqualTo("UPC");
        assertThat(perfilFinal.getCarrera()).isEqualTo("Ingeniería de Software");
        assertThat(perfilFinal.getCiclo()).isEqualTo((short) 7);

        // Verificamos interacciones con el repo
        verify(usuarioRepository).findById(idUsuario);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // US04 - Eliminar usuario: Caso exitoso
    @Test
    @DisplayName("Debe eliminar un usuario existente sin errores")
    void eliminarUsuario_usuarioExistente_ok() {
        // Arrange
        Integer idUsuario = 1;
        Usuario usuario = new Usuario();
        usuario.setId_usuario(idUsuario);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.eliminarUsuario(idUsuario);

        // Assert
        verify(usuarioRepository).findById(idUsuario);
        verify(usuarioRepository).delete(usuario);
    }

    // FIN Renzo Tests -----------------------------------------------------------

    @Test
    @DisplayName("Debe mostrar 5 en contador de recursos publicados cuando el usuario tiene 5 recursos")
    void obtenerEstadisticas_usuarioCon5Recursos_retorna5EnContador() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(5L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalRecursosPublicados()).isEqualTo(5);
        verify(recursoRepository).countByAutorId(1);
    }

    @Test
    @DisplayName("Debe mostrar 12 en contador de comentarios cuando el usuario ha realizado 12 comentarios")
    void obtenerEstadisticas_usuarioCon12Comentarios_retorna12EnContador() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(12L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalComentariosRealizados()).isEqualTo(12);
        verify(comentarioRepository).countByAutorId(1);
    }

    @Test
    @DisplayName("Debe mostrar 8 votos positivos y 2 negativos cuando los recursos han recibido esos votos")
    void obtenerEstadisticas_recursosCon8VotosPositivosY2Negativos_retornaContadoresCorrectos() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(10L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(8L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(2L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalResenasPositivas()).isEqualTo(8);
        assertThat(response.totalResenasNegativas()).isEqualTo(2);
        verify(resenaRepository).countResenasPositivasPorAutor(1);
        verify(resenaRepository).countResenasNegativasPorAutor(1);
    }

    @Test
    @DisplayName("Debe mostrar 15 en contador de biblioteca personal cuando el usuario tiene 15 recursos guardados")
    void obtenerEstadisticas_usuarioCon15RecursosGuardados_retorna15EnContadorBiblioteca() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(15L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalItemsBiblioteca()).isEqualTo(15);
        verify(bibliotecaRecursoRepository).countItemsByUsuarioId(1);
    }

    @Test
    @DisplayName("Debe mostrar 0 en todos los contadores cuando el usuario no tiene actividad")
    void obtenerEstadisticas_nuevoUsuarioSinActividad_retornaTodosLosContadoresEnCero() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalRecursosPublicados()).isZero();
        assertThat(response.totalComentariosRealizados()).isZero();
        assertThat(response.totalResenasRecibidas()).isZero();
        assertThat(response.totalResenasPositivas()).isZero();
        assertThat(response.totalResenasNegativas()).isZero();
        assertThat(response.totalItemsBiblioteca()).isZero();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void obtenerEstadisticas_usuarioNoExistente_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.obtenerEstadisticas(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado: 999");
    }
}
