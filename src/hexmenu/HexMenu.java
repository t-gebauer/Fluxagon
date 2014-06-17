/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexmenu;

import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Timo
 */
public class HexMenu {

	private ArrayList<HexMenuItem> hexList;
	private boolean visible;
	private float x, y;

	public HexMenu(float x, float y, boolean visible) {
		this.visible = visible;
		this.x = x;
		this.y = y;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void click(int x, int y) {
		if (!visible || hexList == null) {
			return;
		}
		float relX = (x - this.x) / HexMenuItem.width;
		float relY = (y - this.y) / HexMenuItem.height;
		for (HexMenuItem item : hexList) {
			if (item.pick(relX, relY)) {
				item.click();
				return;
			}
		}
	}

	public void render(int mouseX, int mouseY) {
		if (!visible) {
			return;
		}
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		for (HexMenuItem item : hexList) {
			item.draw();
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
