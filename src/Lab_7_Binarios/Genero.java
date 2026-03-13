package lab_7_binarios;

public class Genero {

    private String nombre;

    public Genero(String nombre) {
        this.nombre = (nombre == null || nombre.trim().isEmpty()) ? "Otro" : nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public static final String[] SUGERENCIAS = {
        "Pop", "Rock", "Hip Hop", "Electrónica", "Clásica",
        "Jazz", "Reggaetón", "Latin", "Metal", "Otro"
    };
}
