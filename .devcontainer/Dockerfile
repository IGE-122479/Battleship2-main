FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y \
    maven \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -ms /bin/bash developer

WORKDIR /workspace

USER developer