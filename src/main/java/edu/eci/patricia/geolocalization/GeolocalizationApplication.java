package edu.eci.patricia.geolocalization;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(
        title = "PATRICIA Geolocation Service",
        version = "v1",
        description = "RF06 — Motor contextual de geolocalización en el campus universitario ECI. " +
                "Expone endpoints para actualizar y consultar la ubicación de estudiantes activos. " +
                "Módulo M07 desarrollado por el equipo Snorlax Energy.",
        contact = @Contact(name = "Snorlax Energy", email = "snorlax@escuelaing.edu.co")
))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT token issued by M01 — Auth Service. Include as: Authorization: Bearer <token>"
)
@EnableFeignClients
@SpringBootApplication
public class GeolocalizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeolocalizationApplication.class, args);
    }
}
