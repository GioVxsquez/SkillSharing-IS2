# analisis release 1 y diagramas plantuml

Documento base: `Proyecto Inge Software-1.pdf`

Adaptacion actual: EventMaster se transforma en SkillSharing. Por eso, `Evento` se interpreta como `Sesion de Aprendizaje`, las categorias pasan a `Habilidad`, los recursos pasan a `Material educativo`, y el enfoque ODS se mueve hacia ODS 4 educacion de calidad.

## historias del release 1

Segun la tabla del Sprint Planning 1 del PDF, el Release 1 incluye:

| id | historia original | adaptacion skillsharing |
| --- | --- | --- |
| UH01 | Crear evento | Crear sesion de aprendizaje |
| UH02 | Visualizar eventos gestionados | Visualizar sesiones que gestiono |
| UH04 | Detalle de evento | Detalle de sesion |
| UH06 | Invitar asistentes | Invitar aprendices a sesion privada |
| UH07 | Confirmar asistencia privada | Aceptar o rechazar invitacion privada |
| UH10 | Visualizar eventos a los que asisto | Visualizar sesiones a las que asisto |
| UH14 | Registrarse | Crear cuenta |
| UH15 | Iniciar sesion | Login con JWT |
| UH16 | Visualizar eventos publicos | Ver sesiones publicas activas |
| UH17 | Confirmar asistencia publica | Inscribirse a sesion publica |
| UH23 | Visualizar mi perfil | Ver perfil propio |
| UH24 | Cerrar sesion | Eliminar token local |
| UH26 | Visualizar invitados | Ver participantes confirmados |
| UH28 | Visualizar invitaciones privadas | Bandeja de invitaciones |
| UH29 | Activar cuenta | Endpoint de verificacion de cuenta |

## analisis del frontend

| id | pantalla frontend | estado | observacion |
| --- | --- | --- | --- |
| UH01 | `CrearSesionScreen` | cubierto | Ya envia `tipo` PUBLICA/PRIVADA y crea la sesion como pendiente |
| UH02 | `MisSesionesScreen` | cubierto | Tab `Que gestiono` consume `/sesiones/mis-sesiones` |
| UH04 | `DetalleSesionScreen` | cubierto | Muestra datos principales de la sesion |
| UH06 | `InvitarAsistentesScreen` | cubierto | Busca usuarios y envia invitacion a sesiones privadas |
| UH07 | `InvitacionesScreen` | cubierto | Permite aceptar o rechazar invitaciones |
| UH10 | `MisSesionesScreen` | cubierto | Tab `Que asisto` consume `/sesiones/mis-inscripciones` |
| UH14 | `RegisterScreen` | cubierto | Registra usuario inactivo y muestra mensaje para revisar correo |
| UH15 | `LoginScreen` | cubierto | Guarda JWT en AsyncStorage |
| UH16 | `HomeScreen` | cubierto | Lista sesiones publicas activas |
| UH17 | `DetalleSesionScreen` | cubierto | Boton confirmar asistencia para sesiones publicas activas |
| UH23 | `PerfilScreen` | cubierto | Muestra perfil y habilidades |
| UH24 | `HomeScreen` | cubierto | Boton salir elimina token local |
| UH26 | `DetalleSesionScreen` | cubierto | Muestra invitados confirmados desde `/sesiones/{id}/invitados` |
| UH28 | `InvitacionesScreen` | cubierto | Lista invitaciones privadas pendientes |
| UH29 | correo externo + navegador | cubierto | El backend envia enlace por SMTP2GO y activa la cuenta con `/api/auth/verificar` |

## faltantes o riesgos para hablar con el grupo

1. `UH29` depende de SMTP2GO y de las variables de entorno `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `APP_EMAIL_FROM` y `APP_BASE_URL` en Render.
2. El PDF original no exige `HU05` en Release 1, pero la adaptacion SkillSharing pide material educativo obligatorio para aprobar sesiones. Backend existe; falta pantalla movil para subir PDF.
3. Las sesiones creadas quedan `PENDIENTE`; para que aparezcan en Home deben aprobarse con endpoint admin. No hay pantalla movil admin.
4. `HU28` tiene doble interpretacion: en el PDF es invitaciones privadas, pero en la adaptacion del proyecto tambien se uso como buscador por habilidad. El codigo soporta ambas, pero el informe debe aclarar esta decision.
5. Aun quedan textos con tildes/emoji en algunas pantallas antiguas. Para capturas finales conviene dejar todo en ASCII o corregir codificacion completa.

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
  tipo : varchar
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

## diagrama de clases plantuml

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
    - fotoPerfil : String
    - rol : RolUsuario
    - activo : Boolean
    - fechaRegistro : LocalDateTime
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
    - descripcion : String
    - fechaSesion : LocalDateTime
    - modalidad : ModalidadSesion
    - estado : EstadoSesion
    - tipo : TipoSesion
    - maxParticipantes : Integer
    - linkSesion : String
    - lugar : String
    - materialCargado : Boolean
    - fechaCreacion : LocalDateTime
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

  class MaterialEducativo {
    - materialId : Long
    - nombre : String
    - rutaArchivo : String
    - tipoArchivo : String
    - fechaSubida : LocalDateTime
  }

  class Notificacion {
    - notificacionId : Long
    - mensaje : String
    - visto : Boolean
    - fechaCreacion : LocalDateTime
  }

  class VerificacionToken {
    - tokenId : Long
    - token : String
    - fechaExpira : LocalDateTime
    - usado : Boolean
  }
}

package "application.service" {
  class SesionService {
    + crearSesion(instructorId : Long, dto : SesionRequestDto) : SesionAprendizaje
    + listarActivas() : List<SesionAprendizaje>
    + obtenerDetalle(sesionId : Long) : SesionAprendizaje
    + listarPorInstructor(instructorId : Long) : List<SesionAprendizaje>
  }

  class InscripcionService {
    + inscribir(sesionId : Long, usuarioId : Long) : Inscripcion
    + listarPorUsuario(usuarioId : Long) : List<Inscripcion>
    + listarInvitados(sesionId : Long) : List<Inscripcion>
  }

  class InvitacionService {
    + enviarInvitacion(organizadorId : Long, sesionId : Long, invitadoId : Long) : Invitacion
    + listarMisInvitaciones(usuarioId : Long) : List<Invitacion>
    + responderInvitacion(invitadoId : Long, invitacionId : Long, aceptar : boolean) : void
  }

  class UsuarioService
  class MaterialService
  class BuscadorService
  class NotificacionService
}

package "patterns" {
  class SesionFactory <<factory>>
  class SesionFacade <<facade>>
  class SesionStateHandler <<state>>
  interface SesionObserver <<observer>>
  class NotificacionObserver <<observer>>
  interface BusquedaStrategy <<strategy>>
  class BusquedaPorHabilidad <<strategy>>
  class BusquedaPorTitulo <<strategy>>
  interface MaterialStoragePort <<adapter>>
  class MaterialStorageAdapter <<adapter>>
}

Usuario "1" -- "0..*" SesionAprendizaje : instructor
Usuario "0..*" -- "0..*" Habilidad
Habilidad "1" -- "0..*" SesionAprendizaje : requerida
SesionAprendizaje "1" -- "0..*" MaterialEducativo
SesionAprendizaje "1" -- "0..*" Inscripcion
Usuario "1" -- "0..*" Inscripcion
SesionAprendizaje "1" -- "0..*" Invitacion
Usuario "1" -- "0..*" Invitacion : invitado
Usuario "1" -- "0..*" Notificacion
SesionAprendizaje "0..1" -- "0..*" Notificacion
Usuario "1" -- "0..*" VerificacionToken

SesionService --> SesionFactory
SesionFacade --> SesionStateHandler
SesionFacade --> SesionObserver
NotificacionObserver ..|> SesionObserver
BusquedaPorHabilidad ..|> BusquedaStrategy
BusquedaPorTitulo ..|> BusquedaStrategy
BuscadorService --> BusquedaStrategy
MaterialService --> MaterialStoragePort
MaterialStorageAdapter ..|> MaterialStoragePort
@enduml
```

## diagrama de componentes plantuml

```plantuml
@startuml
skinparam componentStyle rectangle

actor "usuario android" as user

component "React Native - Expo" as mobile {
  [LoginScreen]
  [RegisterScreen]
  [HomeScreen]
  [CrearSesionScreen]
  [DetalleSesionScreen]
  [MisSesionesScreen]
  [InvitacionesScreen]
  [InvitarAsistentesScreen]
  [PerfilScreen]
  [Axios API client]
}

node "Render" as render {
  component "Spring Boot API" as api
  component "Security JWT" as jwt
  component "REST Controllers" as controllers
  component "Application Services" as services
  component "Domain Patterns" as patterns
  component "JPA Repositories" as repos
  component "Flyway" as flyway
}

database "Supabase PostgreSQL" as db
folder "uploads local / futuro storage" as storage

user --> mobile
mobile --> api : HTTPS JSON
api --> jwt : filtra token
api --> controllers
controllers --> services
services --> patterns
services --> repos
repos --> db : JDBC pooler
flyway --> db : V1 V2 V3 V4
services --> storage : material educativo
@enduml
```

## arquitectura de software plantuml

```plantuml
@startuml
skinparam packageStyle rectangle

package "capa presentacion" {
  [AuthController]
  [SesionController]
  [InvitacionController]
  [InscripcionController]
  [UsuarioController]
  [MaterialController]
  [AdminController]
  [ApiExceptionHandler]
}

package "capa aplicacion" {
  [SesionService]
  [InscripcionService]
  [InvitacionService]
  [UsuarioService]
  [BuscadorService]
  [MaterialService]
  [NotificacionService]
  [SesionFacade]
}

package "capa dominio" {
  [Usuario]
  [SesionAprendizaje]
  [Invitacion]
  [Inscripcion]
  [Habilidad]
  [MaterialEducativo]
  [Notificacion]
  [SesionFactory]
  [SesionStateHandler]
}

package "capa infraestructura" {
  [JwtAuthFilter]
  [JwtUtil]
  [SecurityConfig]
  [Repositories JPA]
  [MaterialStorageAdapter]
}

database "Supabase" as db

[React Native App] --> [AuthController]
[React Native App] --> [SesionController]
[React Native App] --> [InvitacionController]
[React Native App] --> [UsuarioController]
[React Native App] --> [InscripcionController]

[AuthController] --> [UsuarioService]
[SesionController] --> [SesionService]
[InvitacionController] --> [InvitacionService]
[InscripcionController] --> [InscripcionService]
[MaterialController] --> [MaterialService]
[AdminController] --> [SesionFacade]

[SesionService] --> [SesionAprendizaje]
[SesionService] --> [SesionFactory]
[SesionFacade] --> [SesionStateHandler]
[Repositories JPA] --> db
@enduml
```

## secuencia UH01 crear sesion

```plantuml
@startuml
actor Instructor
participant "CrearSesionScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "SesionService" as service
participant "SesionFactory" as factory
participant "SesionRepository" as repo
database "Supabase" as db

Instructor -> app : completa formulario
app -> ctrl : POST /api/sesiones
ctrl -> userRepo : findByEmail(jwt)
userRepo -> db : select usuario
ctrl -> service : crearSesion(instructorId,dto)
service -> repo : existsByTituloIgnoreCase()
service -> repo : countByInstructorAndEstadoIn()
service -> factory : crearVirtual/crearPresencial()
factory --> service : sesion PENDIENTE
service -> repo : save(sesion)
repo -> db : insert sesion_aprendizaje
ctrl --> app : ok=true
@enduml
```

## secuencia UH02 visualizar sesiones gestionadas

```plantuml
@startuml
actor Instructor
participant "MisSesionesScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "SesionService" as service
participant "SesionRepository" as repo
database "Supabase" as db

Instructor -> app : abre Mis Sesiones
app -> ctrl : GET /api/sesiones/mis-sesiones
ctrl -> userRepo : findByEmail(jwt)
userRepo -> db : select usuario
ctrl -> service : listarPorInstructor(usuarioId)
service -> repo : findByInstructorUsuarioIdAndFechaSesionAfter()
repo -> db : select sesiones gestionadas
repo --> service : lista
ctrl --> app : lista SesionResponseDto
@enduml
```

## secuencia UH04 detalle de sesion

```plantuml
@startuml
actor Usuario
participant "DetalleSesionScreen" as app
participant "SesionController" as ctrl
participant "SesionService" as service
participant "SesionRepository" as repo
database "Supabase" as db

Usuario -> app : toca una sesion
app -> ctrl : GET /api/sesiones/{id}
ctrl -> service : obtenerDetalle(id)
service -> repo : findById(id)
repo -> db : select sesion
repo --> service : sesion
ctrl --> app : detalle
@enduml
```

## secuencia UH06 invitar asistentes

```plantuml
@startuml
actor Instructor
participant "InvitarAsistentesScreen" as app
participant "UsuarioController" as userCtrl
participant "InvitacionController" as invCtrl
participant "InvitacionService" as service
participant "InvitacionRepository" as repo
participant "NotificacionRepository" as notifRepo
database "Supabase" as db

Instructor -> app : busca usuario
app -> userCtrl : GET /api/usuarios/buscar?q=texto
userCtrl -> db : select usuarios por nombre/email
userCtrl --> app : usuarios encontrados

Instructor -> app : toca Invitar
app -> invCtrl : POST /api/invitaciones
invCtrl -> service : enviarInvitacion(organizador,sesion,invitado)
service -> repo : existsBySesionAndInvitado()
service -> repo : countBySesionAndEstadoIn()
service -> repo : save(PENDIENTE)
repo -> db : insert invitaciones
service -> notifRepo : save(notificacion)
notifRepo -> db : insert notificacion
invCtrl --> app : ok=true
@enduml
```

## secuencia UH07 confirmar asistencia privada

```plantuml
@startuml
actor Aprendiz
participant "InvitacionesScreen" as app
participant "InvitacionController" as ctrl
participant "InvitacionService" as service
participant "InvitacionRepository" as repo
participant "InscripcionService" as insService
participant "InscripcionRepository" as insRepo
database "Supabase" as db

Aprendiz -> app : acepta invitacion
app -> ctrl : PUT /api/invitaciones/{id}/responder?aceptar=true
ctrl -> service : responderInvitacion(usuarioId,id,true)
service -> repo : findById(id)
repo -> db : select invitacion
service -> service : valida propietario, fecha y estado
service -> insService : inscribir(sesionId, usuarioId)
insService -> insRepo : save(inscripcion)
insRepo -> db : insert inscripcion
service -> repo : save(ACEPTADA)
repo -> db : update invitacion
ctrl --> app : ok=true
@enduml
```

## secuencia UH10 visualizar sesiones a las que asisto

```plantuml
@startuml
actor Aprendiz
participant "MisSesionesScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "InscripcionService" as service
participant "InscripcionRepository" as repo
database "Supabase" as db

Aprendiz -> app : abre tab Que asisto
app -> ctrl : GET /api/sesiones/mis-inscripciones
ctrl -> userRepo : findByEmail(jwt)
userRepo -> db : select usuario
ctrl -> service : listarPorUsuario(usuarioId)
service -> repo : findVigentesByUsuario()
repo -> db : select inscripciones
repo --> service : lista
ctrl --> app : lista de sesiones
@enduml
```

## secuencia UH14 registrarse

```plantuml
@startuml
actor Usuario
participant "RegisterScreen" as app
participant "AuthController" as ctrl
participant "UsuarioRepository" as repo
participant "PasswordEncoder" as encoder
participant "VerificacionTokenRepository" as tokenRepo
participant "EmailService" as email
database "Supabase" as db

Usuario -> app : completa registro
app -> ctrl : POST /api/auth/registro
ctrl -> repo : existsByEmail(email)
repo -> db : select usuario
alt correo existente
  ctrl --> app : ok=false
else correo nuevo
  ctrl -> encoder : encode(password)
  ctrl -> repo : save(usuario inactivo)
  repo -> db : insert usuario
  ctrl -> tokenRepo : save(token de verificacion)
  tokenRepo -> db : insert verificacion_token
  ctrl -> email : enviarCorreoVerificacion(email, token)
  email --> Usuario : correo con enlace
  ctrl --> app : ok=true, revisar correo
end
@enduml
```

## secuencia UH15 iniciar sesion

```plantuml
@startuml
actor Usuario
participant "LoginScreen" as app
participant "AuthController" as ctrl
participant "AuthenticationManager" as manager
participant "UserDetailsServiceImpl" as uds
participant "JwtUtil" as jwt
database "Supabase" as db

Usuario -> app : ingresa credenciales
app -> ctrl : POST /api/auth/login
ctrl -> manager : authenticate(email,password)
manager -> uds : loadUserByUsername(email)
uds -> db : select usuario
manager --> ctrl : autenticado
ctrl -> jwt : generateToken(userDetails)
jwt --> ctrl : token
ctrl --> app : ok=true token
app -> app : guarda token
@enduml
```

## secuencia UH16 visualizar sesiones publicas

```plantuml
@startuml
actor Usuario
participant "HomeScreen" as app
participant "SesionController" as ctrl
participant "SesionService" as service
participant "SesionRepository" as repo
database "Supabase" as db

Usuario -> app : entra al home
app -> ctrl : GET /api/sesiones/publicas
ctrl -> service : listarActivas()
service -> repo : findByEstadoAndTipoAndFechaSesionAfter(ACTIVA,PUBLICA)
repo -> db : select sesiones publicas vigentes
ctrl --> app : lista
@enduml
```

## secuencia UH17 confirmar asistencia publica

```plantuml
@startuml
actor Aprendiz
participant "DetalleSesionScreen" as app
participant "SesionController" as ctrl
participant "InscripcionService" as service
participant "SesionRepository" as sesionRepo
participant "InscripcionRepository" as insRepo
database "Supabase" as db

Aprendiz -> app : toca confirmar asistencia
app -> ctrl : POST /api/sesiones/{id}/inscribirse
ctrl -> service : inscribir(sesionId, usuarioId)
service -> sesionRepo : findById(sesionId)
sesionRepo -> db : select sesion
service -> service : valida ACTIVA, futura, cupo, limite 5
service -> insRepo : save(inscripcion)
insRepo -> db : insert inscripcion
ctrl --> app : ok=true
@enduml
```

## secuencia UH23 visualizar mi perfil

```plantuml
@startuml
actor Usuario
participant "PerfilScreen" as app
participant "UsuarioController" as ctrl
participant "UsuarioRepository" as repo
database "Supabase" as db

Usuario -> app : abre Perfil
app -> ctrl : GET /api/usuarios/me
ctrl -> repo : findByEmail(jwt)
repo -> db : select usuario y habilidades
repo --> ctrl : usuario
ctrl --> app : UsuarioResponseDto
@enduml
```

## secuencia UH24 cerrar sesion

```plantuml
@startuml
actor Usuario
participant "HomeScreen" as app
collections "AsyncStorage" as storage

Usuario -> app : toca Salir
app -> storage : removeItem(userToken)
storage --> app : token eliminado
app -> app : navigation.replace(Login)
@enduml
```

## secuencia UH26 visualizar invitados

```plantuml
@startuml
actor Organizador
participant "DetalleSesionScreen" as app
participant "SesionController" as ctrl
participant "InscripcionService" as service
participant "InscripcionRepository" as repo
database "Supabase" as db

Organizador -> app : abre detalle
app -> ctrl : GET /api/sesiones/{id}/invitados
ctrl -> service : listarInvitados(sesionId)
service -> repo : findBySesionSesionId(sesionId)
repo -> db : select inscritos
repo --> service : inscripciones
ctrl --> app : ParticipanteResponseDto[]
@enduml
```

## secuencia UH28 visualizar invitaciones privadas

```plantuml
@startuml
actor Aprendiz
participant "InvitacionesScreen" as app
participant "InvitacionController" as ctrl
participant "InvitacionService" as service
participant "InvitacionRepository" as repo
database "Supabase" as db

Aprendiz -> app : abre Mis Invitaciones
app -> ctrl : GET /api/invitaciones/mis-invitaciones
ctrl -> service : listarMisInvitaciones(usuarioId)
service -> repo : findByInvitadoAndEstado(PENDIENTE)
repo -> db : select invitaciones
repo --> service : lista
ctrl --> app : InvitacionResponseDto[]
@enduml
```

## secuencia UH29 activar cuenta

```plantuml
@startuml
actor Usuario
participant "Correo / navegador" as browser
participant "AuthController" as ctrl
participant "VerificacionTokenRepository" as tokenRepo
participant "UsuarioRepository" as userRepo
database "Supabase" as db

Usuario -> browser : abre enlace de verificacion
browser -> ctrl : GET /api/auth/verificar?token=...
ctrl -> tokenRepo : findByToken(token)
tokenRepo -> db : select verificacion_token
alt token invalido o expirado
  ctrl --> browser : html error
else token valido
  ctrl -> userRepo : save(usuario activo)
  userRepo -> db : update usuario
  ctrl -> tokenRepo : save(usado=true)
  tokenRepo -> db : update token
  ctrl --> browser : html cuenta verificada
end
@enduml
```
