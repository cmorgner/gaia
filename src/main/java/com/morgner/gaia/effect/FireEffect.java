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
public class FireEffect extends Effect{
	
	public FireEffect(Resource res) {
		super(res);
	}

	@Override
	public void effect() {
		
		if(affectedResource.hasResource(Resource.FIRE)) {

			if(affectedResource.hasResource(Resource.PLANTS)) {

				for(Resource n : affectedResource.getNeighbours()) {
					
					if(n.hasResource(Resource.PLANTS) && Gaia.rand.nextDouble() > 0.75) {
						n.setResource(Resource.FIRE, 1);
					}
				}
				
				affectedResource.addResource(Resource.PLANTS, -5);
				
				if(!affectedResource.hasResource(Resource.PLANTS)) {
					affectedResource.addResource(Resource.ASH, 1);
				}
				
			} else {
				
				affectedResource.setResource(Resource.FIRE, 0);
			}
		}
	}
}
