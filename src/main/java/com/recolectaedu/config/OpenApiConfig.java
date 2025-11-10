package com.recolectaedu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recolectaEduOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080/api/v1");
        localServer.setDescription("Servidor de desarrollo local");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.fintech.com/api/v1");
        prodServer.setDescription("Servidor de producción");

        Contact contact = new Contact();
        contact.setName("Equipo RecolectaEdu API");
        contact.setEmail("support@recoletaedu.com");
        contact.setUrl("https://www.recolectaedu.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("RecolectaEdu")
                .version("1.0.0")
                .contact(contact)
                .description("""
                        API REST para operaciones bancarias básicas desarrollada con Spring Boot 3.5.7 y Java 21.
                        
                        ## Características principales:
                        - Gestión completa de cuentas bancarias
                        - Procesamiento de transacciones (depósitos y retiros)
                        - Validaciones de negocio robustas
                        - Manejo global de excepciones
                        - API REST versionada (/api/v1)
                        
                        ## Tecnologías:
                        - **Java 21**
                        - **Spring Boot 3.5.7**
                        - **PostgreSQL** (Producción)
                        - **H2 Database** (Tests)
                        - **JUnit 5 & Mockito** (Testing)""")
                .termsOfService("https://www.recolectaedu.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, prodServer));
    }

}
