FROM gradle:7.5.1-jdk17 as builder

RUN gradle --version && java --version

WORKDIR /app

# Only copy dependency-related files
COPY build.gradle.kts settings.gradle.kts /app/
# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

# Copy sourcecode into build image
COPY . /app

RUN gradle build --no-daemon

EXPOSE 3000

# copy customizations file, so the app.jar can use it
COPY customizations.json .
# Run it
CMD java -jar build/libs/*.jar