/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import hexmenu.HexMenu;
import hexmenu.HexMenuItem;
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
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.ImageIOImageData;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

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
	/** Score */
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
	/** Color theme fading */
	private long fadeStartTime;
	private int oldHexColorIndex;
	private int hexColorIndex;
	/** Display size */
	private int windowWidth = 600;
	private int windowHeight;
	private float hexRadius;
	/** GUI */
	private HexMenu menuMain;
	private HexMenu menuOptions;
	/** Textures */
	private Texture texLeer;
	private Texture texEmpty;
	private Texture texPlay;
	private Texture texReset;
	private Texture texMute;
	private Texture texQuit;
	private Texture texLogoBlack;
	private Texture texUnmute;

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

		// load textures
		texEmpty = loadTexture("PNG", "gfx/menu/empty.png");
		texPlay = loadTexture("PNG", "gfx/menu/play.png");
		texReset = loadTexture("PNG", "gfx/menu/reset.png");
		texMute = loadTexture("PNG", "gfx/menu/mute.png");
		texUnmute = loadTexture("PNG", "gfx/menu/unmute.png");
		texQuit = loadTexture("PNG", "gfx/menu/quit.png");
		texLogoBlack = loadTexture("PNG", "gfx/menu/logo_black.png");

		Display.setTitle(WINDOW_TITLE);
	}

	private Texture loadTexture(String format, String resName) {
		Texture tex = null;
		try {
			tex = TextureLoader.getTexture(format,
					ResourceLoader.getResourceAsStream(resName));
			System.out.println("Texture loaded: " + resName + " as " + format + ">> ID " + tex.getTextureID());
		} catch (IOException e) {
			System.out.println("FILE NOT FOUND: " + resName);
		}
		return tex;
	}

	private void initMenu() {
		// Calculate hexagon width and height
		HexMenuItem.width = windowWidth / 10;
		// Weite sollte halbierbar sein
		if (HexMenuItem.width % 2 != 0) {
			HexMenuItem.width++;
		}
		HexMenuItem.height = Math.round(HexMenuItem.width * 2 / (float) Math.sqrt(3));
		
		// Main menu
		menuMain = new HexMenu(windowWidth / 2, windowHeight / 2, true);
		menuMain.add(new HexMenuItem(-0.5f, -0.75f, texPlay) {
			@Override
			public void click() {
				menuMain.setVisible(false);
				menuOptions.setVisible(false);
			}
		});
		menuMain.add(new HexMenuItem(0.5f, -0.75f, texReset) {
			@Override
			public void click() {
				initGame();
				menuMain.setVisible(false);
				menuOptions.setVisible(false);
			}
		});
		menuMain.add(new HexMenuItem(-1, 0, texEmpty));
		menuMain.add(new HexMenuItem(0, 0, texLogoBlack));
		menuMain.add(new HexMenuItem(1, 0, texEmpty) {
			@Override
			public void click() {
				if (menuOptions.isVisible()) {
					menuOptions.setVisible(false);
				} else {
					menuOptions.setVisible(true);
				}
			}
		});
		menuMain.add(new HexMenuItem(-0.5f, 0.75f, texUnmute) {
			@Override
			public void click() {
				SoundPlayer.toggleMute();
				if (SoundPlayer.isMuted()) {
					setTexture(texMute);
				} else {
					setTexture(texUnmute);
				}
			}
		});
		menuMain.add(new HexMenuItem(0.5f, 0.75f, texQuit) {
			@Override
			public void click() {
				running = false;
			}
		});
		// Options menu
		menuOptions = new HexMenu(windowWidth / 2, windowHeight / 2, false);
		menuOptions.add(new HexMenuItem(1.5f, -0.75f, texEmpty) {
			@Override
			public void click() {
				setDisplay(600);
			}
		});
		menuOptions.add(new HexMenuItem(2, 0, texEmpty) {
			@Override
			public void click() {
				setDisplay(900);
			}
		});
		menuOptions.add(new HexMenuItem(1.5f, 0.75f, texEmpty) {
			@Override
			public void click() {
				setDisplay(1200);
			}
		});
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
					if (menuOptions.isVisible()) {
						menuOptions.setVisible(false);
					} else if (menuMain.isVisible()) {
						menuMain.setVisible(false);
					} else {
						menuMain.setVisible(true);
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
				if (isMenuOpen()) {
					menuMain.click(Mouse.getEventX(), windowHeight - Mouse.getEventY());
					menuOptions.click(Mouse.getEventX(), windowHeight - Mouse.getEventY());
				} else if (Mouse.getEventButton() == 0) {
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
		if (!isMenuOpen()) {
			if (isOver) {
				glTranslatef(0, windowHeight / 2 - 30, 0);
				glColor4d(0, 0, 0, 0.75);
				Renderer.drawQuad(windowWidth, 60);
				glTranslatef(0, -windowHeight / 2 + 30, 0);
				Renderer.drawText("Game over :/",
						windowWidth / 2, windowHeight / 2, true);
			} else if (paused) {
				glTranslatef(0, windowHeight / 2 - 30, 0);
				glColor4d(0, 0, 0, 0.75);
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
		if (menuMain.isVisible()) {
			glColor4d(0, 0, 0, 0.4);
			Renderer.drawQuad(windowWidth, windowHeight);
			if (menuOptions.isVisible()) {
				glColor4d(0.75, 0.75, 0.75, 1);
				menuMain.render(0, 0);
				Color.white.bind();
				menuOptions.render(0, 0);
			} else {
				Color.white.bind();
				menuMain.render(0, 0);
			}
		}
	}

	public boolean isGamePaused() {
		return paused || isOver || isMenuOpen();
	}

	public boolean isMenuOpen() {
		return (menuMain != null && menuMain.isVisible());
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
