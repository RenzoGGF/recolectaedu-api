package com.recolectaedu.config;

import com.recolectaedu.model.Curso;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos de prueba...");

        // Crear usuarios
        log.info("Creando usuarios...");
        crearUsuarioSiNoExiste("admin@recolectaedu.com", "adminadmin", RolTipo.ROLE_ADMIN, "Admin", "Admin");
        crearUsuarioSiNoExiste("example@email.com", "password", RolTipo.ROLE_FREE, "Usuario", "Prueba");
        log.info("Usuarios creados.");

        // Crear curso de prueba
        log.info("Creando cursos...");
        crearCursoSiNoExiste("Universidad Peruana de Ciencias Aplicadas", "Ingeniería de Software", "Ciencias de la Computación");
        crearCursoSiNoExiste("Universidad Peruana de Ciencias Aplicadas", "Base de Datos", "Ciencias de la Computación");
        crearCursoSiNoExiste("Universidad Peruana de Ciencias Aplicadas", "Matemática Básica", "Ingeniería de Software");
        crearCursoSiNoExiste("Universidad de Lima", "Cálculo I", "Ingeniería de Sistemas");
        crearCursoSiNoExiste("Universidad San Martín de Porres", "Estadística I", "Ingeniería en Ciencias de Datos");
        log.info("Cursos creados.");

        log.info("Carga de datos completada.");
    }

    private void crearUsuarioSiNoExiste(String email, String password, RolTipo rol, String nombre, String apellido) {
        if (usuarioRepository.existsByEmail(email)) {
            log.info("El usuario {} ya existe", email);
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword_hash(passwordEncoder.encode(password));
        usuario.setRolTipo(rol);

        // Crear y asociar el perfil
        Perfil perfil = new Perfil();
        perfil.setNombre(nombre);
        perfil.setApellidos(apellido);
        perfil.setUniversidad("Universidad Peruana de Ciencias Aplicadas"); // Dato por defecto
        perfil.setCarrera("Ciencias e la Computación"); // Dato por defecto
        perfil.setCiclo((short) 1); // Dato por defecto

        // Asignamos el perfil
        usuario.attachPerfil(perfil);

        usuarioRepository.save(usuario);
        log.info("Usuario creado: {}", email);
    }

    private void crearCursoSiNoExiste(String universidad, String nombre, String carrera) {
        cursoRepository.findByUniversidadAndCarreraAndNombre(universidad, carrera, nombre).ifPresentOrElse(c -> log.info("El curso '{}' ya existe", nombre), () -> {
            Curso curso = new Curso();
            curso.setUniversidad(universidad);
            curso.setNombre(nombre);
            curso.setCarrera(carrera);
            cursoRepository.save(curso);
            log.info("Curso creado: {}", nombre);
        });
    }
}
