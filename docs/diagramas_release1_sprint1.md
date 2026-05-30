# diagramas release 1 sprint 1

Alcance usado: UH01, UH02, UH04, UH06, UH07, UH10, UH14, UH15, UH16, UH17, UH23, UH24, UH26, UH28 y UH29.

Los diagramas reflejan el backend Java actual despues de limpiar funcionalidades de release 2.

## der

```plantuml
@startuml
hide circle
skinparam linetype ortho
skinparam entity {
  BackgroundColor #FFFFFF
  BorderColor #1B3A6B
}

entity "usuario" as usuario {
  * usuario_id : bigint <<pk>>
  --
  nombre : varchar(100)
  email : varchar(150) <<unique>>
  password : varchar(255)
  foto_perfil : varchar(500)
  rol : varchar(20)
  activo : boolean
  fecha_registro : timestamp
}

entity "sesion_aprendizaje" as sesion {
  * sesion_id : bigint <<pk>>
  --
  titulo : varchar(200)
  descripcion : text
  fecha_sesion : timestamp
  modalidad : varchar(20)
  estado : varchar(20)
  tipo : varchar(20)
  max_participantes : integer
  link_sesion : varchar(500)
  lugar : varchar(300)
  instructor_id : bigint <<fk>>
  fecha_creacion : timestamp
}

entity "inscripcion" as inscripcion {
  * inscripcion_id : bigint <<pk>>
  --
  sesion_id : bigint <<fk>>
  usuario_id : bigint <<fk>>
  rol_sesion : varchar(20)
  fecha_inscripcion : timestamp
}

entity "invitaciones" as invitacion {
  * invitacion_id : bigint <<pk>>
  --
  sesion_id : bigint <<fk>>
  invitado_id : bigint <<fk>>
  estado : varchar(20)
  fecha_envio : timestamp
}

entity "verificacion_token" as token {
  * token_id : bigint <<pk>>
  --
  token : varchar(255) <<unique>>
  usuario_id : bigint <<fk>>
  fecha_expira : timestamp
  usado : boolean
}

usuario ||--o{ sesion : crea
usuario ||--o{ inscripcion : confirma
sesion ||--o{ inscripcion : tiene
usuario ||--o{ invitacion : recibe
sesion ||--o{ invitacion : genera
usuario ||--o{ token : valida
@enduml
```

## diagrama de clases

```plantuml
@startuml
skinparam classAttributeIconSize 0
skinparam packageStyle rectangle
skinparam linetype ortho

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
    - instructor : Usuario
    - fechaCreacion : LocalDateTime
  }

  class Inscripcion {
    - inscripcionId : Long
    - sesion : SesionAprendizaje
    - usuario : Usuario
    - rolSesion : String
    - fechaInscripcion : LocalDateTime
  }

  class Invitacion {
    - invitacionId : Long
    - sesion : SesionAprendizaje
    - invitado : Usuario
    - estado : EstadoInvitacion
    - fechaEnvio : LocalDateTime
  }

  class VerificacionToken {
    - tokenId : Long
    - token : String
    - usuario : Usuario
    - fechaExpira : LocalDateTime
    - usado : Boolean
  }

  enum RolUsuario {
    APRENDIZ
    INSTRUCTOR
  }

  enum ModalidadSesion {
    VIRTUAL
    PRESENCIAL
  }

  enum EstadoSesion {
    PENDIENTE
    ACTIVA
    FINALIZADA
    RECHAZADA
  }

  enum TipoSesion {
    PUBLICA
    PRIVADA
  }

  enum EstadoInvitacion {
    PENDIENTE
    ACEPTADA
    RECHAZADA
  }
}

package "domain.factory" {
  class SesionFactory {
    + crearVirtual(titulo, descripcion, fecha, maxParticipantes, linkSesion, instructor, tipo) : SesionAprendizaje
    + crearPresencial(titulo, descripcion, fecha, maxParticipantes, lugar, instructor, tipo) : SesionAprendizaje
  }
}

package "application.service" {
  class SesionService {
    + crearSesion(instructorId, dto) : SesionAprendizaje
    + listarActivas() : List<SesionAprendizaje>
    + obtenerDetalle(sesionId) : SesionAprendizaje
    + listarPorInstructor(instructorId) : List<SesionAprendizaje>
  }

  class InscripcionService {
    + inscribir(sesionId, usuarioId) : Inscripcion
    + listarPorUsuario(usuarioId) : List<Inscripcion>
    + listarInvitados(sesionId) : List<Inscripcion>
  }

  class InvitacionService {
    + enviarInvitacion(organizadorId, sesionId, invitadoId) : Invitacion
    + listarMisInvitaciones(usuarioId) : List<Invitacion>
    + responderInvitacion(invitadoId, invitacionId, aceptar) : void
  }

  class EmailService {
    + enviarCorreoVerificacion(destinatario, nombre, token) : void
  }
}

package "presentation.controller" {
  class AuthController {
    + registro(dto) : ResponseEntity<ApiResponse>
    + login(dto) : ResponseEntity<ApiResponse>
    + verificarCuenta(token) : ResponseEntity<String>
  }

  class SesionController {
    + crearSesion(dto, auth) : ResponseEntity<ApiResponse>
    + listarPublicas() : ResponseEntity<ApiResponse>
    + misSesiones(auth) : ResponseEntity<ApiResponse>
    + misInscripciones(auth) : ResponseEntity<ApiResponse>
    + inscribirse(sesionId, auth) : ResponseEntity<ApiResponse>
    + invitados(sesionId) : ResponseEntity<ApiResponse>
    + verDetalle(id) : ResponseEntity<ApiResponse>
  }

  class InvitacionController {
    + invitar(sesionId, invitadoId, auth) : ResponseEntity<ApiResponse>
    + invitarConBody(body, auth) : ResponseEntity<ApiResponse>
    + misInvitaciones(auth) : ResponseEntity<ApiResponse>
    + responder(invitacionId, aceptar, auth) : ResponseEntity<ApiResponse>
  }

  class InscripcionController {
    + asistirPublico(sesionId, auth) : ResponseEntity<ApiResponse>
    + misAsistencias(auth) : ResponseEntity<ApiResponse>
    + verInvitados(sesionId, auth) : ResponseEntity<ApiResponse>
  }

  class UsuarioController {
    + obtenerMiPerfil(auth) : ResponseEntity<ApiResponse>
    + buscarUsuarios(q) : ResponseEntity<ApiResponse>
  }
}

package "infrastructure.repository" {
  interface UsuarioRepository
  interface SesionRepository
  interface InscripcionRepository
  interface InvitacionRepository
  interface VerificacionTokenRepository
}

package "infrastructure.security" {
  class JwtUtil {
    + generateToken(userDetails) : String
    + validateToken(token, userDetails) : Boolean
  }
  class JwtAuthFilter
  class UserDetailsServiceImpl
  class SecurityConfig
}

Usuario "1" <-- "0..*" SesionAprendizaje : instructor
Usuario "1" <-- "0..*" Inscripcion : usuario
SesionAprendizaje "1" <-- "0..*" Inscripcion : sesion
Usuario "1" <-- "0..*" Invitacion : invitado
SesionAprendizaje "1" <-- "0..*" Invitacion : sesion
Usuario "1" <-- "0..*" VerificacionToken : usuario

SesionAprendizaje --> EstadoSesion
SesionAprendizaje --> ModalidadSesion
SesionAprendizaje --> TipoSesion
Usuario --> RolUsuario
Invitacion --> EstadoInvitacion

SesionController --> SesionService
SesionController --> InscripcionService
InvitacionController --> InvitacionService
AuthController --> EmailService
SesionService --> SesionFactory
SesionService --> SesionRepository
SesionService --> UsuarioRepository
InscripcionService --> InscripcionRepository
InscripcionService --> SesionRepository
InscripcionService --> UsuarioRepository
InvitacionService --> InvitacionRepository
InvitacionService --> InscripcionService
AuthController --> UsuarioRepository
AuthController --> VerificacionTokenRepository
AuthController --> JwtUtil
@enduml
```

## diagrama de componentes

```plantuml
@startuml
skinparam componentStyle rectangle
skinparam linetype ortho

actor "Usuario movil" as user

node "Android / Expo" {
  component "React Native App" as app
  component "Pantallas Sprint 1" as screens
  component "API client Axios" as axios
}

cloud "Render" {
  component "Spring Boot API" as api
  component "AuthController" as auth
  component "SesionController" as sesionCtrl
  component "InvitacionController" as invitCtrl
  component "InscripcionController" as inscCtrl
  component "UsuarioController" as userCtrl
  component "JWT Filter" as jwtFilter
  component "Servicios de aplicacion" as services
  component "Repositorios JPA" as repos
  component "Flyway" as flyway
}

cloud "SMTP2GO" {
  component "Servidor SMTP" as smtp
}

database "Supabase PostgreSQL" as db

user --> screens
screens --> axios
axios --> api : HTTPS /api
api --> jwtFilter : valida JWT
api --> auth
api --> sesionCtrl
api --> invitCtrl
api --> inscCtrl
api --> userCtrl
auth --> services
sesionCtrl --> services
invitCtrl --> services
inscCtrl --> services
userCtrl --> repos
services --> repos
repos --> db : SQL/JPA
flyway --> db : migraciones
services --> smtp : correo verificacion UH29
@enduml
```

## secuencia UH01 crear evento / sesion de aprendizaje

```plantuml
@startuml
actor Instructor
participant "CrearSesionScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "SesionService" as service
participant "SesionRepository" as sesionRepo
participant "SesionFactory" as factory
database "Supabase" as db

Instructor -> app : completa datos de sesion
app -> ctrl : POST /api/sesiones
ctrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select usuario
alt no es instructor
  ctrl --> app : 403 solo instructores
else instructor valido
  ctrl -> service : crearSesion(instructorId, dto)
  service -> sesionRepo : existsByTituloIgnoreCase(titulo)
  sesionRepo -> db : select sesion
  service -> sesionRepo : countByInstructorUsuarioIdAndEstadoIn(...)
  sesionRepo -> db : count sesiones activas
  service -> factory : crearVirtual o crearPresencial(...)
  factory --> service : SesionAprendizaje ACTIVA
  service -> sesionRepo : save(sesion)
  sesionRepo -> db : insert sesion_aprendizaje
  ctrl --> app : sesion creada con exito
end
@enduml
```

## secuencia UH02 visualizar eventos gestionados

```plantuml
@startuml
actor Instructor
participant "MisSesionesScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "SesionService" as service
participant "SesionRepository" as sesionRepo
database "Supabase" as db

Instructor -> app : abre mis sesiones
app -> ctrl : GET /api/sesiones/mis-eventos
ctrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select usuario
ctrl -> service : listarPorInstructor(usuarioId)
service -> sesionRepo : findByInstructorUsuarioIdAndFechaSesionAfterOrderByFechaSesionAsc
sesionRepo -> db : select sesiones del instructor
ctrl --> app : lista de sesiones gestionadas
@enduml
```

## secuencia UH04 detalle de evento

```plantuml
@startuml
actor Usuario
participant "DetalleSesionScreen" as app
participant "SesionController" as ctrl
participant "SesionService" as service
participant "SesionRepository" as repo
database "Supabase" as db

Usuario -> app : selecciona una sesion
app -> ctrl : GET /api/sesiones/{id}
ctrl -> service : obtenerDetalle(id)
service -> repo : findById(id)
repo -> db : select sesion_aprendizaje
alt existe
  ctrl --> app : detalle de sesion
else no existe
  ctrl --> app : error sesion no encontrada
end
@enduml
```

## secuencia UH06 invitar asistentes

```plantuml
@startuml
actor Instructor
participant "InvitarAsistentesScreen" as app
participant "UsuarioController" as userCtrl
participant "InvitacionController" as invCtrl
participant "UsuarioRepository" as userRepo
participant "InvitacionService" as service
participant "SesionRepository" as sesionRepo
participant "InvitacionRepository" as invRepo
database "Supabase" as db

Instructor -> app : busca usuario
app -> userCtrl : GET /api/usuarios/buscar?q=texto
userCtrl -> userRepo : findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase
userRepo -> db : select usuarios
userCtrl --> app : usuarios encontrados

Instructor -> app : envia invitacion
app -> invCtrl : POST /api/invitaciones
invCtrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select organizador
invCtrl -> service : enviarInvitacion(organizadorId, sesionId, invitadoId)
service -> sesionRepo : findById(sesionId)
sesionRepo -> db : select sesion
service -> userRepo : findById(invitadoId)
userRepo -> db : select invitado
service -> invRepo : existsBySesionSesionIdAndInvitadoUsuarioId
invRepo -> db : select invitacion
alt ya invitado o sesion no privada
  invCtrl --> app : error de regla de negocio
else valido
  service -> invRepo : save(invitacion PENDIENTE)
  invRepo -> db : insert invitaciones
  invCtrl --> app : invitacion enviada exitosamente
end
@enduml
```

## secuencia UH07 confirmar asistencia privada

```plantuml
@startuml
actor Invitado
participant "InvitacionesScreen" as app
participant "InvitacionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "InvitacionService" as invService
participant "InvitacionRepository" as invRepo
participant "InscripcionService" as insService
participant "InscripcionRepository" as insRepo
database "Supabase" as db

Invitado -> app : acepta o rechaza invitacion
app -> ctrl : PUT /api/invitaciones/{id}/responder?aceptar=true
ctrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select usuario
ctrl -> invService : responderInvitacion(usuarioId, invitacionId, aceptar)
invService -> invRepo : findById(invitacionId)
invRepo -> db : select invitacion
alt no pertenece o ya respondida
  ctrl --> app : error
else aceptar
  invService -> insService : inscribir(sesionId, usuarioId)
  insService -> insRepo : existsBySesionSesionIdAndUsuarioUsuarioId
  insRepo -> db : select inscripcion
  insService -> insRepo : save(inscripcion)
  insRepo -> db : insert inscripcion
  invService -> invRepo : save(estado ACEPTADA)
  invRepo -> db : update invitaciones
  ctrl --> app : asistencia confirmada
else rechazar
  invService -> invRepo : save(estado RECHAZADA)
  invRepo -> db : update invitaciones
  ctrl --> app : invitacion rechazada
end
@enduml
```

## secuencia UH10 visualizar eventos asistidos

```plantuml
@startuml
actor Usuario
participant "MisSesionesScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "InscripcionService" as service
participant "InscripcionRepository" as repo
database "Supabase" as db

Usuario -> app : abre tab que asisto
app -> ctrl : GET /api/sesiones/mis-inscripciones
ctrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select usuario
ctrl -> service : listarPorUsuario(usuarioId)
service -> repo : findVigentesByUsuario(usuarioId, limite)
repo -> db : select inscripciones con sesion
ctrl --> app : sesiones asistidas
@enduml
```

## secuencia UH14 registrarse

```plantuml
@startuml
actor Usuario
participant "RegisterScreen" as app
participant "AuthController" as ctrl
participant "UsuarioRepository" as userRepo
participant "PasswordEncoder" as encoder
participant "VerificacionTokenRepository" as tokenRepo
participant "EmailService" as email
database "Supabase" as db
cloud "SMTP2GO" as smtp

Usuario -> app : completa registro
app -> ctrl : POST /api/auth/registro
ctrl -> userRepo : existsByEmail(email)
userRepo -> db : select usuario
alt email existente
  ctrl --> app : email ya registrado
else email nuevo
  ctrl -> encoder : encode(password)
  ctrl -> userRepo : save(usuario activo=false)
  userRepo -> db : insert usuario
  ctrl -> tokenRepo : save(token)
  tokenRepo -> db : insert verificacion_token
  ctrl -> email : enviarCorreoVerificacion(destinatario, nombre, token)
  email -> smtp : enviar correo
  ctrl --> app : registro exitoso, revisar correo
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
participant "UsuarioRepository" as userRepo
participant "JwtUtil" as jwt
database "Supabase" as db

Usuario -> app : ingresa email y password
app -> ctrl : POST /api/auth/login
ctrl -> manager : authenticate(email, password)
manager -> uds : loadUserByUsername(email)
uds -> userRepo : findByEmail(email)
userRepo -> db : select usuario
alt credenciales invalidas
  ctrl --> app : 401 credenciales incorrectas
else cuenta inactiva
  ctrl --> app : 403 revisar correo de verificacion
else valido
  ctrl -> jwt : generateToken(userDetails)
  jwt --> ctrl : token
  ctrl --> app : login exitoso + jwt
end
@enduml
```

## secuencia UH16 visualizar eventos publicos

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
service -> repo : findByEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(ACTIVA, PUBLICA, now)
repo -> db : select sesiones publicas activas
ctrl --> app : sesiones disponibles
@enduml
```

## secuencia UH17 confirmar asistencia publica

```plantuml
@startuml
actor Usuario
participant "DetalleSesionScreen" as app
participant "SesionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "InscripcionService" as service
participant "SesionRepository" as sesionRepo
participant "InscripcionRepository" as insRepo
database "Supabase" as db

Usuario -> app : pulsa confirmar asistencia
app -> ctrl : POST /api/sesiones/{id}/inscribirse
ctrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select usuario
ctrl -> service : inscribir(sesionId, usuarioId)
service -> sesionRepo : findById(sesionId)
sesionRepo -> db : select sesion
service -> insRepo : existsBySesionSesionIdAndUsuarioUsuarioId
insRepo -> db : select inscripcion
alt sesion llena, pasada o ya inscrito
  ctrl --> app : error de regla
else valido
  service -> insRepo : save(inscripcion)
  insRepo -> db : insert inscripcion
  ctrl --> app : asistencia publica confirmada
end
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

Usuario -> app : abre perfil
app -> ctrl : GET /api/usuarios/me
ctrl -> repo : findByEmail(auth.name)
repo -> db : select usuario
ctrl --> app : datos del perfil
@enduml
```

## secuencia UH24 cerrar sesion

```plantuml
@startuml
actor Usuario
participant "HomeScreen" as app
participant "AsyncStorage" as storage

Usuario -> app : pulsa salir
app -> storage : removeItem(token)
storage --> app : token eliminado
app --> Usuario : vuelve a LoginScreen
note right of app
  no requiere endpoint
  jwt es stateless
end note
@enduml
```

## secuencia UH26 visualizar invitados

```plantuml
@startuml
actor Instructor
participant "DetalleSesionScreen" as app
participant "SesionController" as ctrl
participant "InscripcionService" as service
participant "InscripcionRepository" as repo
database "Supabase" as db

Instructor -> app : abre asistentes confirmados
app -> ctrl : GET /api/sesiones/{id}/invitados
ctrl -> service : listarInvitados(sesionId)
service -> repo : findBySesionSesionId(sesionId)
repo -> db : select inscripciones
ctrl --> app : lista de participantes
@enduml
```

## secuencia UH28 visualizar invitaciones privadas

```plantuml
@startuml
actor Usuario
participant "InvitacionesScreen" as app
participant "InvitacionController" as ctrl
participant "UsuarioRepository" as userRepo
participant "InvitacionService" as service
participant "InvitacionRepository" as repo
database "Supabase" as db

Usuario -> app : abre invitaciones
app -> ctrl : GET /api/invitaciones/mis-invitaciones
ctrl -> userRepo : findByEmail(auth.name)
userRepo -> db : select usuario
ctrl -> service : listarMisInvitaciones(usuarioId)
service -> repo : findByInvitadoUsuarioIdAndEstado(usuarioId, PENDIENTE)
repo -> db : select invitaciones pendientes
ctrl --> app : invitaciones privadas
@enduml
```

## secuencia UH29 activar cuenta

```plantuml
@startuml
actor Usuario
participant "Correo / Navegador" as browser
participant "AuthController" as ctrl
participant "VerificacionTokenRepository" as tokenRepo
participant "UsuarioRepository" as userRepo
database "Supabase" as db

Usuario -> browser : abre enlace del correo
browser -> ctrl : GET /api/auth/verificar?token=...
ctrl -> tokenRepo : findByToken(token)
tokenRepo -> db : select verificacion_token
alt token invalido
  ctrl --> browser : html token invalido
else token usado
  ctrl --> browser : html enlace ya utilizado
else token expirado
  ctrl --> browser : html enlace expirado
else token valido
  ctrl -> userRepo : save(usuario activo=true)
  userRepo -> db : update usuario
  ctrl -> tokenRepo : save(usado=true)
  tokenRepo -> db : update verificacion_token
  ctrl --> browser : html cuenta verificada
end
@enduml
```
