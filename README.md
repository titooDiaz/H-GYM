# H-GYM — Aplicación Integral de Seguimiento Deportivo (Offline-First & 100% Gratuita)

H-GYM es una aplicación móvil Android minimalista, moderna y privada orientada al entrenamiento de fuerza, nutrición, evolución física y comunidad.

## 🚀 Guía de Configuración de Firebase (Para Desarrolladores / Usuarios)

H-GYM utiliza **Firebase Authentication (Google Sign-In)** y **Cloud Firestore** bajo el plan gratuito **Spark** para la sincronización colaborativa y social (grupos, retos y rankings). Ningún dato privado (como alimentación o fotografías corporales) sale jamás del dispositivo del usuario.

Para compilar y ejecutar tu propia instancia de H-GYM con tu propio proyecto de Firebase, sigue estos sencillos pasos:

### 1. Crear un Proyecto en Firebase
1. Entra a la [Consola de Firebase](https://console.firebase.google.com/).
2. Haz clic en **Crear proyecto** (puedes nombrarlo `H-GYM`).
3. Desactiva Google Analytics (opcional) y pulsa **Crear proyecto**.

### 2. Añadir la Aplicación Android
1. En la página de inicio del proyecto de Firebase, haz clic en el icono de **Android** para añadir una app.
2. Introduce el nombre de paquete exacto de la aplicación:
   ```
   com.titoodev.h_gym
   ```
3. *(Opcional)* Introduce la huella digital SHA-1 de tu keystore de depuración (puedes obtenerla ejecutando `./gradlew signingReport` en tu terminal).
4. Haz clic en **Registrar app**.

### 3. Descargar el archivo `google-services.json`
1. Descarga el archivo **`google-services.json`**.
2. Colócalo dentro de la carpeta raíz del módulo app de tu proyecto:
   ```
   /app/google-services.json
   ```
   *(Este archivo ya está excluido en el `.gitignore` para garantizar que tus credenciales nunca se expongan en repositorios públicos).*

### 4. Habilitar Autenticación con Google
1. En la consola de Firebase, ve al menú lateral izquierdo: **Build > Authentication**.
2. Haz clic en **Get started** (Comenzar).
3. En la pestaña *Sign-in method*, selecciona **Google**.
4. Habilítalo, selecciona tu correo de asistencia del proyecto y haz clic en **Guardar**.

### 5. Configurar Cloud Firestore (Plan Spark - Gratuito)
1. In la consola de Firebase, ve a **Build > Cloud Firestore**.
2. Haz clic en **Crear base de datos**.
3. Selecciona la ubicación de servidores más cercana a ti y elige el modo de inicio en **Modo de producción** (o modo de prueba para desarrollo).
4. Configura las reglas de seguridad de Firestore para restringir el acceso exclusivo de los miembros autorizados a los grupos deportivos.

> 💡 **¿Quieres probar la app de inmediato sin configurar Firebase?**
> Puedes pulsar el botón **"Entrar como Invitado / Offline"** en la pantalla de bienvenida. H-GYM está construida bajo una arquitectura estricta *Offline-First*, lo que significa que el 100% de las funcionalidades (registro de entrenamientos, series, nutrición con compresión de fotos, evolución física y gráficas 1RM) operan perfectamente de forma local con Room sin requerir credenciales externas.

---

## 🛠️ Tecnologías y Arquitectura
- **Kotlin & Jetpack Compose** (UI minimalista inspirada en Apple / iOS).
- **MVVM + Clean Architecture**.
- **Room (SQLite)** como base de datos local principal (*Offline-First*).
- **WorkManager** para sincronización automática en segundo plano.
- **Credential Manager & Firebase Auth** para autenticación segura con Google.
