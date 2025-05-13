package mom;

import java.io.*;
import java.net.Socket;

/**
 * Implementación del cliente MOM que se comunica con el servidor por TCP.
 */
public class MomImpl implements Mom {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void open(String cliID) {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Lee mensaje de bienvenida del servidor
            System.out.println(in.readLine());

            // Envia comando OPEN
            out.println("OPEN " + cliID);
            System.out.println(in.readLine());  // debería ser OPEN_OK

        } catch (IOException e) {
            throw new RuntimeException("Error al abrir conexión", e);
        }
    }

    @Override
    public void close() {
        try {
            out.println("CLOSE");
            System.out.println(in.readLine());  // debería ser CLOSE_OK

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException("Error al cerrar conexión", e);
        }
    }

    @Override
    public void mkChannel(String name) {
        out.println("MKCHAN " + name);
        printRespuesta();
    }

    @Override
    public void rmChannel(String name) {
        out.println("RMCHAN " + name);
        printRespuesta();
    }

    @Override
    public void writeChannel(String chan, String msg) {
        out.println("WRITE " + chan + " " + msg);
        printRespuesta();
    }

    @Override
    public String readChannel(String chan, boolean dontwait) {
        out.println("READ " + chan + " " + dontwait);
        String respuesta = leerLinea();
        System.out.println("Cliente lee: " + respuesta);  // NUEVO
        if (respuesta == null) return null;
        if (respuesta.startsWith("MSG ")) {
            return respuesta.substring(4);
        } else if (respuesta.equals("NULL")) {
            return null;
        } else {
            return "ERROR: " + respuesta;
        }
    }

    private void printRespuesta() {
        System.out.println("Servidor: " + leerLinea());
    }

    private String leerLinea() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo del servidor", e);
        }
    }
}