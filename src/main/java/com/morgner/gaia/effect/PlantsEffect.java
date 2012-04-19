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
public class PlantsEffect extends Effect {

	public PlantsEffect(Resource res) {
		super(res);
	}
	
	@Override
	public Effect effect() {
		
		int neighboursWithPlants = affectedResource.getResource("humus") / 8;
		int neighboursWithWater = 0;

		for(Resource n : affectedResource.getNeighbours()) {
			if(n.hasWater()) {
				neighboursWithWater++;
			}
			if(n.hasResource("plants")) {
				neighboursWithPlants++;
			}
		}

		int plantsMaxAge = affectedResource.getResource("plantsMaxAge");
		int plantsAge = affectedResource.getResource("plantsAge");

		// plants die after max age has been reached
		if(plantsMaxAge > 0) {

			if(plantsAge > plantsMaxAge) {

				affectedResource.setResource("plantsMaxAge", 0);
				affectedResource.setResource("plantsAge", 0);
				affectedResource.setResource("plants", 0);

				affectedResource.addResource("deadPlants");

				return null;

			} else {

				affectedResource.addResource("plantsAge");
			}
		}


		if(neighboursWithWater >= 3 || neighboursWithPlants >= 3) {

			int existingPlants = affectedResource.getResource("plants");

			if(!affectedResource.hasResource("fire")) {

				if(existingPlants == 0) {

					// plant will be added, set randomized maximum age
					affectedResource.setResource("plantsMaxAge", Gaia.rand.nextInt(1000));
				}

				if(existingPlants < 25) {
					affectedResource.addResource("plants", 1);
				}
			}

		} else {

			affectedResource.addResource("plants", -1);
		}
		
		return null;
	}
	
}
