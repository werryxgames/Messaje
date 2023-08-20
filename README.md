# Messaje
[![Messaje](https://lh3.googleusercontent.com/u/2/drive-viewer/AITFw-x4mtr-vjeqzxo9PE7zruZFLmXcOaclfbzi9rlEzVN9ULOb7bXzDERhIzgZgxr67wF9Lw4J_djsQAUjB3OkEl_2lMqZRA=w1366-h651)](https://github.com/werryxgames/Messaje/releases)

[![CodeQL](https://github.com/werryxgames/Messaje/actions/workflows/codeql.yml/badge.svg)](https://github.com/werryxgames/Messaje/actions/workflows/codeql.yml)
[![reviewdog](https://github.com/werryxgames/Messaje/actions/workflows/reviewdog.yml/badge.svg)](https://github.com/werryxgames/Messaje/actions/workflows/reviewdog.yml)

Fast cross-platform Messaje client and server, written in Java for mobile devices and personal
computers.

# Configuration
You should configure client and server, using custom properties. To customize properties, overwrite
them in `./config/client.properties` and `./config/server.properties` (create them first; default
values are in `./config/client.example.properties` and `./config/server.example.properties`).

Client properties:
```
aes.key
```

Server properties:
```
aes.key

db.url
db.user
db.password
```

For their description, look at `./config/(config|server).example.properties`

To apply configuration properties, write `java -jar server.jar --saveConfig` (to compile
`server.jar`, write `gradle server:dist` and then move `./server/build/libs/server-1.0.jar` to
`./server.jar`; also you can write `java -jar ./server/build/libs/server-1.0.jar --saveConfig`,
without moving)

**In development. Don't use it in production (don't write any confidential messages), until at least first release!**
