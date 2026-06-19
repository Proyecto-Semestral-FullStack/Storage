package ms_Storage.storage.service;

import com.google.cloud.storage.*;
import ms_Storage.storage.model.ArchivoMedia;
import ms_Storage.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {
    @Mock
    private Storage storage; // Mock del bean de Google Cloud

    @Mock
    private StorageRepository repo;

    @InjectMocks
    private StorageService storageService;

    // ============================
    // guardarArchivo
    // ============================

    @Test
    void guardarArchivo_givenValidFile_savesAndReturnsArchivoMedia() throws Exception {
        // Given
        ReflectionTestUtils.setField(storageService, "bucketName", "frikitienda_img");

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "imagen.png",
                "image/png",
                "contenido de prueba".getBytes()
        );

        ArchivoMedia mediaGuardada = ArchivoMedia.builder()
                .id(1L)
                .nombreOriginal("imagen.png")
                .nombreAlmacenado("uuid_imagen.png")
                .tipoMime("image/png")
                .tamanioBytes(archivo.getSize())
                .build();

        // Simulamos que GCS acepta el archivo sin error
        when(storage.create(any(BlobInfo.class), any(byte[].class)))
                .thenReturn(mock(Blob.class));
        when(repo.save(any(ArchivoMedia.class))).thenReturn(mediaGuardada);

        // When
        ArchivoMedia resultado = storageService.guardarArchivo(archivo);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals("imagen.png", resultado.getNombreOriginal());
        verify(storage, times(1)).create(any(BlobInfo.class), any(byte[].class));
        verify(repo, times(1)).save(any(ArchivoMedia.class));
    }

    @Test
    void guardarArchivo_whenGCSFails_throwsRuntimeException() throws Exception {
        // Given
        ReflectionTestUtils.setField(storageService, "bucketName", "frikitienda_img");

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "imagen.png",
                "image/png",
                "contenido".getBytes()
        );

        // Simulamos que GCS lanza excepción
        when(storage.create(any(BlobInfo.class), any(byte[].class)))
                .thenThrow(new StorageException(500, "GCS no disponible"));

        // When / Then
        assertThrows(RuntimeException.class,
                () -> storageService.guardarArchivo(archivo));

        // Nunca debe guardarse en BD si GCS falla
        verify(repo, never()).save(any());
    }

    // ============================
    // obtenerArchivo
    // ============================

    @Test
    void obtenerArchivo_whenNotFound_throwsRuntimeException() {
        // Given
        when(repo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(RuntimeException.class,
                () -> storageService.obtenerArchivo(99L));
        verify(repo, times(1)).findById(99L);
    }

}
