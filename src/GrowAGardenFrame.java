import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GrowAGardenFrame extends JFrame {
    private final BackgroundMusic music = new BackgroundMusic();
    private final StorePanel storePanel = new StorePanel();

    public GrowAGardenFrame() {
        super("Grow a Garden — Shop & Garden");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 680));
        setSize(1050, 780);
        setLocationRelativeTo(null);
        setContentPane(storePanel);
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { music.start(); }
            @Override public void windowClosed(WindowEvent e) { music.stop(); storePanel.stopTimers(); }
        });
    }
}
