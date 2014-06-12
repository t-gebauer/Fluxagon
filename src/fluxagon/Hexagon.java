/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import java.util.Random;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Timo
 */
public class Hexagon implements Constants {

	private Lane[] lanes;
	private int row, column;
	private boolean connected;
	private double brightness = Math.random() * 0.5 + 0.5;
	private Fluxagon main = null;
	private float rotation; // momentane Rotation (in Grad)
	private float goalRotation; // gewollte Rotation (in Grad)

	public Hexagon(Fluxagon main, int row, int column) {
		this.main = main;
		this.row = row;
		this.column = column;
		this.connected = false;
		this.rotation = 0;
		lanes = new Lane[6];
		for (int i = 0; i < 6; i++) {
			if (Math.random() < LANE_PROBABILITY) {
				lanes[i] = new Lane();
			} else {
				lanes[i] = null;
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}

	private float getX() {
		float x = HEX_OFFSET_X + (column * 2 + 1) * HEX_WIDTH;
				// Jede zweite Reihe wird eingeschoben
		if (main.getMap().isIndentOdd() ^ row % 2 == 0) {
			x += HEX_WIDTH;
		}
		return x;
	}

	private float getY() {
		return HEX_OFFSET_Y + (row * 1.5f - 1) * HEX_HEIGHT
				- main.getMap().getAnimationOffset();
	}

	/**
	 * vergibt Punkte anhand der Anzahlder Lanes auf dem Hexagon
	 */
	public void connect() {
		if (!connected) {
			connected = true;
			// Lanes zählen
			int laneCount = 0;
			for (int i = 0; i < 6; i++) {
				if (lanes[i] != null) {
					laneCount++;
				}
			}
			if (laneCount > 0) {
				main.incScore(POINTS_PER_HEX[laneCount - 1]);
				new TextPopup(getX(), getY(),
						Integer.toString(POINTS_PER_HEX[laneCount - 1]));
			}
		}
	}

	/**
	 * Verbindet das Hexagon "von innen"
	 */
	public void connectMid() {
		connect();
		for (int i = 0;
				i < 6; i++) {
			if (lanes[i] != null) {
				lanes[i].setOutConnected(true);
			}
		}
	}

	/**
	 * Verbindet eine Lane des Hexagons von außen.
	 * <p>
	 * Die Nummern der Lanes sind gegen den Uhrzeigersinn verteilt; startend bei
	 * der linken unteren Ecke mit 0.
	 * <br>&nbsp/ 2&nbsp| 3&nbsp\
	 * <br>| 1&nbsp&nbsp|&nbsp 4&nbsp|
	 * <br>&nbsp\ 0&nbsp| 5&nbsp/
	 *
	 * @param num Die Nummer der Lane:
	 * <ul>
	 * <li>0: links-unten
	 * <li>1: links-mitte
	 * <li>2: links-oben
	 * <li>3: rechts-oben
	 * <li>4: rechts-mitte
	 * <li>5: rechts-unten
	 */
	public void connectLane(int num) {
		num = (num - Math.round(goalRotation / 60)) % 6;
		if (num < 0) {
			num = 6 + num;
		}
		try {
			if (lanes[num] != null) {
				connect();
				lanes[num].setInConnected(true);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Sollte niemals eintreten
			System.out.println("Index out of bounds: " + num);
		}
	}

	private void connectLane(HexSide side) {
		connectLane(side.ordinal());
	}

	private void createLane(HexSide side) {
		if (lanes[side.ordinal()] == null) {
			lanes[side.ordinal()] = new Lane();
		}
	}

	public int createWay(HexSide source, Random random) {
		if (source != null) {
			createLane(source);
		}
		if (row == ROW_COUNT - 1) {
			return column;
		}
		Hexagon hex = null;
		HexSide side = null;
		// Vorsicht! Endlosschleife möglich
		do {
			if (random.nextDouble() < 0.25) {
				side = HexSide.LEFT_MID;
			} else if (random.nextDouble() < 0.5) {
				side = HexSide.RIGHT_MID;
			} else if (random.nextDouble() < 0.75) {
				side = HexSide.LEFT_BOTTOM;
			} else {
				side = HexSide.RIGHT_BOTTOM;
			}
			hex = main.getMap().getHexRel(row, column, side);
		} while (hex == null);
		// Eigenen Weg erstellen
		createLane(side);
		// Verdrehen
		// solange es nicht um das Start-Hexagon geht
		if (!(row == ROW_COUNT / 4 && column == COLUMN_COUNT / 2)) {
			rotation += random.nextInt(6) * 60;
			goalRotation = rotation;
		}
		return hex.createWay(side.opposite(), random);
	}

	/**
	 * Update der Game-Mechanik (Strom-Ausbreitung usw.)
	 */
	public void update() {
		// Strom Ausbreitung
		if (connected) {
			for (int i = 0; i < 6; i++) {
				Lane l = lanes[i];
				if (l == null) {
					continue;
				}
				if (l.isInConnected() && l.getInOut() < 1) {
					main.incScore(0.03125);
					l.incIn(BASE_FLUX_SPEED + main.getLevel() * LEVEL_FLUX_SPEED);
					if (l.getIn() == 1) {
						// alle anderen Triangles "infizieren"
						connectMid();
					}
				}
				if (l.isOutConnected() && l.getInOut() < 1) {
					main.incScore(0.03125);
					l.incOut(BASE_FLUX_SPEED + main.getLevel() * LEVEL_FLUX_SPEED);
					if (l.getOut() == 1) {
						int num = (i + Math.round(goalRotation / 60)) % 6;
						if (num < 0) {
							num = 6 + num;
						}
						HexSide side = HexSide.fromValue(num);
						Hexagon hex = main.getMap().getHexRel(row, column, side);
						if (hex != null) {
							hex.connectLane(side.opposite());
						}
					}
				}
			}
		}
	}

	/**
	 * Rotiert das Hexagon im Uhrzeigersinn um 60 Grad
	 */
	public void rotateCW() {
		if (!connected) {
			goalRotation += 60;
		}
	}

	/**
	 * Rotiert das Hexagon gegen den Uhrzeigersinn um 60 Grad
	 */
	public void rotateCCW() {
		if (!connected) {
			goalRotation -= 60;
		}
	}

	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Update der Animations-Mechanik (Drehung)
	 */
	public void animate() {
		// Drehen
		float dif = goalRotation - rotation;
		if (dif < 0) {
			dif = -dif;
			if (dif < HEX_ROTATION_DIST) {
				rotation -= dif;
			} else {
				rotation -= HEX_ROTATION_DIST;
			}
		} else if (dif > 0) {
			if (dif < HEX_ROTATION_DIST) {
				rotation += dif;
			} else {
				rotation += HEX_ROTATION_DIST;
			}
		} else if (rotation >= 360 || rotation <= 360) {
			rotation %= 360;
			goalRotation = rotation;
		}

		// Farbe leicht verändern
		if (Math.random() > 0.5) {
			if (brightness < 0.9)
			brightness += 0.01;
		} else {
			if (brightness > 0.3)
			brightness -= 0.01;
		}
	}

	/**
	 * Zeichnen des Hexagons, sowie der dazugehörigen Lanes
	 */
	public void render() {
		float x = getX();
		float y = getY();

		// Farbe errechnen
		COLOR_HEXAGON[main.getHexColorIndex()].
				mult(main.getFadePercent()).
				add(COLOR_HEXAGON[main.getOldHexColorIndex()].
				mult(1-main.getFadePercent())).
				mult(brightness).bind();
		
		// draw rotating hexagons in the background
		glLoadIdentity();
		if (rotation != goalRotation) {
			glTranslatef(x, y, 1);
		} else {
			glTranslatef(x, y, 0);
		}
		
		// draw hexagon (or Circle)
		glBegin(GL_POLYGON);
		if (main.getCircleMode()) {
			glVertex2f(0, 0);
			for (int i = 0; i < 360; i++) {
				glVertex2f((float) Math.sin(i) * HEX_WIDTH,
						(float) Math.cos(i) * HEX_WIDTH);
			}
		} else {
			for (int i = 0; i < 6; i++) {
				glVertex2f((float) Math.sin(i * Math.PI / 3) * HEX_HEIGHT,
						(float) Math.cos(i * Math.PI / 3) * HEX_HEIGHT);
			}
		}
		glEnd();
		
		// apply rotation only to lanes
		glRotatef(rotation, 0, 0, 1);

		// Lanes
		Lane l;
		// left-bottom
		l = lanes[0];
		if (l != null) {
			drawLineBackgroundAndPercents(0, 0,
					-HEX_WIDTH * 0.5f, HEX_HEIGHT * 0.75f,
					l.getOut(), l.getIn());
		}
		// left-mid
		l = lanes[1];
		if (l != null) {
			drawLineBackgroundAndPercents(0, 0,
					-HEX_WIDTH, 0,
					l.getOut(), l.getIn());
		}
		// left-top
		l = lanes[2];
		if (l != null) {
			drawLineBackgroundAndPercents(0, 0,
					-HEX_WIDTH * 0.5f, -HEX_HEIGHT * 0.75f,
					l.getOut(), l.getIn());
		}
		// right-top
		l = lanes[3];
		if (l != null) {
			drawLineBackgroundAndPercents(0, 0,
					HEX_WIDTH * 0.5f, -HEX_HEIGHT * 0.75f,
					l.getOut(), l.getIn());
		}
		// right-mid
		l = lanes[4];
		if (l != null) {
			drawLineBackgroundAndPercents(0, 0,
					HEX_WIDTH, 0,
					l.getOut(), l.getIn());
		}
		// right-bottom
		l = lanes[5];
		if (l != null) {
			drawLineBackgroundAndPercents(0, 0,
					HEX_WIDTH * 0.5f, HEX_HEIGHT * 0.75f,
					l.getOut(), l.getIn());
		}
	}

	/*
	 * Zeichnet eine Linie
	 * von dem Punkt (x,y) in Richtung (dx,dy)
	 */
	private void drawLine(float x, float y, float dx, float dy) {
		drawPercentLine(x, y, dx, dy, 1);
	}

	/*
	 * Zeichnet einen Prozentteil einer Linie
	 * von dem Punkt (x,y) in Richtung (dx,dy)
	 */
	private void drawPercentLine(float x, float y, float dx, float dy, float percent) {
		glBegin(GL_LINES);
		glVertex2f(x, y);
		glVertex2f(x + dx * percent, y + dy * percent);
		glEnd();
	}

	/*
	 * Draws a line from (x,y) to (x+dx,y+dy) with a Background and
	 * connected from (x,y) with pc1 percent and from (x+dx,y+dy) with pc2
	 */
	private void drawLineBackgroundAndPercents(float x, float y, float dx, float dy,
			float pc1, float pc2) {
		// background
		COLOR_LINE_BG.bind();
		drawLine(x, y, dx, dy);
		// gefüllte Linien
		COLOR_LINE_FG[main.getHexColorIndex()].mult(main.getFadePercent()).
				add(COLOR_LINE_FG[main.getOldHexColorIndex()].
				mult(1-main.getFadePercent())).bind();
		if (pc1 > 0) {
			// Linie von innen nach außen
			drawPercentLine(x, y, dx, dy, pc1);
		}
		if (pc2 > 0) {
			// entgegengerichtete Linie (von außen)
			drawPercentLine(
					x + dx, y + dy,
					-1 * dx, -1 * dy,
					pc2);
		}
	}
}
