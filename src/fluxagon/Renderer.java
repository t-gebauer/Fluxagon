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

	/** Schrift Objekt zum zeichnen von Text */
	public static TrueTypeFont standardFont;
	public static TrueTypeFont popupFont;
	/** Boolean flag on whether AntiAliasing is enabled or not */
	private static boolean antiAlias = true;

	public static void init() {
		Font awtFont = new Font("Verdana", Font.BOLD, 20);
		standardFont = new TrueTypeFont(awtFont, antiAlias);
		awtFont = new Font("Verdana", Font.ITALIC, 15);
		popupFont = new TrueTypeFont(awtFont, antiAlias);
	}

	public static void appendColor(double[] color) {
		appendColor(color, 1);
	}

	public static void appendColor(double[] color, double brightness) {
		glColor3d(brightness * color[0], brightness * color[1], brightness * color[2]);
	}

	/**
	 * Zeichnet einen Text
	 *
	 * @param x Position der linken oberen Ecke
	 * @param y Position der linken oberen Ecke
	 * @param text Der Text
	 * @param color Die Farbe des Textes
	 * @param background Gibt an, ob ein rechteckiger Hintergrund gezeichnet
	 * werden soll
	 * @param alignMid Gibt an, ob (x,y) der Mittelpunkt des Textes sein soll
	 */
	public static void drawText(int x, int y, String text, Color color,
			boolean background, boolean alignMid) {
		drawText(standardFont, x, y, text, color, background, alignMid);
	}

	public static void drawText(TrueTypeFont font, int x, int y, String text, Color color,
			boolean background, boolean alignMid) {
		int width = font.getWidth(text);
		int height = font.getHeight(text);
		if (alignMid) {
			x -= width / 2;
			y -= height / 2;
		}
		glPushMatrix();
		glLoadIdentity();
		glTranslatef(x, y, 0);
		if (background) {
			glColor4d(0.4, 0.4, 0.4, 0.4);
			// Hintergrund zeichnen
			drawQuad(width, height);
		}
		// Text zeichnen
		glEnable(GL_TEXTURE_2D);
		font.drawString(4, 0, text, color);
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
		glDisable(GL_TEXTURE_2D);
		glBegin(GL_QUADS);
		glVertex2i(0, 0);
		glVertex2i(0, height + 2);
		glVertex2i(width + 6, height + 2);
		glVertex2i(width + 6, 0);
		glEnd();
	}
	
	
}
