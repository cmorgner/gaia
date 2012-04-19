package com.morgner.gaia;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

/**
 *
 * @author Christian Morgner
 */
public class Canvas extends java.awt.Canvas {

	private Environment env = null;
	
	public Canvas(Environment env) {
		this.env = env;
	}
	
	public void paint()
	{
		BufferStrategy bs = this.getBufferStrategy();
		Graphics g = bs.getDrawGraphics();
		
		// draw environment
		env.draw(g);
		
		g.dispose();
		
		bs.show();
	}
}
