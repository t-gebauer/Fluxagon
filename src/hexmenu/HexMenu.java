/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexmenu;

import fluxagon.GlColor;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Timo
 */
public class HexMenu {

	private ArrayList<HexMenuItem> hexList;
	private int animTime = -1;
	private static final int TIME_PER_HEX = 60;
	private boolean visible = false;
	private float x, y;

	public HexMenu(float x, float y, boolean visible) {
		this.visible = visible;
		this.x = x;
		this.y = y;
	}

	public boolean isVisible() {
		return visible || animTime >= 0;
	}

	public void setVisible(boolean visible) {
		if (!this.isVisible() && visible) {
			animTime = 0;
		} else if (this.isVisible() && !visible) {
			animTime = -1;
//			if (hexList != null) {
//				animTime = TIME_PER_HEX * hexList.size();
//			}
		}
		this.visible = visible;
	}

	public void click(int x, int y) {
		if (!visible || hexList == null) {
			return;
		}
		float relX = (x - this.x) / HexMenuItem.getWidth();
		float relY = (y - this.y) / HexMenuItem.getHeight();
		for (HexMenuItem item : hexList) {
			if (item.pick(relX, relY)) {
				item.click();
				return;
			}
		}
	}

	public void animate(int time) {
		if (visible) {
			animTime += time;
			if (animTime > TIME_PER_HEX * hexList.size()) {
				animTime = TIME_PER_HEX * hexList.size();
			}
		} else {
			animTime -= time;
			if (animTime < 0) {
				animTime = -1;
			}
		}
	}

	public void render(int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		for (int i = 0; i < hexList.size(); i++) {
			if (animTime < i * TIME_PER_HEX) {
				break;
			} else if (animTime < (i + 1) * TIME_PER_HEX) {
				float fade = (float) (animTime - (i * TIME_PER_HEX))
						/ TIME_PER_HEX;
				HexMenuItem.setColor(new GlColor(1, 1, 1, fade));
				hexList.get(i).draw();
				HexMenuItem.setColor(GlColor.white());
			}
			hexList.get(i).draw();
		}
		GL11.glPopMatrix();
	}

	public void add(HexMenuItem item) {
		if (hexList == null) {
			hexList = new ArrayList<>();
		}
		hexList.add(item);
	}
}
