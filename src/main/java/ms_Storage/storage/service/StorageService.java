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
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final Storage storage; // Bean de Google Cloud
    private final StorageRepository repo;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    /**
     * Guarda un archivo en Google Cloud Storage y almacena sus metadatos en la base de datos.
     * Ya no se guarda una URL pública fija, solo el identificador del blob.
     */
    public ArchivoMedia guardarArchivo(MultipartFile archivo) {
        try {
            String nombreAlmacenado = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, nombreAlmacenado);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(archivo.getContentType())
                    .build();

            // Subir el archivo a GCS (bucket privado)
            storage.create(blobInfo, archivo.getBytes());

            // Construir la entidad SIN guardar una URL pública (se usará signed URL)
            ArchivoMedia media = ArchivoMedia.builder()
                    .nombreOriginal(archivo.getOriginalFilename())
                    .nombreAlmacenado(nombreAlmacenado)
                    .tipoMime(archivo.getContentType())
                    .tamanioBytes(archivo.getSize())
                    // El campo urlPublica se deja como null (la columna debe permitir nulos)
                    .urlPublica(null)
                    .build();

            ArchivoMedia guardado = repo.save(media);
            log.info("Archivo subido a Google Cloud (bucket privado): id={}, nombreAlmacenado={}",
                    guardado.getId(), nombreAlmacenado);
            return guardado;

        } catch (Exception e) {
            log.error("Error al subir archivo a Google Cloud", e);
            throw new RuntimeException("Error al guardar el archivo en la nube", e);
        }
    }

    /**
     * Obtiene los metadatos de un archivo por su ID.
     */
    public ArchivoMedia obtenerArchivo(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con id " + id));
    }

    /**
     * Elimina el archivo de GCS y sus metadatos de la BD.
     */
    public void eliminarArchivo(Long id) {
        ArchivoMedia media = obtenerArchivo(id);
        BlobId blobId = BlobId.of(bucketName, media.getNombreAlmacenado());
        storage.delete(blobId);
        repo.delete(media);
        log.info("Archivo eliminado de Google Cloud y BD: id={}", id);
    }

    /**
     * Genera una URL firmada (signed URL) para acceder temporalmente al archivo privado.
     * @param nombreAlmacenado Nombre del blob en GCS
     * @return URL firmada válida por 15 minutos
     */
    public String generarSignedUrl(String nombreAlmacenado) {
        try {
            Blob blob = storage.get(BlobId.of(bucketName, nombreAlmacenado));
            if (blob == null) {
                throw new RuntimeException("Archivo no encontrado en GCS: " + nombreAlmacenado);
            }
            // La URL será válida por 15 minutos. Ajusta según necesites.
            String signedUrl = blob.signUrl(15, TimeUnit.MINUTES).toString();
            log.debug("Signed URL generada para {} (válida 15 min)", nombreAlmacenado);
            return signedUrl;
        } catch (Exception e) {
            log.error("Error generando signed URL para {}: {}", nombreAlmacenado, e.getMessage());
            throw new RuntimeException("No se pudo generar la URL temporal del archivo", e);
        }
    }
}
