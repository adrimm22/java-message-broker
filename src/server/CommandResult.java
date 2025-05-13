package server;

/**
 * Indica la respuesta y si se debe cerrar la conexión.
 */
public record CommandResult(String respuesta, boolean cerrarConexion) {
    public CommandResult(String respuesta) {
        this(respuesta, false);
    }
}