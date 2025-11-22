# To-Do App API REST

API REST para gestión de tareas (To-Do App) desarrollada con Java Spring Boot, PostgreSQL y Docker.

## Características

- **Arquitectura por capas**: Modelos, Repositorios, Servicios y Controladores
- **Base de datos**: PostgreSQL con Docker
- **Pruebas completas**:
  - Pruebas unitarias para servicios (con Mockito)
  - Pruebas de integración para repositorios (con H2 en memoria)
  - Pruebas E2E que validan flujos completos (con H2 en memoria)
- **Análisis estático de código**: Checkstyle y SpotBugs
- **CI/CD**: Pipeline de GitHub Actions
- **Containerización**: Docker y Docker Compose

## Estructura de la Base de Datos

### Tabla `users`
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `name` (VARCHAR, NOT NULL)
- `email` (VARCHAR, NOT NULL, UNIQUE)

### Tabla `tasks`
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `title` (VARCHAR, NOT NULL)
- `description` (VARCHAR(1000))
- `is_completed` (BOOLEAN, NOT NULL, DEFAULT false)
- `user_id` (BIGINT, FK → users.id, NOT NULL)

## Requisitos Previos

- Docker y Docker Compose
- Java 21 (opcional, si no usas Docker)
- Gradle (opcional, incluido wrapper)

## Instalación y Ejecución

### Opción 1: Usando Docker Compose (Recomendado)

1. **Iniciar la aplicación y la base de datos:**

```bash
docker-compose up --build
```

La API estará disponible en `http://localhost:8080`

2. **Detener los servicios:**

```bash
docker-compose down
```

### Opción 2: Ejecutar localmente (sin Docker para la app)

1. **Iniciar solo PostgreSQL con Docker:**

```bash
docker-compose up postgres
```

2. **Compilar y ejecutar la aplicación:**

```bash
./gradlew bootRun
```

## Endpoints de la API

### Usuarios

#### Crear usuario
```http
POST /api/users
Content-Type: application/json

{
  "name": "Juan Pérez",
  "email": "juan@example.com"
}
```

#### Obtener todos los usuarios
```http
GET /api/users
```

#### Obtener usuario por ID
```http
GET /api/users/{id}
```

#### Actualizar usuario
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "name": "Juan Pérez Actualizado",
  "email": "juan.nuevo@example.com"
}
```

#### Eliminar usuario
```http
DELETE /api/users/{id}
```

### Tareas

#### Crear tarea
```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Completar documentación",
  "description": "Escribir README completo",
  "isCompleted": false,
  "userId": 1
}
```

#### Obtener todas las tareas
```http
GET /api/tasks
```

#### Obtener tarea por ID
```http
GET /api/tasks/{id}
```

#### Obtener tareas de un usuario
```http
GET /api/tasks/user/{userId}
```

#### Actualizar tarea
```http
PUT /api/tasks/{id}
Content-Type: application/json

{
  "title": "Título actualizado",
  "description": "Descripción actualizada",
  "isCompleted": true,
  "userId": 1
}
```

#### Actualizar solo el estado de una tarea
```http
PATCH /api/tasks/{id}/status
Content-Type: application/json

{
  "isCompleted": true
}
```

#### Eliminar tarea
```http
DELETE /api/tasks/{id}
```

## Ejecutar Pruebas

### Todas las pruebas
```bash
./gradlew test
```

### Solo pruebas unitarias
```bash
./gradlew test --tests "*.service.*Test"
```

### Solo pruebas de integración
```bash
./gradlew test --tests "*.repository.*IntegrationTest"
```

### Solo pruebas E2E
```bash
./gradlew test --tests "*E2ETest"
```

## Análisis Estático de Código

### Ejecutar Checkstyle
```bash
./gradlew checkstyleMain checkstyleTest
```

### Ejecutar SpotBugs
```bash
./gradlew spotbugsMain
```

Los reportes se generan en:
- Checkstyle: `build/reports/checkstyle/`
- SpotBugs: `build/reports/spotbugs/`

## Pipeline CI/CD

El pipeline de GitHub Actions se ejecuta automáticamente en:
- Push a ramas: `master`, `main`, `develop`
- Pull requests a estas ramas

El pipeline ejecuta:
1. Build del proyecto
2. Análisis estático (Checkstyle y SpotBugs)
3. Pruebas unitarias
4. Pruebas de integración
5. Pruebas E2E
6. Generación de reportes

Si todas las etapas pasan correctamente, imprime "OK" y el pipeline se marca como exitoso. Si alguna falla, el pipeline falla.

## Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 3.4.0**
  - Spring Web
  - Spring Data JPA
  - Spring Validation
- **PostgreSQL 16** (producción)
- **H2 Database** (pruebas)
- **Lombok** - Reducción de código boilerplate
- **Gradle** - Gestión de dependencias y build
- **JUnit 5** - Framework de pruebas
- **Mockito** - Mocking para pruebas unitarias
- **Testcontainers** (opcional) - Pruebas de integración con contenedores
- **Checkstyle** - Análisis estático de estilo de código
- **SpotBugs** - Detección de bugs
- **Docker & Docker Compose** - Containerización
- **GitHub Actions** - CI/CD

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/cue/edu/co/parcial/
│   │   ├── controller/       # Controladores REST
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Manejo de excepciones
│   │   ├── model/            # Entidades JPA
│   │   ├── repository/       # Repositorios Spring Data
│   │   ├── service/          # Lógica de negocio
│   │   └── ParcialApplication.java
│   └── resources/
│       └── application.properties
├── test/
│   ├── java/cue/edu/co/parcial/
│   │   ├── repository/       # Pruebas de integración
│   │   ├── service/          # Pruebas unitarias
│   │   └── E2ETest.java      # Pruebas end-to-end
│   └── resources/
│       └── application-test.properties
├── config/
│   └── checkstyle/
│       └── checkstyle.xml
├── .github/
│   └── workflows/
│       └── ci.yml
├── build.gradle
├── docker-compose.yml
├── Dockerfile
└── README.md
```

## Ejemplo de Flujo E2E

La prueba E2E (`E2ETest.java`) valida el siguiente flujo completo:

1. Crear un usuario
2. Crear 3 tareas para ese usuario
3. Listar todas las tareas del usuario (verifica que haya 3)
4. Actualizar el estado de una tarea a completada
5. Actualizar completamente otra tarea
6. Eliminar una tarea
7. Verificar que quedan 2 tareas
8. Eliminar el usuario (cascade delete de tareas)
9. Verificar que el usuario fue eliminado

## Variables de Entorno

Puedes configurar las siguientes variables de entorno:

- `DB_HOST` - Host de PostgreSQL (default: localhost)
- `DB_PORT` - Puerto de PostgreSQL (default: 5432)
- `DB_NAME` - Nombre de la base de datos (default: tododb)
- `DB_USERNAME` - Usuario de PostgreSQL (default: postgres)
- `DB_PASSWORD` - Contraseña de PostgreSQL (default: postgres)

## Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## Autor

Desarrollado como proyecto parcial para la Universidad CUE.
