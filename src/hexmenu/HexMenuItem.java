/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexmenu;

import fluxagon.GlColor;
import fluxagon.Renderer;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Timo
 */
public class HexMenuItem {

	private int width, height;
	private static Texture backgroundTex;
	private GlColor backgroundColor = new GlColor(1, 1, 1);
	private GlColor color = new GlColor(1, 1, 1);
	/** relative Position des Mittelpunktes */
	private float x, y;
	private Texture texture;
	private boolean drawBackground = true;
	private int rotation, goalRotation, clickCount;

	public HexMenuItem(float x, float y, Texture texture) {
		this.x = x;
		this.y = y;
		this.texture = texture;
	}

	public HexMenuItem(float x, float y, Texture texture, boolean background) {
		this.x = x;
		this.y = y;
		this.texture = texture;
		this.drawBackground = background;
	}

	public static void init(Texture empty) {
		HexMenuItem.backgroundTex = empty;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public void setHexColor(GlColor hexColor) {
		this.backgroundColor = hexColor;
	}

	public void setColor(GlColor color) {
		this.color = color;
	}

	public GlColor getBackgroundColor() {
		return backgroundColor;
	}

	public GlColor getColor() {
		return color;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void click() {
	}

	public void rightClick() {
		setRotation(360);
	}

	public boolean pick(float x, float y) {
		return 0.5 > Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y));
	}

	public void draw() {
		GL11.glPushMatrix();
		GL11.glTranslatef((x - 0.5f) * width, (y - 0.5f) * height, 0);

		if (backgroundTex != null && drawBackground) {
			backgroundColor.mult(color).bind();
			Renderer.drawTexture(backgroundTex, width, height);
		}
		if (texture != null) {
			GL11.glTranslatef(0.5f * width, 0.5f * height, 0);
			GL11.glRotatef(rotation, 0, 0, 1);
			GL11.glTranslatef(-0.5f * width, -0.5f * height, 0);
			color.bind();
			Renderer.drawTexture(texture, width, height);
		}
		GL11.glPopMatrix();
	}

	public void setRotation(int rot) {
		goalRotation = rot;
	}

	public void animate(int time) {
		// Drehen
		float dif = goalRotation - rotation;
		final float rotDistance = time;
		if (dif < 0) {
			dif = -dif;
			if (dif < rotDistance) {
				rotation -= dif;
			} else {
				rotation -= rotDistance;
			}
		} else if (dif > 0) {
			if (dif < rotDistance) {
				rotation += dif;
			} else {
				rotation += rotDistance;
			}
		} else if (rotation >= 360 || rotation <= 360) {
			rotation %= 360;
			goalRotation = rotation;
		}
	}

	public void mouseOver() {
	}
}
