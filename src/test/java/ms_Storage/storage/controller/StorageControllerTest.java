package ms_Storage.storage.controller;



import ms_Storage.storage.model.ArchivoMedia;
import ms_Storage.storage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageController.class)
public class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService service;

    // ============================
    // POST /api/archivos
    // ============================

    @Test
    void subirArchivo_givenValidFile_returns201() throws Exception {
        // Given
        ArchivoMedia media = ArchivoMedia.builder()
                .id(1L)
                .nombreOriginal("imagen.png")
                .nombreAlmacenado("uuid_imagen.png")
                .tipoMime("image/png")
                .tamanioBytes(100L)
                .build();

        when(service.guardarArchivo(any())).thenReturn(media);

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "imagen.png",
                "image/png",
                "contenido".getBytes()
        );

        // When / Then
        mockMvc.perform(multipart("/api/archivos")
                        .file(archivo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreOriginal").value("imagen.png"));

        verify(service, times(1)).guardarArchivo(any());
    }

    // ============================
    // GET /api/archivos/{id}/url
    // ============================

    @Test
    void obtenerSignedUrl_whenExists_returns200WithUrl() throws Exception {
        // Given
        ArchivoMedia media = ArchivoMedia.builder()
                .id(1L)
                .nombreOriginal("imagen.png")
                .nombreAlmacenado("uuid_imagen.png")
                .tipoMime("image/png")
                .tamanioBytes(100L)
                .build();

        when(service.obtenerArchivo(1L)).thenReturn(media);
        when(service.generarSignedUrl("uuid_imagen.png"))
                .thenReturn("https://storage.googleapis.com/signed-url-ejemplo");

        // When / Then
        mockMvc.perform(get("/api/archivos/1/url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://storage.googleapis.com/signed-url-ejemplo"));

        verify(service, times(1)).obtenerArchivo(1L);
        verify(service, times(1)).generarSignedUrl("uuid_imagen.png");
    }

    // ============================
    // DELETE /api/archivos/{id}
    // ============================

    @Test
    void eliminarArchivo_whenExists_returns204() throws Exception {
        // Given
        doNothing().when(service).eliminarArchivo(1L);

        // When / Then
        mockMvc.perform(delete("/api/archivos/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).eliminarArchivo(1L);
    }
}
