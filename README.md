# RedMuqui Platform Backend

Backend de la Plataforma de Gestión de Proyectos, Informes y Trazabilidad Institucional de Red Muqui.

> **Versión 0.1.0-SNAPSHOT — Esqueleto inicial**
> Curso 1INF47 (PUCP)

## Stack

- **Java 21 LTS**
- **Spring Boot 3.3.5**
- **PostgreSQL** como BD relacional
- **Spring Security + JWT** para autenticación
- **Spring Data JPA / Hibernate** como ORM
- **Lombok** para reducir boilerplate
- **springdoc-openapi** para documentación de API (Swagger UI)
- **Maven** como build tool

## Estructura del proyecto

Arquitectura híbrida: organización por **módulos de dominio**, con **capas internas** (controller → service → repository → entity / dto).

```
com.redmuqui.platform
├── PlatformApplication.java        # Entry point
├── config/                          # Configuración transversal (Security, OpenAPI, CORS, etc.)
├── common/                          # Código compartido (excepciones, DTOs base, auditoría)
├── auth/                            # Login, refresh token, recuperación de cuenta
├── usuario/                         # Gestión de usuarios (RF-001 a RF-018)
├── rol/                             # Roles y permisos
├── macroregion/                     # Catálogo
├── institucion/                     # Catálogo
├── territorio/                      # Catálogo
├── ejetematico/                     # Catálogo
├── proyecto/                        # Gestión de proyectos
├── actividad/                       # Actividades e hitos
├── documento/                       # Documentos y archivos
├── trazabilidad/                    # Bitácora y observaciones
└── reporte/                         # Reportes e indicadores
```

## Configuración local

### Requisitos previos

- JDK 21 instalado (`java -version` debe mostrar 21.x)
- Maven 3.9+ (`mvn -version`)
- PostgreSQL 14+ corriendo localmente
- IDE recomendado: IntelliJ IDEA, VS Code con extensión "Extension Pack for Java", o Eclipse

### Crear la base de datos

```sql
CREATE DATABASE redmuqui_dev;
CREATE USER redmuqui WITH PASSWORD 'redmuqui';
GRANT ALL PRIVILEGES ON DATABASE redmuqui_dev TO redmuqui;
```

### Variables de entorno

Crear archivo `.env` o exportar antes de ejecutar:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=redmuqui_dev
export DB_USER=redmuqui
export DB_PASSWORD=redmuqui
export JWT_SECRET=$(openssl rand -base64 32)
```

### Ejecutar

```bash
mvn spring-boot:run
```

La aplicación arranca en `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Perfil de desarrollo (sin auth)

Para desarrollo local sin pelearse con tokens JWT:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

En perfil `dev`, todos los endpoints están abiertos (no requieren autenticación). **Nunca usar este perfil en producción.**

## Estado actual del esqueleto

✅ Estructura de paquetes completa por módulo
✅ Entidades JPA del modelo de dominio (Usuario, Rol, Permiso, Proyecto, Actividad, Hito, Documento, Archivo, etc.)
✅ Enumeraciones (EstadoProyecto, EstadoActividad, EstadoHito, EstadoDocumento, EstadoObservacion)
✅ Repositorios JPA base
✅ Esqueleto de servicios y controllers para módulos principales
✅ DTOs base (Create / Update / Response)
✅ Spring Security + JWT configurado
✅ Manejo global de excepciones
✅ Auditoría automática (createdAt / updatedAt)
✅ Configuración OpenAPI / Swagger

⏳ Pendiente (por confirmar con cliente):
- Lógica de `rolEnProyecto` (¿etiqueta o controla permisos?)
- Versionado fino de Documento ↔ Archivo
- Matriz exacta de permisos por rol
- Decisión sobre Flyway

## Decisiones técnicas tomadas

Ver documento de esqueleto: `Esqueleto_Backend_RedMuqui_v0.1.docx`.

## Comandos útiles

```bash
mvn clean install              # Build completo + tests
mvn test                       # Solo tests
mvn spring-boot:run            # Ejecutar la app
mvn dependency:tree            # Ver árbol de dependencias
```

## Próximos pasos sugeridos

1. Configurar el repositorio en GitHub.
2. Configurar GitHub Actions para CI (build + tests).
3. Crear las primeras migraciones de Flyway (cuando el equipo decida).
4. Completar la implementación de servicios y controllers en cada módulo.
5. Agregar tests unitarios y de integración con `@SpringBootTest` y/o Testcontainers.
