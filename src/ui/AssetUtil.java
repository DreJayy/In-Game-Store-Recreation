import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;

final class AssetUtil {
    private AssetUtil() { }

    static ImageIcon icon(String path, int width, int height) {
        File file = new File(path);
        if (!file.isFile()) return null;
        Image image = new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    static Image image(String path) {
        File file = new File(path);
        return file.isFile() ? new ImageIcon(path).getImage() : null;
    }
}
