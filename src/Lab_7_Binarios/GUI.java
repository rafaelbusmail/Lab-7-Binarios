package lab_7_binarios;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import javax.imageio.ImageIO;

public class GUI extends JFrame {

    private PlaylistManager playlistManager;
    private Reproductor reproductor;
    private List<Cancion> playlist;
    private int indiceActual = -1;

    private JLabel lblImagen;
    private JLabel lblNombre;
    private JLabel lblArtista;
    private JLabel lblDuracion;
    private JLabel lblGenero;
    private JProgressBar barraProgreso;
    private JButton btnPlayPause;
    private JButton btnStop;
    private JButton btnAdd, btnRemove;
    private JList<String> listaUI;
    private DefaultListModel<String> modeloLista;
    private Timer timerUI;

    private static final int IMG_W = 340;
    private static final int IMG_H = 340;

    private static final Color BG_DARK = new Color(18, 18, 24);
    private static final Color BG_CARD = new Color(30, 30, 40);
    private static final Color BG_LIST = new Color(24, 24, 32);
    private static final Color ACCENT = new Color(255, 80, 80);
    private static final Color TEXT_MAIN = new Color(240, 240, 250);
    private static final Color TEXT_SUB = new Color(160, 160, 180);
    private static final Color TEXT_DIM = new Color(100, 100, 120);
    private static final Color SEL_BG = new Color(50, 50, 70);

    public GUI() {
        try {
            playlistManager = new PlaylistManager();
            playlist = playlistManager.leerTodas();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error starting database: " + e.getMessage());
            System.exit(1);
        }
        reproductor = new Reproductor();
        initUI();
        refrescarLista();
        timerUI = new Timer(1000, e -> tickUI());
        timerUI.start();
    }

    
    private void initUI() {
        setTitle("Music Player");
        setSize(900, 580);
        setMinimumSize(new Dimension(800, 520));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reproductor.stop();
                playlistManager.cerrar();
                dispose();
                System.exit(0);
            }
        });

        add(panelCenter(), BorderLayout.CENTER);
        add(panelRight(), BorderLayout.EAST);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel panelCenter() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 16));

        lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(IMG_W, IMG_H));
        lblImagen.setMaximumSize(new Dimension(IMG_W, IMG_H));
        lblImagen.setMinimumSize(new Dimension(IMG_W, IMG_H));
        lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setBackground(new Color(35, 35, 48));
        lblImagen.setOpaque(true);
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 68), 1));
        mostrarImagenDefault();

        lblNombre = label("No song selected", 17, Font.BOLD, TEXT_MAIN);
        lblArtista = label("—", 13, Font.PLAIN, TEXT_SUB);
        lblGenero = label("", 12, Font.PLAIN, ACCENT);
        lblDuracion = label("0:00 / 0:00", 12, Font.PLAIN, TEXT_DIM);

        barraProgreso = new JProgressBar(0, 1000);
        barraProgreso.setMaximumSize(new Dimension(IMG_W, 8));
        barraProgreso.setPreferredSize(new Dimension(IMG_W, 8));
        barraProgreso.setAlignmentX(Component.CENTER_ALIGNMENT);
        barraProgreso.setForeground(ACCENT);
        barraProgreso.setBackground(new Color(50, 50, 68));
        barraProgreso.setBorderPainted(false);
        barraProgreso.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        barraProgreso.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seek(e.getX());
            }
        });
        barraProgreso.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                seek(e.getX());
            }
        });

        JPanel controls = buildControls();

        panel.add(lblImagen);
        panel.add(Box.createVerticalStrut(14));
        panel.add(lblNombre);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblArtista);
        panel.add(Box.createVerticalStrut(2));
        panel.add(lblGenero);
        panel.add(Box.createVerticalStrut(8));
        panel.add(barraProgreso);
        panel.add(Box.createVerticalStrut(2));
        panel.add(lblDuracion);
        panel.add(Box.createVerticalStrut(12));
        panel.add(controls);
        return panel;
    }

    private void seek(int clickX) {
        if (!reproductor.isPlaying() && !reproductor.isPaused()) {
            return;
        }
        double ratio = Math.max(0, Math.min(1.0,
                (double) clickX / barraProgreso.getWidth()));
        reproductor.seekTo(ratio);
        int total = reproductor.getDuracionRealSegundos();
        int elapsed = (int) (ratio * total);
        barraProgreso.setValue((int) (ratio * 1000));
        lblDuracion.setText(formatSeg(elapsed) + " / " + formatSeg(total));
    }

    private JPanel buildControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        p.setBackground(BG_DARK);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(340, 50));

        btnPlayPause = ctrlBtn("▶", "Play");
        btnStop = ctrlBtn("⏹", "Stop");

        btnPlayPause.addActionListener(e -> accionPlayPause());
        btnStop.addActionListener(e -> accionStop());

        p.add(btnPlayPause);
        p.add(btnStop);
        return p;
    }

    private JPanel panelRight() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_LIST);
        panel.setPreferredSize(new Dimension(310, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 16, 0));

        JLabel title = new JLabel("  Playlist");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 68)));

        modeloLista = new DefaultListModel<>();
        listaUI = new JList<>(modeloLista);
        listaUI.setBackground(BG_LIST);
        listaUI.setForeground(TEXT_MAIN);
        listaUI.setSelectionBackground(SEL_BG);
        listaUI.setSelectionForeground(TEXT_MAIN);
        listaUI.setFont(new Font("Arial", Font.PLAIN, 12));
        listaUI.setFixedCellHeight(48);
        listaUI.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        listaUI.setCellRenderer(new PlaylistCellRenderer());

        listaUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    accionSelect();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listaUI);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_LIST);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        btnPanel.setBackground(BG_LIST);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));

        btnAdd = actionBtn("+ Add", new Color(50, 110, 200));
        btnRemove = actionBtn("− Remove", new Color(180, 50, 50));
        btnAdd.addActionListener(e -> accionAdd());
        btnRemove.addActionListener(e -> accionRemove());

        btnPanel.add(btnAdd);
        btnPanel.add(btnRemove);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    
    private void accionPlayPause() {
        if (reproductor.isPlaying()) {
            reproductor.pause();
            btnPlayPause.setText("▶");
            btnPlayPause.setToolTipText("Play");
        } else if (reproductor.isPaused()) {
            reproductor.play();
            btnPlayPause.setText("⏸");
            btnPlayPause.setToolTipText("Pause");
        } else {
            int idx = listaUI.getSelectedIndex();
            if (idx < 0 && !playlist.isEmpty()) {
                idx = 0;
            }
            if (idx < 0) {
                return;
            }
            cargarYMostrar(idx);
            reproductor.play();
            btnPlayPause.setText("⏸");
            btnPlayPause.setToolTipText("Pause");
        }
    }

    private void accionStop() {
        Cancion c = reproductor.getCancionActual();
        reproductor.stop();
        btnPlayPause.setText("▶");
        btnPlayPause.setToolTipText("Play");
        barraProgreso.setValue(0);
        lblDuracion.setText("0:00 / " + (c != null ? formatSeg(c.getDuracion()) : "0:00"));
    }

    private void accionSelect() {
        int idx = listaUI.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        reproductor.stop();
        cargarYMostrar(idx);
        reproductor.play();
        btnPlayPause.setText("⏸");
        btnPlayPause.setToolTipText("Pause");
    }

    private void accionAdd() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select song");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Audio files (*.wav, *.mp3)", "wav", "mp3"));
        chooser.setAcceptAllFileFilterUsed(false);

        while (true) {
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File audioFile = chooser.getSelectedFile();
            if (!audioFile.exists() || !audioFile.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "\"" + audioFile.getName() + "\" was not found.\n"
                        + "Please select an existing audio file.",
                        "File not found", JOptionPane.WARNING_MESSAGE);
                continue;  
            }
            String ext = audioFile.getName().toLowerCase();
            if (!ext.endsWith(".mp3") && !ext.endsWith(".wav")) {
                JOptionPane.showMessageDialog(this,
                        "Please select a .mp3 or .wav file.",
                        "Invalid file type", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            String rutaAudio = audioFile.getAbsolutePath();

            // 2. Select cover image — also validated
            String rutaImagen = seleccionarImagen();

            // 3. Detect duration via Clip
            int duracionDetectada = detectarDuracionPorClip(rutaAudio);
            if (duracionDetectada == 0) {
                JOptionPane.showMessageDialog(this,
                        "Could not read duration from this file.\n"
                        + "Make sure mp3spi is in your lib/ folder for MP3 files.",
                        "Duration error", JOptionPane.WARNING_MESSAGE);
            }

            // 4. Metadata form
            JTextField tfName = new JTextField(
                    audioFile.getName().replaceAll("\\.[^.]+$", ""), 22);
            JTextField tfArtist = new JTextField(22);

            JComboBox<String> cbGenre = new JComboBox<>(Genero.SUGERENCIAS);
            cbGenre.setEditable(true);
            cbGenre.setSelectedIndex(-1);
            cbGenre.setToolTipText("Choose a suggestion or type your own");

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            form.add(new JLabel("Song name:"));
            form.add(tfName);
            form.add(new JLabel("Artist:"));
            form.add(tfArtist);
            form.add(new JLabel("Genre:"));
            form.add(cbGenre);
            form.add(new JLabel("Duration:"));
            form.add(new JLabel(formatSeg(duracionDetectada) + "  (auto-detected)"));

            int res = JOptionPane.showConfirmDialog(this, form,
                    "Song details", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return;
            }

            String genreText = "";
            Object genreSel = cbGenre.getEditor().getItem();
            if (genreSel != null) {
                genreText = genreSel.toString().trim();
            }
            if (genreText.isEmpty()) {
                genreText = "Other";
            }

            Cancion nueva = new CancionMP3(
                    tfName.getText().trim(),
                    tfArtist.getText().trim(),
                    duracionDetectada,
                    rutaAudio, rutaImagen,
                    new Genero(genreText)
            );

            try {
                playlistManager.agregar(nueva);
                playlist.add(nueva);
                refrescarLista();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving song: " + e.getMessage());
            }
            return;  // done
        }
    }

    // Image selection with validation
    private String seleccionarImagen() {
        JFileChooser imgChooser = new JFileChooser();
        imgChooser.setDialogTitle("Select album art (optional — Cancel to skip)");
        imgChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (*.jpg, *.png)", "jpg", "jpeg", "png"));
        imgChooser.setAcceptAllFileFilterUsed(false);

        while (true) {
            int result = imgChooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return "";  // user cancelled = no image
            }
            File imgFile = imgChooser.getSelectedFile();
            if (!imgFile.exists() || !imgFile.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "\"" + imgFile.getName() + "\" was not found.\n"
                        + "Please select an existing image, or Cancel to skip.",
                        "File not found", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            String ext = imgFile.getName().toLowerCase();
            if (!ext.endsWith(".jpg") && !ext.endsWith(".jpeg") && !ext.endsWith(".png")) {
                JOptionPane.showMessageDialog(this,
                        "Please select a .jpg or .png file, or Cancel to skip.",
                        "Invalid file type", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return imgFile.getAbsolutePath();
        }
    }

    private void accionRemove() {
        int idx = listaUI.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a song to remove.");
            return;
        }
        if (idx == indiceActual) {
            reproductor.stop();
            btnPlayPause.setText("▶");
            btnPlayPause.setToolTipText("Play");
            indiceActual = -1;
            mostrarInfoCancion(null);
            mostrarImagenDefault();
            barraProgreso.setValue(0);
        }
        try {
            playlistManager.eliminar(idx);
            playlist.remove(idx);
            if (indiceActual > idx) {
                indiceActual--;
            }
            refrescarLista();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error removing song: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TIMER TICK
    // ═══════════════════════════════════════════════════════════════════
    private void tickUI() {
        if (!reproductor.isPlaying()) {
            return;
        }
        Cancion c = reproductor.getCancionActual();
        if (c == null) {
            return;
        }

        int total = reproductor.getDuracionRealSegundos();
        int elapsed = (int) (reproductor.getProgreso() * total);

        barraProgreso.setValue((int) (reproductor.getProgreso() * 1000));
        lblDuracion.setText(formatSeg(elapsed) + " / " + formatSeg(total));
    }

    
    private void cargarYMostrar(int idx) {
        if (idx < 0 || idx >= playlist.size()) {
            return;
        }
        indiceActual = idx;
        Cancion c = playlist.get(idx);

        if (!new File(c.getRutaAudio()).exists()) {
            JOptionPane.showMessageDialog(this,
                    "The file for \"" + c.getNombre() + "\" could not be found.\n"
                    + "It may have been moved or deleted:\n" + c.getRutaAudio(),
                    "File not found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        reproductor.cargar(c);

        if (c.getDuracion() == 0) {
            int durReal = reproductor.getDuracionRealSegundos();
            if (durReal > 0) {
                c.setDuracion(durReal);
                try {
                    List<Cancion> todas = playlistManager.leerTodas();
                    if (idx < todas.size()) {
                        todas.get(idx).setDuracion(durReal);
                        playlistManager.reescribirTodas(todas);
                    }
                } catch (IOException ignored) {
                }
                refrescarLista();
            }
        }

        reproductor.setLineListener(ev -> {
            if (ev.getType() == LineEvent.Type.STOP && reproductor.getProgreso() >= 0.99) {
                SwingUtilities.invokeLater(() -> {
                    int next = indiceActual + 1;
                    if (next < playlist.size()) {
                        cargarYMostrar(next);
                        reproductor.play();
                        btnPlayPause.setText("⏸");
                        btnPlayPause.setToolTipText("Pause");
                    } else {
                        barraProgreso.setValue(0);
                        btnPlayPause.setText("▶");
                        btnPlayPause.setToolTipText("Play");
                        lblDuracion.setText("0:00 / " + formatSeg(c.getDuracion()));
                    }
                });
            }
        });

        mostrarInfoCancion(c);
        cargarImagen(c.getRutaImagen());
        listaUI.setSelectedIndex(idx);
        listaUI.repaint(); 
    }

    private void mostrarInfoCancion(Cancion c) {
        if (c == null) {
            lblNombre.setText("No song selected");
            lblArtista.setText("—");
            lblDuracion.setText("0:00 / 0:00");
            lblGenero.setText("");
        } else {
            lblNombre.setText(c.getNombre());
            lblArtista.setText(c.getArtista());
            lblDuracion.setText("0:00 / " + formatSeg(c.getDuracion()));
            lblGenero.setText(c.getGenero().toString());
        }
    }

    private void cargarImagen(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            mostrarImagenDefault();
            return;
        }
        try {
            BufferedImage src = ImageIO.read(new File(ruta));
            if (src == null) {
                mostrarImagenDefault();
                return;
            }
            lblImagen.setIcon(new ImageIcon(letterbox(src, IMG_W, IMG_H)));
            lblImagen.setText("");
        } catch (IOException e) {
            mostrarImagenDefault();
        }
    }

    private Image letterbox(BufferedImage src, int maxW, int maxH) {
        double scale = Math.min((double) maxW / src.getWidth(),
                (double) maxH / src.getHeight());
        int w = (int) (src.getWidth() * scale);
        int h = (int) (src.getHeight() * scale);
        return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    private void mostrarImagenDefault() {
        BufferedImage img = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(35, 35, 48));
        g2.fillRect(0, 0, IMG_W, IMG_H);
        g2.setColor(new Color(70, 70, 95));
        g2.setFont(new Font("Arial", Font.PLAIN, 90));
        FontMetrics fm = g2.getFontMetrics();
        String nota = "♪";
        g2.drawString(nota, (IMG_W - fm.stringWidth(nota)) / 2,
                (IMG_H + fm.getAscent()) / 2 - 15);
        g2.dispose();
        lblImagen.setIcon(new ImageIcon(img));
        lblImagen.setText("");
    }

    private void refrescarLista() {
        int sel = listaUI.getSelectedIndex();
        modeloLista.clear();
        for (Cancion c : playlist) {
            modeloLista.addElement(c.getNombre());
        }
        if (sel >= 0 && sel < modeloLista.size()) {
            listaUI.setSelectedIndex(sel);
        }
        listaUI.repaint();
    }

    private int detectarDuracionPorClip(String ruta) {
        try {
            File archivo = new File(ruta);
            AudioInputStream ais = AudioSystem.getAudioInputStream(archivo);
            AudioFormat fmt = ais.getFormat();
            if (fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat pcm = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        fmt.getSampleRate(), 16,
                        fmt.getChannels(), fmt.getChannels() * 2,
                        fmt.getSampleRate(), false);
                ais = AudioSystem.getAudioInputStream(pcm, ais);
            }
            DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
            Clip tempClip = (Clip) AudioSystem.getLine(info);
            tempClip.open(ais);
            int segundos = (int) (tempClip.getMicrosecondLength() / 1_000_000L);
            tempClip.close();
            return segundos;
        } catch (Exception e) {
            return 0;
        }
    }

    private String truncar(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }

    private String formatSeg(int seg) {
        return String.format("%d:%02d", seg / 60, seg % 60);
    }

   
    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JButton ctrlBtn(String icon, String tip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tip);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(BG_CARD);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 72), 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 68));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_CARD);
            }
        });
        return btn;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    
    private class PlaylistCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JPanel cell = new JPanel(new BorderLayout(8, 0));
            cell.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

            Cancion c = (index < playlist.size()) ? playlist.get(index) : null;

            boolean esActual = (index == indiceActual);
            JLabel num = new JLabel(esActual ? "▶" : String.valueOf(index + 1));
            num.setFont(new Font("Arial", Font.PLAIN, esActual ? 12 : 11));
            num.setForeground(esActual ? ACCENT : TEXT_DIM);
            num.setPreferredSize(new Dimension(22, 0));

            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setOpaque(false);

            String nombre = truncar(c != null ? c.getNombre() : value.toString(), 28);
            String artista = truncar(c != null ? c.getArtista() : "", 28);

            JLabel lNombre = new JLabel(nombre);
            lNombre.setFont(new Font("Arial", Font.BOLD, 12));
            lNombre.setForeground(esActual ? ACCENT : TEXT_MAIN);

            JLabel lArtista = new JLabel(artista);
            lArtista.setFont(new Font("Arial", Font.PLAIN, 11));
            lArtista.setForeground(TEXT_SUB);

            info.add(lNombre);
            info.add(lArtista);

            JLabel lDur = new JLabel(c != null ? formatSeg(c.getDuracion()) : "");
            lDur.setFont(new Font("Arial", Font.PLAIN, 11));
            lDur.setForeground(TEXT_DIM);

            cell.setBackground(isSelected ? SEL_BG : BG_LIST);
            cell.add(num, BorderLayout.WEST);
            cell.add(info, BorderLayout.CENTER);
            cell.add(lDur, BorderLayout.EAST);
            return cell;
        }
    }
}
