package ms_Storage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms_Storage.storage.model.ArchivoMedia;
import ms_Storage.storage.repository.StorageRepository;
import org.springframework.stereotype.Service;


import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final Storage storage; // Bean de Google Cloud
    private final StorageRepository repo;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public ArchivoMedia guardarArchivo(MultipartFile archivo) {
        try {
            String nombreAlmacenado = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, nombreAlmacenado);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(archivo.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, archivo.getBytes());

            ArchivoMedia media = ArchivoMedia.builder()
                    .nombreOriginal(archivo.getOriginalFilename())
                    .nombreAlmacenado(nombreAlmacenado)
                    .tipoMime(archivo.getContentType())
                    .tamanioBytes(archivo.getSize())
                    .urlPublica(blob.getMediaLink())
                    .build();

            ArchivoMedia guardado = repo.save(media);
            log.info("Archivo subido a Google Cloud: id={}, nombre={}", guardado.getId(), guardado.getNombreOriginal());
            return guardado;

        } catch (Exception e) {
            log.error("Error al subir archivo a Google Cloud", e);
            throw new RuntimeException("Error al guardar el archivo en la nube", e);
        }
    }

    public ArchivoMedia obtenerArchivo(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con id " + id));
    }

    public void eliminarArchivo(Long id) {
        ArchivoMedia media = obtenerArchivo(id);
        BlobId blobId = BlobId.of(bucketName, media.getNombreAlmacenado());
        storage.delete(blobId);
        repo.delete(media);
        log.info("Archivo eliminado de Google Cloud y BD: id={}", id);
    }
}
