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
public class HydroPlant extends Wire {
	
	public HydroPlant(Resource resource) {
		super(resource);
	}

	@Override
	public IntColor getColor(IntColor color, double stepNum, double stepWidth, double stepHeight, int i, int j) {
		return new IntColor(0, 0, 180, 255);
	}
	
	@Override
	public void update(Collection<Effect> effects, final long dt) {
		
		int val = resource.getResource(Resource.FLOW) * 100;
		val = Math.max(val, 200);
		
		resource.setResource(Resource.CURRENT, val);
	}
}
