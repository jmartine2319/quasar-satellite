# Quasar-Satellite

Quasar-fuego es una aplicación que permite obtener la localización de un satellite y descifrar el mensaje recibido por el mismo.

## Instalación

En el directorio de los archivos del aplicativo ejecutar los siguientes comandos

```bash
mvn clean -DskipTests package
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