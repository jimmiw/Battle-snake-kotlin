# This builder image will not be used at runtime
# See https://docs.docker.com/develop/develop-images/multistage-build/
FROM gradle:7.5.1 as builder

# Copy sourcecode into build image
COPY . /app

WORKDIR /app

RUN gradle clean build --no-daemon

# Now switch to the runtime image; base it on the latest Java, in a "slim" variant.
FROM openjdk:17-slim
EXPOSE 3000 8080

COPY --from=builder ./app/build/libs/mister-sneaky-pants-0.1.jar app.jar
# copy customizations file, so the app.jar can use it
COPY customizations.json .
# Run it
CMD [ "java", "-jar",  "./app.jar" ]