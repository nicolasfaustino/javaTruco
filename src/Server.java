import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int PORTA = 8080;
    private static final List<ClientHandler> conections = Collections.synchronizedList(new ArrayList<>());
    private static final List<Jogador> jogadores = new ArrayList<>();
    private static volatile boolean jogoIniciado = false;
    private static boolean teamGame = false;

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket server = new ServerSocket(PORTA);
            System.out.println("Servidor aberto com a porta " + PORTA);

            Scanner scanner = new Scanner(System.in);
            boolean continuar = false;
            while (!continuar) {
                System.out.println("1 - Jogo solo");
                System.out.println("2 - Jogo com Duplas");
                int escolha = scanner.nextInt();
                switch (escolha) {
                    case 1:
                        continuar = true;
                        teamGame = false;
                        break;
                    case 2:
                        continuar = true;
                        teamGame = true;
                        break;
                    default:
                        break;
                }
            }

            while (!jogoIniciado) {
                if ((teamGame ? jogadores.size() < 4 : jogadores.size() < 2)) {
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
        private final PrintWriter out;
        private final BufferedReader in;
        private boolean host;
        private String nome;

        public ClientHandler(Socket socket) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }
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
                            if (teamGame ? jogadores.size() < 4 : jogadores.size() < 2) {
                                enviarMensagem("Aguarde os jogadores entrarem");
                            } else {
                                jogoIniciado = true;
                                Server.broadcast("O jogo foi iniciado pelo host!");
                            }
                        } else {
                            enviarMensagem("Para iniciar o jogo, digite 'iniciar'.");
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public String getNome() {
            return nome;
        }
    }
}
