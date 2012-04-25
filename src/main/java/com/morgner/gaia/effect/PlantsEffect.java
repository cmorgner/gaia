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
		
		if(!affectedResource.hasResource("moisture")) {
			return null;
		}
		
		int neighboursWithPlants = affectedResource.getResource("humus") / 8;
		int neighboursWithWater = 0;

		for(Resource n : affectedResource.getNeighbours()) {

			if(n.hasWater()) {
				neighboursWithWater++;
			}

			if(n.hasResource("plants")) { // && n.getTerrain() >= affectedResource.getTerrain() + 5) {
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


		if(neighboursWithWater == 4 || neighboursWithPlants >= 2) {

			int existingPlants = affectedResource.getResource("plants");

			if(!affectedResource.hasResource("fire")) {

				if(Gaia.rand.nextDouble() > Math.pow(0.99, affectedResource.getEnvironment().getPlantsFactor())) {

					if(existingPlants == 0) {

						// plant will be added, set randomized maximum age
						affectedResource.setResource("plantsMaxAge", Gaia.rand.nextInt(1000));
					}

					if(existingPlants < 25) {
						affectedResource.addResource("moisture", -8);
						affectedResource.addResource("plants", 1);
					}
				}
			}
		}
		
		return null;
	}
	
}
