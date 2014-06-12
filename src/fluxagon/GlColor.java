/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import org.lwjgl.opengl.GL11;

/**
 *
 * @author Timo
 */
public class GlColor {

	public double r, g, b, a;

	public GlColor(double r, double g, double b, double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public GlColor(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1;
	}

	public static GlColor from255(int r, int g, int b) {
		return new GlColor(r / 255.0, g / 255.0, b / 255.0);
	}

	public void bind() {
		GL11.glColor4d(r, g, b, a);
	}

	public GlColor mult(double d) {
		return new GlColor(r * d, g * d, b * d, a);
	}

	public GlColor add(GlColor clr) {
		return new GlColor(r + clr.r, g + clr.g, b + clr.b, a + clr.a);
	}
}
