package seoulbin;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class Main extends JFrame {
    public Main() {
        setSize(1000, 800);
        setLayout(new BorderLayout());

        MapPanel mapPanel = new MapPanel();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mapPanel.engineClose();
            }
        });

//        add(mapPanel);
        add(mapPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);
    }

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                TestServer.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();

        new Main();
    }
}
