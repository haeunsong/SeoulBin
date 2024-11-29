package seoulbin.browser;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.view.swing.BrowserView;

public class BrowserManager {
    private final Engine engine;
    private final Browser browser;

    public BrowserManager() {
        // JxBrowser 엔진 초기화
        engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
                .build());
        browser = engine.newBrowser();
        browser.devTools().show();
    }

    public BrowserView getBrowserView() {
        return BrowserView.newInstance(browser);
    }

    public Browser getBrowser() {
        return browser;
    }

    public void executeJavaScript(String script) {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(script));
    }

    public void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }
}
