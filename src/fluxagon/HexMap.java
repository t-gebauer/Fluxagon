/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.util.Random;

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
	/** X-Position des Weges (in der letzten Zeile) */
	private int wayColumn;
	private Random random = new Random();
	private int rowCount, columnCount;
	private float hexOffsetX;

	public HexMap(Fluxagon main) {
		this.main = main;
	}

	public void init(int rows, int colums) {
		rowCount = rows;
		columnCount = colums;
		hexOffsetX = (main.getWindowWidth()
				- (main.getHexWidth() * (2 * columnCount + 1))) / 2;
		hexagons = new Hexagon[rows][colums];
		for (int i = 1; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				hexagons[i][j] = new Hexagon(main, i, j);
			}
		}
		wayColumn = hexagons[rowCount / 4][columnCount / 2]
				.createWay(null, random);
		hexagons[rowCount / 4][columnCount / 2].connectMid();
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

	public float getHexOffsetX() {
		return hexOffsetX;
	}

	public Hexagon getHex(int row, int column) {
		return hexagons[row][column];
	}

	public Hexagon getHexAt(float x, float y) {
		int row = Math.round(((y - main.getHexHeight() + animationOffset) / main.getHexHeight() + 1) / 1.5f);
		// Verschiebung beachten
		if (indentOdd ^ row % 2 == 0) {
			x -= main.getHexWidth();
		}
		int column = Math.round(((x - hexOffsetX) / main.getHexWidth() - 1) / 2);

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
		animationOffset += main.getHexHeight() * BASE_SCROLL_SPEED * Math.pow(LEVEL_SCROLL_SPEED, main.getLevel());
		if (animationOffset >= 1.5 * main.getHexHeight()) {
			animationOffset = 0;
			indentOdd = !indentOdd;
			for (int i = 0; i < rowCount - 1; i++) {
				hexagons[i] = hexagons[i + 1];
				for (int k = 0; k < columnCount; k++) {
					hexagons[i][k].setRow(i);
				}
			}
			hexagons[rowCount - 1] = new Hexagon[columnCount];
			for (int j = 0; j < columnCount; j++) {
				hexagons[rowCount - 1][j] = new Hexagon(main, rowCount - 1, j);
			}
			wayColumn = hexagons[rowCount - 2][wayColumn].createWay(null, random);
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
