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

	public ErosionEffect(Resource resource) {
		
		super(resource);
	}
	
	@Override
	public void effect() {

		if(affectedResource.getType() != 0) {
			return;
		}

		if(affectedResource.isSink()) {
			return;
		}
		
		if(affectedResource.hasResource(Resource.PLANTS)) {
			return;
		}

		int flow = affectedResource.getResource(Resource.FLOW);
		if(flow > 0 ) {

			if(flow < 8) {
				
				// erosion
				if(Gaia.rand.nextDouble() > 0.99) {
					affectedResource.addResource(Resource.TERRAIN, -1);
				}
			}
		}
	}
}
