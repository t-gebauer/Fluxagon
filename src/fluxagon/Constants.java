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
	//public static final float FLUX_SPEED = 0.025f;
	/** Scrollgeschwindigkeit der Map */
	public static final double BASE_SCROLL_SPEED = 0.25;
	public static final double LEVEL_SCROLL_SPEED = 0.1;
	//public static final double SCROLL_SPEED = 0.35;
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
	/** Punkte bis zum nächsten Level (Level * LEVEL_POINTS) */
	public static final int LEVEL_POINTS = 600;
	/** Maximales Level.<p>
	 * Erreicht der Spieler MAX_LEVEL * LEVEL_POINTS Punkte, hat er gewonnen. */
	public static final int MAX_LEVEL = 6;
	/** Punkte pro verbundenes Feld */
	public static final int[] POINTS_PER_HEX = {100, 75, 40, 20, 10, 10};
}
