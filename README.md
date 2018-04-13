Dokopp
================

Dokopp orkestrerer opprettelse av oppgaver i GSAK gjennom et MQ-grensesnitt.

# Komme i gang

## Bygge app.jar og kjøre tester

`mvn clean package`

## Kjøre systemtester

Denne applikasjonen har ingen automatiske systemtester

## Hvordan kjøre lokalt med mvn spring-boot plugin

Det ligger profil for t8 i `dokopp/src/main/resources`:

* application-t8.properties

Noen secrets må settes - se https://fasit.adeo.no/instances/4417627 

```

# Systembruker
export SRVDOKOPP_USERNAME=srvdokopp
export SRVDOKOPP_***REMOVED***>

# System cert
export SRVDOKOPP_CERT_KEYSTORE=<keystorepath>
export SRVDOKOPP_CERT_KEYSTOREALIAS=<keystorealias>   # Default app-key
export SRVDOKOPP_CERT_***REMOVED***>
```

Kjøre appen med mvn spring boot plugin. Truststore finnes f.eks i modig-testcertificates testdependency eller på Fasit som `nav_truststore` alias. 

```
mvn spring-boot:run -Drun.profiles=t8 -Drun.jvmArguments="-Dsrvdokopp_cert_keystore=/path/til/cert.jks -Dsrvdokopp_cert_***REMOVED***>"
```

## Hvordan kjøre lokalt med IntelliJ

Start `Application.java` som en Spring Boot/Java Application. På denne måten kan man kjøre lokalt og få full debug-støtte. 

VM Options: `-Dsrvdokopp_cert_keystore=/path/til/cert.jks -Dsrvdokopp_cert_***REMOVED***>`

Active profiles: `t8`.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Applikasjonsansvarlig Paul Magne Lunde <Paul.Magne.Lunde@nav.no> 

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #dokumenthåndtering.
