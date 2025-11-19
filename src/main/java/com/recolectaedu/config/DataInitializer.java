package com.recolectaedu.config;

import com.recolectaedu.model.Curso;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Periodo;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.model.enums.Tipo_recurso;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
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
    private final RecursoRepository recursoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos de prueba...");

        // 1. Crear usuarios
        Usuario admin = crearUsuarioSiNoExiste("admin@recolectaedu.com", "admin123", RolTipo.ROLE_ADMIN);
        Usuario usuarioNormal = crearUsuarioSiNoExiste("user@recolectaedu.com", "user123", RolTipo.ROLE_FREE);

        // 2. Crear curso de prueba
        Curso cursoJava = crearCursoSiNoExiste("Universidad Tecnológica", "Programación en Java", "Ingeniería de Software");

        // 3. Crear recursos asociados
        crearRecursoSiNoExiste(usuarioNormal, cursoJava, "Apuntes Java Básico", "Introducción a variables y clases", Tipo_recurso.Apuntes);
        crearRecursoSiNoExiste(admin, cursoJava, "Ejercicios POO", "Práctica de herencia y polimorfismo", Tipo_recurso.Ejercicios);

        log.info("Carga de datos completada.");
    }

    private Usuario crearUsuarioSiNoExiste(String email, String password, RolTipo rol) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setPassword_hash(passwordEncoder.encode(password));
            usuario.setRolTipo(rol);
            log.info("Creando usuario: {}", email);
            return usuarioRepository.save(usuario);
        });
    }

    private Curso crearCursoSiNoExiste(String universidad, String nombre, String carrera) {
        return cursoRepository.findByUniversidadAndCarreraAndNombre(universidad, carrera, nombre)
                .orElseGet(() -> {
                    Curso curso = new Curso();
                    curso.setUniversidad(universidad);
                    curso.setNombre(nombre);
                    curso.setCarrera(carrera);
                    log.info("Creando curso: {}", nombre);
                    return cursoRepository.save(curso);
                });
    }

    private void crearRecursoSiNoExiste(Usuario autor, Curso curso, String titulo, String descripcion, Tipo_recurso tipo) {
        // Verificamos simplemente si existe algún recurso con ese título en ese curso para no duplicar
        boolean existe = recursoRepository.findAll().stream()
                .anyMatch(r -> r.getTitulo().equals(titulo) && r.getCurso().getId_curso().equals(curso.getId_curso()));

        if (!existe) {
            Recurso recurso = new Recurso();
            recurso.setTitulo(titulo);
            recurso.setDescripcion(descripcion);
            recurso.setContenido("Contenido de ejemplo para " + titulo);
            recurso.setFormato(FormatoRecurso.TEXTO); // Valor por defecto
            recurso.setTipo(tipo);
            recurso.setAno(2024);
            recurso.setPeriodo(Periodo.primer);
            recurso.setUsuario(autor);
            recurso.setCurso(curso);

            log.info("Creando recurso: {}", titulo);
            recursoRepository.save(recurso);
        }
    }
}
