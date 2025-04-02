import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int PORTA = 8080;
    private static final String IP = "127.0.0.1";
    private static final List<ClientHandler> conections = Collections.synchronizedList(new ArrayList<>());
    private static volatile boolean jogoIniciado = false;

    public static void main(String[] args) throws IOException {
        List<Jogador> jogadores = new ArrayList<>();
        try {
            ServerSocket server = new ServerSocket(PORTA);
            System.out.println("Servidor aberto com a porta " + PORTA);
            while (jogadores.size() < 2 || !jogoIniciado) {
                if (jogadores.size() < 2) {
                    System.out.println("Jogo Iniciado " + jogoIniciado);
                    Socket socket = server.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    conections.add(clientHandler);

                    Thread thread = new Thread(clientHandler);
                    System.out.println("Conectado ao servidor. Digite mensagem:");
                    thread.start();

                    while (clientHandler.getNome() == null) {
                        Thread.sleep(100);
                    }

                    Jogador jogador = new Jogador(clientHandler.getNome(), ((jogadores.isEmpty() || jogadores.size() == 2) ? 1 : 2));
                    jogadores.add(jogador);

                    if (jogadores.size() == 1) {
                        clientHandler.setHost(true);
                        clientHandler.enviarMensagem("Você é o host. Digite 'iniciar' para começar o jogo!");
                    } else {
                        clientHandler.enviarMensagem("Aguarde o host iniciar o jogo!");
                    }
                }
            }

            System.out.println("Jogo vai começar! " + jogadores);

            Jogo jogo = new Jogo(jogadores);
            jogo.iniciarJogo();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcast(String message) {
        synchronized (conections) {
            for (ClientHandler player : conections) {
                player.enviarMensagem(message);
            }
        }
    }

    public static void enviarMensagem(String nome, String mensagem) {
        for (ClientHandler player : conections) {
            if (Objects.equals(player.nome, nome)) {
                player.enviarMensagem(mensagem);
            }
        }
    }

    public String recebeMensagem(String nome) {
        for (ClientHandler player : conections) {
            if (Objects.equals(player.nome, nome)) {
                return player.recebeMensagem();
            }
        }

        return null;
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private boolean host;
        private String nome;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setHost(boolean host) {
            this.host = host;
        }

        public void enviarMensagem(String mensagem) {
            out.println(mensagem);
        }

        public String recebeMensagem() {
            try {
                return in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void run() {
            try {
                out.println("Digite seu nome:");
                nome = in.readLine();

                TimeUnit.SECONDS.sleep(1);

                System.out.println("Jogador conectado: " + nome + " " + host);

                if (host) {
                    while (!jogoIniciado) {
                        String input = recebeMensagem();
                        System.out.println(input);
                        if (Objects.equals(input, "iniciar")) {
                            jogoIniciado = true;
                            Server.broadcast("O jogo foi iniciado pelo host!");
                        } else {
                            enviarMensagem("Para iniciar o jogo, digite 'iniciar'.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public String getNome() {
            return nome;
        }
    }
}
