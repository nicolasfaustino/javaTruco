import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Baralho {
    private final List<Carta> cartas;
    private final String[] naipes = {"Ouros", "Espadas", "Copas", "Paus"};
    private final String[] valores = {"A", "2", "3", "4", "5", "6", "7", "Q", "J", "K"};

    public Baralho() {
        cartas = new ArrayList<>();
        iniciarBaralho();
    }

    private void iniciarBaralho() {
        for (String naipe : naipes) {
            for (String valor : valores) {
                cartas.add(new Carta(naipe, valor));
            }
        }
    }

    public void embaralhar() {
        Collections.shuffle(cartas);
    }

    public Carta distribuirCarta() {
        return cartas.isEmpty() ? null : cartas.removeFirst();
    }

    public void resetarBaralho() {
        this.cartas.clear();
        iniciarBaralho();
    }
}
