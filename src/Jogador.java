import java.util.ArrayList;
import java.util.List;

class Jogador {
    private final String nome;
    private final List<Carta> mao;

    public Jogador(String nome) {
        this.nome = nome;
        this.mao = new ArrayList<>();
    }

    public void receberCarta(Carta carta) {
        if (mao.size() < 3) {
            mao.add(carta);
        }
    }

    public List<Carta> getMao() {
        return mao;
    }

    @Override
    public String toString() {
        return nome + " tem as cartas: " + mao;
    }

    public String getNome() {
        return nome;
    }
}
