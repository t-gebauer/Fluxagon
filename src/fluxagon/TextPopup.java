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

	private static final float MAX_OFFSET = 30;
	private static final float OFFSET_INC = 1;
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

	public static void renderAll() {
		for (TextPopup textPopup : list) {
			textPopup.render();
		}
	}

	private void render() {
		Renderer.setFont(Constants.FONT_POPUP);
		Renderer.setFontColor(Color.yellow);
		Renderer.drawText(text, Math.round(x), Math.round(y - offset), true);
		Renderer.setFontColor(Color.white);
		Renderer.setFont(0);
	}
}
