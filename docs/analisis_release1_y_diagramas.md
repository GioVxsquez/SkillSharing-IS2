# analisis release 1 y diagramas plantuml

## alcance leido del documento release 1

El documento `TrabajoSoft_Documentacion release 1 (1).docx` define estas historias principales del release:

| id | historia del documento | cobertura actual |
| --- | --- | --- |
| US01 | registro de usuario | cubierto por `POST /api/auth/registro` |
| US02 | inicio de sesion | cubierto por `POST /api/auth/login` con JWT |
| US03 | registro de perfil con habilidad principal | cubierto parcialmente por perfil y multiples habilidades |
| US04 | visualizar catalogo de mentores/sesiones | cubierto como catalogo de sesiones activas, no catalogo directo de mentores |
| US05 | disponibilidad horaria o material educativo, segun seccion del documento | backend cubre material educativo, no horarios disponibles |
| US06 | solicitar sesion de aprendizaje a mentor | cubierto como inscripcion a sesion o invitacion privada, no como solicitud pendiente a mentor |

## incongruencias para conversar con el grupo

1. El documento dice "catalogo de mentores", pero la app muestra "sesiones disponibles".
2. El documento menciona horarios disponibles del mentor, pero la base de datos no tiene entidad de horarios.
3. El documento tambien menciona material educativo como parte del sprint, y el backend si lo tiene, pero el frontend movil no tiene pantalla para subir material.
4. La solicitud del documento queda en estado "pendiente"; el codigo actual maneja inscripcion directa o invitacion privada.
5. La app movil tiene un switch de sesion privada, pero el backend no guarda un campo de privacidad de sesion. Hoy se conserva como sesion publica para no romper el flujo.
6. La app pide duracion, pero la base de datos no tiene columna de duracion. El backend responde una duracion fija de 60 minutos solo para que la pantalla no quede vacia.
7. Las sesiones nuevas quedan en estado `PENDIENTE`. No salen en Home hasta que un admin las apruebe, y no hay pantalla movil de admin.
8. La verificacion por correo existe en codigo y base de datos, pero esta desactivada en registro por bloqueo SMTP de Render gratuito.
9. Hay funcionalidades extra frente al release: notificaciones, admin, busqueda por habilidad, invitaciones privadas y JWT.

## congruencia frontend backend corregida

| pantalla | llamada frontend | estado |
| --- | --- | --- |
| home | `GET /api/sesiones/publicas` | corregido con alias backend |
| mis sesiones | `GET /api/sesiones/mis-sesiones` | corregido con alias backend |
| mis asistencias | `GET /api/sesiones/mis-inscripciones` | corregido con alias backend |
| detalle | `POST /api/sesiones/{id}/inscribirse` | corregido con alias backend |
| invitaciones | `PUT /api/invitaciones/{id}/responder` | corregido con alias backend |
| invitar asistentes | `POST /api/invitaciones` con body | corregido con alias backend |
| perfil habilidades | `PUT /api/usuarios/me/habilidades` | backend acepta objeto `{ habilidadIds: [] }` |
| respuestas api | frontend revisa `ok` | corregido donde usaba `exito` |

## der plantuml

```plantuml
@startuml
hide circle
skinparam linetype ortho

entity "usuario" as usuario {
  * usuario_id : bigint <<pk>>
  --
  nombre : varchar
  email : varchar <<unique>>
  password : varchar
  foto_perfil : varchar
  rol : varchar
  activo : boolean
  fecha_registro : timestamp
}

entity "habilidad" as habilidad {
  * habilidad_id : bigint <<pk>>
  --
  nombre : varchar <<unique>>
  descripcion : varchar
}

entity "usuario_habilidad" as usuario_habilidad {
  * usuario_id : bigint <<pk, fk>>
  * habilidad_id : bigint <<pk, fk>>
}

entity "sesion_aprendizaje" as sesion {
  * sesion_id : bigint <<pk>>
  --
  titulo : varchar
  descripcion : text
  fecha_sesion : timestamp
  modalidad : varchar
  estado : varchar
  max_participantes : integer
  link_sesion : varchar
  lugar : varchar
  instructor_id : bigint <<fk>>
  habilidad_id : bigint <<fk>>
  material_cargado : boolean
  fecha_creacion : timestamp
}

entity "material_educativo" as material {
  * material_id : bigint <<pk>>
  --
  nombre : varchar
  ruta_archivo : varchar
  tipo_archivo : varchar
  sesion_id : bigint <<fk>>
  fecha_subida : timestamp
}

entity "inscripcion" as inscripcion {
  * inscripcion_id : bigint <<pk>>
  --
  sesion_id : bigint <<fk>>
  usuario_id : bigint <<fk>>
  rol_sesion : varchar
  fecha_inscripcion : timestamp
}

entity "invitaciones" as invitacion {
  * invitacion_id : bigint <<pk>>
  --
  sesion_id : bigint <<fk>>
  invitado_id : bigint <<fk>>
  estado : varchar
  fecha_envio : timestamp
}

entity "notificacion" as notificacion {
  * notificacion_id : bigint <<pk>>
  --
  usuario_id : bigint <<fk>>
  sesion_id : bigint <<fk nullable>>
  mensaje : varchar
  visto : boolean
  fecha_creacion : timestamp
}

entity "verificacion_token" as token {
  * token_id : bigint <<pk>>
  --
  token : varchar <<unique>>
  usuario_id : bigint <<fk>>
  fecha_expira : timestamp
  usado : boolean
}

usuario ||--o{ usuario_habilidad
habilidad ||--o{ usuario_habilidad
usuario ||--o{ sesion : instructor
habilidad ||--o{ sesion : requerida
sesion ||--o{ material
sesion ||--o{ inscripcion
usuario ||--o{ inscripcion
sesion ||--o{ invitacion
usuario ||--o{ invitacion : invitado
usuario ||--o{ notificacion
sesion ||--o{ notificacion
usuario ||--o{ token
@enduml
```

## diagrama de componentes

```plantuml
@startuml
skinparam componentStyle rectangle

actor "usuario movil" as user

component "React Native App" as app {
  [LoginScreen]
  [RegisterScreen]
  [HomeScreen]
  [MisSesionesScreen]
  [PerfilScreen]
  [InvitacionesScreen]
}

component "Render Web Service" as render {
  component "Spring Boot API" as api
  component "Security JWT" as security
  component "Controllers REST" as controllers
  component "Services" as services
  component "Repositories JPA" as repos
  component "Flyway migrations" as flyway
}

database "Supabase PostgreSQL" as db
cloud "Almacen local / futuro storage" as storage

user --> app
app --> api : HTTPS JSON
api --> security : valida token
api --> controllers
controllers --> services
services --> repos
repos --> db : JDBC pooler
flyway --> db : migraciones V1 V2 V3
services --> storage : material educativo
@enduml
```

## diagrama de clases

```plantuml
@startuml
skinparam classAttributeIconSize 0
skinparam packageStyle rectangle

package "domain.entity" {
  class Usuario {
    - usuarioId : Long
    - nombre : String
    - email : String
    - password : String
    - rol : RolUsuario
    - activo : Boolean
    - habilidades : Set<Habilidad>
  }

  class Habilidad {
    - habilidadId : Long
    - nombre : String
    - descripcion : String
  }

  class SesionAprendizaje {
    - sesionId : Long
    - titulo : String
    - fechaSesion : LocalDateTime
    - modalidad : ModalidadSesion
    - estado : EstadoSesion
    - maxParticipantes : Integer
    - materialCargado : Boolean
  }

  class MaterialEducativo {
    - materialId : Long
    - nombre : String
    - rutaArchivo : String
    - tipoArchivo : String
  }

  class Inscripcion {
    - inscripcionId : Long
    - rolSesion : String
    - fechaInscripcion : LocalDateTime
  }

  class Invitacion {
    - invitacionId : Long
    - estado : EstadoInvitacion
    - fechaEnvio : LocalDateTime
  }

  class Notificacion {
    - notificacionId : Long
    - mensaje : String
    - visto : Boolean
  }
}

package "application.service" {
  class SesionService {
    + crearSesion(instructorId, dto)
    + listarActivas()
    + obtenerDetalle(sesionId)
    + listarPorInstructor(instructorId)
  }

  class UsuarioService {
    + actualizarHabilidades(usuarioId, habilidadesIds)
    + buscarPorId(usuarioId)
  }

  class InscripcionService {
    + inscribir(sesionId, usuarioId)
    + listarPorUsuario(usuarioId)
  }

  class InvitacionService {
    + enviarInvitacion(organizadorId, sesionId, invitadoId)
    + listarMisInvitaciones(usuarioId)
    + responderInvitacion(invitadoId, invitacionId, aceptar)
  }

  class MaterialService {
    + subirMaterial(sesionId, instructorId, dto, archivo)
    + listarPorSesion(sesionId)
  }
}

package "presentation.controller" {
  class AuthController
  class UsuarioController
  class SesionController
  class InvitacionController
  class MaterialController
  class AdminController
}

package "patterns" {
  class SesionFactory <<factory>>
  class SesionFacade <<facade>>
  interface SesionObserver <<observer>>
  interface BusquedaStrategy <<strategy>>
}

Usuario "1" -- "0..*" SesionAprendizaje : instructor
Usuario "0..*" -- "0..*" Habilidad
Habilidad "1" -- "0..*" SesionAprendizaje
SesionAprendizaje "1" -- "0..*" MaterialEducativo
SesionAprendizaje "1" -- "0..*" Inscripcion
Usuario "1" -- "0..*" Inscripcion
SesionAprendizaje "1" -- "0..*" Invitacion
Usuario "1" -- "0..*" Invitacion : invitado
Usuario "1" -- "0..*" Notificacion
SesionAprendizaje "0..1" -- "0..*" Notificacion

AuthController --> UsuarioService
UsuarioController --> UsuarioService
SesionController --> SesionService
SesionController --> InscripcionService
InvitacionController --> InvitacionService
MaterialController --> MaterialService
AdminController --> SesionFacade
SesionService --> SesionFactory
SesionFacade --> SesionObserver
@enduml
```

## secuencia US01 registro de usuario

```plantuml
@startuml
actor Usuario
participant "RegisterScreen" as app
participant "AuthController" as auth
participant "UsuarioRepository" as repo
participant "PasswordEncoder" as encoder
database "Supabase" as db

Usuario -> app : completa formulario
app -> auth : POST /api/auth/registro
auth -> repo : existsByEmail(email)
repo -> db : select usuario por email
db --> repo : existe o no existe

alt email ya registrado
  auth --> app : ok=false
else email disponible
  auth -> encoder : encode(password)
  encoder --> auth : hash bcrypt
  auth -> repo : save(usuario activo)
  repo -> db : insert usuario
  db --> repo : usuario_id
  auth --> app : ok=true
end
@enduml
```

## secuencia US02 inicio de sesion

```plantuml
@startuml
actor Usuario
participant "LoginScreen" as app
participant "AuthController" as auth
participant "AuthenticationManager" as manager
participant "UserDetailsServiceImpl" as uds
participant "UsuarioRepository" as repo
participant "JwtUtil" as jwt
database "Supabase" as db

Usuario -> app : ingresa email y password
app -> auth : POST /api/auth/login
auth -> manager : authenticate(email,password)
manager -> uds : loadUserByUsername(email)
uds -> repo : findByEmail(email)
repo -> db : select usuario
db --> repo : usuario
uds --> manager : UserDetails
manager --> auth : Authentication
auth -> jwt : generateToken(userDetails)
jwt --> auth : token
auth --> app : ok=true + token
app -> app : guarda token en AsyncStorage
@enduml
```

## secuencia US03 perfil y habilidades

```plantuml
@startuml
actor Usuario
participant "PerfilScreen" as app
participant "UsuarioController" as usuarios
participant "HabilidadController" as habCtrl
participant "UsuarioService" as service
participant "UsuarioRepository" as userRepo
participant "HabilidadRepository" as habRepo
database "Supabase" as db

Usuario -> app : abre perfil
app -> usuarios : GET /api/usuarios/me
usuarios -> userRepo : findByEmail(jwt.email)
userRepo -> db : select usuario
db --> userRepo : usuario
usuarios --> app : datos de perfil

app -> habCtrl : GET /api/habilidades
habCtrl -> habRepo : findAll()
habRepo -> db : select habilidades
db --> habRepo : lista
habCtrl --> app : habilidades disponibles

Usuario -> app : selecciona habilidades
app -> usuarios : PUT /api/usuarios/me/habilidades
usuarios -> service : actualizarHabilidades(usuarioId, ids)
service -> habRepo : findAllById(ids)
habRepo -> db : select habilidades
service -> userRepo : save(usuario)
userRepo -> db : update usuario_habilidad
usuarios --> app : ok=true
@enduml
```

## secuencia US04 catalogo de sesiones

```plantuml
@startuml
actor Usuario
participant "HomeScreen" as app
participant "SesionController" as controller
participant "SesionService" as service
participant "SesionRepository" as repo
database "Supabase" as db

Usuario -> app : entra al home
app -> controller : GET /api/sesiones/publicas
controller -> service : listarActivas()
service -> repo : findByEstado(ACTIVA)
repo -> db : select sesiones activas
db --> repo : lista
repo --> service : entidades
service --> controller : sesiones
controller --> app : lista de SesionResponseDto
@enduml
```

## secuencia US05 material educativo

```plantuml
@startuml
actor Instructor
participant "Cliente movil/Postman" as app
participant "MaterialController" as controller
participant "UsuarioRepository" as userRepo
participant "MaterialService" as service
participant "SesionRepository" as sesionRepo
participant "MaterialRepository" as materialRepo
database "Supabase" as db
collections "uploads/" as storage

Instructor -> app : selecciona archivo educativo
app -> controller : POST /api/sesiones/{id}/materiales multipart
controller -> userRepo : findByEmail(jwt.email)
userRepo -> db : select usuario
controller -> service : subirMaterial(sesionId,instructorId,dto,archivo)
service -> sesionRepo : findById(sesionId)
sesionRepo -> db : select sesion
service -> storage : guardar archivo
service -> materialRepo : save(material)
materialRepo -> db : insert material_educativo
service -> sesionRepo : save(materialCargado=true)
sesionRepo -> db : update sesion
controller --> app : ok=true
@enduml
```

## secuencia US06 solicitar participacion en sesion

```plantuml
@startuml
actor Aprendiz
participant "DetalleSesionScreen" as app
participant "SesionController" as controller
participant "InscripcionService" as service
participant "SesionRepository" as sesionRepo
participant "UsuarioRepository" as userRepo
participant "InscripcionRepository" as insRepo
participant "SesionObserver" as observer
database "Supabase" as db

Aprendiz -> app : toca confirmar asistencia
app -> controller : POST /api/sesiones/{id}/inscribirse
controller -> userRepo : findByEmail(jwt.email)
userRepo -> db : select usuario
controller -> service : inscribir(sesionId, usuarioId)
service -> sesionRepo : findById(sesionId)
sesionRepo -> db : select sesion
service -> userRepo : findById(usuarioId)
userRepo -> db : select usuario
service -> insRepo : existsBySesionAndUsuario
insRepo -> db : select inscripcion
service -> insRepo : save(inscripcion)
insRepo -> db : insert inscripcion
service -> observer : onSesionActualizada(sesion, "INSCRIPCION")
controller --> app : ok=true
@enduml
```

## secuencia extra invitacion privada

```plantuml
@startuml
actor Instructor
actor Aprendiz
participant "InvitarAsistentesScreen" as invitar
participant "InvitacionesScreen" as inbox
participant "InvitacionController" as controller
participant "InvitacionService" as service
participant "InvitacionRepository" as repo
participant "InscripcionService" as insService
database "Supabase" as db

Instructor -> invitar : busca usuario y toca invitar
invitar -> controller : POST /api/invitaciones
controller -> service : enviarInvitacion(organizadorId,sesionId,invitadoId)
service -> repo : save(PENDIENTE)
repo -> db : insert invitacion
controller --> invitar : ok=true

Aprendiz -> inbox : abre mis invitaciones
inbox -> controller : GET /api/invitaciones/mis-invitaciones
controller -> service : listarMisInvitaciones(usuarioId)
service -> repo : findByInvitadoAndEstado(PENDIENTE)
repo -> db : select invitaciones
controller --> inbox : lista

Aprendiz -> inbox : acepta
inbox -> controller : PUT /api/invitaciones/{id}/responder?aceptar=true
controller -> service : responderInvitacion(invitadoId,id,true)
service -> repo : findById(id)
service -> insService : inscribir(sesionId,invitadoId)
service -> repo : save(ACEPTADA)
repo -> db : update invitacion
controller --> inbox : ok=true
@enduml
```
