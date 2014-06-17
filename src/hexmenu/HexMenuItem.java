/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexmenu;

import fluxagon.Renderer;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Timo
 */
public class HexMenuItem {

	public static int width, height;
	/** relative Position des Mittelpunktes */
	private float x, y;
	private Texture texture;

	public HexMenuItem(float x, float y, Texture texture) {
		this.x = x;
		this.y = y;
		this.texture = texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public void click() {
	}

	public boolean pick(float x, float y) {
		return 0.5 > Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y));
	}

	public void draw() {
		texture.bind();
		GL11.glPushMatrix();
		GL11.glTranslatef((x - 0.5f) * width, (y - 0.5f) * height, 0);
		Renderer.drawTexture(texture, width, height);
		GL11.glPopMatrix();
	}
}
