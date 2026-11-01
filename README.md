# Sistema de Almacén

Aplicación web para gestionar un almacén con dos sedes: productos, compras, ventas, traslados internos, precios y costo promedio ponderado.

| Carpeta | Tecnología |
|---|---|
| `almacen_backend` | Spring Boot 4 (Java 21), Spring Data JPA, Spring Security con JWT, MySQL |
| `almacen_frontend` | Angular 22 |

## Requisitos

- Java 21 (JDK) y la variable `JAVA_HOME`
- MySQL Server 8 o superior
- Node.js y Angular CLI (`npm install -g @angular/cli`)

## Cómo ejecutarlo

### 1. Backend (puerto 8080)

Define la contraseña de MySQL en la variable de entorno `DB_PASSWORD`. La base de datos `almacen_db` se crea sola al iniciar.

```bash
cd almacen_backend
./mvnw spring-boot:run
```

En Windows usa `mvnw.cmd spring-boot:run`, o ejecuta `AlmacenBackendApplication` desde IntelliJ IDEA.

### 2. Frontend (puerto 4200)

```bash
cd almacen_frontend
npm install
ng serve
```

Abre http://localhost:4200.

## Usuario inicial

Al primer arranque se crea el usuario `admin` con rol ADMIN. Su contraseña inicial es `admin123`, o el valor de la variable `ADMIN_PASSWORD` si la defines. Los usuarios no cambian su propia contraseña: el administrador la asigna o restablece desde **Administración › Usuarios**.

## Módulos y roles

El menú principal muestra solo los módulos del rol del usuario. Cada pantalla tiene el botón **Regresar** para volver al nivel anterior.

| Rol | Área | Puede |
|---|---|---|
| `COMERCIAL` | Comercial | Actualizar precios de venta, registrar y ver ventas |
| `COMPRAS` | Logística | Registrar y ver compras, mantener productos, ver costos |
| `ALMACEN` | Logística | Mantener productos, registrar y ver traslados, ver todos los movimientos |
| `ADMIN` | Administración | Todo, incluida la gestión de usuarios |

El backend aplica los mismos permisos: una petición fuera del rol recibe `403 Forbidden`.

## Variables de entorno

| Variable | Uso | Obligatoria |
|---|---|---|
| `DB_PASSWORD` | Contraseña del usuario de MySQL | Sí |
| `DB_USER` | Usuario de MySQL (por defecto `root`) | No |
| `JWT_SECRET` | Clave para firmar los tokens (usa una larga y aleatoria fuera de desarrollo) | No |
| `ADMIN_PASSWORD` | Contraseña inicial del usuario `admin` | No |
