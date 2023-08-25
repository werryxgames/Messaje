<p align="center"><a href="https://github.com/werryxgames/Messaje/releases" target="blank"><img src="https://github.com/werryxgames/Messaje/blob/main/icons/icon.png" width="128" alt="Messaje logo"></a></p>
<p align="center>
<a href="https://github.com/werryxgames/Messaje/actions/workflows/codeql.yml"><img src="https://github.com/werryxgames/Messaje/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://github.com/werryxgames/Messaje/actions/workflows/reviewdog.yml"><img src="https://github.com/werryxgames/Messaje/actions/workflows/reviewdog.yml/badge.svg" alt="reviewdog"></a>
</p>

**Messaje** is a fully secure fast cross-platform client and server, written in Java, that supports desktop (Linux, Windows, Mac OS) and mobile platforms (only Android right now).

# Security
## Network
No one of packets (create account, log in to account, write message) can be read using packet interceptor like Wireshark.

Every packet is encrypted, using custom 256-bit AES-GCM key.

Key, or it's encrypted parts is never sent over network.

## Local
Configuration, that contains AES key is encrypted using custom algorithm (should be changed after installation).

Even when somebody has exact same JAR/EXE/APK file, to decrypt properties, that person should spend large amount of time.

# Installation
## 1. Clone and unzip this repository.
### Web
1. Press `Code`
2. Press `Download ZIP`
3. Unzip downloaded zip file through any unzipper

### Linux
1. Open terminal
2. Write command `git clone https://github.com/werryxgames/Messaje`

## 2. Run server
**Don't forget to first configure properties (instruction located below).**

First run will be much longer, than others.

### Windows
1. Press `R`, while holding `Super` (`Win`).
2. Write `cmd.exe` (or `cmd`) and press `Enter`.
3. Write `cd `, then write path to unpacked Messaje directory and press `Enter`.
4. Write `.\gradlew.bat server:run` and press `Enter`.

### Linux
1. Open terminal (or use already opened).
2. Write `cd `, then write path to unpacked Messaje directory and press `Enter`.
3. Write `./gradle server:run` and press `Enter`.

## 3. Run client
**Don't forget to first configure properties (instructions located below).**

First run will be much longer, than others.

### Windows
1. Press `R`, while holding `Super` (`Win`).
2. Write `cmd.exe` (or `cmd`) and press `Enter`.
3. Write `cd `, then write absolute path to unpacked Messaje directory and press `Enter`.
4. Write `.\gradlew.bat desktop:run` and press `Enter`.

### Linux
1. Open terminal (or use already opened).
2. Write `cd `, then write absolute path to unpacked Messaje directory and press `Enter`.
3. Write `./gradle desktop:run` and press `Enter`.

# Update
## GUI
1. Follow **Installation** steps.
2. Open file manager (like `explorer.exe` or `nautilus`).
3. Go to directory of old server version.
4. Copy `config` directory.
5. Go to directory of new server version.
6. Paste `config` here.
7. Change configuration: look at added, changed and removed configuration properties below.

## Linux CLI
1. Open terminal.
2. Follow **Installation** steps.
3. Write `cd `, write absolute path to directory of old server version and press `Enter`.
4. Write `cp config/ -r `, write absolute path to new server version and press `Enter`
5. Change configuration: look at added, changed and removed configuration properties below.

# Configuration
You should configure client and server, using custom properties. To customize properties, overwrite
them in `./config/client.properties` and `./config/server.properties` (create them first).

Description of configuration properties is located in `./config/client.example.properties` and `./config/server.example.properties`.

Client properties:
```
aes.key

debug

server.host
server.port

password.pepper
```

Server properties:
```
aes.key

db.url
db.user
db.password

server.host
server.port
server.maxPendingConnections
```

To apply configuration properties, write `java -jar server.jar --saveConfig` (to compile
`server.jar`, write `gradle server:dist` and then move `./server/build/libs/server-1.0.jar` to
`./server.jar`; also you can write `java -jar ./server/build/libs/server-1.0.jar --saveConfig`,
without moving)
