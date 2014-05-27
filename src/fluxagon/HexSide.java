/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

/**
 *
 * @author Timo
 */
public enum HexSide {

	LEFT_BOTTOM, LEFT_MID, LEFT_TOP, RIGHT_TOP, RIGHT_MID, RIGHT_BOTTOM;

	public HexSide opposite() {
		return values()[(ordinal() + 3) % 6];
	}

	public static HexSide fromValue(int value) {
		if (value < 0) {
			value = values().length - value;
		}
		return values()[value % 6];
	}
}
