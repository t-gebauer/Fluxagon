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
import org.lwjgl.opengl.PixelFormat;
import org.newdawn.slick.Color;

/**
 *
 * @author Timo
 */

public class Fluxagon implements Constants {

	/** frames rendered during the last second */
	private int fps;
	/** Anzahl der Frames dieser Sekunde */
	private int frameCount;
	private long lastFpsTime;
	private long lastUpdateTime;
	/** Boolean flag on whether are capped to 60 per second or not */
	private boolean capFPS = true;
	private double score = 0;
	private double oldScore = -1;
	private boolean running = true;
	/** Zeitpunkt des Spielstarts in ms */
	private long startTime;
	/** Zeitpunkt des Spielendes in ms */
	private long stopTime;
	private HexMap map;
	/** Boolean flag on whether the game is paused or not */
	private boolean paused = false;
	/** Boolean flag on whether the game is over */
	private boolean isOver;
	/** Boolean flag on whether circles or hexagons are drawn */
	private boolean circleMode = false;
	/** aktuelles Level */
	private int level = 1;
	
	public int getLevel() {
		return level;
	}

	public HexMap getMap() {
		return map;
	}

	public void incScore(double scr) {
		score += scr;
		if (score >= level * LEVEL_POINTS) {
			level++;
			SoundPlayer.playSound(SoundPlayer.LEVEL_UP);
		}
	}

	/**
	 *
	 * @return if startup time is not over
	 */
	public boolean isWaitingToStart() {
		return getTime() < startTime;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// Library Path
		System.setProperty(
				"org.lwjgl.librarypath", new File("native/windows").getAbsolutePath());

		Fluxagon flux = new Fluxagon();

		flux.init();
		flux.run();

		flux.destroy();
	}

	private void init() {
		try {
			initGL();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		initResources();
		initGame();
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
		Display.create(new PixelFormat());

		// Keyboard
		Keyboard.create();

		// Mouse
		Mouse.create();

		// OpenGl
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		// Tiefenpuffer
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glClearDepth(1);
		glDepthMask(true);

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

	/**
	 * Initialise (/reset) the game variables
	 */
	private void initGame() {
		map = new HexMap(this);
		map.init();
		paused = false;
		isOver = false;
		score = 0;
		level = 1;
		startTime = getTime() + STARTUP_TIME;
	}

	/**
	 * Get the value of circleMode
	 *
	 * @return the value of circleMode
	 */
	public boolean getCircleMode() {
		return circleMode;
	}

	/**
	 * Initialise resources
	 */
	private void initResources() {
		Display.setTitle("loading Resources");

		// init renderer-helper-class
		Renderer.init();

		// init openAL
		SoundPlayer.init();

		Display.setTitle(WINDOW_TITLE);
	}

	/**
	 * Game loop
	 */
	private void run() {
		lastFpsTime = getTime();
		lastUpdateTime = getTime();
		while (running && !Display.isCloseRequested()) {
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

	/**
	 * Deal with keyboard input
	 */
	private void processKeyboard() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_PAUSE) {
					if (!isOver) {
						paused = paused ? false : true;
					}
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					running = false;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					capFPS = capFPS ? false : true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_R) {
					initGame();
				} else if (Keyboard.getEventKey() == Keyboard.KEY_C) {
					circleMode = circleMode ? false : true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_M) {
					SoundPlayer.toggleMute();
				}
			} else {
				// Key Release
			}
		}
	}

	/**
	 * Deal with mouse input
	 */
	private void processMouse() {
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				if (Mouse.getEventButton() == 0) {
					if (!paused && !isOver) {
						Hexagon hex = map.getHexAt(Mouse.getEventX(), WINDOW_HEIGHT - Mouse.getEventY());
						if (hex != null) {
							hex.rotateCW();
							SoundPlayer.playSound(SoundPlayer.CLICK);
						}
					}
				} else if (Mouse.getEventButton() == 1) {
					if (!paused && !isOver) {
						Hexagon hex = map.getHexAt(Mouse.getEventX(), WINDOW_HEIGHT - Mouse.getEventY());
						if (hex != null) {
							hex.rotateCCW();
							SoundPlayer.playSound(SoundPlayer.CLICK);
						}
					}
				}
			}
		}
	}

	/**
	 * Updates game-logic (25 times per second)
	 */
	private void update() {
		// Updates auf 25x pro Sekunde begrenzen
		if (getTime() - lastUpdateTime < UPDATE_TIME) {
			return;
		}
		lastUpdateTime += UPDATE_TIME;

		TextPopup.moveAll();

		if (!paused) {
			if (isWaitingToStart()) {
				// jede Sekunde Sound abspielen
				if ((startTime - getTime()) / 1000 != (startTime - getTime() - UPDATE_TIME) / 1000) {
					SoundPlayer.playSound(SoundPlayer.COUNTDOWN);
				}
				updateTitle();
			} else {
				oldScore = score;

				map.scroll();
				map.update();

				// Spiel verloren?
				if (score == oldScore) {
					if (!isOver) {
						SoundPlayer.playSound(SoundPlayer.GAME_OVER);
						stopTime = getTime();
						isOver = true;
					}

				}
			}
		}
		map.animate();
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_TEXTURE_2D);
		map.render();

		// render text
		// fps
		Renderer.drawText(WINDOW_WIDTH - 120, 0, "FPS: " + fps, Color.white, true, false);

		// startup time
		if (startTime - getTime() > 0) {
			Renderer.drawText(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 4,
					"Starting in: " + ((int) Math.ceil((float) (startTime - getTime()) / 1000)), Color.white,
					true, true);
		} else {
			// game time
			if (!isOver) {
				Renderer.drawText(WINDOW_WIDTH - 120, 30, "Zeit: " + (getTime() - startTime) / 1000, Color.white, true, false);
			} else {
				Renderer.drawText(WINDOW_WIDTH - 120, 30, "Zeit: " + (stopTime - startTime) / 1000, Color.white, true, false);
			}
		}
		// level
		Renderer.drawText(0, 0, "Level: " + level, Color.white, true, false);
		// score
		Renderer.drawText(0, 30, "Score: " + (int) score, Color.white, true, false);

		// game over and pause messages
		if (isOver) {
			Renderer.drawText(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2,
					"Game over :/", Color.white, true, true);
		} else if (paused) {
			Renderer.drawText(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2,
					"Paused", Color.white, true, true);
		}
		// score popups
		if (!isWaitingToStart()) {
			TextPopup.renderAll(this);
		}
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

	private void destroy() {
		Keyboard.destroy();
		Mouse.destroy();
		Display.destroy();
		SoundPlayer.destroy();
	}
}
