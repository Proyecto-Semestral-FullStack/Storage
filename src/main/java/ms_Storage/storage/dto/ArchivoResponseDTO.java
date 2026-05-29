package ms_Storage.storage.dto;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ArchivoResponseDTO {
    private Long id;
    private String nombreOriginal;
    private String urlPublica;
}
