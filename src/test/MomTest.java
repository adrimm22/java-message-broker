package test;

import mom.Mom;
import mom.MomImpl;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MomTest {

    private static Mom cliente1;
    private static Mom cliente2;

    @BeforeAll
    public static void setUp() {
        cliente1 = new MomImpl();
        cliente2 = new MomImpl();

        cliente1.open("cliente1");
        cliente2.open("cliente2");
    }

    @AfterAll
    public static void tearDown() {
        cliente1.close();
        cliente2.close();
    }

    @Test
    @Order(1)
    public void testCrearCanalYEscribir() {
        cliente1.mkChannel("canalTest");
        cliente1.writeChannel("canalTest", "msg1");
        cliente1.writeChannel("canalTest", "msg2");
        cliente1.writeChannel("canalTest", "msg3");
    }

    @Test
    @Order(2)
    public void testCliente1LeeUnMensaje() {
        String msg = cliente1.readChannel("canalTest", true);
        assertEquals("msg1", msg);
    }

    @Test
    @Order(3)
    public void testCliente2LeeDosMensajes() {
        String msg1 = cliente2.readChannel("canalTest", true);
        String msg2 = cliente2.readChannel("canalTest", true);
        assertEquals("msg1", msg1);
        assertEquals("msg2", msg2);
    }

    @Test
    @Order(4)
    public void testCliente1LeeSegundoMensaje() {
        String msg = cliente1.readChannel("canalTest", true);
        assertEquals("msg2", msg);
    }

    @Test
    @Order(5)
    public void testCliente2LeeTercerMensaje() {
        String msg = cliente2.readChannel("canalTest", true);
        assertEquals("msg3", msg);
    }

    @Test
    @Order(6)
    public void testLecturaNullAmbos() {
        // cliente1 aún tiene pendiente el tercer mensaje ("msg3")
        String msg3Cliente1 = cliente1.readChannel("canalTest", true);
        assertEquals("msg3", msg3Cliente1);

        // Ahora ambos deberían haber leído los 3
        String cliente1Null = cliente1.readChannel("canalTest", true);
        assertNull(cliente1Null);

        String cliente2Null = cliente2.readChannel("canalTest", true);
        assertNull(cliente2Null);
    }

    @Test
    @Order(7)
    public void testEliminarCanal() {
        cliente1.rmChannel("canalTest");

        String result = cliente1.readChannel("canalTest", true);
        assertTrue(result.contains("ERROR Canal no existe"));
    }

    @Test
    @Order(8)
    public void testLecturaBloqueante() throws Exception {
        cliente1.mkChannel("canalBloqueante");

        // cliente2 empieza a leer sin dontwait, así que debe quedarse esperando
        java.util.concurrent.CompletableFuture<String> lectura =
                java.util.concurrent.CompletableFuture.supplyAsync(
                        () -> cliente2.readChannel("canalBloqueante", false));

        Thread.sleep(500);
        assertFalse(lectura.isDone());

        // En cuanto cliente1 escribe, cliente2 recibe el mensaje
        cliente1.writeChannel("canalBloqueante", "llega tarde");
        assertEquals("llega tarde", lectura.get(5, java.util.concurrent.TimeUnit.SECONDS));
    }
}
