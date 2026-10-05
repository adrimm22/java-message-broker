# Java Message Broker

A small message-oriented middleware (MOM) written in Java. Clients connect to a server over TCP, create channels, publish messages to them and read them back.

I built it as a university project at Universidad Rey Juan Carlos to practise concurrency and client–server programming in Java.

## How it works

- **Channels.** Any client can create or delete a channel and write messages to it. Messages are kept in order.
- **Independent readers.** The server keeps a read position for every client on every channel. If two clients read the same channel, each one receives all the messages at its own pace, without affecting the other. The idea is similar to how consumers track their offsets in systems like Kafka.
- **Blocking and non-blocking reads.** A client can ask to read without waiting (it gets `NULL` if there is nothing new) or wait until a message arrives. Waiting clients are woken up as soon as someone writes to the channel, or get an error if the channel is deleted.
- **One thread per client.** The server starts a thread for each connection, and all of them share a single `MessageBus`. Access to the channels is synchronised, and waiting readers release the lock, so other clients can keep working while they wait.
- **Client API.** The `Mom` interface hides the protocol behind simple methods: `open`, `mkChannel`, `rmChannel`, `writeChannel`, `readChannel` and `close`.

## Protocol

Plain-text commands, one per line:

| Command                        | Response                                       |
|--------------------------------|------------------------------------------------|
| `OPEN <clientId>`              | `OPEN_OK`                                      |
| `MKCHAN <channel>`             | `MKCHAN_OK` or `ERROR ...`                     |
| `RMCHAN <channel>`             | `RMCHAN_OK` or `ERROR ...`                     |
| `WRITE <channel> <message>`    | `WRITE_OK` or `ERROR ...`                      |
| `READ <channel> <dontwait>`    | `MSG <message>`, `NULL` (only if `dontwait` is `true`) or `ERROR ...` |
| `CLOSE`                        | `CLOSE_OK`                                     |

## Project structure

```
src/
├── server/
│   ├── Server.java          Accepts connections and starts a thread per client
│   ├── ClientHandler.java   Reads commands from one client and sends the replies
│   ├── MessageBus.java      Channels, messages and read positions per client
│   └── CommandResult.java   Reply plus whether to close the connection
├── mom/
│   ├── Mom.java             Client interface
│   ├── MomImpl.java         TCP implementation of the interface
│   └── InteractiveClient.java   Menu-based client for manual testing
└── test/
    └── MomTest.java         JUnit 5 tests with two clients
```

## How to run it

Requires Java 17 or later. The server listens on port 12345.

**From the terminal (Linux/macOS):**

```bash
javac -d out -cp "lib/*" $(find src -name "*.java")
java -cp out server.Server              # terminal 1
java -cp out mom.InteractiveClient      # terminal 2 (open several to try more clients)
```

**With IntelliJ IDEA:** open the folder, add the `lib` folder as a library, run `server.Server` and then `mom.InteractiveClient`.

**Tests:** start `server.Server` first and then run `test.MomTest`. The tests cover writing, reading by two independent clients, non-blocking reads, blocking reads and channel deletion.

## Tech

Java · TCP sockets · multithreading · `synchronized` / `wait` / `notifyAll` · JUnit 5
