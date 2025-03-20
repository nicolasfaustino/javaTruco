import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Baralho {
    private final List<Carta> cartas;

    public Baralho() {
        String[] naipes = {"Ouros", "Espadas", "Copas", "Paus"};
        String[] valores = {"A", "2", "3", "4", "5", "6", "7", "Q", "J", "K"};

        cartas = new ArrayList<>();
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
}
