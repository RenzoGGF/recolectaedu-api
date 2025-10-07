package com.recolectaedu.service;

import org.springframework.web.multipart.MultipartFile;

public interface IAlmacenamientoService {

    /**
     * Valida y almacena un archivo subido.
     *
     * @param archivo El archivo MultipartFile a almacenar.
     * @return El nombre único del archivo guardado (que se usará como 'contenido' en la entidad Recurso).
     * @throws com.recolectaedu.exception.AlmacenamientoException Si ocurre un error durante el almacenamiento o la validación.
     */
    String almacenar(MultipartFile archivo);

    /**
     * Elimina un archivo del almacenamiento.
     *
     * @param nombreArchivo El nombre del archivo a eliminar.
     */
    void eliminar(String nombreArchivo);
}
