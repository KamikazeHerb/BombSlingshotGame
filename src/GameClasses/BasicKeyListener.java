package GameClasses;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class BasicKeyListener extends KeyAdapter {
	/* Author: Michael Fairbank, Adapted by Patrick O'Dell
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	private static boolean detonateKeyPressed;

	public static boolean isDetonateKeyPressed() {
		return detonateKeyPressed;
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		switch (key) {
		case KeyEvent.VK_SPACE:
			detonateKeyPressed =true;
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		switch (key) {
		case KeyEvent.VK_SPACE:
			detonateKeyPressed =false;
			break;
		}
	}
}
