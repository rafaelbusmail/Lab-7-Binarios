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
    private JButton btnPlay, btnPause, btnStop;
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
        barraProgreso.setMaximumSize(new Dimension(IMG_W, 4));
        barraProgreso.setPreferredSize(new Dimension(IMG_W, 4));
        barraProgreso.setAlignmentX(Component.CENTER_ALIGNMENT);
        barraProgreso.setForeground(ACCENT);
        barraProgreso.setBackground(new Color(50, 50, 68));
        barraProgreso.setBorderPainted(false);

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

    private JPanel buildControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        p.setBackground(BG_DARK);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(340, 50));

        btnPlay = ctrlBtn("▶", "Play");
        btnPause = ctrlBtn("⏸", "Pause");
        btnStop = ctrlBtn("⏹", "Stop");

        btnPlay.addActionListener(e -> accionPlay());
        btnPause.addActionListener(e -> accionPause());
        btnStop.addActionListener(e -> accionStop());

        p.add(btnPlay);
        p.add(btnPause);
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
        title.setBorder(BorderFactory.createMatteBorder(0, 0, 1,
                0, new Color(50, 50, 68)));

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

    private void accionPlay() {
        if (!reproductor.isPlaying() && !reproductor.isPaused()) {
            int idx = listaUI.getSelectedIndex();
            if (idx < 0 && !playlist.isEmpty()) {
                idx = 0;
            }
            if (idx < 0) {
                return;
            }
            cargarYMostrar(idx);
        }
        reproductor.play();
    }

    private void accionPause() {
        if (reproductor.isPlaying()) {
            reproductor.pause();
        } else if (reproductor.isPaused()) {
            reproductor.play();
        }
    }

    private void accionStop() {
        reproductor.stop();
        barraProgreso.setValue(0);
        lblDuracion.setText(totalFormateado());
    }

    private void accionSelect() {
        int idx = listaUI.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        reproductor.stop();
        cargarYMostrar(idx);
        reproductor.play();
    }

    private void accionAdd() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select song");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Audio files (*.wav, *.mp3)", "wav", "mp3"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File audioFile = chooser.getSelectedFile();
        String rutaAudio = audioFile.getAbsolutePath();

        int duracionDetectada = detectarDuracion(rutaAudio);

        JFileChooser imgChooser = new JFileChooser();
        imgChooser.setDialogTitle("Select album art (optional — you can cancel)");
        imgChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (*.jpg, *.png)", "jpg", "jpeg", "png"));
        String rutaImagen = "";
        if (imgChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            rutaImagen = imgChooser.getSelectedFile().getAbsolutePath();
        }

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

        String durFmt = formatSeg(duracionDetectada);
        form.add(new JLabel("Duration:"));
        form.add(new JLabel(durFmt + "  (auto-detected)"));

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
    }

    private void accionRemove() {
        int idx = listaUI.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a song to remove.");
            return;
        }
        if (idx == indiceActual) {
            reproductor.stop();
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

    private void tickUI() {
        if (!reproductor.isPlaying()) {
            return;
        }
        Cancion c = reproductor.getCancionActual();
        if (c == null) {
            return;
        }

        int total = c.getDuracion();
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
        reproductor.cargar(c);
        reproductor.setLineListener(ev -> {
            if (ev.getType() == LineEvent.Type.STOP && reproductor.getProgreso() >= 0.99) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue(0);
                    lblDuracion.setText("0:00 / " + formatSeg(c.getDuracion()));
                });
            }
        });
        mostrarInfoCancion(c);
        cargarImagen(c.getRutaImagen());
        listaUI.setSelectedIndex(idx);
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

    
}
