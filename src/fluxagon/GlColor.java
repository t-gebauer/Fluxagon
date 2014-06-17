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

	public static GlColor fromByte(int r, int g, int b) {
		return new GlColor(r / 255.0, g / 255.0, b / 255.0);
	}

	public static GlColor fromByte(int r, int g, int b, int a) {
		return new GlColor(r / 255.0, g / 255.0, b / 255.0, a / 255.0);
	}

	public static GlColor fromHex(int hexValue) {
		return fromByte(
				(hexValue & 0xff0000) / 0x010000,
				(hexValue & 0x00ff00) / 0x000100,
				(hexValue & 0x0000ff) / 0x000001);
	}

	public static GlColor fromHex(long hexValue) {
		return fromByte(
				(int) (hexValue / 0x00010000) & 0xff,
				(int) (hexValue / 0x00000100) & 0xff,
				(int) (hexValue / 0x00000001) & 0xff,
				(int) (hexValue / 0x01000000) & 0xff);
	}

	public static GlColor white() {
		return new GlColor(1, 1, 1, 1);
	}

	public void bind() {
		GL11.glColor4d(r, g, b, a);
	}

	public GlColor mult(double d) {
		return new GlColor(r * d, g * d, b * d, a);
	}

	public GlColor mult(GlColor color) {
		return new GlColor(r * color.r, g * color.g, b * color.b, a * color.a);
	}

	public GlColor add(GlColor clr) {
		return new GlColor(r + clr.r, g + clr.g, b + clr.b, a + clr.a);
	}
}
