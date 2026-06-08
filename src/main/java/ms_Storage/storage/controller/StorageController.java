package ms_Storage.storage.controller;

import lombok.RequiredArgsConstructor;
import ms_Storage.storage.dto.ArchivoResponseDTO;
import ms_Storage.storage.model.ArchivoMedia;
import ms_Storage.storage.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService service;

    // 1. Subir archivo
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchivoResponseDTO> subirArchivo(@RequestPart("archivo") MultipartFile archivo) {
        ArchivoMedia media = service.guardarArchivo(archivo);
        // Devolvemos el id y nombre; la URL ya no se incluye porque es privada
        ArchivoResponseDTO dto = new ArchivoResponseDTO(media.getId(), media.getNombreOriginal(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // 2. Obtener URL firmada (temporal)
    @GetMapping("/{id}/url")
    public ResponseEntity<Map<String, String>> obtenerSignedUrl(@PathVariable Long id) {
        ArchivoMedia media = service.obtenerArchivo(id);
        String signedUrl = service.generarSignedUrl(media.getNombreAlmacenado());
        return ResponseEntity.ok(Map.of("url", signedUrl));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarArchivo(@PathVariable Long id) {
        service.eliminarArchivo(id);
        return ResponseEntity.noContent().build();
    }

}
