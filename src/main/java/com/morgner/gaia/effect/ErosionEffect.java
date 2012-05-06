/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.effect;

import com.morgner.gaia.Gaia;
import com.morgner.gaia.Resource;
import com.morgner.gaia.Effect;

/**
 *
 * @author Christian Morgner
 */
public class ErosionEffect extends Effect {

	private int amount = 0;
	
	public ErosionEffect(Resource resource, int amount) {
		
		super(resource);

		this.amount = amount;
	}
	
	@Override
	public void effect() {

		if(affectedResource.getType() != 0) {
			return;
		}

		if(affectedResource.isSink()) {
			return;
		}
		
		if(affectedResource.hasResource("plants")) {
			return;
		}
		
		// erosion
		if(amount > 0) {

			// erosion
			if(Gaia.rand.nextDouble() > 0.99) {
				affectedResource.addTerrain(-1);
			}

		} else {

			// sedimentation
			if(Gaia.rand.nextDouble() > 0.99) {
				affectedResource.addTerrain(1);
			}
		}
	}
}
