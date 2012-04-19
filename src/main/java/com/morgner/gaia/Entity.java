package com.morgner.gaia;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

/**
 *
 * @author Christian Morgner
 */
public interface Entity {
	
	public Color getCellColor();
	public List<Effect> update(long dt);
	public int getX();
	public int getY();
	public boolean contains(Point p);
}
