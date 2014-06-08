/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 * Zur Wiedergabe von Sounds
 *
 * @author Timo
 */
public class SoundPlayer {

	public static final int CLICK = 0;
	public static final int COUNTDOWN = 1;
	public static final int GAME_OVER = 2;
	public static final int LEVEL_UP = 3;
	private static final String SOUND_FILE_NAMES[] = {"rotate.wav", "countdown.wav", "game over0.wav", "level up2.wav"};
	/** Buffers hold sound data. */
	private static IntBuffer buffer = BufferUtils.createIntBuffer(SOUND_FILE_NAMES.length);
	/** Sources are points emitting sound. */
	private static IntBuffer source = BufferUtils.createIntBuffer(SOUND_FILE_NAMES.length);
	/** Position of the source sound. */
	private static FloatBuffer sourcePos =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Velocity of the source sound. */
	private static FloatBuffer sourceVel =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Position of the listener. */
	private static FloatBuffer listenerPos =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Velocity of the listener. */
	private static FloatBuffer listenerVel =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Orientation of the listener. (first 3 elements are "at", second 3 are
	 * "up") */
	private static FloatBuffer listenerOri =
			(FloatBuffer) BufferUtils.createFloatBuffer(6).
			put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).rewind();
	
	private static boolean muted = false;
	
	public static void toggleMute() {
		muted = muted ? false : true;
	}

	public static void init() {
		// Initialize OpenAL and clear the error bit.
		try {
			AL.create();
		} catch (LWJGLException le) {
			le.printStackTrace();
			return;
		}
		AL10.alGetError();

		// Generate Source Buffer
		AL10.alGenSources(source);

		// Load wav data into a buffer.
		AL10.alGenBuffers(buffer);

		if (AL10.alGetError() != AL10.AL_NO_ERROR) {
			//return AL10.AL_FALSE;
		}

		for (int i = 0; i < SOUND_FILE_NAMES.length; i++) {
			WaveData waveFile = WaveData.create("sounds/" + SOUND_FILE_NAMES[i]);
			if (waveFile == null) {
				System.err.println("WaveFile not found: " + SOUND_FILE_NAMES[i]);
				continue;
			}
			AL10.alBufferData(buffer.get(i), waveFile.format, waveFile.data, waveFile.samplerate);
			waveFile.dispose();

			// Bind the buffer with the source.
			if (AL10.alGetError() != AL10.AL_NO_ERROR) {
				continue;//AL10.AL_FALSE;
			}

			AL10.alSourcei(source.get(i), AL10.AL_BUFFER, buffer.get(i));
			AL10.alSourcef(source.get(i), AL10.AL_PITCH, 1.0f);
			AL10.alSourcef(source.get(i), AL10.AL_GAIN, 1.0f);
			AL10.alSource(source.get(i), AL10.AL_POSITION, sourcePos);
			AL10.alSource(source.get(i), AL10.AL_VELOCITY, sourceVel);
		}

		// Set listener values
		AL10.alListener(AL10.AL_POSITION, listenerPos);
		AL10.alListener(AL10.AL_VELOCITY, listenerVel);
		AL10.alListener(AL10.AL_ORIENTATION, listenerOri);

		/*// Do another error check and return.
		 if (AL10.alGetError() == AL10.AL_NO_ERROR) {
		 return;//AL10.AL_TRUE;
		 }

		 //return AL10.AL_FALSE;*/
	}

	public static void playSound(int sound) {
		if (muted) {
			return;
		}
		AL10.alSourcePlay(source.get(sound));
	}

	public static void destroy() {
		AL10.alDeleteSources(source);
		AL10.alDeleteBuffers(buffer);
		AL.destroy();
	}
}
