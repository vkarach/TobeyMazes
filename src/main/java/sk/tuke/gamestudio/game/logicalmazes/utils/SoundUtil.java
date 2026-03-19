package sk.tuke.gamestudio.game.logicalmazes.utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundUtil {
    private Clip clip;
    private static float volumeCoef = 1f;
    private float localVolume = 1f;
    private static final java.util.List<SoundUtil> ALLSOUNDS = new java.util.ArrayList<>();
    public SoundUtil(String path) {
        try {
            InputStream raw = SoundUtil.class.getClassLoader().getResourceAsStream(path);
            if (raw == null) {
                throw new RuntimeException("File not found: " + path);
            }

            BufferedInputStream buf = new BufferedInputStream(raw);
            AudioInputStream in = AudioSystem.getAudioInputStream(buf);

            clip = AudioSystem.getClip();
            clip.open(in);

            ALLSOUNDS.add(this);
        }
        catch (Exception e) {
            clip = null;
        }
    }
    private void setVolume(float volume) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            volume = Math.clamp(volume, 0f, 1f);

            float dB;
            if (volume == 0f) {
                dB = gain.getMinimum();
            }
            else {
                dB = (float) (20.0 * Math.log10(volume));
            }
            gain.setValue(dB);
        }
        catch (Exception ignored) {
        }
    }
    public static void setVolumeCoef(float coef) {
        if (coef < 0f) {
            coef = 0f;
        }
        if (coef > 1f) {
            coef = 1f;
        }
        volumeCoef = coef;

        updateAllVolumes();
    }
    public void updateVolume() {
        setVolume(localVolume * volumeCoef);

        if (clip.isActive()) {
            int pos = clip.getFramePosition();
            clip.stop();
            clip.setFramePosition(pos);
            clip.start();
        }

    }
    public static void updateAllVolumes() {
        for (SoundUtil sound : ALLSOUNDS) {
            sound.updateVolume();
        }
//        System.out.println("updated all volumes at " + System.nanoTime());
    }
    public void play(float volume) {
        if (clip == null) {
            return;
        }
        localVolume = volume;
        setVolume(localVolume * volumeCoef);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }
    public void play() {
        if (clip == null) {
            return;
        }
        play(1f);
    }
    public void loop() {
        if (clip == null) {
            return;
        }
        setVolume(localVolume  * volumeCoef);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }
    public void loop(float volume) {
        if (clip == null) {
            return;
        }
        localVolume = volume;
        setVolume(localVolume * volumeCoef);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }
    public void stop() {
        if (clip == null) {
            return;
        }
        clip.stop();
    }
}
