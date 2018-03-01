package jxtras.primitive.application.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class MainFrame extends JFrame {
    private JPanel transPanel = new JPanel();
    private JPanel footer;

    public MainFrame() {
        super("Geometerizer");

        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(transPanel, BorderLayout.CENTER);
        contentPanel.add((footer = new Footer()), BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new FrameBorder());
        setContentPane(panel);

        TitlePanel titlePanel = new TitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        titlePanel.installListeners();

        add(contentPanel, BorderLayout.CENTER);

        setSize(780, 614);
    }

    public JPanel getContentPanel() {
        return transPanel;
    }

    public JPanel getFooter() {
        return footer;
    }

//    void showIntroduction() {
//        setGlassPane(new IntroductionPanel(DataType.PHOTOS, panel));
//        getGlassPane().setVisible(true);
//        this.panel.setContentVisible(false);
//    }
//
//    void showTransitionPanel() {
//        this.panel.setContentVisible(true);
//    }
//
//    void showWaitOverlay() {
//        setGlassPane(new WaitOverlay(panel));
//        getGlassPane().setSize(getSize());
//        getGlassPane().validate();
//    }
//
//    void hideWaitOverlay() {
//        getGlassPane().setVisible(false);
//    }
//
//    void killOverlay() {
//        setGlassPane(originalOverlay);
//    }
//
//    void showLoginOverlay() {
//        showLoginOverlay(false);
//    }
//
//    void showLoginOverlay(boolean visible) {
//        loginOverlay = new LoginOverlay(panel, visible);
//        setGlassPane(loginOverlay);
//        getGlassPane().setSize(getSize());
//        getGlassPane().validate();
//        if (visible) {
//            getGlassPane().setVisible(true);
//        }
//    }
//
//    String getUserName() {
//        return loginOverlay.getUserName();
//    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
