/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.io.File;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Timo
 */
interface Constants {

	public static final int WINDOW_WIDTH = 600;
	public static final int WINDOW_HEIGHT = 600;
	public static final String WINDOW_TITLE = "Fluxagon";
	public static final int COLUMN_COUNT = 10;
	public static final int ROW_COUNT =
			Math.round(COLUMN_COUNT * 1.0f / WINDOW_WIDTH * WINDOW_HEIGHT) + 2;
	public static final float HEX_OFFSET_PERCENT = 0.75f;
	public static final float HEX_WIDTH =
			WINDOW_WIDTH / (COLUMN_COUNT * 2 + HEX_OFFSET_PERCENT * 4);
	public static final float HEX_HEIGHT =
			HEX_WIDTH * 2 / (float) Math.sqrt(3);
	public static final float HEX_OFFSET_X =
			HEX_OFFSET_PERCENT * HEX_WIDTH;
	public static final float HEX_OFFSET_Y =
			HEX_OFFSET_PERCENT * HEX_HEIGHT;
	public static final float LANE_PROBABILITY = 0.4f;
	public static final float HEX_ROTATION_DIST = 15;
	public static final float FLUX_SPEED = 0.02f;
	public static final double[] COLOR_HEXAGON = {1, 0.6, 0.1};
	public static final double[] COLOR_LINE_BG = {0.4, 0.4, 0.4};
	public static final double[] COLOR_LINE_FG = {0, 0.8, 0.1};
	public static final long UPDATE_TIME = 40; // Alle 40ms ein Update
}

public class Fluxagon implements Constants {

	private int fps;
	private long lastFpsTime;
	private long lastUpdateTime;
	private boolean capFPS = true;
	private boolean drawLines = true;
	private Hexagon[][] hexagons;
	private float animationOffset = 0;
	private boolean indent_odd = false;
	private int score = 0;
	private int oldScore = -1;
	private boolean running = true;
	private long startingTime;

	public float getAnimation_offset() {
		return animationOffset;
	}

	public boolean isIndent_odd() {
		return indent_odd;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// Library Path
		System.setProperty(
				"org.lwjgl.librarypath", new File("native/windows").getAbsolutePath());

		Fluxagon flux = new Fluxagon();
		try {
			flux.initGL();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		flux.init();

		flux.run();

		flux.destroy();
	}

	private void initGL() throws LWJGLException {
		// Display
		Display.setDisplayMode(new DisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT));
		Display.setFullscreen(false);
		Display.setTitle(WINDOW_TITLE);
		Display.create();

		// Keyboard
		Keyboard.create();

		// Mouse
		Mouse.create();

		// OpenGl
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glLineWidth(3.0f);

		// Antialiasing
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
		// Blending for Antialiasing in RGBA mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glClearColor(0, 0, 0, 1);
	}

	private void destroy() {
		Keyboard.destroy();
		Mouse.destroy();
		Display.destroy();
	}

	private void init() {
		hexagons = new Hexagon[ROW_COUNT][COLUMN_COUNT];
		for (int i = 1; i < ROW_COUNT; i++) {
			for (int j = 0; j < COLUMN_COUNT; j++) {
				hexagons[i][j] = new Hexagon(this, i, j);
			}
		}
		hexagons[4][4].connect();
		indent_odd = false;
		animationOffset = 0;
		score = 0;
		startingTime = 4000L;
	}

	public void incScore(int scr) {
		score += scr;
	}

	private Hexagon getHexAt(float x, float y) {
		int row = Math.round(((y - HEX_OFFSET_Y + animationOffset) / HEX_HEIGHT + 1) / 1.5f);
		// Verschiebung beachten
		if (indent_odd ^ row % 2 == 0) {
			x -= HEX_WIDTH;
		}
		int column = Math.round(((x - HEX_OFFSET_X) / HEX_WIDTH - 1) / 2);

		try {
			return hexagons[row][column];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return null;
	}

	private void run() {
		lastFpsTime = getTime();
		lastUpdateTime = getTime();
		while (running && !Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			if (Display.isVisible()) {
				processKeyboard();
				processMouse();
				update();
				render();
				updateFps();
			} else {
				if (Display.isDirty()) {
					render();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			Display.update();
			if (capFPS) {
				Display.sync(60);
			}
		}
	}

	private void processKeyboard() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_L) {
					drawLines = drawLines ? false : true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					capFPS = capFPS ? false : true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_R) {
					init();
				} else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					for (int i = 0; i < hexagons.length; i++) {
						for (int j = 0; j < hexagons[i].length; j++) {
							hexagons[i][j].update();
						}
					}
				}
			} else {
				// Key Release
			}
		}
	}

	private void processMouse() {
		//System.out.println(Mouse.getX() + ", " + Mouse.getY());
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				if (Mouse.getEventButton() == 0) {
					Hexagon hex = getHexAt(Mouse.getEventX(), WINDOW_HEIGHT - Mouse.getEventY());
					if (hex != null) {
						hex.rotateCW();
					}
				} else if (Mouse.getEventButton() == 1) {
					Hexagon hex = getHexAt(Mouse.getEventX(), WINDOW_HEIGHT - Mouse.getEventY());
					if (hex != null) {
						hex.rotateCCW();
					}
				}
			}
		}
	}

	private void update() {
		// Updates auf 25x pro Sekunde begrenzen
		if (getTime() - lastUpdateTime < UPDATE_TIME) {
			return;
		}
		lastUpdateTime += UPDATE_TIME;
		
		oldScore = score;
		animationOffset += 0.1; // Abhängig von der Zeit machen TODO!
		if (animationOffset >= 1.5 * HEX_HEIGHT) {
			animationOffset = 0;
			indent_odd = !indent_odd;
			for (int i = 0; i < ROW_COUNT - 1; i++) {
				hexagons[i] = hexagons[i + 1];
				for (int k = 0; k < COLUMN_COUNT; k++) {
					hexagons[i][k].setRow(i);
				}
			}
			hexagons[ROW_COUNT - 1] = new Hexagon[COLUMN_COUNT];
			for (int j = 0; j < COLUMN_COUNT; j++) {
				hexagons[ROW_COUNT - 1][j] = new Hexagon(this, ROW_COUNT - 1, j);
			}
		}

		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].update();
				}
			}

		}
		// Spiel verloren?
		// tritt leider manchmal einfach so ein
		/*if (score == oldScore) {
		 System.out.println("You lost. Score: " + score / 100);
		 running = false;
		 }*/
	}

	public void appendColor(double[] color) {
		appendColor(color, 1);
	}

	public void appendColor(double[] color, double brightness) {
		glColor3d(brightness * color[0], brightness * color[1], brightness * color[2]);
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].draw();
				}
			}
		}
	}

	public Hexagon[][] getHexagons() {
		return hexagons;
	}

	/**
	 * Get the accurate system time
	 *
	 * @return The system time in milliseconds
	 */
	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFps() {
		if (getTime() - lastFpsTime > 1000) {
			updateTitle();
			fps = 0;
			lastFpsTime += 1000;
		}
		fps++;
	}

	public void updateTitle() {
		Display.setTitle(WINDOW_TITLE + " - " + fps + "fps" + " score: " + score / 100);
	}
}
