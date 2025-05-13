package mom;

/**
 * Interfaz que define el comportamiento del cliente en el sistema MOM.
 */
public interface Mom {
    void open(String cliID);
    void close();
    void mkChannel(String name);
    void rmChannel(String name);
    void writeChannel(String chan, String msg);
    String readChannel(String chan, boolean dontwait);
}