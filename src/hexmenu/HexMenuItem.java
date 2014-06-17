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

	private static int width, height;
	private static Texture backgroundTex;
	private static GlColor backgroundColor;
	private static GlColor color = new GlColor(1,1,1);
	/** relative Position des Mittelpunktes */
	private float x, y;
	private Texture texture;
	private boolean drawBackground = true;

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
	public static void init(int width, int height, Texture empty, GlColor hexColor) {
		HexMenuItem.width = width;
		HexMenuItem.height = height;
		HexMenuItem.backgroundTex = empty;
		HexMenuItem.backgroundColor = hexColor;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public static void setHexColor(GlColor hexColor) {
		HexMenuItem.backgroundColor = hexColor;
	}

	public static void setColor(GlColor color) {
		HexMenuItem.color = color;
	}

	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}
	

	public void click() {
	}

	public boolean pick(float x, float y) {
		return 0.5 > Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y));
	}

	public void draw() {
		GL11.glPushMatrix();
		GL11.glTranslatef((x - 0.5f) * width, (y - 0.5f) * height, 0);
		if (backgroundTex != null && drawBackground) {
			backgroundColor.mult(color).bind();
			backgroundTex.bind();
			Renderer.drawTexture(backgroundTex, width, height);
		}
		if (texture != null) {
			color.bind();
			texture.bind();
			Renderer.drawTexture(texture, width, height);
		}
		GL11.glPopMatrix();
	}
}
