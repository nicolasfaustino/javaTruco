import java.util.*;

public class Main {
    public static void main(String[] args) {
        Jogador jogador1 = new Jogador("Nicolas", 1);
        Jogador jogador2 = new Jogador("Patinho do Nicolas", 2);
        Jogador jogador3 = new Jogador("Nicolas 2", 1);
        Jogador jogador4 = new Jogador("Patinho do Nicolas 2", 2);

        List<Jogador> jogadores = Arrays.asList(jogador1, jogador2, jogador3, jogador4);
        Jogo jogo = new Jogo(jogadores);
        jogo.iniciarJogo();
    }
}
