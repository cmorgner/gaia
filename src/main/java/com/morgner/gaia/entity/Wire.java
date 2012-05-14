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

	private static final double RHO_CU = 17.8e-3;				// resistivity of copper
	private static final double WIRE_RESISTANCE = RHO_CU * (1.0 / 1.0);	// length / area
	
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
		
		int max = resource.getResource(Resource.CURRENT);
		double count = 1;
		
		for(Resource n : resource.getNeighbours(true)) {
		
			if(n.hasStructure() && n.getStructure() instanceof Wire) {
				
				int current = n.getResource(Resource.CURRENT);
				if(current > max) {
					max = current;
				} else {
					count++;
				}
			}
		}
		
		final int val = FastMath.rint((double)max * FastMath.pow(0.9, count));
		
		effects.add(new Effect(resource) {
			@Override public void effect() {
				affectedResource.setResource(Resource.CURRENT, val);
			}
		});
		
	}
}
