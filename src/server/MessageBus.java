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
        lecturaClientes.putIfAbsent(clientID, new HashMap<>());
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
            return new CommandResult("WRITE_OK");
        }
    }

    public CommandResult leerMensaje(String[] partes, String clientID) {
        if (partes.length < 3) return new CommandResult("ERROR Faltan parámetros READ");
        String canal = partes[1];
        boolean dontwait = partes[2].equalsIgnoreCase("true");

        synchronized (canales) {
            List<String> mensajes = canales.get(canal);
            if (mensajes == null) return new CommandResult("ERROR Canal no existe");

            Map<String, Integer> leidos = lecturaClientes.get(clientID);
            int indice = leidos.getOrDefault(canal, 0);

            if (indice < mensajes.size()) {
                leidos.put(canal, indice + 1);
                return new CommandResult("MSG " + mensajes.get(indice));
            } else {
                return dontwait ? new CommandResult("NULL") : new CommandResult("WAIT");
            }
        }
    }
}