/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package basicmenu;

import fluxagon.GlColor;
import fluxagon.Renderer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Timo
 */
public class MenuItem {

	protected String text;
	protected int left, top, width, height;
	protected ArrayList<MenuItem> children;
	protected int borderWidth = 0;
	protected GlColor borderColor = new GlColor(1, 1, 1);
	protected GlColor backgroundColor = new GlColor(0, 0, 0, 1);
	protected boolean visible = true;

	public MenuItem(int left, int top, int width, int height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}

	public void drawAll() {
		if (!visible) {
			return;
		}
		GL11.glPushMatrix();
		draw();
		if (children != null) {
			for (MenuItem child : children) {
				child.drawAll();
			}
		}
		GL11.glPopMatrix();
	}

	public void draw() {
		GL11.glTranslatef(left, top, 0);
		if (borderWidth > 0) {
			borderColor.bind();
		} else {
			backgroundColor.bind();
		}
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(0, 0);
		GL11.glVertex2i(0, height);
		GL11.glVertex2i(width, height);
		GL11.glVertex2i(width, 0);
		if (borderWidth > 0) {
			backgroundColor.bind();
			GL11.glVertex2i(borderWidth, borderWidth);
			GL11.glVertex2i(borderWidth, height - borderWidth);
			GL11.glVertex2i(width - borderWidth, height - borderWidth);
			GL11.glVertex2i(width - borderWidth, borderWidth);
		}
		GL11.glEnd();
		if (text != null) {
			GL11.glTranslatef(width / 2, height / 2, 0);
			Renderer.drawText(Renderer.standardFont, text,
					org.newdawn.slick.Color.white, false, true);
		}
	}

	public MenuItem pickItem(int x, int y) {
		x -= left;
		y -= top;
		if (!visible || x < 0 || y < 0 || x >= width || y >= height) {
			return null;
		}
		MenuItem item;
		if (children != null) {
			for (MenuItem child : children) {
				if ((item = child.pickItem(x, y)) != null) {
					return item;
				}
			}
		}
		return this;
	}

	public void click() {
	}

	public void add(MenuItem item) {
		if (item == null) {
			return;
		}
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(item);
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setLeft(int left) {
		if (left >= 0) {
			this.left = left;
		}
	}

	public void setTop(int top) {
		if (top >= 0) {
			this.top = top;
		}
	}

	public void setWidth(int width) {
		if (width >= 0) {
			this.width = width;
		}
	}

	public void setHeight(int height) {
		if (height >= 0) {
			this.height = height;
		}
	}

	public void setBorderWidth(int borderWidth) {
		if (borderWidth >= 0) {
			this.borderWidth = borderWidth;
		}
	}

	public void setBorderColor(GlColor borderColor) {
		this.borderColor = borderColor;
	}

	public void setBackgroundColor(GlColor backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}
}
