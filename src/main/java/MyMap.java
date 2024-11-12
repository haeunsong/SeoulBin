import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class MyMap extends JFrame {
    public MyMap() {
        Engine engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
                .build());
        Browser browser = engine.newBrowser();
        BrowserView view = BrowserView.newInstance(browser);

        setTitle("MyMap");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                engine.close();
            }
        });

        Container c = getContentPane();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.add(view, BorderLayout.CENTER);

        setSize(800, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        browser.navigation().loadUrl("localhost:8080/map");
    }
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                TestServer.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();

        SwingUtilities.invokeLater(() -> {
            new MyMap();
        });

    }
}
