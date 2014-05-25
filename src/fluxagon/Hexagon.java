/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Timo
 */
public class Hexagon implements Constants {

//	private Triangle[] lanes;
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

	public void connect() {
		connected = true;
		for (int i = 0; i < 6; i++) {
			if (lanes[i] != null) {
				lanes[i].setOutConnected(true);
			}
		}
	}

	public void connectLane(int num) {
		num = (num - Math.round(goalRotation / 60)) % 6;
		if (num < 0) {
			num = 6 + num;
		}
		try {
			if (lanes[num] != null) {
				connected = true;
				lanes[num].setInConnected(true);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Index out of bounds: " + num);
		}
	}

	public void update() {
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
			brightness += 0.01;
		} else {
			brightness -= 0.01;
		}

		// Strom Ausbreitung
		// (nicht fließen lassen, wenn rotierend, oder gar nicht connected)
		if (connected && goalRotation == rotation) {
			for (int i = 0; i < 6; i++) {
				Lane l = lanes[i];
				if (l == null) {
					continue;
				}
				if (l.isInConnected() && l.getInOut() < 1) {
					main.incScore(1);
					l.incIn();
					if (l.getIn() == 1) {
						// alle anderen Triangles "infizieren"
						for (int j = 0; j < 6; j++) {
							if (lanes[j] != null && lanes[j].getInOut() < 1) {
								lanes[j].setOutConnected(true);
							}
						}
					}
				}
				if (l.isOutConnected() && l.getInOut() < 1) {
					main.incScore(1);
					l.incOut();
					if (l.getOut() == 1) {
						int num = (i + Math.round(goalRotation / 60)) % 6;
						if (num < 0) {
							num = 6 + num;
						}
						try {
							switch (num) {
								case 0:
									if (row % 2 == 0 ^ !main.isIndent_odd()) {
										main.getHexagons()[row + 1][column - 1].connectLane(3);
									} else {
										main.getHexagons()[row + 1][column].connectLane(3);
									}
									break;
								case 1:
									main.getHexagons()[row][column - 1].connectLane(4);
									break;
								case 2:
									if (row % 2 == 0 ^ !main.isIndent_odd()) {
										main.getHexagons()[row - 1][column - 1].connectLane(5);
									} else {
										main.getHexagons()[row - 1][column].connectLane(5);
									}
									break;
								case 3:
									if (row % 2 == 0 ^ !main.isIndent_odd()) {
										main.getHexagons()[row - 1][column].connectLane(0);
									} else {
										main.getHexagons()[row - 1][column + 1].connectLane(0);
									}
									break;
								case 4:
									main.getHexagons()[row][column + 1].connectLane(1);
									break;
								case 5:
									if (row % 2 == 0 ^ !main.isIndent_odd()) {
										main.getHexagons()[row + 1][column].connectLane(2);
									} else {
										main.getHexagons()[row + 1][column + 1].connectLane(2);
									}
									break;
							}
						} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
						}
					}
				}
			}
		}
	}

	public void rotateCW() {
		if (!connected) {
			goalRotation += 60;
		}
	}

	public void rotateCCW() {
		if (!connected) {
			goalRotation -= 60;
		}
	}

	public void setRow(int row) {
		this.row = row;
	}

	// zeichnet das Hexagon
	public void draw() {

		float x = HEX_OFFSET_X + (column * 2 + 1) * HEX_WIDTH;
		float y = HEX_OFFSET_Y + (row * 1.5f - 1) * HEX_HEIGHT - main.getAnimation_offset();

		// Jede zweite Reihe wird eingeschoben
		if (main.isIndent_odd() ^ row % 2 == 0) {
			x += HEX_WIDTH;
		}

		main.appendColor(COLOR_HEXAGON, brightness);

		glLoadIdentity();
		glTranslatef(x, y, 0);
		glRotatef(rotation, 0, 0, 1);

		glBegin(GL_POLYGON);
		glVertex2f(0, 0 - HEX_HEIGHT);
		glVertex2f(0 - HEX_WIDTH, 0 - HEX_HEIGHT / 2f);
		glVertex2f(0 - HEX_WIDTH, 0 + HEX_HEIGHT / 2f);
		glVertex2f(0, 0 + HEX_HEIGHT);
		glVertex2f(0 + HEX_WIDTH, 0 + HEX_HEIGHT / 2f);
		glVertex2f(0 + HEX_WIDTH, 0 - HEX_HEIGHT / 2f);
		glEnd();

		// Wege
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
		main.appendColor(COLOR_LINE_BG);
		drawLine(x, y, dx, dy);
		// gefüllte Linien
		main.appendColor(COLOR_LINE_FG);
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

	public void drawLines() {
		/*float x = HEX_OFFSET + column * (2 * HEX_WIDTH + HEX_OFFSET);
		 float y = main.y_offset + row * (1.5f * HEX_HEIGHT + HEX_OFFSET);
		 if (row % 2 != 0) {
		 x += HEX_WIDTH;
		 }
		 */
	}
}
