package server;

import java.util.*;

/**
 * Clase que gestiona los canales, mensajes y seguimiento de lecturas por cliente.
 * Esta es la "lógica" del middleware MOM.
 */
public class MessageBus {

    private final Map<String, List<String>> canales = new HashMap<>();
    private final Map<String, Map<String, Integer>> lecturaClientes = new HashMap<>();

    public void registrarCliente(String clientID) {
        synchronized (canales) {
            lecturaClientes.putIfAbsent(clientID, new HashMap<>());
        }
    }

    public CommandResult crearCanal(String[] partes, String clientID) {
        if (partes.length < 2) return new CommandResult("ERROR Falta nombre canal");
        String canal = partes[1];

        synchronized (canales) {
            if (canales.containsKey(canal)) {
                return new CommandResult("ERROR Canal ya existe");
            } else {
                canales.put(canal, new ArrayList<>());
                return new CommandResult("MKCHAN_OK");
            }
        }
    }

    public CommandResult eliminarCanal(String[] partes, String clientID) {
        if (partes.length < 2) return new CommandResult("ERROR Falta nombre canal");
        String canal = partes[1];

        synchronized (canales) {
            if (canales.containsKey(canal)) {
                canales.remove(canal);
                // Despierta a los clientes que esperaban en este canal para que reciban el error
                canales.notifyAll();
                return new CommandResult("RMCHAN_OK");
            } else {
                return new CommandResult("ERROR Canal no existe");
            }
        }
    }

    public CommandResult escribirMensaje(String[] partes, String clientID) {
        if (partes.length < 3) return new CommandResult("ERROR Faltan parámetros WRITE");
        String canal = partes[1];
        String mensaje = partes[2];

        synchronized (canales) {
            List<String> lista = canales.get(canal);
            if (lista == null) return new CommandResult("ERROR Canal no existe");
            lista.add(mensaje);
            // Avisa a los clientes que estaban esperando un mensaje nuevo
            canales.notifyAll();
            return new CommandResult("WRITE_OK");
        }
    }

    public CommandResult leerMensaje(String[] partes, String clientID) {
        if (partes.length < 3) return new CommandResult("ERROR Faltan parámetros READ");
        if (clientID == null) return new CommandResult("ERROR Cliente no registrado (usa OPEN)");
        String canal = partes[1];
        boolean dontwait = partes[2].equalsIgnoreCase("true");

        synchronized (canales) {
            while (true) {
                List<String> mensajes = canales.get(canal);
                if (mensajes == null) return new CommandResult("ERROR Canal no existe");

                Map<String, Integer> leidos = lecturaClientes.get(clientID);
                int indice = leidos.getOrDefault(canal, 0);

                if (indice < mensajes.size()) {
                    leidos.put(canal, indice + 1);
                    return new CommandResult("MSG " + mensajes.get(indice));
                }

                if (dontwait) return new CommandResult("NULL");

                // Lectura bloqueante: espera hasta que alguien escriba o borre el canal.
                // wait() libera el cerrojo mientras espera, así los demás clientes pueden seguir trabajando.
                try {
                    canales.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new CommandResult("ERROR Lectura interrumpida");
                }
            }
        }
    }
}
