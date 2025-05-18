# CINEMA-JRC - Sistema de Gestión de Cine

## 1. Descripción del Proyecto

CINEMA-JRC es un sistema de gestión de cine que permite a los usuarios reservar entradas para espectáculos, gestionar sus reservas y acceder a información sobre las funciones disponibles. El sistema incluye funcionalidades tanto para usuarios finales como para administradores del cine. MÁS INFO EN: https://deepwiki.com/jrc191/COPIA-PROYECTO-3TRIM

### 1.1 Características Principales
- Reserva de entradas para espectáculos
- Sistema de butacas con diferentes categorías (VIP y estándar)
- Gestión de usuarios y perfiles
- Panel de administración
- Sistema de mensajes al administrador
- Gestión de espectáculos y disponibilidad

### 1.2 Roles del Sistema
- **Usuarios**: Pueden reservar entradas, ver cartelera y gestionar sus reservas
- **Administradores**: Pueden gestionar espectáculos, usuarios, reservas y mensajes

## 2. Tecnologías y Dependencias

### 2.1 Tecnologías Principales
- **Java**: Lenguaje principal de desarrollo
- **JavaFX**: Framework para la interfaz gráfica
- **Oracle**: Base de datos relacional


### 2.2 Frameworks y Bibliotecas
- **JavaFX**: Para la interfaz gráfica
- **JDBC**: Para la conexión con la base de datos


### 2.3 Herramientas de Desarrollo
- **IntelliJ IDEA**: IDE principal
- **Git**: Control de versiones
- **SceneBuilder**: Diseño de FXML

## 3. Arquitectura del Sistema

### 3.1 Patrones de Diseño
- **MVC (Model-View-Controller)**: Separación clara de responsabilidades
- **DAO (Data Access Object)**: Acceso a datos


### 3.2 Capas del Sistema
1. **Presentación (Views)**
    - Interfaces de usuario JavaFX
    - Manejo de eventos
    - Animaciones y transiciones

2. **Control (Controllers)**
    - Lógica de negocio
    - Coordinación entre vistas y modelos
    - Manejo de flujo de datos

3. **Modelo (Models)**
    - Entidades del dominio
    - Reglas de negocio
    - Validaciones

4. **Acceso a Datos (DAO)**
    - Operaciones CRUD
    - Gestión de conexiones
    - Transacciones

### 3.3 Componentes Principales
- **Controllers**: Manejan la lógica de negocio
- **DAO**: Acceso a datos
- **Models**: Entidades del dominio
- **Views**: Interfaces de usuario
- **Utils**: Funciones auxiliares
- **Resources**: Recursos estáticos


<div style="page-break-after: always;"></div>




## 4. Diagrama de Relaciones entre clases

![[Pasted image 20250518233745.png]]
<div style="page-break-after: always;"></div>

<div style="page-break-after: always;"></div>



## 5. Flujo Principal de la Aplicación

### 5.1 Flujo Principal del Usuario

```mermaid
flowchart TD
    A[Inicio] --> B[LoginController]
    B --> C{Validar Usuario}
    C -->|Éxito| D[CarteleraController]
    C -->|Error| B
    D --> E[Seleccionar Espectáculo]
    E --> F[ReservasController]
    F --> G{Confirmar Reserva}
    G -->|Sí| H[Crear Reserva]
    G -->|No| D
    H --> I[Guardar en Base de Datos]
    I --> J[Mostrar Confirmación]
    J --> D
```


<div style="page-break-after: always;"></div>



## 6. Flujo de Reserva de Entradas

```mermaid
flowchart TD
    A[Usuario Ve Cartelera] --> B[Selecciona Espectáculo]
    B --> C[Verifica Disponibilidad]
    C -->|Sí| D[Selecciona Butacas]
    C -->|No| E[Mostrar Mensaje]
    D --> F[Validar Selección]
    F -->|Válida| G[Calcular Precio]
    F -->|No Válida| H[Mostrar Error]
    G --> I[Guardar en Cesta]
    I --> J{Confirmar Reserva?}
    J -->|Sí| K[Crear Reserva]
    J -->|No| L[Modificar Selección]
    K --> M[Guardar en Base de Datos]
    M --> N[Generar Reserva]
    O --> P[Mostrar Confirmación]
```


<div style="page-break-after: always;"></div>



## 7. Flujo de Gestión de Usuarios (Admin)

```mermaid

graph TD

A[Panel Admin] --> B{Seleccionar Acción}

B -->|Listar Usuarios| C[Mostrar Lista]

B -->|Crear Usuario| D[Crear Usuario]

B -->|Modificar Usuario| E[Modificar Usuario]

B -->|Eliminar Usuario| F[Eliminar Usuario]

D -->|Validar Datos| H[Crear Usuario]

E -->|Buscar Usuario| I[Modificar Usuario]

F -->|Confirmar| J[Eliminar Usuario]

H --> K[Mostrar Mensaje]

I --> K

J --> K

```

<div style="page-break-after: always;"></div>



## 8. Flujo de Gestión de Espectáculos (Admin)

```mermaid

graph TD

A[Panel Admin] --> B{Seleccionar Acción}

B -->|Listar| C[Mostrar Lista]

B -->|Crear| D[Crear Espectáculo]

B -->|Modificar| E[Modificar Espectáculo]

B -->|Eliminar| F[Eliminar Espectáculo]

D -->|Validar| H[Crear Espectáculo]

E -->|Buscar| I[Modificar Espectáculo]

F -->|Confirmar| J[Eliminar Espectáculo]

H --> K[Actualizar Disponibilidad]

I --> K

J --> K

K --> L[Mostrar Mensaje]

```

<div style="page-break-after: always;"></div>




## 9. Flujo de Gestión de Mensajes (Admin)

```mermaid
flowchart TD
    A[Panel Admin] --> B[Listar Mensajes]
    B --> C{Mensaje Leído?}
    C -->|Sí| D[Mostrar Mensaje]
    C -->|No| E[Marcar como Leído]
    D --> F{Responder?}
    F -->|Sí| G[Seleccionar respuesta Pendiente, Aprobada, Rechazada]
    F -->|No| H[Eliminar Mensaje]
    G --> I[Guardar Respuesta]
    I --> K[Mostrar Confirmación]
    H --> K
```


<div style="page-break-after: always;"></div>




## 10. Flujo de Gestión de Cesta

```mermaid
flowchart TD
    A[Ver Cartelera] --> B[Seleccionar Espectáculo]
    B --> C[Seleccionar Butacas]
    C --> D[Validar Selección]
    D -->|Válida| E[Guardar en Cesta]
    D -->|No Válida| F[Mostrar Error]
    E --> G[Mostrar Cesta]
    G --> H{Modificar?}
    H -->|Sí| C
    H -->|No| I{Confirmar?}
    I -->|Sí| J[Procesar Reserva]
    I -->|No| K[Cancelar]
    J --> L[Guardar Reserva]
    L --> M[Mostrar Confirmación]
```


<div style="page-break-after: always;"></div>




## 11. Flujo de Sesión y Seguridad

```mermaid
flowchart TD
    A[Inicio] --> B[LoginController]
    B --> C{Validar Sesión}
    C -->|Activa| D[Redirigir a Cartelera]
    C -->|Inactiva| E[Mostrar Login]
    E --> F[Validar Credenciales]
    F -->|Válidas| G[Crear Sesión]
    F -->|No Válidas| H[Mostrar Error]
    G --> I[Guardar Estado]
    I --> D
    H --> E
```

<div style="page-break-after: always;"></div>




## 12. Flujo de Manejo de Errores

```mermaid
flowchart TD
    A[Error Detectado] --> B{Tipo de Error}
    B -->|Validación| C[Mostrar Mensaje]
    B -->|Base de Datos| D[Reintentar Operación]
    B -->|Sistema| E[Logging]
    B -->|Usuario| F[Notificar Usuario]
    C --> G[Pedir Corrección]
    D --> H[Actualizar Estado]
    E --> I[Notificar Admin]
    F --> J[Mostrar Mensaje]
```

<div style="page-break-after: always;"></div>


## 13. Flujo de Gestión de Butacas

```mermaid
flowchart TD
    A[Seleccionar Espectáculo] --> B[Obtener Sala]
    B --> C[Mostrar Butacas]
    C --> D[Seleccionar Butacas]
    D --> E{Butacas Disponibles?}
    E -->|Sí| F[Guardar Selección]
    E -->|No| G[Mostrar Mensaje]
    F --> H[Actualizar Estado]
    G --> I[Seleccionar Otras]
    H --> J[Mostrar Cesta]
```

<div style="page-break-after: always;"></div>



## 14. Flujo de Actualización de Datos

```mermaid
flowchart TD
    A[Cambio de Datos] --> B{Tipo de Actualización}
    B -->|Usuario| C[Actualizar Usuario]
    B -->|Espectáculo| D[Actualizar Espectáculo]
    B -->|Reserva| E[Actualizar Reserva]
    C --> F[Validar Datos]
    D --> F
    E --> F
    F -->|Válidos| G[Guardar Cambios]
    F -->|No Válidos| H[Mostrar Error]
    G --> I[Actualizar UI]
    H --> J[Pedir Corrección]
```

<div style="page-break-after: always;"></div>




## 15. Flujo de Manejo de Sesiones

```mermaid
flowchart TD
    A[Inicio Sesión] --> B[LoginController]
    B --> C{Validar Credenciales}
    C -->|Válidas| D[Crear Sesión]
    C -->|No Válidas| E[Mostrar Error]
    D --> F[Guardar Estado]
    F --> G[Redirigir a Cartelera]
    G --> H{Sesión Válida?}
    H -->|Sí| I[Permitir Acceso]
    H -->|No| J[Pedir Login]
```

<div style="page-break-after: always;"></div>



## 16. Clases Principales y Funcionalidades

### 16.1 Controladores

#### CarteleraController
Propiedades:
- Labels para mostrar información
- HBox para contenedor de espectáculos
- TextField para filtro de nombre
- DatePicker para filtro de fecha
- Labels para mensajes y botones

Responsabilidades:
- Gestiona la visualización de espectáculos
- Maneja filtros por nombre y fecha
- Implementa animaciones y transiciones
- Controla el scroll de espectáculos
- Gestiona la interacción con la UI

Funcionalidades:
- Carga de espectáculos
- Filtrado dinámico
- Animaciones de transición
- Manejo de eventos
- Validación de entrada

#### ReservasController
Propiedades:
- Labels para espectáculo y usuario
- GridPane para disposición de butacas
- ChoiceBox para elección
- Lista de butacas (ocupadas, VIP, estándar)
- Rutas de imágenes para butacas

Responsabilidades:
- Gestiona el proceso de reserva
- Valida disponibilidad de butacas
- Calcula precios
- Genera tickets
- Maneja el flujo de confirmación

Funcionalidades:
- Visualización de butacas
- Selección y deselección
- Cálculo de precios
- Generación de ticket
- Validación de disponibilidad

#### LoginController
Propiedades:
- Campos de texto para credenciales
- Labels para mensajes
- Botones de acción

Responsabilidades:
- Maneja la autenticación de usuarios
- Gestiona sesiones
- Valida credenciales
- Redirige según el rol del usuario

Funcionalidades:
- Validación de credenciales
- Gestión de sesiones
- Manejo de estados
- Redirección según rol

### 16.2 Modelos

#### Espectaculo
Propiedades:
- ID único
- Nombre
- Fecha
- Precio base
- Precio VIP

Responsabilidades:
- Representa un espectáculo
- Almacena información de precios
- Gestiona fechas

Funcionalidades:
- Getters y setters
- Validación de precios
- Manejo de fechas
- Cálculo de precios

#### Usuario
Propiedades:
- ID
- Nombre
- Email
- Contraseña
- Rol (usuario/admin)

Responsabilidades:
- Representa un usuario del sistema
- Gestiona credenciales
- Define permisos

Funcionalidades:
- Autenticación
- Gestión de perfil
- Validación de credenciales
- Manejo de roles

#### Reservas
Propiedades:
- ID
- Usuario
- Espectáculo
- Fecha
- Estado

Responsabilidades:
- Representa una reserva
- Gestiona el estado
- Almacena información de usuario

Funcionalidades:
- Cálculo de precio total
- Generación de ticket
- Validación de disponibilidad
- Actualización de estado

### 16.3 DAOs

#### EspectaculoDao
Propiedades:
- Conexión a base de datos
- Consultas SQL

Responsabilidades:
- CRUD de espectáculos
- Búsqueda por criterios
- Gestión de disponibilidad
- Actualización de precios

Funcionalidades:
- Crear espectáculos
- Leer espectáculos
- Actualizar datos
- Eliminar espectáculos
- Filtrado avanzado

#### UsuarioDao
Propiedades:
- Conexión a base de datos
- Consultas de autenticación

Responsabilidades:
- Autenticación
- Gestión de usuarios
- Búsqueda de usuarios
- Actualización de perfiles

Funcionalidades:
- Validación de credenciales
- Gestión de sesiones
- Búsqueda por criterios
- Actualización de datos

#### ReservasDao
Propiedades:
- Conexión a base de datos
- Consultas de disponibilidad

Responsabilidades:
- Gestión de reservas
- Validación de disponibilidad
- Generación de tickets
- Historial de reservas

Funcionalidades:
- Crear reservas
- Validar disponibilidad
- Generar tickets
- Consultar historial
- Actualizar estado

### 16.4 Utilidades

#### DatabaseConnection
Propiedades:
- Pool de conexiones
- Configuración de base de datos

Responsabilidades:
- Conexión a base de datos
- Manejo de transacciones
- Pool de conexiones
- Manejo de errores

Funcionalidades:
- Conexión/Desconexión
- Manejo de transacciones
- Pool de conexiones
- Manejo de errores

#### Transitions
Propiedades:
- Efectos de transición
- Duraciones
- Estilos

Responsabilidades:
- Animaciones UI
- Transiciones
- Efectos visuales
- Manejo de estados

Funcionalidades:
- Animaciones básicas
- Transiciones complejas
- Efectos visuales
- Manejo de estados

### 16.5 Recomendaciones de Uso

#### Para Administradores
- Seguir el flujo de administración
- Usar el panel de administración para gestión
- Mantener respaldos de datos

#### Para Usuarios
- Seguir el flujo de reserva
- Mantener sesiones activas
- Verificar confirmaciones de reserva

## 17. Mejoras Futuras

### 17.1 Optimizaciones Posibles
- Implementar caché para datos frecuentemente accedidos
- Agregar más validaciones en el flujo de reserva
- Mejorar el manejo de errores con más detalles

### 17.2 Nuevas Funcionalidades A implementar a futuro
- Sistema de reseñas
- Sistema de notificaciones
- Historial de búsquedas

