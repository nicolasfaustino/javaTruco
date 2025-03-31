import java.util.ArrayList;
import java.util.List;

class Jogador {
    private final String nome;
    private final int team;
    private final List<Carta> mao;

    public Jogador(String nome, int team) {
        this.nome = nome;
        this.mao = new ArrayList<>();
        this.team = team;

    }

    public void receberCarta(Carta carta) {
        if (mao.size() < 3) {
            mao.add(carta);
        }
    }

    public List<Carta> getMao() {
        return mao;
    }

    public int getTeam() {
        return team;
    }

    @Override
    public String toString() {
        return nome + " tem as cartas: " + mao;
    }

    public String getNome() {
        return nome;
    }
}
