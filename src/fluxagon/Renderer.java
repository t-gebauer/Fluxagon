package fluxagon;

import java.awt.Font;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Timo Gebauer
 */
public class Renderer {

	/** Schrift zum zeichnen von Text */
	private static TrueTypeFont[] fonts;
	private static int fontIndex = 0;
	private static Color fontColor = Color.white;
	/** Boolean flag on whether AntiAliasing is enabled or not */
	private static final boolean antiAlias = true;
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
		drawText(text, 0, 0, false);
	}

	public static void drawText(String text, boolean background) {
		drawText(text, 0, 0, background);
	}

	public static void drawText(String text, float cX, float cY) {
		drawText(text, cX, cY, false);
	}

	public static void drawText(String text, float cX, float cY, boolean background) {
		int width = fonts[fontIndex].getWidth(text);
		int height = fonts[fontIndex].getHeight(text);
		glPushMatrix();
		glTranslatef(-cX * width, -cY * height, 0);
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
		glPopMatrix();
	}

	public static void drawText(int x, int y, String text) {
		drawText(x, y, text, 0, 0, false);
	}

	public static void drawText(int x, int y, String text, boolean background) {
		drawText(x, y, text, 0, 0, background);
	}

	public static void drawText(int x, int y, String text, float cX, float cY) {
		drawText(x, y, text, cX, cY, false);
	}

	public static void drawText(int x, int y, String text, float cX, float cY,
			boolean background) {
		glPushMatrix();
		glLoadIdentity();
		glTranslatef(x, y, 0);
		drawText(text, cX, cY, background);
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
		if (glIsEnabled(GL_TEXTURE_2D)) {
			glDisable(GL_TEXTURE_2D);
		}
		glBegin(GL_QUADS);
		glVertex2i(0, 0);
		glVertex2i(0, height);
		glVertex2i(width, height);
		glVertex2i(width, 0);
		glEnd();
	}

	public static void drawTexture(Texture tex) {
		drawTexture(tex, -1, -1);
	}

	public static void drawTexture(Texture tex, int width, int height) {
		if (!glIsEnabled(GL_TEXTURE_2D)) {
			glEnable(GL_TEXTURE_2D);
		}
		if (width < 0) {
			width = tex.getImageWidth();
		}
		if (height < 0) {
			height = tex.getImageHeight();
		}
		
		tex.bind();
		
		float right = tex.getImageWidth() / (float) tex.getTextureWidth();
		float bottom = tex.getImageHeight() / (float) tex.getTextureHeight();
		
		glBegin(GL_QUADS);
		glTexCoord2f(0, 0);
		glVertex2f(0, 0);
		glTexCoord2f(right, 0);
		glVertex2f(width, 0);
		glTexCoord2f(right, bottom);
		glVertex2f(width, height);
		glTexCoord2f(0, bottom);
		glVertex2f(0, height);
		glEnd();
	}
}
