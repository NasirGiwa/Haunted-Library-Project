package scene_creation;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundManager {
    private Clip backgroundClip; // To keep track of the background sound for stopping if needed

    // Method to play a sound file, with an option to loop continuously
    public void playSound(String soundFileName, boolean loop) {
        try {
            // Load the sound file from the resources
            URL soundURL = SoundManager.class.getResource("Sound/"+soundFileName);
            if (soundURL == null) {
                System.err.println(soundFileName + " not found!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            if (loop) {
                backgroundClip = clip; // Store the background clip if looping
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (Exception ex) {
            System.err.println("Error playing sound '" + soundFileName + "': " + ex.getMessage());
        }
    }

    // Optional: Method to stop the background sound if needed
    public void stopBackgroundSound() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }
}