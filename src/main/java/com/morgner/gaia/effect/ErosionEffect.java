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
public class ErosionEffect extends Effect {

	private Resource directionNeighbour = null;
	private Resource rightNeighbour = null;
	private Resource leftNeighbour = null;
	private int speed = 0;
	
	public ErosionEffect(Resource resource, Resource neighbour, int speed) {
		
		super(resource);
		
		int dx = resource.getX() - neighbour.getX();
		int dy = resource.getY() - neighbour.getY();
		
		this.directionNeighbour = resource.getEnvironment().getResource(resource.getX() - dx, resource.getY() - dy);
		
		int direction = getDirection(dx, dy);
		int left = direction -2;
		int right = direction + 2;
		
		if(left < 0) left += 8;
		if(right > 7) right -= 8;
		
		this.leftNeighbour = getDirectionalNeighbour(left);
		this.rightNeighbour = getDirectionalNeighbour(right);
		
		this.speed = speed;
	}
	
	@Override
	public Effect effect() {

		if(affectedResource.getType() != 0) {
			return null;
		}

		if(affectedResource.hasResource("sink")) {
			return null;
		}
		
		if(affectedResource.hasResource("plants")) {
			return null;
		}
		
		// erosion
		if(affectedResource.getWater() > 0) {

//			// local, direct erosion
//			if(Gaia.rand.nextDouble() > 0.5 && speed > 0) {
//				affectedResource.addTerrain(-Gaia.rand.nextInt(1));
//			}
			
			if(speed < 2) {
				
				// sedimentation
				if(Gaia.rand.nextDouble() > 0.9) {
					
					affectedResource.addTerrain(1);
				}
				
			} else {
				
				// erosion
				if(Gaia.rand.nextDouble() > 0.5) {
					affectedResource.addTerrain(-1);
					
					if(!leftNeighbour.hasResource("sink") && !leftNeighbour.hasResource("plants") && !leftNeighbour.higher(affectedResource, 2)) {
						
						if(Gaia.rand.nextDouble() > 0.5) {
							leftNeighbour.addTerrain(1);
						}
					}
					if(!rightNeighbour.hasResource("sink") && !rightNeighbour.hasResource("plants") && !rightNeighbour.higher(affectedResource, 2)) {
						
						if(Gaia.rand.nextDouble() > 0.5) {
							rightNeighbour.addTerrain(1);
						}
					}
				}
			
				if(!directionNeighbour.hasResource("sink") && !directionNeighbour.hasResource("plants") && directionNeighbour.getTerrain() > affectedResource.getTerrain()) {

					if(Gaia.rand.nextDouble() > 0.5) {
						directionNeighbour.addTerrain(-1);
					}
				}
			}

		}
		
		return null;
	}
	
	private Resource getDirectionalNeighbour(int direction) {
		
		int dx = 0;
		int dy = 0;
		
		switch(direction) {
			case 0: dx =  0; dy = -1; break;
			case 1: dx =  1; dy = -1; break;
			case 2: dx =  1; dy =  0; break;
			case 3: dx =  1; dy =  1; break;
			case 4: dx =  0; dy =  1; break;
			case 5: dx = -1; dy =  1; break;
			case 6: dx = -1; dy =  0; break;
			case 7: dx = -1; dy = -1; break;
		}
		
		return affectedResource.getEnvironment().getResource(affectedResource.getX() + dx, affectedResource.getY() + dy);
	}
	
	private int getDirection(int dx, int dy) {
		
		int vertical = 0;
		int horizontal = 0;
		
		if(dx == -1) horizontal = -1;
		if(dx ==  1) horizontal =  1;
		if(dy == -1) vertical = -1;
		if(dy ==  1) vertical =  1;
		
		if(horizontal  < 0 && vertical  < 0)	return 7;
		if(horizontal == 0 && vertical  < 0)	return 0;
		if(horizontal  > 0 && vertical  < 0)	return 1;
		if(horizontal  < 0 && vertical == 0)	return 6;
		if(horizontal  > 0 && vertical == 0)	return 2;
		if(horizontal  < 0 && vertical  > 0)	return 5;
		if(horizontal == 0 && vertical  > 0)	return 4;
		if(horizontal  > 0 && vertical  > 0)	return 3;
		
		return -1;
	}
	
	private double pow(double val, double exp) {
		return Math.pow(val, exp);
	}
	
	private double invPow(double val, double exp) {
		return Math.pow(val, 1.0 / exp);
	}
}
