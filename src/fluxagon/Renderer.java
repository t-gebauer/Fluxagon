/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.awt.Font;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

/**
 *
 * @author Timo
 */
public class Renderer {

	/** Schrift zum zeichnen von Text */
	private static TrueTypeFont[] fonts;
	private static int fontIndex = 0;
	private static Color fontColor = Color.white;
	/** Boolean flag on whether AntiAliasing is enabled or not */
	private static boolean antiAlias = true;
	/** background Color */
	private static GlColor backgroundColor = new GlColor(0.3, 0.3, 0.3, 0.5);

	public static void init(Font[] fonts) {
		System.out.println("Renderer -- Loading fonts");
		Renderer.fonts = new TrueTypeFont[fonts.length];
		for (int i = 0; i < fonts.length; i++) {
			Renderer.fonts[i] = new TrueTypeFont(fonts[i], antiAlias);
		}
		System.out.println("Renderer -- Finished");
	}

	public static void setFont(int fontIndex) {
		Renderer.fontIndex = fontIndex;
	}

	public static void setFontColor(Color fontColor) {
		Renderer.fontColor = fontColor;
	}

	/**
	 * Zeichnet einen Text
	 */
	public static void drawText(String text) {
		drawText(text, false, false);
	}

	public static void drawText(String text, boolean center) {
		drawText(text, center, false);
	}

	public static void drawText(String text, boolean center, boolean background) {
		int width = fonts[fontIndex].getWidth(text);
		int height = fonts[fontIndex].getHeight(text);
		if (center) {
			int x = 0;
			int y = 0;
			x -= width / 2;
			y -= height / 2;
			glPushMatrix();
			glTranslatef(x, y, 0);
		}
		// Hintergrund zeichnen
		if (background) {
			backgroundColor.bind();
			drawQuad(width, height);
		}
		// Text zeichnen
		boolean glTexEnabled = glIsEnabled(GL_TEXTURE_2D);
		if (!glTexEnabled) {
			glEnable(GL_TEXTURE_2D);
		}
		fonts[fontIndex].drawString(0, 0, text, fontColor);
		if (!glTexEnabled) {
			glDisable(GL_TEXTURE_2D);
		}
		if (center) {
			glPopMatrix();
		}
	}
	
	public static void drawText(String text, int x, int y) {
		drawText(text, x, y, false, false);
	}
	
	public static void drawText(String text, int x, int y, boolean center) {
		drawText(text, x, y, center, false);
	}

	public static void drawText(String text, int x, int y, boolean center,
			boolean background) {
		glPushMatrix();
		glTranslatef(x, y, 0);
		drawText(text, center, background);
		glPopMatrix();
	}

	/**
	 * Zeichnet ein Viereck mit der linken oberen Ecke im aktuellen Koordinaten-
	 * Ursprung
	 *
	 * @param width Weite des Rechtecks
	 * @param height Höhe des Rechtecks
	 */
	public static void drawQuad(int width, int height) {
		backgroundColor.bind();
		glBegin(GL_QUADS);
		glVertex2i(0, 0);
		glVertex2i(0, height);
		glVertex2i(width, height);
		glVertex2i(width, 0);
		glEnd();
	}
}
