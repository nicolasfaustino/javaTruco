import java.util.*;

class Jogo {
    private final List<Jogador> jogadores;
    private final Baralho baralho;
    private Map<String, Integer> rankingValores;
    private Map<String, Integer> rankingNaipes;
    private final int[] pontos;
    private String manilha;
    private boolean trucado;
    private final Server server;
    private final Trucar trucar;

    public Jogo(List<Jogador> players) {
        this.jogadores = players;
        this.baralho = new Baralho();
        this.pontos = new int[players.size()];
        this.trucado = false;
        this.server = new Server();
        this.trucar = new Trucar();
        inicializarRankingValores();
    }

    private void inicializarRankingValores() {
        rankingValores = new HashMap<>();
        rankingValores.put("3", 10);
        rankingValores.put("2", 9);
        rankingValores.put("A", 8);
        rankingValores.put("K", 7);
        rankingValores.put("J", 6);
        rankingValores.put("Q", 5);
        rankingValores.put("7", 4);
        rankingValores.put("6", 3);
        rankingValores.put("5", 2);
        rankingValores.put("4", 1);

        rankingNaipes = new HashMap<>();
        rankingNaipes.put("Paus", 4);
        rankingNaipes.put("Copas", 3);
        rankingNaipes.put("Espadas", 2);
        rankingNaipes.put("Ouros", 1);
    }

    private void replaceRankingValores(String nome) {
        rankingValores.replace(nome, 11);
    }

    private void randomManilha() {
        List<String> cartas = new ArrayList<>(Arrays.asList("4", "5", "6", "7", "Q", "J", "K", "A", "2", "3"));

        Random random = new Random();
        int index = random.nextInt(cartas.size() - 1);

        manilha = cartas.get(index);
        if (index == 9) {
            replaceRankingValores("A");
        } else {
            replaceRankingValores(cartas.get(index + 1));
        }
    }

    public void iniciarJogo() {
        baralho.embaralhar();

        int rodada = 1;

        while (pontos[0] < 12 && pontos[1] < 12) {
            Server.broadcast("\n===== Rodada " + rodada + " =====");
            distribuirCartas();
            randomManilha();
            Jogador vencedorRodada = jogarRodada();

            if (vencedorRodada != null) {
                int indexVencedor = vencedorRodada.getTeam();
                pontos[indexVencedor - 1] += (trucar.getValue() == 0 ? 1 : trucar.getValue());
                baralho.resetarBaralho();
                baralho.embaralhar();
                Server.broadcast("Vencedor da rodada: Time " + indexVencedor);
            } else {
                Server.broadcast("Rodada empatada! Nenhum ponto foi atribuído.");
            }

            Server.broadcast("Placar: Time " + jogadores.get(0).getTeam() + " " + pontos[0] + " - " + pontos[1] + " Time " + jogadores.get(1).getTeam());

            trucar.resetTrucar();
            rodada++;
        }

        Server.broadcast("\n===== Jogo Encerrado! =====");
        Server.broadcast("Vencedor: " + (pontos[0] > pontos[1] ? jogadores.get(0).getTeam() : jogadores.get(1).getTeam()));
    }

    private void distribuirCartas() {
        for (Jogador jogador : jogadores) {
            jogador.getMao().clear();
            for (int i = 0; i < 3; i++) {
                jogador.receberCarta(baralho.distribuirCarta());
            }
        }
    }

    private boolean Trucar(int team) {
        int index = team == 1 ? 1 : 0;
        String nome = jogadores.get(index).getNome();
        Server.enviarMensagem(nome, "Deseja aceitar ? (S/N)");
        String escolha = server.recebeMensagem(nome);

        while (!escolha.equals("S") || !escolha.equals("N")) {
            if (escolha.equals("S")) {
                return true;
            } else if (escolha.equals("N")) {
                return false;
            } else {
                Server.enviarMensagem(nome, "Valor Invalido! Digite 'S' ou 'N'.");
                escolha = server.recebeMensagem(nome);
            }
        }

        return false;
    }

    private Jogador jogarRodada() {
        int[] vitoriasTurno = new int[jogadores.size()];
        boolean escolhaTruco = false;
        Jogador vencedorTurno = null;
        Jogador vencedorRodada = null;

        for (int turno = 1; turno <= 3; turno++) {
            Server.broadcast("\n--- Turno " + turno + " ---");
            Server.broadcast("Vira na Mesa: " + manilha);

            Map<Jogador, Carta> jogadas = new HashMap<>();

            for (Jogador jogador : jogadores) {
                int team = jogador.getTeam();
                String nome = jogador.getNome();
                Carta cartaJogada = null;

                boolean b = trucar.haveTrucar(team);
                Server.enviarMensagem(nome, "1 - Escolher Carta \n" + (b ? "2 - Trucar" : ""));
                boolean unlocked = false;
                int escolha = 0;
                while (!unlocked) {
                    escolha = Integer.parseInt(server.recebeMensagem(nome));
                    if (escolha == 1) {
                        unlocked = true;
                    } else if (escolha == 2 && b) {
                        unlocked = true;
                    } else {
                        Server.enviarMensagem(nome, "1 - Escolher Carta \n " + (b ? "2 - Trucar" : ""));
                    }
                }

                if (escolha == 1) {
                    cartaJogada = escolherCarta(jogador);
                    Server.broadcast(nome + " jogou " + cartaJogada);
                    jogadas.put(jogador, cartaJogada);
                } else if (escolha == 2) {
                    Server.broadcast(nome + " pediu " + trucar.getType());
                    escolhaTruco = Trucar(team);
                    if (escolhaTruco) {
                        Server.broadcast("A outra equipe aceitou o truco");
                        trucar.setTeam(team);
                        cartaJogada = escolherCarta(jogador);
                        jogadas.put(jogador, cartaJogada);
                    } else {
                        vencedorRodada = jogador;
                        break;
                    }
                }
            }

            if (vencedorRodada != null) {
                break;
            }

            vencedorTurno = determinarVencedor(jogadas);

            if (vencedorTurno != null) {
                int indexVencedor = vencedorTurno.getTeam();
                vitoriasTurno[indexVencedor - 1]++;
                Server.broadcast("Vencedor do turno: Time " + vencedorTurno.getTeam());

                if (vitoriasTurno[indexVencedor - 1] == 2) {
                    return vencedorTurno;
                }
            } else {
                Server.broadcast("Turno empatado!");
            }
        }

        if (vencedorRodada != null) {
            return vencedorRodada;
        }else if (vitoriasTurno[0] > vitoriasTurno[1]) {
            return jogadores.getFirst();
        } else if (vitoriasTurno[1] > vitoriasTurno[0]) {
            return jogadores.getLast();
        }

        return null;
    }

    private Carta escolherCarta(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        Server.enviarMensagem(jogador.getNome(), "escolha uma carta para jogar:");

        for (int i = 0; i < mao.size(); i++) {
            Server.enviarMensagem(jogador.getNome(),(i + 1) + " - " + mao.get(i));
        }


        int escolha;
        do {
            Server.enviarMensagem(jogador.getNome(),"Digite o número da carta: ");
            escolha = Integer.parseInt(server.recebeMensagem(jogador.getNome()));
        } while (escolha < 1 || escolha > mao.size());

        return mao.remove(escolha - 1);
    }

    private Jogador determinarVencedor(Map<Jogador, Carta> jogadas) {
        Jogador vencedor = null;
        int maiorValor = -1;
        String melhorNaipe = null;

        for (Map.Entry<Jogador, Carta> entrada : jogadas.entrySet()) {
            Carta carta = entrada.getValue();
            int valorCarta = rankingValores.get(carta.getValor());
            String naipeCarta = carta.getNaipe();

            if (valorCarta > maiorValor || (valorCarta == maiorValor && rankingNaipes.get(naipeCarta) > rankingNaipes.get(melhorNaipe))) {
                maiorValor = valorCarta;
                melhorNaipe = naipeCarta;
                vencedor = entrada.getKey();
            } else if (valorCarta == maiorValor && rankingNaipes.get(naipeCarta).equals(rankingNaipes.get(melhorNaipe))) {
                return null; // Empate
            }
        }
        return vencedor;
    }
}
