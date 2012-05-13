/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.effect;

import com.morgner.gaia.Gaia;
import com.morgner.gaia.Resource;
import com.morgner.gaia.Effect;
import com.morgner.gaia.Resource.Direction;
import com.morgner.gaia.util.FastMath;

/**
 *
 * @author Christian Morgner
 */
public class PlantsEffect extends Effect {

	public PlantsEffect(Resource res) {
		super(res);
	}
	
	@Override
	public void effect() {
		
		if(!affectedResource.hasResource(Resource.MOISTURE)) {
			return;
		}
		
		affectedResource.getEnvironment().activate(affectedResource);
		
		int neighboursWithPlants = affectedResource.getResource(Resource.HUMUS) / 8;
		int secondaryNeighboursWithWater = 0;
		int directNeighboursWithWater = 0;

		directNeighboursWithWater = affectedResource.getNeighbourWater(Direction.values());
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.N).getNeighbourWater(Direction.NW, Direction.N, Direction.NE);
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.E).getNeighbourWater(Direction.NE, Direction.E, Direction.SE);
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.S).getNeighbourWater(Direction.SE, Direction.S, Direction.SW);
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.W).getNeighbourWater(Direction.SW, Direction.W, Direction.NW);
		
		for(Resource n : affectedResource.getNeighbours()) {

			if(n.hasResource(Resource.PLANTS)) {
				neighboursWithPlants++;
			}
		}

		int plantsMaxAge = affectedResource.getResource(Resource.PLANTS_MAX_AGE);
		int plantsAge = affectedResource.getResource(Resource.PLANTS_AGE);

		// plants die after max age has been reached
		if(plantsMaxAge > 0) {

			if(plantsAge > plantsMaxAge) {

				affectedResource.setResource(Resource.PLANTS_MAX_AGE, 0);
				affectedResource.setResource(Resource.PLANTS_AGE, 0);
				affectedResource.setResource(Resource.PLANTS, 0);

				affectedResource.addResource(Resource.DEAD_PLANTS);

				return;

			} else {

				affectedResource.addResource(Resource.PLANTS_AGE);
			}
		}


		if(directNeighboursWithWater == 0 && (secondaryNeighboursWithWater == 4 || neighboursWithPlants >= 2)) {

			int existingPlants = affectedResource.getResource(Resource.PLANTS);

			if(!affectedResource.hasResource(Resource.FIRE)) {

				if(Gaia.rand.nextDouble() > FastMath.pow(0.999, (affectedResource.getSunExposure() + affectedResource.getEnvironment().getPlantsFactor()))) {

					if(existingPlants == 0) {

						// plant will be added, set randomized maximum age
						affectedResource.setResource(Resource.PLANTS_MAX_AGE, Gaia.rand.nextInt(1000));
					}

					if(existingPlants < 25) {
						affectedResource.addResource(Resource.MOISTURE, -1);
						affectedResource.addResource(Resource.PLANTS, 1);
						
						for(Resource r : affectedResource.getNeighbours(true)) {
							r.addResource(Resource.MOISTURE);
						}
					}
				}
			}
		}
	}
	
}
