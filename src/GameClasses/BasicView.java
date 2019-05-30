package GameClasses;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

public class BasicView extends JComponent {
	/* Author: Michael Fairbank
	 * Edited by Patrick O'Dell
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	// background colour
	public static final Color BG_COLOR = Color.BLACK;

	private Game game;

	public BasicView(Game game) {
		this.game = game;
	}
	
	@Override
	public void paintComponent(Graphics g0) {
		Game game;
		synchronized(this) {
			game=this.game;
		}
		Graphics2D g = (Graphics2D) g0;
		// paint the background
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		for (Shape2D s : game.shapes)
			s.draw(g);
		for (Shape2D s : game.shrapnel)
			s.draw(g);
		for (CatapultRope c : game.connectors)
			c.draw(g);
		for (StraightBarrier b : game.barriers)
			b.draw(g);
		for(Bomb b : game.bombs)
			b.draw(g);
		for(Shape2D o : game.objectives)
			o.draw(g);
		for(ScreenText text : game.messages)
			text.paint(g);
		for(Rectangle block : game.movingBlocks)
			block.draw(g);
		for(Vortex v : game.vortexes)
			v.draw(g);
	}


	@Override
	public Dimension getPreferredSize() {
		return Game.FRAME_SIZE;
	}
	
	public synchronized void updateGame(Game game) {
		this.game=game;
	}
}