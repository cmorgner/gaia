/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.Effect;
import com.morgner.gaia.Resource;
import com.morgner.gaia.util.FastMath;
import com.morgner.gaia.util.IntColor;
import java.util.Collection;

/**
 *
 * @author Christian Morgner
 */
public class PowerPlant extends Wire {
	
	public PowerPlant(Resource resource) {
		super(resource);
	}

	@Override
	public IntColor getColor(IntColor color, double stepNum, double stepWidth, double stepHeight, int i, int j) {
		return new IntColor(128, 128, 128, 255);
	}
	
	@Override
	public void update(Collection<Effect> effects, final long dt) {

		double count = 0;
		
		for(Resource n : resource.getNeighbours(true)) {
		
			if(n.hasStructure() && n.getStructure() instanceof Wire) {
				count++;
			}
		}
		
		if(count == 0) {
			count = 1;
		}
		
		resource.setResource(Resource.CURRENT, FastMath.rint((1000 * resource.getEnvironment().getWaterInterpolationFactor()) / count));
	}
}
