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
                System.err.println("File not found: " + c.getRutaAudio());
                return false;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(archivo);
            AudioFormat fmt = ais.getFormat();

            
            if (fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat pcm = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        fmt.getSampleRate(),
                        16,
                        fmt.getChannels(),
                        fmt.getChannels() * 2,
                        fmt.getSampleRate(),
                        false);
                ais = AudioSystem.getAudioInputStream(pcm, ais);
            }

            DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(ais);
            pausePosition = 0;
            return true;

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported format (install mp3spi for MP3): " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error loading audio: " + e.getMessage());
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
        if (clip == null || clip.getMicrosecondLength() <= 0) {
            return 0;
        }
        return (double) clip.getMicrosecondPosition() / clip.getMicrosecondLength();
    }

   
    public int getDuracionRealSegundos() {
        if (clip == null || clip.getMicrosecondLength() <= 0) {
            return 0;
        }
        return (int) (clip.getMicrosecondLength() / 1_000_000L);
    }

    public void setVolumen(float nivel) {
        if (clip == null) {
            return;
        }
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = nivel <= 0 ? gain.getMinimum()
                    : (float) (20.0 * Math.log10(nivel));
            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
            gain.setValue(dB);
        }
    }

    public void seekTo(double ratio) {
        if (clip == null) {
            return;
        }
        long pos = (long) (clip.getMicrosecondLength() * Math.max(0, Math.min(1.0, ratio)));
        boolean wasPlaying = playing;
        clip.stop();
        clip.setMicrosecondPosition(pos);
        pausePosition = pos;
        if (wasPlaying) {
            clip.start();
        }
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
