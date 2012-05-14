/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.Effect;
import com.morgner.gaia.Resource;
import com.morgner.gaia.util.IntColor;
import java.util.Collection;

/**
 *
 * @author Christian Morgner
 */
public abstract class Structure {

	protected Resource resource = null;
	protected boolean hover = false;
	
	public Structure(Resource resource, int x, int y) {
		this.resource = resource;
	}

	public abstract IntColor getColor(IntColor color, double stepNum, double stepWidth, double stepHeight, int i, int j);
	public abstract void update(Collection<Effect> effects, final long dt);
}
