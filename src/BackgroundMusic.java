import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

/** Plays the bundled music track when supported by the host audio device. */
public final class BackgroundMusic {
    private Clip clip;

    public void start() {
        if (clip != null && clip.isRunning()) return;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(new File("music/background_music.wav"))) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(Math.max(volume.getMinimum(), Math.min(-12f, volume.getMaximum())));
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception ex) {
            System.err.println("Background music could not be started: " + ex.getMessage());
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
