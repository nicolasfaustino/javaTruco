public class Trucar {
    private int team,value;
    public Trucar() {
        this.team = 0;
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
        this.value += 3;
    }

    public void resetTrucar() {
        this.team = 0;
        this.value = 0;
    }

    public boolean haveTrucar(int team) {
        if (this.team == 0 || this.team != team) {
            if (this.value != 12) {
                return true;
            }
        }

        return false;
    }

    public String getType() {
        switch (this.value) {
            case 0:
                return "Truco";
            case 3:
                return "Seis";
            case 6:
                return "Nove";
            case 9:
                return "Doze";
        }

        return null;
    }
}
