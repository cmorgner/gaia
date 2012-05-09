/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.effect;

import com.morgner.gaia.Gaia;
import com.morgner.gaia.Resource;
import com.morgner.gaia.Effect;
import com.morgner.gaia.Resource.Direction;

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
		
		if(!affectedResource.hasResource("moisture")) {
			return;
		}
		
		affectedResource.getEnvironment().activate(affectedResource);
		
		int neighboursWithPlants = affectedResource.getResource("humus") / 8;
		int secondaryNeighboursWithWater = 0;
		int directNeighboursWithWater = 0;

		directNeighboursWithWater = affectedResource.getNeighbourWater(Direction.values());
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.N).getNeighbourWater(Direction.NW, Direction.N, Direction.NE);
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.E).getNeighbourWater(Direction.NE, Direction.E, Direction.SE);
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.S).getNeighbourWater(Direction.SE, Direction.S, Direction.SW);
		secondaryNeighboursWithWater += affectedResource.getNeighbour(Direction.W).getNeighbourWater(Direction.SW, Direction.W, Direction.NW);
		
		for(Resource n : affectedResource.getNeighbours()) {

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

				return;

			} else {

				affectedResource.addResource("plantsAge");
			}
		}


		if(directNeighboursWithWater == 0 && (secondaryNeighboursWithWater == 4 || neighboursWithPlants >= 2)) {

			int existingPlants = affectedResource.getResource("plants");

			if(!affectedResource.hasResource("fire")) {

				if(Gaia.rand.nextDouble() > Math.pow(0.999, (affectedResource.getSunExposure() + affectedResource.getEnvironment().getPlantsFactor()))) {

					if(existingPlants == 0) {

						// plant will be added, set randomized maximum age
						affectedResource.setResource("plantsMaxAge", Gaia.rand.nextInt(1000));
					}

					if(existingPlants < 25) {
						affectedResource.addResource("moisture", -1);
						affectedResource.addResource("plants", 1);
						
						for(Resource r : affectedResource.getNeighbours(true, false)) {
							r.addResource("moisture");
						}
					}
				}
			}
		}
	}
	
}
