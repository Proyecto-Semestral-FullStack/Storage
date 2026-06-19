package ms_Storage.storage.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "archivo_media")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ArchivoMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreOriginal;

    @Column(nullable = false, unique = true)
    private String nombreAlmacenado;

    @Column(nullable = false)
    private String tipoMime;

    @Column(nullable = false)
    private Long tamanioBytes;


}
