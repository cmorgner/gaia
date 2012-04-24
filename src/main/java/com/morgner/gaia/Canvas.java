package com.morgner.gaia;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

/**
 *
 * @author Christian Morgner
 */
public class Canvas extends java.awt.Canvas {

	private Environment env = null;
	private int w = 0;
	private int h = 0;
	
	public Canvas(Environment env) {
		this.env = env;
		this.w = getWidth();
		this.h = getHeight();
	}
	
	public void paint()
	{
		BufferStrategy bs = this.getBufferStrategy();
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		g.clearRect(0, 0, w, h);

		// draw environment
		env.draw(g);
		
		g.dispose();
		
		bs.show();
	}
}
