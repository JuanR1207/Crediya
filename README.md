# CrediYa - Microservicio de Autenticación (`autenticacion-crediya-ms`)

Stack: Java 17, Spring Boot (Spring MVC), Spring Data JPA, H2, arquitectura hexagonal (scaffold Clean Architecture de Bancolombia).

## HU1 - Registrar usuarios

`POST /api/v1/usuarios`

```json
{
  "nombres": "Juan José",
  "apellidos": "Rendón Pérez",
  "fecha_nacimiento": "2000-05-10",
  "direccion": "Calle 10 # 20-30",
  "telefono": "3001234567",
  "correo_electronico": "juan@correo.com",
  "documento_identidad": "1020304050",
  "salario_base": 2500000
}
```

| Respuesta | Cuándo |
|---|---|
| `201 Created` | Usuario registrado (header `Location: /api/v1/usuarios/{id}`) |
| `400 Bad Request` (`USR-002`) | Campos obligatorios vacíos (nombres, apellidos, correo_electronico, salario_base), correo con formato inválido, salario fuera de 0 - 15.000.000, etc. Se listan todos los errores en `detalles` |
| `400 Bad Request` (`GEN-400`) | JSON mal formado o tipos incorrectos (ej. `salario_base` no numérico) |
| `409 Conflict` (`USR-003` / `USR-004`) | Correo o documento ya registrado |
| `500` (`GEN-500`) | Error inesperado (sin exponer detalles internos) |

Formato de error:

```json
{
  "codigo": "USR-002",
  "mensaje": "La información enviada no es válida",
  "detalles": ["El campo salario_base debe estar entre 0 y 15000000"],
  "ruta": "/api/v1/usuarios",
  "timestamp": "2026-11-25T02:00:00Z"
}
```

### Dónde está cada cosa

- `domain/model` → `Usuario`, puertos `UsuarioRepository` y `TransaccionGateway`, `BusinessException` / `TipoError`.
- `domain/usecase` → `RegistrarUsuarioUseCase` (orquesta) y `UsuarioValidador` (reglas de la HU).
- `infrastructure/driven-adapters/jpa-repository` → entidad `UsuarioData`, `UsuarioRepositoryAdapter` y `JpaTransaccionAdapter` (`@Transactional`: verificar duplicados + guardar en una sola transacción).
- `infrastructure/entry-points/api-rest` → `UsuarioController`, DTOs y `GlobalExceptionHandler`.

### Ejecutar

```bash
./gradlew clean build        # compila y corre las pruebas
./gradlew bootRun            # o ejecutar MainApplication
```

- Swagger: http://localhost:8080/v3/swagger-ui.html
- Consola H2: http://localhost:8080/h2 (JDBC URL `jdbc:h2:file:./data/autenticacion-db`, usuario `sa`, sin clave)

---

# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**
