/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.util.concurrent.CopyOnWriteArrayList;
import org.newdawn.slick.Color;

/**
 *
 * @author Timo
 */
public class TextPopup {

	private static final float MAX_OFFSET = 15;
	private static final float OFFSET_INC = 0.5f;
	private String text;
	private float offset;
	private float x;
	private float y;
	private static CopyOnWriteArrayList<TextPopup> list = new CopyOnWriteArrayList<>();

	public TextPopup(float x, float y, String text) {
		this.text = text;
		this.x = x;
		this.y = y;
		list.add(this);
	}
	
	public static void moveAll() {
		for (TextPopup textPopup : list) {
			textPopup.move();
		}
	}

	private void move() {
		offset += OFFSET_INC;
		if (offset >= MAX_OFFSET) {
			list.remove(this);
		}
	}

	public static void renderAll(Fluxagon flux) {
		for (TextPopup textPopup : list) {
			textPopup.render(flux);
		}
	}

	private void render(Fluxagon flux) {
		flux.drawText(flux.popupFont, Math.round(x), Math.round(y-offset), text, Color.yellow, false, true);
	}
}
