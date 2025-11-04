package com.recolectaedu.controller;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.AporteConContadoresResponseDTO;
import com.recolectaedu.dto.response.AporteListadoResponseDTO;
import com.recolectaedu.dto.response.RespuestaPagina;
import com.recolectaedu.dto.response.UserResponseDTO;
import com.recolectaedu.dto.response.UsuarioStatsResponseDTO;
import com.recolectaedu.service.RecursoService;
import com.recolectaedu.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RecursoService recursoService;

//    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO req) {
//        var resp = usuarioService.registrarUsuario(req);
//        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
//    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Integer id) {
        var resp = usuarioService.obtenerUsuarioPorIdDTO(id);
        return ResponseEntity.ok(resp);
    }

    @PutMapping(value = "/{id}/perfil", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> upsertProfile(
            @PathVariable Integer id,
            @Valid @RequestBody PerfilRequestDTO body) {
        var resp = usuarioService.actualizarPerfilDTO(id, body);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id_usuario}/estadisticas")
    public ResponseEntity<UsuarioStatsResponseDTO> obtenerEstadisticas(
            @PathVariable Integer id_usuario
    ) {
        return ResponseEntity.ok(usuarioService.obtenerEstadisticas(id_usuario));
    }

    // US-08: Historial de aportes de un usuario
    @GetMapping("/{usuarioId}/aportes")
    public ResponseEntity<RespuestaPagina<AporteConContadoresResponseDTO>> getAportesPorUsuario(
            @PathVariable Integer usuarioId,
            @RequestParam(required = false) Integer cursoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creado_el,desc") String[] sort
    ) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<AporteConContadoresResponseDTO> aportesPage = recursoService.listarMisAportes(
                usuarioId,
                cursoId,
                tipo,
                pageable
        );

        RespuestaPagina<AporteConContadoresResponseDTO> respuesta = new RespuestaPagina<>(
                aportesPage.getContent(),
                aportesPage.getNumber(),
                aportesPage.getSize(),
                aportesPage.getTotalElements(),
                aportesPage.getTotalPages(),
                aportesPage.isLast()
        );

        return ResponseEntity.ok(respuesta);
    }
}
