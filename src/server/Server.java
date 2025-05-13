package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase principal que lanza el servidor MOM.
 * Acepta múltiples clientes y lanza un hilo por cada uno.
 */
public class Server {

    private static final int PORT = 12345;
    private final MessageBus bus = new MessageBus();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor MOM escuchando en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());

                // Lanza un hilo que manejará al cliente con su propio ClientHandler
                new Thread(new ClientHandler(clientSocket, bus)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}