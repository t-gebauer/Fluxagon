/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fluxagon;

/**
 *
 * @author Timo
 */
public class Lane {

	private float in, out;
	private boolean inConnected, outConnected;

	public Lane() {
		in = 0;
		out = 0;
		inConnected = false;
		outConnected = false;
	}

	public boolean isInConnected() {
		return inConnected;
	}

	public void setInConnected(boolean inConnected) {
		this.inConnected = inConnected;
	}

	public boolean isOutConnected() {
		return outConnected;
	}

	public void setOutConnected(boolean outConnected) {
		this.outConnected = outConnected;
	}

	public float getIn() {
		return in;
	}

	public float getOut() {
		return out;
	}

	public float getInOut() {
		return in + out;
	}

	public void incIn(double amount) {
		incIn((float) amount);
	}

	public void incIn(float amount) {
		if (in + amount < 1) {
			in += amount;
		} else {
			in = 1;
		}
	}

	public void incOut(double amount) {
		incOut((float) amount);
	}

	public void incOut(float amount) {
		if (out + amount < 1) {
			out += amount;
		} else {
			out = 1;
		}
	}
}
