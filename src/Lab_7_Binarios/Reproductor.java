package lab_7_binarios;

import java.io.*;
import javax.sound.sampled.*;

public class Reproductor implements Reproducible {

    private Clip clip;
    private long pausePosition = 0;
    private boolean playing = false;
    private boolean paused = false;
    private Cancion cancionActual;

    public boolean cargar(Cancion c) {
        detenerYLimpiar();
        cancionActual = c;

        try {
            File archivo = new File(c.getRutaAudio());
            if (!archivo.exists()) {
                System.err.println("Archivo no encontrado: " + c.getRutaAudio());
                return false;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(archivo);
            AudioFormat formato = ais.getFormat();

            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            ais = AudioSystem.getAudioInputStream(decodedFormat, ais);

            DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(ais);
            pausePosition = 0;
            return true;

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Formato no soportado (instala mp3spi para MP3): " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error cargando audio: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void play() {
        if (clip == null) {
            return;
        }
        clip.setMicrosecondPosition(pausePosition);
        clip.start();
        playing = true;
        paused = false;
    }

    @Override
    public void pause() {
        if (clip == null || !playing) {
            return;
        }
        pausePosition = clip.getMicrosecondPosition();
        clip.stop();
        playing = false;
        paused = true;
    }

    @Override
    public void stop() {
        detenerYLimpiar();
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    public Cancion getCancionActual() {
        return cancionActual;
    }

    public double getProgreso() {
        if (clip == null || clip.getMicrosecondLength() == 0) {
            return 0;
        }
        return (double) clip.getMicrosecondPosition() / clip.getMicrosecondLength();
    }

    public void setLineListener(LineListener listener) {
        if (clip != null) {
            clip.addLineListener(listener);
        }
    }

    private void detenerYLimpiar() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        pausePosition = 0;
        playing = false;
        paused = false;
    }
}
