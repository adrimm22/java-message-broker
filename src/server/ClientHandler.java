package server;

import java.io.*;
import java.net.Socket;

/**
 * Maneja la interacción con un cliente.
 * Recibe comandos y llama al MessageBus para ejecutarlos.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final MessageBus bus;
    private String clientID = null;

    public ClientHandler(Socket socket, MessageBus bus) {
        this.socket = socket;
        this.bus = bus;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("Bienvenido al servidor MOM. Usa OPEN <cliID> para comenzar.");

            String line;
            while ((line = in.readLine()) != null) {
                CommandResult result = procesarComando(line.trim());
                out.println(result.respuesta());

                if (result.cerrarConexion()) break;
            }

            socket.close();
            System.out.println("Cliente desconectado: " + clientID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CommandResult procesarComando(String comando) {
        String[] partes = comando.split(" ", 3);
        String operacion = partes[0].toUpperCase();

        switch (operacion) {
            case "OPEN":
                if (partes.length < 2) return new CommandResult("ERROR Falta ID");
                clientID = partes[1];
                bus.registrarCliente(clientID);
                return new CommandResult("OPEN_OK");

            case "CLOSE":
                return new CommandResult("CLOSE_OK", true);

            case "MKCHAN":
                return bus.crearCanal(partes, clientID);

            case "RMCHAN":
                return bus.eliminarCanal(partes, clientID);

            case "WRITE":
                return bus.escribirMensaje(partes, clientID);

            case "READ":
                return bus.leerMensaje(partes, clientID);

            default:
                return new CommandResult("ERROR Comando desconocido");
        }
    }
}