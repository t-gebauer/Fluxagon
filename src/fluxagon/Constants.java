/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.awt.Font;

/**
 *
 * @author Timo
 */
public interface Constants {

	/** Der Titel des Fensters */
	public static final String WINDOW_TITLE = "Fluxagon";
	/** Seitenverhältnis */
	public static final float ASPECT_RATIO = 16 / 9f;
	/** Anzahl der horizontalen Spalten */
	public static final int COLUMN_COUNT = 5;
	/** Anzahl der Reihen */
	public static final int ROW_COUNT = 10;
	/** Anzahl der sichtbaren Reihen */
	public static final int VISIBLE_ROWS = 6;
	/** Wahrscheinlichkeit für das Entstehen einer Lane */
	public static final float LANE_PROBABILITY = 0.4f;
	/** Drehweite der Hexagone pro Animationsdurchlauf */
	public static final float HEX_ROTATION_DIST = 15;
	/** Flussgeschwindigkeit */
	public static final double BASE_FLUX_SPEED = 0.025;
	public static final double LEVEL_FLUX_SPEED = 1.044;
	/** Scrollgeschwindigkeit der Map */
	public static final double BASE_SCROLL_SPEED = 0.011;
	public static final double LEVEL_SCROLL_SPEED = 1.04;
	/** Farbdefinitionen für die Hexagone */
	public static final GlColor[] COLOR_HEXAGON = {
		/* 0 orange */new GlColor(1, 0.6, 0.1),
		/* 1 blau   */ GlColor.fromByte(62, 116, 237),
		/* 2 grün   */ GlColor.fromByte(63, 174, 72),
		/* 3 türkis */ GlColor.fromByte(76, 160, 124),
		/* 4 magenta*/ GlColor.fromByte(110, 58, 81)};
	/** Farbdefinition für den Vordergrund der Lanes */
	public static final GlColor[] COLOR_LINE_FG = {
		/* 0 */new GlColor(0, 0.8, 0.1),
		/* 1 */ GlColor.fromByte(188, 0, 166),
		/* 2 */ GlColor.fromHex(0xAA4839),
		/* 3 */ GlColor.fromHex(0xAA6A39),
		/* 4 */ GlColor.fromByte(159, 177, 40)};
	/** Farbdefinition für den Hintergrund der Lanes */
	public static final GlColor COLOR_LINE_BG = new GlColor(0.5, 0.5, 0.5, 0.7);
	/** Zeit zwischen zwei Update-Durchgängen */
	public static final long UPDATE_TIME = 40;
	/** Zeit bis zum Start des Spiels (in ms) */
	public static final int STARTUP_TIME = 5000;
	/** Punkte bis zum nächsten Level (Level * LEVEL_POINTS) */
	public static final int LEVEL_POINTS = 600;
	/** Punkte pro verbundenes Feld */
	public static final int[] POINTS_PER_HEX = {100, 75, 40, 20, 10, 10};
	/** Sounds */
	public static final int SOUND_CLICK = 0;
	public static final int SOUND_COUNTDOWN = 1;
	public static final int SOUND_COUNTDOWN_LAST = 2;
	public static final int SOUND_GAME_OVER = 3;
	public static final int SOUND_LEVEL_UP = 4;
	public static final String[] SOUND_FILE_NAMES = {"rotate.wav",
		"countdown.wav", "countdown_last.wav", "game over1.wav",
		"level up2.wav"};
	/** Time to change color theme */
	public static final int FADE_TIME = 1600;
	/** Fonts */
	public static final Font[] FONT_LIST = {
		new Font("Verdana", Font.BOLD, 20),
		new Font("Verdana", Font.ITALIC, 15)
	};
	public static final int FONT_STANDARD = 0;
	public static final int FONT_POPUP = 1;
}
