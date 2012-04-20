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
	public Effect effect() {
		
		if(affectedResource.hasResource("fire")) {

			if(affectedResource.hasResource("plants")) {

				for(Resource n : affectedResource.getNeighbours()) {
					
					if(n.hasResource("plants") && Gaia.rand.nextDouble() > 0.75) {
						n.setResource("fire", 1);
					}
				}
				
				affectedResource.addResource("plants", -5);
				
				if(!affectedResource.hasResource("plants")) {
					affectedResource.addResource("ashes", 1);
				}
				
			} else {
				
				affectedResource.setResource("fire", 0);
			}
		}
		
		return null;
	}
}
