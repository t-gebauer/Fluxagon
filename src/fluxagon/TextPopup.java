package fluxagon;

import java.util.concurrent.CopyOnWriteArrayList;
import org.newdawn.slick.Color;

/**
 *
 * @author Timo Gebauer
 */
public class TextPopup {

	private static final float MAX_OFFSET = 30;
	private static final float OFFSET_INC = 1;
	private static CopyOnWriteArrayList<TextPopup> list = new CopyOnWriteArrayList<>();

	public static void moveAll() {
		for (TextPopup textPopup : list) {
			textPopup.move();
		}
	}

	public static void renderAll() {
		for (TextPopup textPopup : list) {
			textPopup.render();
		}
	}

	private String text;
	private float offset;
	private float x;
	private float y;

	public TextPopup(float x, float y, String text) {
		this.text = text;
		this.x = x;
		this.y = y;
		list.add(this);
	}

	private void move() {
		offset += OFFSET_INC;
		if (offset >= MAX_OFFSET) {
			list.remove(this);
		}
	}

	private void render() {
		Renderer.setFont(Constants.FONT_POPUP);
		Renderer.setFontColor(Color.yellow);
		Renderer.drawText(Math.round(x), Math.round(y - offset), text, 0.5f, 0.5f);
		Renderer.setFontColor(Color.white);
		Renderer.setFont(0);
	}
}
