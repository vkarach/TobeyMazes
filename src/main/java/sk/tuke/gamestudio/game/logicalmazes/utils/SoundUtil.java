package sk.tuke.gamestudio.game.logicalmazes.utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class SoundUtil {
    private static final int POOL_SIZE = 4;
    private final Clip[] clips;
    private int poolIndex = 0;

    private static float volumeCoef = 1f;
    private float localVolume = 1f;
    private static final java.util.List<SoundUtil> ALL_SOUNDS = new java.util.ArrayList<>();

    public SoundUtil(String path, float volumeCoef) {
        this(path);
        setVolumeCoef(volumeCoef);
    }

    public SoundUtil(String path) {
        clips = new Clip[POOL_SIZE];
        try {
            InputStream raw = SoundUtil.class.getClassLoader().getResourceAsStream(path);
            if (raw == null) {
                throw new RuntimeException("File not found: " + path);
            }

            // read audio data into memory so it can be reused for each clip in the pool
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            raw.transferTo(buffer);
            byte[] audioData = buffer.toByteArray();

            for (int i = 0; i < POOL_SIZE; i++) {
                AudioInputStream in = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(new ByteArrayInputStream(audioData))
                );
                clips[i] = AudioSystem.getClip();
                clips[i].open(in);
            }

            ALL_SOUNDS.add(this);
        }
        catch (Exception e) {
            for (int i = 0; i < POOL_SIZE; i++) clips[i] = null;
        }
    }

    private Clip currentClip() {
        return clips[0];
    }

    private void setVolume(Clip clip, float volume) {
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
        if (coef < 0f) coef = 0f;
        if (coef > 1f) coef = 1f;
        volumeCoef = coef;
        updateAllVolumes();
    }

    public void updateVolume() {
        for (Clip clip : clips) {
            if (clip == null) continue;
            setVolume(clip, localVolume * volumeCoef);

            if (clip.isActive()) {
                int pos = clip.getFramePosition();
                clip.stop();
                clip.setFramePosition(pos);
                clip.start();
            }
        }
    }

    public static void updateAllVolumes() {
        for (SoundUtil sound : ALL_SOUNDS) {
            sound.updateVolume();
        }
    }

    public void play(float volume) {
        if (clips[0] == null) return;
        localVolume = volume;

        Clip clip = clips[poolIndex];
        poolIndex = (poolIndex + 1) % POOL_SIZE;

        setVolume(clip, localVolume * volumeCoef);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void play() {
        play(1f);
    }

    public void loop() {
        if (clips[0] == null) return;
        setVolume(currentClip(), localVolume * volumeCoef);
        currentClip().loop(Clip.LOOP_CONTINUOUSLY);
        currentClip().start();
    }

    public void loop(float volume) {
        if (clips[0] == null) return;
        localVolume = volume;
        setVolume(currentClip(), localVolume * volumeCoef);
        currentClip().loop(Clip.LOOP_CONTINUOUSLY);
        currentClip().start();
    }

    public void stop() {
        if (clips[0] == null) return;
        for (Clip clip : clips) {
            if (clip != null) clip.stop();
        }
    }
}