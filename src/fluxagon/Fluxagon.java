/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import basicmenu.MenuItem;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.PixelFormat;
import org.newdawn.slick.opengl.ImageIOImageData;

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
	private double highscore = 0;
	private boolean running = true;
	/** Zeit seit Spielstart in ms */
	private long gameTime;
	private HexMap map;
	/** Boolean flag on whether the game is paused or not */
	private boolean paused = false;
	/** Boolean flag on whether the game is over */
	private boolean isOver;
	/** Boolean flag on whether circles or hexagons are drawn */
	private boolean circleMode = false;
	/** aktuelles Level */
	private int level = 1;
	/** GUI */
	private MenuItem guiRoot;
	private MenuItem guiMain;
	private MenuItem guiOptions;
	/** Color theme fading */
	private long fadeStartTime;
	private int oldHexColorIndex;
	private int hexColorIndex;
	/** Display size */
	private int windowWidth = 600;
	private int windowHeight;
	private float hexRadius;

	public int getWindowWidth() {
		return windowWidth;
	}

	public float getHexHeight() {
		return hexRadius;
	}

	public float getHexWidth() {
		return (float) (hexRadius * Math.sqrt(3) / 2);
	}

	public void shuffleHexColor() {
		oldHexColorIndex = hexColorIndex;
		do {
			hexColorIndex = (int) (Math.random() * COLOR_HEXAGON.length);
		} while (hexColorIndex == oldHexColorIndex);
		fadeStartTime = getTime();
	}

	public double getFadePercent() {
		double prc = (double) (getTime() - fadeStartTime) / FADE_TIME;
		if (prc < 1) {
			return prc;
		} else {
			return 1;
		}
	}

	public int getOldHexColorIndex() {
		return oldHexColorIndex;
	}

	public int getHexColorIndex() {
		return hexColorIndex;
	}

	public int getLevel() {
		return level;
	}

	public int getTimeUntilStart() {
		if (gameTime > 0) {
			return 0;
		} else {
			return (int) -gameTime;
		}
	}

	public HexMap getMap() {
		return map;
	}

	public void incScore(double scr) {
		score += scr;
		// Level UP !
		if (score >= level * LEVEL_POINTS) {
			level++;
			SoundPlayer.playSound(SOUND_LEVEL_UP);
			shuffleHexColor();
		}
	}

	/**
	 *
	 * @return if startup time is not over
	 */
	public boolean isWaitingToStart() {
		return gameTime < 0;
	}

	private void calculateDisplay(int width) {
		windowHeight = Math.round(width / ASPECT_RATIO);
		hexRadius = windowHeight / (1.5f * (VISIBLE_ROWS - 1) + 2);
		windowWidth = width;
		saveSettings();
	}

	private void setDisplay(int width) {
		calculateDisplay(width);
		Display.destroy();
		try {
			initGL();
		} catch (LWJGLException e) {
			System.exit(1);
		}
		initResources();
		initGame();
		initMenu();
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
		loadSettings();
		System.out.println(windowWidth);
		calculateDisplay(windowWidth);
		try {
			initGL();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		initResources();
		initGame();
		initMenu();
	}

	/**
	 * Initialise OpenGL
	 *
	 * @throws LWJGLException
	 */
	private void initGL() throws LWJGLException {
		// Display
		Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
		Display.setFullscreen(false);
		Display.setTitle(WINDOW_TITLE);
		BufferedImage icon16 = loadImage("icons/16x16.png");
		BufferedImage icon32 = loadImage("icons/32x32.png");
		BufferedImage icon128 = loadImage("icons/128x128.png");
		if (icon16 != null && icon32 != null && icon128 != null) {
			Display.setIcon(new ByteBuffer[]{
				new ImageIOImageData().imageToByteBuffer(icon16, false, false, null),
				new ImageIOImageData().imageToByteBuffer(icon32, false, false, null),
				new ImageIOImageData().imageToByteBuffer(icon128, false, false, null)
			});
		}
		Display.create(new PixelFormat().withSamples(8));

		// Keyboard
		Keyboard.create();

		// Mouse
		Mouse.create();

		// OpenGl
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, windowWidth, windowHeight, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		// Tiefenpuffer
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glClearDepth(1);
		glDepthMask(true);

		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glLineWidth(hexRadius / 15);

		// Antialiasing
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
		// Blending for Antialiasing in RGBA mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glClearColor(0.17f, 0.17f, 0.17f, 1);
	}

	private BufferedImage loadImage(String filename) {
		try {
			return ImageIO.read(new File(filename));
		} catch (IOException e) {
			System.err.println("Can't load file: " + filename);
		}
		return null;
	}

	/**
	 * Initialise (/reset) the game variables
	 */
	private void initGame() {
		map = new HexMap(this);
		map.init(ROW_COUNT, COLUMN_COUNT);
		paused = false;
		isOver = false;
		score = 0;
		level = 1;
		gameTime = -STARTUP_TIME;
		oldHexColorIndex = 0;
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
		Renderer.init(FONT_LIST);

		// init openAL
		SoundPlayer.init(SOUND_FILE_NAMES);

		Display.setTitle(WINDOW_TITLE);
	}

	private void initMenu() {
		GlColor labelColor = new GlColor(0.3, 0.3, 0.3, 0);
		// Menu root
		guiRoot = new MenuItem(0, 0, windowWidth, windowHeight);
		guiRoot.setBackgroundColor(new GlColor(0, 0, 0, 0));
		// Main menu
		guiMain = new MenuItem(windowWidth / 3, windowHeight / 2 - 135,
				windowWidth / 3, 270);
		guiMain.setBackgroundColor(new GlColor(0, 0, 0, 0.8));
		guiRoot.add(guiMain);
		// Resume button
		MenuItem item = new MenuItem(0, 20, windowWidth / 3, 30) {
			@Override
			public void click() {
				guiMain.setVisible(false);
			}
		};
		item.setText("Resume");
		item.setBackgroundColor(labelColor);
		guiMain.add(item);
		// Restart button
		item = new MenuItem(0, 70, windowWidth / 3, 30) {
			@Override
			public void click() {
				initGame();
				guiMain.setVisible(false);
			}
		};
		item.setText("Restart");
		item.setBackgroundColor(labelColor);
		guiMain.add(item);
		// Mute button
		item = new MenuItem(0, 120, windowWidth / 3, 30) {
			@Override
			public void click() {
				SoundPlayer.toggleMute();
				if (SoundPlayer.isMuted()) {
					setText("Unmute");
				} else {
					setText("Mute");
				}
			}
		};
		if (SoundPlayer.isMuted()) {
			item.setText("Unmute");
		} else {
			item.setText("Mute");
		}
		item.setBackgroundColor(labelColor);
		guiMain.add(item);
		// Options button
		item = new MenuItem(0, 170, windowWidth / 3, 30) {
			@Override
			public void click() {
				guiMain.setVisible(false);
				guiOptions.setVisible(true);
			}
		};
		item.setText("Options");
		item.setBackgroundColor(labelColor);
		guiMain.add(item);
		// Quit button
		item = new MenuItem(0, 220, windowWidth / 3, 30) {
			@Override
			public void click() {
				running = false;
			}
		};
		item.setText("Quit");
		item.setBackgroundColor(labelColor);
		guiMain.add(item);
		// Options menu
		guiOptions = new MenuItem(windowWidth / 3, windowHeight / 2 - 110,
				windowWidth / 3, 220);
		guiRoot.add(guiOptions);
		guiOptions.setVisible(false);
		// Back button
		item = new MenuItem(0, 20, windowWidth / 3, 30) {
			@Override
			public void click() {
				guiOptions.setVisible(false);
				guiMain.setVisible(true);
			}
		};
		item.setText("Back");
		item.setBackgroundColor(labelColor);
		guiOptions.add(item);
		// 600 button
		item = new MenuItem(0, 70, windowWidth / 3, 30) {
			@Override
			public void click() {
				setDisplay(600);
			}
		};
		item.setText("600 x " + (int) (600 / ASPECT_RATIO));
		item.setBackgroundColor(labelColor);
		guiOptions.add(item);
		// 1000 button
		item = new MenuItem(0, 120, windowWidth / 3, 30) {
			@Override
			public void click() {
				setDisplay(1000);
			}
		};
		item.setText("1000 x " + (int) (1000 / ASPECT_RATIO));
		item.setBackgroundColor(labelColor);
		guiOptions.add(item);
		// 1200 button
		item = new MenuItem(0, 170, windowWidth / 3, 30) {
			@Override
			public void click() {
				setDisplay(1200);
			}
		};
		item.setText("1200 x " + (int) (1200 / ASPECT_RATIO));
		item.setBackgroundColor(labelColor);
		guiOptions.add(item);
	}

	private void saveSettings() {
		try {
			System.out.println("Settings -- Saving");
			FileOutputStream fileOut = new FileOutputStream("settings.svd");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeInt(windowWidth);
			out.writeDouble(highscore);
			out.close();
			fileOut.close();
			System.out.println("Settings -- Saved");
		} catch (IOException e) {
			System.out.println("Settings !! Failed");
		}
	}

	private void loadSettings() {
		try {
			System.out.println("Settings -- Loading");
			FileInputStream fileIn = new FileInputStream("settings.svd");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			windowWidth = in.readInt();
			highscore = in.readDouble();
			in.close();
			fileIn.close();
			System.out.println("Settings -- Loaded");
		} catch (IOException e) {
			System.out.println("Settings !! Failed");
		}
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
					if (guiMain.isVisible()) {
						guiMain.setVisible(false);
					} else {
						guiMain.setVisible(true);
					}
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
				MenuItem item;
				if ((item = guiRoot.pickItem(Mouse.getEventX(), windowHeight - Mouse.getEventY())) != guiRoot) {
					if (item != null) {
						item.click();
					}
					return;
				}
				if (Mouse.getEventButton() == 0) {
					if (!isGamePaused()) {
						Hexagon hex = map.getHexAt(Mouse.getEventX(), windowHeight - Mouse.getEventY());
						if (hex != null && !hex.isConnected()) {
							hex.rotateCW();
							SoundPlayer.playSound(SOUND_CLICK);
						}
					}
				} else if (Mouse.getEventButton() == 1) {
					if (!isGamePaused()) {
						Hexagon hex = map.getHexAt(Mouse.getEventX(), windowHeight - Mouse.getEventY());
						if (hex != null && !hex.isConnected()) {
							hex.rotateCCW();
							SoundPlayer.playSound(SOUND_CLICK);
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
		lastUpdateTime = getTime();

		TextPopup.moveAll();

		if (!isGamePaused()) {
			gameTime += UPDATE_TIME;
			if (isWaitingToStart()) {
				// jede Sekunde Sound abspielen
				if (gameTime / 1000 != (gameTime - UPDATE_TIME) / 1000) {
					if (gameTime < -1000) {
						SoundPlayer.playSound(SOUND_COUNTDOWN);
					} else {
						SoundPlayer.playSound(SOUND_COUNTDOWN_LAST);
					}
				}
			} else {
				oldScore = score;

				map.scroll();
				map.update();

				// Spiel verloren?
				if (score == oldScore) {
					if (!isOver) {
						SoundPlayer.playSound(SOUND_GAME_OVER);
						isOver = true;
						if (score > highscore) {
							highscore = score;
							saveSettings();
						}
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
		glLoadIdentity();
		// fps
		Renderer.drawText("FPS: " + fps, windowWidth - 120, 0, false, true);

		// startup time
		if (isWaitingToStart()) {
			Renderer.drawText("Starting in: " + (int) Math.ceil((float) (-gameTime / 1000)),
					windowWidth / 2, 20, true, true);
		} else {
			// game time
			Renderer.drawText("Zeit: " + gameTime / 1000, windowWidth - 120, 30,
					false, true);
		}
		// level
		Renderer.drawText("Level: " + level, false, true);
		// score
		Renderer.drawText("Score: " + (int) score, 0, 30, false, true);
		// highscore
		if (isOver) {
			Renderer.drawText("Highscore: " + (int) highscore, 0, 60, false, true);
		}

		// game over and pause messages
		if (!guiMain.isVisible()) {
			if (isOver) {
				glTranslatef(0, windowHeight / 2 - 30, 0);
				Renderer.drawQuad(windowWidth, 60);
				glTranslatef(0, -windowHeight / 2 + 30, 0);
				Renderer.drawText("Game over :/",
						windowWidth / 2, windowHeight / 2, true);
			} else if (paused) {
				glTranslatef(0, windowHeight / 2 - 30, 0);
				Renderer.drawQuad(windowWidth, 60);
				glTranslatef(0, -windowHeight / 2 + 30, 0);
				Renderer.drawText("Paused",
						windowWidth / 2, windowHeight / 2, true);
			}
		}
		// score popups
		if (!isWaitingToStart()) {
			TextPopup.renderAll();
		}

		// Menu
		glLoadIdentity();
		guiRoot.drawAll();
	}

	public boolean isGamePaused() {
		return paused || isOver || (guiMain != null && guiMain.isVisible())
				|| (guiOptions != null && guiOptions.isVisible());
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
			frameCount = 0;
			lastFpsTime += 1000;
		}
		frameCount++;
	}

	private void destroy() {
		Keyboard.destroy();
		Mouse.destroy();
		Display.destroy();
		SoundPlayer.destroy();
	}
}
