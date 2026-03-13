package lab_7_binarios;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {

    private static final String CARPETA = "playlist_data";
    private static final String ARCHIVO = CARPETA + "/playlist.dat";
    private RandomAccessFile raf;

    public PlaylistManager() throws IOException {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdir();
        }

        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            archivo.createNewFile();
        }

        raf = new RandomAccessFile(archivo, "rw");
    }

    public void agregar(Cancion c) throws IOException {
        raf.seek(raf.length());
        escribirRegistro(c);
    }

    public List<Cancion> leerTodas() throws IOException {
        List<Cancion> lista = new ArrayList<>();
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            lista.add(leerRegistro());
        }
        return lista;
    }

    public void eliminar(int indice) throws IOException {
        List<Cancion> lista = leerTodas();
        if (indice < 0 || indice >= lista.size()) {
            return;
        }
        lista.remove(indice);
        raf.seek(0);
        raf.setLength(0);
        for (Cancion c : lista) {
            escribirRegistro(c);
        }
    }

    public void reescribirTodas(List<Cancion> lista) throws IOException {
        raf.seek(0);
        raf.setLength(0);
        for (Cancion c : lista) {
            escribirRegistro(c);
        }
    }

    public void cerrar() {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void escribirRegistro(Cancion c) throws IOException {
        escribirCampo(c.getNombre(), Cancion.NOMBRE_MAX);
        escribirCampo(c.getArtista(), Cancion.ARTISTA_MAX);
        escribirCampo(c.getRutaAudio(), Cancion.RUTA_MAX);
        escribirCampo(c.getRutaImagen(), Cancion.IMAGEN_MAX);
        escribirCampo(c.getGenero().getNombre(), Cancion.GENERO_MAX);  
        raf.writeInt(c.getDuracion());
    }

    private void escribirCampo(String valor, int maxChars) throws IOException {
        String ajustado = Cancion.ajustar(valor, maxChars);
        for (char ch : ajustado.toCharArray()) {
            raf.writeChar(ch);
        }
    }

    private Cancion leerRegistro() throws IOException {
        String nombre = leerCampo(Cancion.NOMBRE_MAX).trim();
        String artista = leerCampo(Cancion.ARTISTA_MAX).trim();
        String rutaAudio = leerCampo(Cancion.RUTA_MAX).trim();
        String rutaImagen = leerCampo(Cancion.IMAGEN_MAX).trim();
        String generoStr = leerCampo(Cancion.GENERO_MAX).trim();
        int duracion = raf.readInt();
        return new CancionMP3(nombre, artista, duracion, rutaAudio, rutaImagen,
                new Genero(generoStr));
    }

    private String leerCampo(int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxChars; i++) {
            sb.append(raf.readChar());
        }
        return sb.toString();
    }
}
