import java.util.*;

class Jogo {
    private final List<Jogador> jogadores;
    private final Baralho baralho;
    private Map<String, Integer> rankingValores;
    private Map<String, Integer> rankingNaipes;
    private final Scanner scanner;
    private final int[] pontos;
    private String manilha;
    private boolean trucado;

    public Jogo(List<Jogador> jogadores) {
        this.jogadores = jogadores;
        this.baralho = new Baralho();
        this.scanner = new Scanner(System.in);
        this.pontos = new int[jogadores.size()];
        this.trucado = false;
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
            System.out.println("\n===== Rodada " + rodada + " =====");
            distribuirCartas();
            randomManilha();
            Jogador vencedorRodada = jogarRodada();

            if (vencedorRodada != null) {
                int indexVencedor = vencedorRodada.getTeam();
                pontos[indexVencedor - 1] += (trucado ? 3 : 1);
                System.out.println("Vencedor da rodada: Time " + indexVencedor);
            } else {
                System.out.println("Rodada empatada! Nenhum ponto foi atribuído.");
            }

            System.out.println("Placar: Time " + jogadores.get(0).getTeam() + " " + pontos[0] + " - " + pontos[1] + " Time " + jogadores.get(1).getTeam());

            trucado = false;
            rodada++;
        }

        System.out.println("\n===== Jogo Encerrado! =====");
        System.out.println("Vencedor: " + (pontos[0] > pontos[1] ? jogadores.get(0).getTeam() : jogadores.get(1).getTeam()));
    }

    private void distribuirCartas() {
        for (Jogador jogador : jogadores) {
            jogador.getMao().clear();
            for (int i = 0; i < 3; i++) {
                jogador.receberCarta(baralho.distribuirCarta());
            }
        }
    }

    private boolean Trucar() {
        System.out.println("Deseja aceitar o truco ? (S/N)");
        char escolha = scanner.next().charAt(0);

        while (escolha != 'S' || escolha != 'N') {
            if (escolha == 'S') {
                return true;
            } else if (escolha == 'N') {
                return false;
            } else {
                System.out.println("Valor Invalido! Digite 'S' ou 'N'.");
                escolha = scanner.next().charAt(0);
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
            System.out.println("\n--- Turno " + turno + " ---");
            System.out.println("Vira na Mesa: " + manilha);

            Map<Jogador, Carta> jogadas = new HashMap<>();

            for (Jogador jogador : jogadores) {
                Carta cartaJogada = escolherCarta(jogador);
                if (cartaJogada == null) {
                    System.out.println(jogador.getNome() + " Pediu truco");
                    escolhaTruco = Trucar();
                    if (escolhaTruco) {
                        System.out.println("Ele aceitou seu truco");
                        trucado = true;
                        cartaJogada = escolherCarta(jogador);
                    } else {
                        vencedorRodada = jogador;
                        break;
                    }
                } else {
                    System.out.println(jogador.getNome() + " jogou " + cartaJogada);
                    jogadas.put(jogador, cartaJogada);
                }
            }

            if (!escolhaTruco) {
                break;
            }

            vencedorTurno = determinarVencedor(jogadas);

            if (vencedorTurno != null) {
                int indexVencedor = vencedorTurno.getTeam();
                vitoriasTurno[indexVencedor - 1]++;
                System.out.println("Vencedor do turno: Time " + vencedorTurno.getTeam());

                if (vitoriasTurno[indexVencedor - 1] == 2) {
                    return vencedorTurno;
                }
            } else {
                System.out.println("Turno empatado!");
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
        System.out.println("\n" + jogador.getNome() + ", escolha uma carta para jogar:");

        for (int i = 0; i < mao.size(); i++) {
            System.out.println((i + 1) + " - " + mao.get(i));
        }

        System.out.println((mao.size() + 1) +" - Pedir truco");


        int escolha;
        do {
            System.out.print("Digite o número da carta: ");
            escolha = scanner.nextInt();
        } while (escolha < 1 || escolha > (mao.size() + 1));

        if (escolha == mao.size() + 1) {
            return null;
        }

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
            System.out.println(carta.getValor() + ", " + valorCarta + ", " + naipeCarta);

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
