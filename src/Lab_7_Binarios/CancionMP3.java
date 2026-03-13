package lab_7_binarios;

public class CancionMP3 extends Cancion {

    public CancionMP3(String nombre, String artista, int duracion,
            String rutaAudio, String rutaImagen, Genero genero) {
        super(nombre, artista, duracion, rutaAudio, rutaImagen, genero);
    }

    @Override
    public String getTipo() {
        String ruta = getRutaAudio().toLowerCase();
        if (ruta.endsWith(".mp3")) {
            return "MP3";
        }
        if (ruta.endsWith(".wav")) {
            return "WAV";
        }
        return "Audio";
    }
}
