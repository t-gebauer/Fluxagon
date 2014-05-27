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
		hexagons[4][4].connect();
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

	public void update() {
		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].update();
				}
			}
		}
	}

	public void animate() {
		for (int i = 0; i < hexagons.length; i++) {
			for (int j = 0; j < hexagons[i].length; j++) {
				if (hexagons[i][j] != null) {
					hexagons[i][j].animate();
				}
			}
		}
	}

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
