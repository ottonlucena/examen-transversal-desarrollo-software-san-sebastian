package cl.ipss.coleccion.dto;

import org.springframework.core.io.Resource;

/**
 * Foto de una lamina lista para enviarse al cliente.
 *
 * @param contenido     archivo en disco
 * @param tipoContenido tipo MIME ({@code image/jpeg}, {@code image/png} o {@code image/webp})
 */
public record ArchivoFoto(Resource contenido, String tipoContenido) {
}
