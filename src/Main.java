import java.util.*;

public class Main {
    public static void main(String[] args) {
        Jogador jogador1 = new Jogador("Nicolas");
        Jogador jogador2 = new Jogador("Patinho do Nicolas");

        List<Jogador> jogadores = Arrays.asList(jogador1, jogador2);
        Jogo jogo = new Jogo(jogadores);
        jogo.iniciarJogo();
    }
}
