# Quasar-Satellite

Quasar-satellite es una aplicación que permite obtener la localización de un satellite y descifrar el mensaje recibido tomando en relación el ultimo satellite buscado.

## Instalación

La aplicación se encuentra desplegada en una azure function, por medio de maven se utiliza el artefacto y se reailza el despliegue, y se activa cuando se realiza una petición HTTP

En el directorio de los archivos del aplicativo ejecutar los siguientes comandos si se desea ejecutar localmente

```bash
mvn clean package
mvn azure-functions:run
```

## Uso

```bash
curl --location --request POST 'https://jdmm-quasar.azurewebsites.net/api/topsecret_split/{nombre-satellite}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "distance":115.5,
    "message":["hola","","mundo"]
}'
```

## Contribucion
Pueden solicitar permisos a https://github.com/jmartine2319/quasar-satellite.git a enviar un correo a jmartinezopg@gmail.