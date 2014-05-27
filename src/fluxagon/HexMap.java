/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

/**
 *
 * @author Timo
 */
public class HexMap implements Constants {

	private float animationOffset = 0;
	/** Boolean flag on whether odd or even Lanes are indented */
	private boolean indentOdd = false;
	private Hexagon[][] hexagons;
	private Fluxagon main;

	public HexMap(Fluxagon main) {
		this.main = main;
		hexagons = new Hexagon[ROW_COUNT][COLUMN_COUNT];
		for (int i = 1; i < ROW_COUNT; i++) {
			for (int j = 0; j < COLUMN_COUNT; j++) {
				hexagons[i][j] = new Hexagon(main, i, j);
			}
		}
		hexagons[ROW_COUNT / 4][COLUMN_COUNT / 2].connect();
	}

	/**
	 * Werden die ungeraden Reihen eingerückt?
	 *
	 * @return if the odd rows are indented
	 */
	public boolean isIndentOdd() {
		return indentOdd;
	}

	public float getAnimationOffset() {
		return animationOffset;
	}

	public Hexagon getHex(int row, int column) {
		return hexagons[row][column];
	}

	public Hexagon getHexAt(float x, float y) {
		int row = Math.round(((y - HEX_OFFSET_Y + animationOffset) / HEX_HEIGHT + 1) / 1.5f);
		// Verschiebung beachten
		if (indentOdd ^ row % 2 == 0) {
			x -= HEX_WIDTH;
		}
		int column = Math.round(((x - HEX_OFFSET_X) / HEX_WIDTH - 1) / 2);

		try {
			return hexagons[row][column];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return null;
	}

	/**
	 * Selects a hexagon dependent on its neighbor
	 *
	 * @param row Row of the neighbor
	 * @param column Column of the neighbor
	 * @param side Bordering side (from the neighbors point of view)
	 * @return
	 */
	public Hexagon getHexRel(int row, int column, HexSide side) {
		try {
			switch (side) {
				case LEFT_BOTTOM:
					if (row % 2 == 0 ^ !main.getMap().isIndentOdd()) {
						return hexagons[row + 1][column - 1];
					} else {
						return hexagons[row + 1][column];
					}
				case LEFT_MID:
					return hexagons[row][column - 1];
				case LEFT_TOP:
					if (row % 2 == 0 ^ !main.getMap().isIndentOdd()) {
						return hexagons[row - 1][column - 1];
					} else {
						return hexagons[row - 1][column];
					}
				case RIGHT_TOP:
					if (row % 2 == 0 ^ !main.getMap().isIndentOdd()) {
						return hexagons[row - 1][column];
					} else {
						return hexagons[row - 1][column + 1];
					}
				case RIGHT_MID:
					return hexagons[row][column + 1];
				case RIGHT_BOTTOM:
					if (row % 2 == 0 ^ !main.getMap().isIndentOdd()) {
						return hexagons[row + 1][column];
					} else {
						return hexagons[row + 1][column + 1];
					}
			}
		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
			// Tritt wahrscheinlich häufiger auf
		}
		return null;
	}

	/**
	 * Bewegt die Map
	 */
	public void scroll() {
		animationOffset += SCROLL_SPEED;
		if (animationOffset >= 1.5 * HEX_HEIGHT) {
			animationOffset = 0;
			indentOdd = !indentOdd;
			for (int i = 0; i < ROW_COUNT - 1; i++) {
				hexagons[i] = hexagons[i + 1];
				for (int k = 0; k < COLUMN_COUNT; k++) {
					hexagons[i][k].setRow(i);
				}
			}
			hexagons[ROW_COUNT - 1] = new Hexagon[COLUMN_COUNT];
			for (int j = 0; j < COLUMN_COUNT; j++) {
				hexagons[ROW_COUNT - 1][j] = new Hexagon(main, ROW_COUNT - 1, j);
			}
		}
	}

	/**
	 * Update der Spiel-Mechanik
	 */
	public void update() {
		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].update();
				}
			}
		}
	}

	/**
	 * Update der Animations-Mechanik
	 */
	public void animate() {
		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].animate();
				}
			}
		}
	}

	/**
	 * Grafische Ausgabe
	 */
	public void render() {
		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].render();
				}
			}
		}
	}
}
