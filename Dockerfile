FROM ghcr.io/navikt/baseimages/temurin:21-appdynamics
ENV APPD_ENABLED=true

COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/50-export-vault-secrets.sh
COPY 51-dokopp-java-opts.sh /init-scripts/51-dokopp-java-opts.sh