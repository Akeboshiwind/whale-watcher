FROM docker:24.0.6-cli-alpine3.18

ARG ARCH=amd64


# >> Install babashka

ARG BB_VERSION=1.3.185

# Copied from babashka/babashka
RUN apk --no-cache add curl ca-certificates tar && \
    curl -Ls https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.28-r0/glibc-2.28-r0.apk > /tmp/glibc-2.28-r0.apk && \
    apk add --allow-untrusted --force-overwrite /tmp/glibc-2.28-r0.apk

# Download and install babashka
RUN curl -Ls https://github.com/babashka/babashka/releases/download/v${BB_VERSION}/babashka-${BB_VERSION}-linux-${ARCH}-static.tar.gz -o /tmp/babashka.tar.gz && \
    curl -Ls https://github.com/babashka/babashka/releases/download/v${BB_VERSION}/babashka-${BB_VERSION}-linux-${ARCH}-static.tar.gz.sha256 -o /tmp/babashka.tar.gz.sha256 && \
    echo "$(cat /tmp/babashka.tar.gz.sha256) /tmp/babashka.tar.gz" | sha256sum -c - && \
    tar -xzf /tmp/babashka.tar.gz -C /bin && \
    chmod +x /bin/bb && \
    rm /tmp/babashka.tar.gz /tmp/babashka.tar.gz.sha256



# >> Install regctl

ARG REGCTL_VERSION=0.5.3

RUN curl -Ls https://github.com/regclient/regclient/releases/download/v${REGCTL_VERSION}/regctl-linux-${ARCH} -o /bin/regctl && \
    chmod +x /bin/regctl



# >> Install our code

WORKDIR /app
COPY . /app



# >> Entrypoint

ENTRYPOINT ["/bin/bb", "--main", "crontab/install&run!"]
