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
	private int hexWidth, hexHeight;

	public HexMenu(float x, float y, boolean visible) {
		this.visible = visible;
		this.x = x;
		this.y = y;
	}

	public void setSize(int width) {
		// Calculate hexagon width and height
		// Weite sollte halbierbar sein
		if (width % 2 != 0) {
			width++;
		}
		this.hexWidth = width;
		this.hexHeight = Math.round(width * 2 / (float) Math.sqrt(3));
	}

	public boolean isVisible() {
		return visible || animTime >= 0;
	}

	public void setVisible(boolean visible) {
		if (!this.isVisible() && visible) {
			animTime = 0;
		} else if (this.isVisible() && !visible) {
			animTime = -1;
		}
		this.visible = visible;
	}

	public void click(int x, int y, int button) {
		if (!visible || hexList == null) {
			return;
		}
		for (HexMenuItem item : hexList) {
			float relX = (x - this.x) / item.getWidth();
			float relY = (y - this.y) / item.getHeight();
			if (item.pick(relX, relY)) {
				if (button == 0) {
					item.click();
				} else if (button == 1) {
					item.rightClick();
				}
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
		if (hexList != null) {
			for (HexMenuItem item : hexList) {
				item.animate(time);
			}
		}
	}

	public boolean render(int mouseX, int mouseY) {
		boolean mouseOver = false;
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		for (int i = 0; i < hexList.size(); i++) {
			float relX = (mouseX - this.x) / hexList.get(i).getWidth();
			float relY = (mouseY - this.y) / hexList.get(i).getHeight();
			if (animTime < i * TIME_PER_HEX) {
				break;
			} else if (animTime < (i + 1) * TIME_PER_HEX) {
				float fade = (float) (animTime - (i * TIME_PER_HEX))
						/ TIME_PER_HEX;
				hexList.get(i).setColor(new GlColor(1, 1, 1, fade));
				hexList.get(i).draw();
			} else {
				// Mouseover ?
				if (hexList.get(i).pick(relX, relY)) {
					mouseOver = true;
					hexList.get(i).setHexColor(hexList.get(i).getBackgroundColor().mult(0.9));
					hexList.get(i).mouseOver();
				}
				hexList.get(i).draw();
			}
		}
		GL11.glPopMatrix();
		return mouseOver;
	}

	public void add(HexMenuItem item) {
		if (hexList == null) {
			hexList = new ArrayList<>();
		}
		hexList.add(item);
		item.setSize(hexWidth, hexHeight);
	}

	public void setHexColor(GlColor color) {
		if (hexList == null) {
			return;
		}
		for (HexMenuItem item : hexList) {
			item.setHexColor(color);
		}
	}

	public void setColor(GlColor color) {
		if (hexList == null) {
			return;
		}
		for (HexMenuItem item : hexList) {
			item.setColor(color);
		}
	}
}
