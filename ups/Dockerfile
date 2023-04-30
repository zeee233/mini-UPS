FROM openjdk:11

WORKDIR /app

COPY build.gradle gradlew ./
COPY gradle ./gradle

RUN ./gradlew --no-daemon --version
RUN ./gradlew --no-daemon clean build

COPY . .

CMD ["./gradlew", "run", "--args='vcm-30481.vm.duke.edu'"]