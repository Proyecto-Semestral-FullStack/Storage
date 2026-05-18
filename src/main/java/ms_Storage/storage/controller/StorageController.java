package ms_Storage.storage.controller;

import lombok.RequiredArgsConstructor;
import ms_Storage.storage.dto.ArchivoResponseDTO;
import ms_Storage.storage.model.ArchivoMedia;
import ms_Storage.storage.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService service;

    @PostMapping
    public ResponseEntity<ArchivoResponseDTO> subirArchivo(@RequestParam("archivo") MultipartFile archivo) {
        ArchivoMedia media = service.guardarArchivo(archivo);
        ArchivoResponseDTO dto = new ArchivoResponseDTO(
                media.getId(),
                media.getNombreOriginal(),
                media.getUrlPublica()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarArchivo(@PathVariable Long id) {
        service.eliminarArchivo(id);
        return ResponseEntity.noContent().build();
    }

}
