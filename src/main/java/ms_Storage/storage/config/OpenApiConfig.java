package ms_Storage.storage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
public class OpenApiConfig {


    @Bean
    public OpenAPI storageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-Storage API")
                        .description(
                                "Microservicio de gestión de archivos multimedia sobre **Google Cloud Storage**.\n\n" +
                                        "### Flujo típico de uso:\n" +
                                        "1. `POST /api/archivos` — Subir el archivo (retorna el `id`).\n" +
                                        "2. `GET /api/archivos/{id}/url` — Obtener una Signed URL temporal (válida 15 min) para acceder al archivo.\n" +
                                        "3. `DELETE /api/archivos/{id}` — Eliminar el archivo del bucket y la base de datos.\n\n" +
                                        "> ️ El bucket es **privado**. Nunca se exponen URLs públicas permanentes."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Backend")
                                .email("backend@empresa.com")
                        )
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                        )
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Entorno local"),
                        new Server().url("https://api.empresa.com").description("Producción")
                ));
    }
}
