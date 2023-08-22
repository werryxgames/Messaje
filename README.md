# Messaje
<p align="center"><a href="https://github.com/werryxgames/Messaje/releases" target="blank"><img src="https://github.com/werryxgames/Messaje/blob/main/icons/icon.png" width="128" alt="Messaje logo"></a></p>

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
