/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.Effect;
import com.morgner.gaia.Resource;
import com.morgner.gaia.util.FastMath;
import com.morgner.gaia.util.IntColor;
import java.util.Collection;

/**
 *
 * @author Christian Morgner
 */
public class Wire extends Structure {

	public Wire(Resource resource) {
		super(resource, resource.getX(), resource.getY());
	}
	
	private IntColor wireColor() {
		
		int val = resource.getResource(Resource.CURRENT);
		
		return new IntColor(val, val, 0, resource.getEnvironment().getCellSize() * 2);
	}
	
	@Override
	public IntColor getColor(IntColor color, double stepNum, double stepWidth, double stepHeight, int i, int j) {
		
		int cx = FastMath.rint(stepNum / 2.0);
		int cy = FastMath.rint(stepNum / 2.0);
			
		Resource west = resource.getNeighbour(Resource.Direction.W);
		if(west.hasStructure() && west.getStructure() instanceof Wire) {

			if(i < cx && j == cy) {
				return IntColor.blend(color, wireColor());
			}
		}

		Resource north = resource.getNeighbour(Resource.Direction.N);
		if(north.hasStructure() && north.getStructure() instanceof Wire) {

			if(i == cx && j < cy) {
				return IntColor.blend(color, wireColor());
			}
		}

		Resource east = resource.getNeighbour(Resource.Direction.E);
		if(east.hasStructure() && east.getStructure() instanceof Wire) {

			if(i > cx && j == cy) {
				return IntColor.blend(color, wireColor());
			}
		}

		Resource south = resource.getNeighbour(Resource.Direction.S);
		if(south.hasStructure() && south.getStructure() instanceof Wire) {

			if(i == cx && j > cy) {
				return IntColor.blend(color, wireColor());
			}
		}

		if(i == cx && j == cy) {
			return IntColor.blend(color, wireColor());
		}
	
		return color;
	}

	@Override
	public void update(Collection<Effect> effects, final long dt) {
		
		double resistanceDependentTransmissionFactor = 0.95 - ((double)resource.getResource(Resource.RESISTANCE) / 1000.0);
		
		updateResource(effects, Resource.CURRENT, resistanceDependentTransmissionFactor);
		
		// resistance is the same as current, but with reversed sign
		// and origin in current-consuming entities.
		updateResource(effects, Resource.RESISTANCE, 0.95);
	}
	
	private void updateResource(Collection<Effect> effects, final int resourceId, double transmissionFactor) {
		
		int max = resource.getResource(resourceId);
		double count = 1;
		
		for(Resource n : resource.getNeighbours(true)) {
		
			if(n.hasStructure() && n.getStructure() instanceof Wire) {
				
				int current = n.getResource(resourceId);
				if(current > max) {
					max = current;
				} else {
					count++;
				}
			}
		}
		
		final int val = FastMath.rint((double)max * FastMath.pow(transmissionFactor, count));
		
		effects.add(new Effect(resource) {
			@Override public void effect() {
				affectedResource.setResource(resourceId, val);
			}
		});
		
	}
}
