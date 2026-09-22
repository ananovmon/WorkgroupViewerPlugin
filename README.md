# WorkGroup Info Plugin

Plugin para SailPoint IdentityIQ que añade interactividad a los atributos de identidades en la interfaz de solicitudes de acceso (Access Requests / Identity Requests), permitiendo visualizar dinámicamente los miembros de un Workgroup en un modal emergente interactivo y moderno.

## Características

* **Escaneo Dinámico**: El plugin escucha los cambios en la interfaz de usuario de SailPoint (renderizada mediante AngularJS) e identifica de forma dinámica los atributos clave.
* **Atributos Clicables**: Convierte los campos como `administrator`, `manager`, `owner`, `propietario`, etc., en enlaces interactivos.
* **Modal Emergente Premium**: Al pulsar sobre un atributo de Workgroup, abre un modal emergente que consulta en tiempo real al backend y lista a todos los miembros de dicho grupo de manera elegante y veloz.
* **Backend de Alto Rendimiento**: Implementa la API nativa de SailPoint `ObjectUtil.getWorkgroupMembers` para garantizar precisión absoluta en la recuperación de miembros y grupos anidados.

## Estructura del Proyecto

* `src/`: Clases Java para los endpoints REST.
* `ui/`: Contenido estático frontend (XHTML, CSS, JS).
  * `ui/js/accessRequest.js`: Inyección interactiva en solicitudes de acceso.
  * `ui/css/accessRequest.css`: Estilo del modal y enlaces clicables.
* `manifest.xml`: Configuración general del plugin en SailPoint.
* `import/`: Objetos de instalación de SailPoint (Derechos y Capacidades).

## Compilación y Empaquetado

Para compilar y empaquetar el plugin en un archivo ZIP listo para importar en SailPoint, ejecuta el siguiente comando Ant desde la raíz del proyecto:

```bash
ant clean package
```

Esto generará el archivo `dist/WorkGroupInfoPlugin.1.0.0.zip` listo para instalar.
