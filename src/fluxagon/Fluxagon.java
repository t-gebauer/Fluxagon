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
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.PixelFormat;
import org.newdawn.slick.opengl.CursorLoader;
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
	/** Boolean flag on whether fps are capped to 60 per second or not */
	private boolean capFPS = true;
	/** Score */
	private double score = 0;
	private double oldScore = -1;
	private double highscore = 0;
	private boolean running = true;
	/** Zeit seit Spielstart in ms */
	private long gameTime;
	private HexMap map;
	/** Boolean flag on whether the game is over */
	private boolean isOver;
	/** Boolean flag on whether circles or hexagons are drawn */
	private boolean circleMode = false;
	private boolean showCredits;
	/** aktuelles Level */
	private int level = 1;
	/** Color theme fading */
	private long fadeStartTime;
	private int oldHexColorIndex;
	private int hexColorIndex;
	/** Display size */
	private int windowWidth = 900;
	private int windowHeight;
	private float hexRadius;
	/** GUI */
	private HexMenu menuMain;
	private HexMenu menuOptions;
	private HexMenu menuReplay;
	/** Textures */
	private Texture texEmpty;
	private Texture texPlay;
	private Texture texReset;
	private Texture texMute;
	private Texture texQuit;
	private Texture texFlxgn;
	private Texture texUnmute;
	private Texture texResolution;
	private Texture texResSmall;
	private Texture texResMiddle;
	private Texture texResBig;
	private Texture texLoading;
	private Cursor cursorHand;
	/** load Animation */
	private boolean loadFaderActive = true;
	private long loadFadeTime;
	private boolean loadTimeElapsed = false;
	private long loadTime;
	/** Window preferences */
	private int realWindowWidth = Display.getDesktopDisplayMode().getWidth();
	private int realWindowHeight;
	private float aspectRatio;
	private boolean fullscreen = true;
	/** Display <li>0: small windowed<li>1: medium windowed<li>2: fullscreen */
	private byte displaySetting = 1;
	/** Highscore name input? */
	private boolean waitingForInput = false;
	/** Highscore name */
	private String bestPlayer = "";

	public int getWindowWidth() {
		return windowWidth;
	}

	/** @return half of the height; the radius */
	public float getHexHeight() {
		return hexRadius;
	}

	/** @return half the width */
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
		windowHeight = Math.round(width / aspectRatio);
		hexRadius = windowHeight / (1.5f * (VISIBLE_ROWS - 1) + 2);
		windowWidth = width;
	}

	/** Resizes the window (without changing any In-Game things (except Menu)) */
	private void setDisplay(int mode) {
		int height;
		switch (mode) {
			case 0:
				height = Display.getDesktopDisplayMode().getHeight() / 3;
				fullscreen = false;
				break;
			case 1:
				height = Display.getDesktopDisplayMode().getHeight() / 2;
				fullscreen = false;
				break;
			default: // 2
				height = Display.getDesktopDisplayMode().getHeight();
				fullscreen = true;
				break;
		}
		int width = Math.round(height * aspectRatio);
		initMenu();
		System.out.println("Setting display mode: " + width + ":" + height + " fs: " + fullscreen);
		setDisplayMode(width, height, fullscreen);
		realWindowWidth = Display.getDisplayMode().getWidth();
		realWindowHeight = Display.getDisplayMode().getHeight();
		if (!Display.isCreated()) {
			try {
				Display.create(new PixelFormat().withSamples(8));
				System.out.println("Display created with 8 samples.");
			} catch (LWJGLException e) {
				System.out.println("ERROR: Failed to create display.");
				System.out.println(e);
			}
		}
		if (!Display.isCreated()) {
			try {
				Display.create(new PixelFormat().withSamples(4));
				System.out.println("Display created with 4 samples.");
			} catch (LWJGLException e) {
				System.out.println("ERROR: Failed to create display.");
				System.out.println(e);
			}
		}
		if (!Display.isCreated()) {
			try {
				Display.create();
				System.out.println("Display created without samples.");
			} catch (LWJGLException e) {
				System.out.println("ERROR: Failed to create display.");
				System.out.println(e);
				System.exit(1);
			}
		}
		glViewport(0, 0, width, height);
		glLineWidth(realWindowHeight / 175f);
		if (mode != displaySetting) {
			displaySetting = (byte) mode;
			saveSettings();
		}
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
		loadTime = getTime();
		loadSettings();
		try {
			initGL();
		} catch (LWJGLException e) {
			System.out.println(e);
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
		// getting aspect ratio
		aspectRatio = (float) Display.getDesktopDisplayMode().getWidth()
				/ Display.getDesktopDisplayMode().getHeight();
		System.out.println("Aspect Ratio: "
				+ Display.getDesktopDisplayMode().getWidth() + " / "
				+ Display.getDesktopDisplayMode().getHeight());

		// Display
		Display.setTitle(WINDOW_TITLE);

		BufferedImage icon16 = loadImage("gfx/icons/16x16.png");
		BufferedImage icon32 = loadImage("gfx/icons/32x32.png");
		BufferedImage icon128 = loadImage("gfx/icons/128x128.png");
		if (icon16 != null && icon32 != null && icon128 != null) {
			Display.setIcon(new ByteBuffer[]{
				new ImageIOImageData().imageToByteBuffer(icon16, false, false, null),
				new ImageIOImageData().imageToByteBuffer(icon32, false, false, null),
				new ImageIOImageData().imageToByteBuffer(icon128, false, false, null)
			});
		}

		setDisplay(displaySetting);

		// calculate windowHeight and hex size
		calculateDisplay(windowWidth);

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

		// Antialiasing
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
		// Blending for Antialiasing in RGBA mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glClearColor(0.17f, 0.17f, 0.17f, 1);
	}

	/**
	 * Set the display mode to be used
	 *
	 * http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_%28Fullscreen%29
	 *
	 * @param width The width of the display required
	 * @param height The height of the display required
	 * @param fullscreen True if we want fullscreen mode
	 */
	public void setDisplayMode(int width, int height, boolean fullscreen) {

		// return if requested DisplayMode is already set
		if ((Display.getDisplayMode().getWidth() == width)
				&& (Display.getDisplayMode().getHeight() == height)
				&& (Display.isFullscreen() == fullscreen)) {
			return;
		}

		try {
			DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (int i = 0; i < modes.length; i++) {
					DisplayMode current = modes[i];

					if ((current.getWidth() == width) && (current.getHeight() == height)) {
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the 
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
								&& (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width, height);
			}

			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
				return;
			}

			// borderless window
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "" + fullscreen);
			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);


		} catch (LWJGLException e) {
			System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
		}
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
		texLoading = loadTexture("PNG", "gfx/logo/logo.png");
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glColor4d(1, 1, 1, 1);
		renderLoadingTexture(1);
		Display.update();

		// init renderer-helper-class
		Renderer.init(FONT_LIST);

		// init openAL
		SoundPlayer.init(SOUND_FILE_NAMES);

		// load textures
		texEmpty = loadTexture("PNG", "gfx/menu/empty.png");
		texPlay = loadTexture("PNG", "gfx/menu/continue.png");
		texReset = loadTexture("PNG", "gfx/menu/restart.png");
		texMute = loadTexture("PNG", "gfx/menu/mute_active.png");
		texUnmute = loadTexture("PNG", "gfx/menu/mute_disabled.png");
		texQuit = loadTexture("PNG", "gfx/menu/exit.png");
		texFlxgn = loadTexture("PNG", "gfx/menu/flxgn.png");
		texResolution = loadTexture("PNG", "gfx/menu/resolution.png");
		texResSmall = loadTexture("PNG", "gfx/menu/resolution_small.png");
		texResMiddle = loadTexture("PNG", "gfx/menu/resolution_middle.png");
		texResBig = loadTexture("PNG", "gfx/menu/resolution_big.png");

		// load cursor
		try {
			cursorHand = CursorLoader.get().getCursor("gfx/cursors/hand.png", 7, 0);
		} catch (IOException | LWJGLException e) {
			System.out.println("ERROR: Failed to load cursor: <hand.png>");
			System.out.println(e);
		}

		Display.setTitle(WINDOW_TITLE);
	}

	private Texture loadTexture(String format, String resName) {
		Texture tex = null;
		try {
			tex = TextureLoader.getTexture(format,
					ResourceLoader.getResourceAsStream(resName));
			System.out.println("Texture loaded: " + resName + " as " + format + ">> ID " + tex.getTextureID());
		} catch (IOException e) {
			System.out.println("ERROR: File not found: " + resName);
		}
		return tex;
	}

	private void renderLoadingTexture(float scale) {
		int width = Math.round(windowWidth / 3 * scale);
		int height = width * texLoading.getImageHeight() / texLoading.getImageWidth();
		glTranslatef(windowWidth / 2, windowHeight / 2, 0);
		glRotated(-scale * 360, 0, 0, 1);
		glTranslatef(-width / 2, -height / 2, 0);
		Renderer.drawTexture(texLoading, width, height);
	}

	private void initMenu() {
		HexMenuItem.init(texEmpty);
		final int tooltipWidth = windowWidth / 2;
		final int tooltipHeight = windowHeight * 3 / 4;

		// Main menu
		menuMain = new HexMenu(windowWidth / 2, windowHeight / 2, true);
		menuMain.setSize(windowHeight / 6);
		menuMain.add(new HexMenuItem(0, 0, texFlxgn, false) {
			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Fluxagonial!", 0.5f, 0);
			}
		});
		menuMain.add(new HexMenuItem(-1, 0, null) {
			@Override
			public void click() {
				showCredits = showCredits ? false : true;
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Credits", 0.5f, 0);
			}
		});
		menuMain.add(new HexMenuItem(-0.5f, -0.75f, texPlay) {
			@Override
			public void click() {
				menuMain.setVisible(false);
				if (menuOptions.isVisible()) {
					menuOptions.setVisible(false);
				}
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Resume", 0.5f, 0);
			}
		});
		menuMain.add(new HexMenuItem(0.5f, -0.75f, texReset) {
			@Override
			public void click() {
				initGame();
				menuMain.setVisible(false);
				if (menuOptions.isVisible()) {
					menuOptions.setVisible(false);
				}
				waitingForInput = false; // not necessary?
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Restart", 0.5f, 0);
			}
		});
		menuMain.add(new HexMenuItem(1, 0, texResolution) {
			@Override
			public void click() {
				if (menuOptions.isVisible()) {
					menuOptions.setVisible(false);
				} else {
					menuOptions.setVisible(true);
				}
				waitingForInput = false; // not necessary?
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Change Resolution", 0.5f, 0);
			}
		});
		menuMain.add(new HexMenuItem(0.5f, 0.75f, texQuit) {
			@Override
			public void click() {
				running = false;
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Quit", 0.5f, 0);
			}
		});
		menuMain.add(new HexMenuItem(-0.5f, 0.75f,
				(SoundPlayer.isMuted() ? texMute : texUnmute)) {
			@Override
			public void click() {
				SoundPlayer.toggleMute();
				if (SoundPlayer.isMuted()) {
					setTexture(texMute);
				} else {
					setTexture(texUnmute);
				}
				saveSettings();
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, SoundPlayer.isMuted() ? "Unmute" : "Mute", 0.5f, 0);
			}
		});
		// Options menu
		menuOptions = new HexMenu(windowWidth / 2, windowHeight / 2, false);
		menuOptions.setSize(windowHeight / 6);
		menuOptions.add(new HexMenuItem(1.5f, -0.75f, texResSmall) {
			@Override
			public void click() {
				if (displaySetting != 0) {
					setDisplay(0);
				}
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Windowed small", 0.5f, 0);
			}
		});
		menuOptions.add(new HexMenuItem(2, 0, texResMiddle) {
			@Override
			public void click() {
				if (displaySetting != 1) {
					setDisplay(1);
				}
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Windowed medium", 0.5f, 0);
			}
		});
		menuOptions.add(new HexMenuItem(1.5f, 0.75f, texResBig) {
			@Override
			public void click() {
				if (displaySetting != 2) {
					setDisplay(2);
				}
			}

			@Override
			public void mouseOver() {
				Renderer.drawText(tooltipWidth, tooltipHeight, "Fullscreen", 0.5f, 0);
			}
		});

		// Replay menu
		menuReplay = new HexMenu(windowWidth / 2 + 180, windowHeight / 2, false);
		menuReplay.setSize(50);
		menuReplay.add(new HexMenuItem(0, 0, texReset) {
			@Override
			public void click() {
				initGame();
				menuReplay.setVisible(false);
				waitingForInput = false;
			}
		});
	}

	private void saveSettings() {
		try {
			System.out.print("Settings ");
			FileOutputStream fileOut = new FileOutputStream("settings.svd");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeByte(displaySetting);
			out.writeBoolean(SoundPlayer.isMuted());
			System.out.println("-- Saved");
			System.out.print("Highscore ");
			out.writeDouble(highscore);
			for (int i = 0; i < NAME_LENGTH; i++) {
				if (i < bestPlayer.length()) {
					out.writeChar(bestPlayer.charAt(i));
				} else {
					out.writeChar(0);
				}
			}
			out.close();
			fileOut.close();
			System.out.println("-- Saved");
		} catch (IOException e) {
			System.out.println("!! Failed");
		}
	}

	private void loadSettings() {
		try {
			System.out.print("Settings ");
			FileInputStream fileIn = new FileInputStream("settings.svd");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			displaySetting = in.readByte();
			if (in.readBoolean()) {
				SoundPlayer.toggleMute();
			}
			System.out.println("-- Loaded");
			System.out.print("Highscore ");
			highscore = in.readDouble();
			if (highscore < 0) {
				highscore = 0;
			}
			bestPlayer = "";
			for (int i = 0; i < NAME_LENGTH; i++) {
				char c = in.readChar();
				if (c == 0) {
					break;
				} else {
					bestPlayer += c;
				}
			}
			in.close();
			fileIn.close();
			System.out.println("-- Loaded");
		} catch (IOException e) {
			System.out.println("!! Failed");
		}
	}

	/**
	 * Game loop
	 */
	private void run() {
		lastFpsTime = getTime();
		lastUpdateTime = getTime();
		while (running && !Display.isCloseRequested()) {
			if (Display.isVisible() && Display.isActive()) {
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
				// Highscore name input
				if (waitingForInput) {
					if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
						waitingForInput = false;
						saveSettings();
					} else if (Keyboard.getEventKey() == Keyboard.KEY_BACK) {
						if (bestPlayer.length() > 0) {
							bestPlayer = bestPlayer.substring(0, bestPlayer.length() - 1);
						}
					} else if (bestPlayer.length() < NAME_LENGTH) {
						char c = Keyboard.getEventCharacter();
						// Eingabebeschränkung
						if (c >= 65 && c <= 90 // A - Z
								|| c >= 97 && c <= 122 // a - z
								|| c >= 48 && c <= 57 // 0 - 9
								|| c == 223 // ß
								|| c == 228 || c == 246 || c == 252 // ä ö ü
								|| c == 196 || c == 214 || c == 220 // Ä Ö Ü
								|| (!(bestPlayer.length() == 0)
								&& !bestPlayer.endsWith(" ") && !bestPlayer.endsWith("-")
								&& (c == 32 || c == 45) // leer -
								)) {
							bestPlayer += c;
						}
					}
					// Normal interaction
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
				}
			} else {
				// Key Release
			}
		}
	}

	private int getMouseEventX() {
		return Math.round(Mouse.getEventX() / (float) realWindowWidth * windowWidth);
	}

	private int getMouseEventY() {
		return Math.round((realWindowHeight - Mouse.getEventY()) / (float) realWindowHeight * windowHeight);
	}

	private int getMouseX() {
		return Math.round(Mouse.getX() / (float) realWindowWidth * windowWidth);
	}

	private int getMouseY() {
		return Math.round((realWindowHeight - Mouse.getY()) / (float) realWindowHeight * windowHeight);
	}

	/**
	 * Deal with mouse input
	 */
	private void processMouse() {
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				if (isMenuOpen()) {
					if (menuOptions.isVisible()) {
						if (!menuOptions.click(getMouseEventX(), getMouseEventY(), Mouse.getEventButton())) {
							menuOptions.setVisible(false);
						}
					} else {
						menuMain.click(getMouseEventX(), getMouseEventY(), Mouse.getEventButton());
					}
				} else if (isOver) {
					menuReplay.click(getMouseEventX(), getMouseEventY(), Mouse.getEventButton());
				} else if (Mouse.getEventButton() == 0) {
					if (!isGamePaused()) {
						Hexagon hex = map.getHexAt(getMouseEventX(), getMouseEventY());
						if (hex != null && !hex.isConnected()) {
							hex.rotateCW();
							SoundPlayer.playSound(SOUND_CLICK);
						}
					}
				} else if (Mouse.getEventButton() == 1) {
					if (!isGamePaused()) {
						Hexagon hex = map.getHexAt(getMouseEventX(), getMouseEventY());
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
						menuReplay.setVisible(true);
						if (score > highscore) {
							highscore = score;
							waitingForInput = true;
							bestPlayer = "";
							saveSettings();
						}
					}

				}
			}
		}
		map.animate();
		menuMain.animate((int) UPDATE_TIME);
		menuOptions.animate((int) UPDATE_TIME);
		menuReplay.animate((int) UPDATE_TIME);
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_TEXTURE_2D);

		// min loading time
		if (!loadTimeElapsed) {
			if (getTime() - loadTime <= 2000) {
				glColor4f(1, 1, 1, 1);
				glLoadIdentity();
				renderLoadingTexture(1);
			} else {
				loadTimeElapsed = true;
				// Start fading
				loadFadeTime = getTime();
			}
			return;
		}

		map.render();
		Cursor cursor = null;

		// render text
		glLoadIdentity();
		// fps
		if (isMenuOpen()) {
			Renderer.drawText(windowWidth - 9, windowHeight - 9, "FPS: " + fps, 1, 1);
		}

		// startup time
		if (isWaitingToStart()) {
			Renderer.drawText(windowWidth / 2, 4,
					"Starting in: " + (int) Math.ceil((float) (-gameTime / 1000)),
					0.5f, 0);
		} else {
			// game time
			Renderer.drawText(windowWidth - 9, 9, "Zeit: " + gameTime / 1000, 1, 0);
		}
		// score
		Renderer.drawText(9, 9, "Score: " + (int) score);
		// level
		Renderer.drawText(9, 25 + 9, "Level: " + level);
		// highscore
		if (highscore != 0 && (isOver || isMenuOpen())) {
			Renderer.drawText(9, 50 + 9, "Highscore: " + (int) highscore
					+ (bestPlayer.length() > 0 ? " - " + bestPlayer : ""));
		}

		// score popups
		if (!isWaitingToStart()) {
			TextPopup.renderAll();
		}

		// game over message
		glPushMatrix();
		if (!isMenuOpen()) {
			if (isOver) {
				glTranslatef(0, windowHeight / 2 - 30, 0);
				glColor4d(0, 0, 0, 0.75);
				Renderer.drawQuad(windowWidth, 60);
				glTranslatef(0, -windowHeight / 2 + 30, 0);
				if (waitingForInput) {
					Renderer.drawText(windowWidth / 2, windowHeight / 2 - 15,
							"Game over :/", 0.5f, 0.5f);
					Renderer.drawText(windowWidth / 2, windowHeight / 2 + 15,
							"Enter name: " + bestPlayer
							+ (bestPlayer.length() < NAME_LENGTH ? "_" : ""),
							0.5f, 0.5f);
				} else {
					Renderer.drawText(windowWidth / 2, windowHeight / 2,
							"Game over :/", 0.5f, 0.5f);
				}
			}
		}
		glPopMatrix();

		menuMain.setHexColor(COLOR_HEXAGON[getHexColorIndex()]);
		menuOptions.setHexColor(COLOR_HEXAGON[getHexColorIndex()]);
		menuReplay.setHexColor(COLOR_HEXAGON[getHexColorIndex()]);

		// replay button
		if (!isMenuOpen() && isOver) {
			menuReplay.setColor(GlColor.white());
			if (menuReplay.render(getMouseX(), getMouseY())) {
				cursor = cursorHand;
			}
		}

		// Menu
		glLoadIdentity();
		if (menuMain.isVisible()) {
			glColor4d(0, 0, 0, 0.4);
			Renderer.drawQuad(windowWidth, windowHeight);
		}

		if (menuOptions.isVisible()) {
			menuMain.setColor(new GlColor(0.75, 0.75, 0.75, 1));
			menuMain.render(-1, -1);
			menuOptions.setColor(GlColor.white());
			if (menuOptions.render(getMouseX(), getMouseY())) {
				cursor = cursorHand;
			}
		} else {
			menuMain.setColor(GlColor.white());
			if (menuMain.render(getMouseX(), getMouseY())) {
				cursor = cursorHand;
			}
		}

		// Creator credits
		if (showCredits) {
			Renderer.drawText(windowWidth / 2, windowHeight - 9,
					"Created by Benjamin Strilziw && Timo Gebauer",
					0.5f, 1);
		}

		if (Mouse.getNativeCursor() != cursor) {
			tryToSetCursor(cursor);
		}

		if (loadFaderActive) {
			final int fadeTime = 500;
			final long timeDelta = getTime() - loadFadeTime;
			if (timeDelta <= fadeTime) {
				glColor4f(1, 1, 1, 1 - (timeDelta / (float) fadeTime));
				glLoadIdentity();
				renderLoadingTexture(1 - (timeDelta / (float) fadeTime));
			} else {
				loadFaderActive = false;
			}
		}
	}

	public void tryToSetCursor(Cursor cursor) {
		try {
			Mouse.setNativeCursor(cursor);
		} catch (LWJGLException e) {
			System.out.println("ERROR: Failed to set native cursor.");
		}
	}

	public boolean isGamePaused() {
		return isOver || isMenuOpen();
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
