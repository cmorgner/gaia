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
		
		if(affectedResource.hasResource("deadPlants") && Gaia.rand.nextDouble() > 0.9) {
			
			affectedResource.addResource("humus", 1);
			affectedResource.addResource("deadPlants", -1);
		}
		
		// a large amount of humus transforms into a terrain height unit
		if(affectedResource.getResource("humus") > 255) {
			affectedResource.addResource("humus", -250);
			affectedResource.addTerrain(1);
		}
		
		if(affectedResource.getTerrain() < affectedResource.getEnvironment().getSeaLevel()) {
			
			if(!affectedResource.isSink()) {
				affectedResource.setTerrain(affectedResource.getEnvironment().getSeaLevel());
			}
		}
		
		if(!affectedResource.isSink() && Gaia.rand.nextDouble() > 0.9) {
			affectedResource.addTerrain(Gaia.rand.nextInt(3) - 1);
		}
	}
	
}
