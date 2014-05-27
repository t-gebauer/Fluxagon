/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.awt.Font;
import java.io.File;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

/**
 *
 * @author Timo
 */
interface Constants {

	/** Die Breite des Fensters */
	public static final int WINDOW_WIDTH = 600;
	/** Die Höhe des Fensters */
	public static final int WINDOW_HEIGHT = 600;
	/** Der Titel des Fensters */
	public static final String WINDOW_TITLE = "Fluxagon";
	/** Anzahl der horizontalen Spalten */
	public static final int COLUMN_COUNT = 10;
	/** Anzahl der Reihen (wird irgendwie aus der Anzahl der Spalten und dem
	 * Seitenverhältnis des Fensters berechnet) */
	public static final int ROW_COUNT =
			Math.round((COLUMN_COUNT + 4) * 1.0f / WINDOW_WIDTH * WINDOW_HEIGHT) + 2;
	public static final float HEX_OFFSET_PERCENT = 0.75f;
	public static final float HEX_WIDTH =
			WINDOW_WIDTH / (COLUMN_COUNT * 2 + HEX_OFFSET_PERCENT * 4);
	public static final float HEX_HEIGHT =
			HEX_WIDTH * 2 / (float) Math.sqrt(3);
	public static final float HEX_OFFSET_X =
			HEX_OFFSET_PERCENT * HEX_WIDTH;
	public static final float HEX_OFFSET_Y =
			HEX_OFFSET_PERCENT * HEX_HEIGHT;
	/** Wahrscheinlichkeit für das Entstehen einer Lane */
	public static final float LANE_PROBABILITY = 0.4f;
	/** Drehweite der Hexagone pro Animationsdurchlauf */
	public static final float HEX_ROTATION_DIST = 15;
	/** Flussgeschwindigkeit */
	public static final float FLUX_SPEED = 0.02f;
	/** Scrollgeschwindigkeit der Map */
	public static final double SCROLL_SPEED = 0.2;
	/** Farbdefinition für die Hexagone */
	public static final double[] COLOR_HEXAGON = {1, 0.6, 0.1};
	/** Farbdefinition für den Hintergrund der Lanes */
	public static final double[] COLOR_LINE_BG = {0.4, 0.4, 0.4};
	/** Farbdefinition für den Vordergrund der Lanes */
	public static final double[] COLOR_LINE_FG = {0, 0.8, 0.1};
	/** Zeit zwischen zwei Update-Durchgängen */
	public static final long UPDATE_TIME = 40;
	/** Zeit bis zum Start des Spiels (in ms) */
	public static final int STARTUP_TIME = 5000;
}

public class Fluxagon implements Constants {

	/** frames rendered during the last second */
	private int fps;
	/** Anzahl der Frames dieser Sekunde */
	private int frameCount;
	private long lastFpsTime;
	private long lastUpdateTime;
	/** Boolean flag on whether are capped to 60 per second or not */
	private boolean capFPS = true;
	/** Boolean flag on whether Lanes are drawn or not */
	private boolean drawLines = true;
	/** Boolean flag on whether AntiAliasing is enabled or not */
	private boolean antiAlias = true;
	private int score = 0;
	private int oldScore = -1;
	private boolean running = true;
	/** Anzahl der Sekunden bis zum Spielstart */
	private long startingTime;
	/** Schrift Objekt zum zeichnen von Text */
	private TrueTypeFont font;
	private HexMap map;

	public HexMap getMap() {
		return map;
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

		flux.initResources();
		flux.initGame();
		flux.run();

		flux.destroy();
	}

	/**
	 * Initialise OpenGL
	 *
	 * @throws LWJGLException
	 */
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

	/**
	 * Initialise (/reset) the game variables
	 */
	private void initGame() {
		map = new HexMap(this);
		score = 0;
		startingTime = STARTUP_TIME;
	}

	/**
	 * Initialise resources
	 */
	private void initResources() {
		Display.setTitle("loading Resources");
		Font awtFont = new Font("Verdana", Font.BOLD, 20);
		font = new TrueTypeFont(awtFont, antiAlias);
		Display.setTitle(WINDOW_TITLE);
	}

	public void incScore(int scr) {
		score += scr;
	}
	
	/**
	 * Game loop
	 */
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
				if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					capFPS = capFPS ? false : true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_R) {
					initGame();
				}
			} else {
				// Key Release
			}
		}
	}

	private void processMouse() {
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				if (Mouse.getEventButton() == 0) {
					Hexagon hex = map.getHexAt(Mouse.getEventX(), WINDOW_HEIGHT - Mouse.getEventY());
					if (hex != null) {
						hex.rotateCW();
					}
				} else if (Mouse.getEventButton() == 1) {
					Hexagon hex = map.getHexAt(Mouse.getEventX(), WINDOW_HEIGHT - Mouse.getEventY());
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

		if (startingTime > 0) {
			startingTime -= UPDATE_TIME;
			updateTitle();
		} else {
			oldScore = score;
			
			map.scroll();
			map.update();

			// Spiel verloren?
			// tritt leider manchmal einfach so ein
			if (score == oldScore) {
			 System.out.println("You lost. Score: " + score / 100);
			 running = false;
			 }

		}

		map.animate();
	}

	public void appendColor(double[] color) {
		appendColor(color, 1);
	}

	public void appendColor(double[] color, double brightness) {
		glColor3d(brightness * color[0], brightness * color[1], brightness * color[2]);
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_TEXTURE_2D);
		map.render();

		// render text
		drawText(0, 0, "FPS: " + fps, Color.white, true, false);
		drawText(0, 30, "Score: " + score, Color.white, true, false);
		if (startingTime > 0) {
			drawText(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 4,
					"Starting in: " + (Math.round((float) startingTime / 1000)), Color.white,
					true, true);
		}
	}

	public void drawText(int x, int y, String text, Color color,
			boolean background, boolean alignMid) {
		int width = font.getWidth(text);
		int height = font.getHeight(text);
		if (alignMid) {
			x -= width / 2;
			y -= height / 2;
		}
		glPushMatrix();
		glLoadIdentity();
		glTranslatef(x, y, 0);
		if (background) {
			glColor4d(0.4, 0.4, 0.4, 0.4);
			drawQuad(width, height);
		}
		// Text zeichnen
		glEnable(GL_TEXTURE_2D);
		font.drawString(4, 0, text, color);
		glPopMatrix();
	}

	public void drawQuad(int width, int height) {
		// Hintergrund zeichnen
		glDisable(GL_TEXTURE_2D);
		glBegin(GL_QUADS);
		glVertex2i(0, 0);
		glVertex2i(0, height + 2);
		glVertex2i(width + 6, height + 2);
		glVertex2i(width + 6, 0);
		glEnd();
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
	 * Calculate the FPS and update the title bar
	 */
	public void updateFps() {
		if (getTime() - lastFpsTime > 1000) {
			fps = frameCount;
			updateTitle();
			frameCount = 0;
			lastFpsTime += 1000;
		}
		frameCount++;
	}

	/**
	 * Update the title bar
	 */
	public void updateTitle() {
		/*if (startingTime > 0) {
		 Display.setTitle("Start in: " + ((int) Math.ceil((float) startingTime / 1000)));
		 } else {
		 Display.setTitle(WINDOW_TITLE + " - " + fps + " fps" + " score: " + score / 100);
		 }*/
	}

	/**
	 *
	 * @return if startup time is not over
	 */
	public boolean isWaitingToStart() {
		return startingTime > 0;
	}
}
