package com.morgner.gaia;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

/**
 *
 * @author Christian Morgner
 */
public class Canvas extends java.awt.Canvas {

	private BufferStrategy bufferStrategy = null;
	private Environment env = null;
	private int w = 0;
	private int h = 0;
	
	public Canvas(Environment env) {
		this.env = env;
		this.w = getWidth();
		this.h = getHeight();
	}
	
	public void initialize() {
		this.bufferStrategy = getBufferStrategy();
	}
	
	public void paint()
	{
		Graphics2D g = (Graphics2D)bufferStrategy.getDrawGraphics();
	
		// draw environment
		env.draw(g);
		
		g.dispose();
		
		bufferStrategy.show();
	}
}
