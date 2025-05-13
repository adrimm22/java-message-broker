package mom;

import java.util.Scanner;

public class InteractiveClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Mom mom = new MomImpl();

        // Pedir ID del cliente
        System.out.print("Introduce tu ID de cliente: ");
        String cliID = scanner.nextLine();
        mom.open(cliID);

        boolean salir = false;
        while (!salir) {
            System.out.println("\n--- MENÚ MOM ---");
            System.out.println("1. Crear canal");
            System.out.println("2. Eliminar canal");
            System.out.println("3. Escribir mensaje");
            System.out.println("4. Leer mensaje");
            System.out.println("5. Salir");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    System.out.print("Nombre del canal: ");
                    String canal1 = scanner.nextLine();
                    mom.mkChannel(canal1);
                    break;

                case "2":
                    System.out.print("Nombre del canal a eliminar: ");
                    String canal2 = scanner.nextLine();
                    mom.rmChannel(canal2);
                    break;

                case "3":
                    System.out.print("Canal: ");
                    String canalEscribir = scanner.nextLine();
                    System.out.print("Mensaje: ");
                    String mensaje = scanner.nextLine();
                    mom.writeChannel(canalEscribir, mensaje);
                    break;

                case "4":
                    System.out.print("Canal: ");
                    String canalLeer = scanner.nextLine();
                    System.out.print("¿dontwait? (true/false): ");
                    boolean dw = Boolean.parseBoolean(scanner.nextLine());
                    String resultado = mom.readChannel(canalLeer, dw);
                    System.out.println("Respuesta: " + resultado);
                    break;

                case "5":
                    salir = true;
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }

        mom.close();
        System.out.println("Cliente desconectado.");
    }
}