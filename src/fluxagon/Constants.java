/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

/**
 *
 * @author Timo
 */
public interface Constants {

	/** Die Breite des Fensters */
	public static final int WINDOW_WIDTH = 600;
	/** Die Höhe des Fensters */
	public static final int WINDOW_HEIGHT = 600;
	/** Der Titel des Fensters */
	public static final String WINDOW_TITLE = "Fluxagon";
	/** Anzahl der horizontalen Spalten */
	public static final int COLUMN_COUNT = 7;
	/** Anzahl der Reihen (wird irgendwie aus der Anzahl der Spalten und dem
	 * Seitenverhältnis des Fensters berechnet) */
	public static final int ROW_COUNT =
			Math.round((COLUMN_COUNT + 4) * 1.0f / WINDOW_WIDTH * WINDOW_HEIGHT) + 2;
	public static final float HEX_OFFSET_PERCENT = 0.5f;
	/** halbe Breite des Hecagons; Abstand der Kanten */
	public static final float HEX_WIDTH =
			WINDOW_WIDTH / (COLUMN_COUNT * 2 + HEX_OFFSET_PERCENT * 6);
	/** halbe Höhe des Hexagons; Abstand der Ecken */
	public static final float HEX_HEIGHT =
			HEX_WIDTH * 2 / (float) Math.sqrt(3);
	public static final float HEX_OFFSET_X =
			HEX_OFFSET_PERCENT * HEX_WIDTH * 2;
	public static final float HEX_OFFSET_Y =
			HEX_OFFSET_PERCENT * HEX_HEIGHT * 2;
	/** Wahrscheinlichkeit für das Entstehen einer Lane */
	public static final float LANE_PROBABILITY = 0.4f;
	/** Drehweite der Hexagone pro Animationsdurchlauf */
	public static final float HEX_ROTATION_DIST = 15;
	/** Flussgeschwindigkeit */
	public static final double BASE_FLUX_SPEED = 0.015;
	public static final double LEVEL_FLUX_SPEED = 0.008;
	/** Scrollgeschwindigkeit der Map */
	public static final double BASE_SCROLL_SPEED = 0.25;
	public static final double LEVEL_SCROLL_SPEED = 0.1;
	/** Farbdefinitionen für die Hexagone */
	public static final GlColor[] COLOR_HEXAGON = {
		/* 0 orange */new GlColor(1, 0.6, 0.1),
		/* 1 blau   */ GlColor.from255(62, 116, 237),
		/* 2 grün   */ GlColor.from255(63, 174, 72),
		/* 3 türkis */ GlColor.from255(76, 160, 124),
		/* 4 magenta*/ GlColor.from255(110, 58, 81)};
	/** Farbdefinition für den Vordergrund der Lanes */
	public static final GlColor[] COLOR_LINE_FG = {
		/* 0 */new GlColor(0, 0.8, 0.1),
		/* 1 */ GlColor.from255(188, 0, 166),
		/* 2 */ GlColor.from255(66, 88, 137),
		/* 3 */ GlColor.from255(74, 62, 134),
		/* 4 */ GlColor.from255(159, 177, 40)};
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
	public static final int SOUND_CLICK = 0;
	public static final int SOUND_COUNTDOWN = 1;
	public static final int SOUND_COUNTDOWN_LAST = 2;
	public static final int SOUND_GAME_OVER = 3;
	public static final int SOUND_LEVEL_UP = 4;
	public static final String[] SOUND_FILE_NAMES = {"rotate.wav",
		"countdown.wav", "countdown_last.wav", "game over1.wav",
		"level up2.wav"};
	public static final int FADE_TIME = 1600;
}
