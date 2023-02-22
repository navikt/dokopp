#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvdokopp/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvdokopp/username)
fi

if test -f /var/run/secrets/nais.io/srvdokopp/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvdokopp/password)
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokopp/keystore
then
    echo "Setting DOKOPPCERT_KEYSTORE"
    CERT_PATH='/var/run/secrets/nais.io/certificate/srvdokopp/keystore-extracted'
    openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/srvdokopp/keystore -out $CERT_PATH
    export DOKOPPCERT_KEYSTORE=$CERT_PATH
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokopp/keystorealias
then
    echo "Setting DOKOPPCERT_KEYSTOREALIAS"
    export DOKOPPCERT_KEYSTOREALIAS=$(cat /var/run/secrets/nais.io/certificate/srvdokopp/keystorealias)
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokopp/keystorepassword
then
    echo "Setting DOKOPPCERT_PASSWORD"
    export DOKOPPCERT_PASSWORD=$(cat /var/run/secrets/nais.io/certificate/srvdokopp/keystorepassword)
fi

echo "Exporting appdynamics environment variables"
if test -f /var/run/secrets/nais.io/appdynamics/appdynamics.env;
then
    export $(cat /var/run/secrets/nais.io/appdynamics/appdynamics.env)
    echo "Appdynamics environment variables exported"
else
    echo "No such file or directory found at /var/run/secrets/nais.io/appdynamics/appdynamics.env"
fi
