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
public class RootEffect extends Effect {

	public RootEffect(Resource res) {
		super(res);
	}
	
	@Override
	public void effect() {
		
		if(affectedResource.hasResource(Resource.DEAD_PLANTS) && Gaia.rand.nextDouble() > 0.9) {
			
			affectedResource.addResource(Resource.HUMUS, 1);
			affectedResource.addResource(Resource.DEAD_PLANTS, -1);
		}
		
		// a large amount of humus transforms into a terrain height unit
		if(affectedResource.getResource(Resource.HUMUS) > 255) {
			affectedResource.addResource(Resource.HUMUS, -250);
			affectedResource.addResource(Resource.TERRAIN, 1);
		}
		
		if(affectedResource.getResource(Resource.TERRAIN) < affectedResource.getEnvironment().getSeaLevel()) {
			
			if(!affectedResource.isSink()) {
				affectedResource.setResource(Resource.TERRAIN, affectedResource.getEnvironment().getSeaLevel());
			}
		}
		
		if(!affectedResource.isSink() && Gaia.rand.nextDouble() > 0.9) {
			affectedResource.addResource(Resource.TERRAIN, Gaia.rand.nextInt(3) - 1);
		}
	}
	
}
