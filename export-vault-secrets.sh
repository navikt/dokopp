#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvdokopp/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvdokopp/username)
fi

if test -f /var/run/secrets/nais.io/srvdokopp/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export SERVICEUSER_***REMOVED***)
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokopp/keystore
then
    echo "Setting SRVDOKOPP_CERT_KEYSTORE"
    CERT_PATH='/var/run/secrets/nais.io/certificate/srvdokopp/keystore-extracted'
    openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/srvdokopp/keystore -out $CERT_PATH
    export SRVDOKOPP_CERT_KEYSTORE=$CERT_PATH
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokopp/keystorealias
then
    echo "Setting SRVDOKOPP_CERT_KEYSTOREALIAS"
    export  SRVDOKOPP_CERT_KEYSTOREALIAS=$(cat /var/run/secrets/nais.io/certificate/srvdokopp/keystorealias)
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokopp/keystorepassword
then
    echo "Setting SRVDOKOPP_CERT_PASSWORD"
    export  SRVDOKOPP_CERT_***REMOVED***)
fi
