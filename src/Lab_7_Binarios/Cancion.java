package lab_7_binarios;

import java.io.Serializable;

public abstract class Cancion implements Serializable {

    public static final int NOMBRE_MAX = 100;
    public static final int ARTISTA_MAX = 100;
    public static final int RUTA_MAX = 300;
    public static final int IMAGEN_MAX = 300;
    public static final int GENERO_MAX = 50;   
    public static final int RECORD_SIZE
            = (NOMBRE_MAX + ARTISTA_MAX + RUTA_MAX + IMAGEN_MAX + GENERO_MAX) * 2 + 4;

    private String nombre;
    private String artista;
    private int duracion;
    private String rutaAudio;
    private String rutaImagen;
    private Genero genero;

    public Cancion(String nombre, String artista, int duracion,
            String rutaAudio, String rutaImagen, Genero genero) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.rutaAudio = rutaAudio;
        this.rutaImagen = rutaImagen;
        this.genero = genero;
    }

    public abstract String getTipo();

    public String getNombre() {
        return nombre;
    }

    public String getArtista() {
        return artista;
    }

    public int getDuracion() {
        return duracion;
    }

    public String getRutaAudio() {
        return rutaAudio;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setNombre(String n) {
        this.nombre = n;
    }

    public void setArtista(String a) {
        this.artista = a;
    }

    public void setDuracion(int d) {
        this.duracion = d;
    }

    public void setRutaAudio(String r) {
        this.rutaAudio = r;
    }

    public void setRutaImagen(String r) {
        this.rutaImagen = r;
    }

    public void setGenero(Genero g) {
        this.genero = g;
    }

    public String getDuracionFormateada() {
        int min = duracion / 60;
        int seg = duracion % 60;
        return String.format("%d:%02d", min, seg);
    }

    @Override
    public String toString() {
        return nombre + " — " + artista + " (" + getDuracionFormateada() + ")";
    }

    public static String ajustar(String s, int maxChars) {
        if (s == null) {
            s = "";
        }
        if (s.length() > maxChars) {
            return s.substring(0, maxChars);
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < maxChars) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
