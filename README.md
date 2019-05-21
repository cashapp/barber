# barber

This is a sample app that makes use of Misk. All commands below assume you're in the root directory
of this repository.

## Building
Build barber:

```
$ ./gradlew clean shadowJar
```

## Run the Service

### From the command line

```
$ ENVIRONMENT=DEVELOPMENT \
    SERVICE_NAME=barber \
    REGION= ACCOUNT_ID= \
    java -jar service/build/libs/service.jar
```

### From IntelliJ
Right-click on `BarberService.kt` and select `Run`. It will fail without the correct environment
variables. Edit the run configuration and add:

```
SERVICE_NAME=barber
ENVIRONMENT=DEVELOPMENT
REGION=
ACCOUNT_ID=
```

The latter two are required in production and staging, and managed by the infrastructure.
They're not used in development mode, so any value is fine.


### Confirm barber works with curl

```
$ curl --data '{"message": "hello"}' -H 'Content-Type: application/json' http://localhost:8080/ping
```

## Additional docs
* Visit [go/cashdevguide](http://go/cashdevguide) for documentation about the Cash Cloud Platform
