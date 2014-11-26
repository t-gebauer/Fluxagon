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
 * @author Timo Gebauer
 */
public class SoundPlayer {

	/** Buffers hold sound data. */
	private static IntBuffer buffer;
	/** Sources are points emitting sound. */
	private static IntBuffer source;
	/** Position of the source sound. */
	private static final FloatBuffer sourcePos =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Velocity of the source sound. */
	private static final FloatBuffer sourceVel =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Position of the listener. */
	private static final FloatBuffer listenerPos =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Velocity of the listener. */
	private static final FloatBuffer listenerVel =
			(FloatBuffer) BufferUtils.createFloatBuffer(3).
			put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
	/** Orientation of the listener. (first 3 elements are "at", second 3 are
	 * "up") */
	private static final FloatBuffer listenerOri =
			(FloatBuffer) BufferUtils.createFloatBuffer(6).
			put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).rewind();
	private static boolean muted = false;

	public static boolean isMuted() {
		return muted;
	}

	public static void toggleMute() {
		muted = !muted;
	}

	public static void init(String[] files) {
		// Initialize OpenAL and clear the error bit.
		if (!AL.isCreated()) {
			try {
				AL.create();
			} catch (LWJGLException e) {
				System.err.println("ERROR: Failed to initialise Sound Player");
				System.err.println(e);
				return;
			}
		}
		AL10.alGetError();

		// Generate Source Buffer
		source = BufferUtils.createIntBuffer(files.length);
		AL10.alGenSources(source);

		// Load wav data into a buffer.
		buffer = BufferUtils.createIntBuffer(files.length);
		AL10.alGenBuffers(buffer);

		if (AL10.alGetError() != AL10.AL_NO_ERROR) {
			//return AL10.AL_FALSE;
		}

		for (int i = 0; i < files.length; i++) {
			WaveData waveFile = WaveData.create("sounds/" + files[i]);
			if (waveFile == null) {
				System.err.println("WaveFile not found: " + files[i]);
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
