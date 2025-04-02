import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String IP = "localhost";
    private static final int PORTA = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(IP, PORTA);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor. Digite mensagens para enviar:");

            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Servidor: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao receber mensagem do servidor.");
                }
            });

            readerThread.start();

            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("sair")) {
                    break;
                }
                out.println(message);
            }

            System.out.println("Desconectando...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
