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
public abstract class PowerConsumer extends Wire {

	private int threshold = 100;
	private int resourceId = Resource.WATER;
	private int productionAmount = 1;
	private double resistance = 20.0;
	
	public PowerConsumer(Resource resource, int producingResource, int productionAmount, int runThreshold, double resistance) {
		super(resource);
		
		this.resourceId = producingResource;
		this.productionAmount = productionAmount;
		this.threshold = runThreshold;
		this.resistance = resistance;
	}

	@Override
	public void update(Collection<Effect> effects, final long dt) {

		int max = 0;
		
		for(Resource n : resource.getNeighbours(true)) {
		
			if(n.hasStructure() && n.getStructure() instanceof Wire) {
				int val = n.getResource(Resource.CURRENT);
				if(val > max) {
					max = val;
				}
			}
		}
		
		resource.setResource(Resource.RESISTANCE, FastMath.rint(resistance));

		if(max >= threshold) {
			
			effects.add(new Effect(resource) {
				@Override public void effect() {
					affectedResource.addResource(resourceId, productionAmount);
				}
			});
		}
	}
	
}
