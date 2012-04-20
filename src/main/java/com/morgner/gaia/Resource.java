package com.morgner.gaia;

import com.morgner.gaia.effect.ErosionEffect;
import com.morgner.gaia.effect.FireEffect;
import com.morgner.gaia.effect.PlantsEffect;
import java.awt.Color;
import java.awt.Point;
import java.util.*;

/**
 *
 * @author Christian Morgner
 */
public class Resource implements Entity {

	private final Map<String, Integer> resources = new HashMap<String, Integer>();
	private Environment env = null;
	private int terrain = 0;
	private int water = 0;
	private int type = 0;
	private int x = 0;
	private int y = 0;
	
	public Resource(Environment env, int x, int y) {
		
		this.env = env;
		this.x = x;
		this.y = y;

		//resources.put("terrain", (int)Math.rint());

		/*
		if(Gaia.rand.nextDouble() > 0.9995) {
			type = 2;
			resources.put("terrain", -1);
			resources.put("water", -1);
		}
		*/
	
	}
	
	public List<Resource> getNeighbours() {
		return getNeighbours(false, true);
	}
	
	public List<Resource> getNeighbours(boolean directOnly, boolean shuffle) {
		
		List<Resource> neighbours = new LinkedList<Resource>();
		
		Resource top = env.getResource(x, y-1);
		if(top != null) neighbours.add(top);
		
		Resource left = env.getResource(x-1, y);
		if(left != null) neighbours.add(left);
		
		Resource right = env.getResource(x+1, y);
		if(right != null) neighbours.add(right);

		Resource bottom = env.getResource(x, y+1);
		if(bottom != null) neighbours.add(bottom);

		if(!directOnly) {
			
			Resource topLeft = env.getResource(x-1, y-1);
			if(topLeft != null) neighbours.add(topLeft);

			Resource topRight = env.getResource(x+1, y-1);
			if(topRight != null) neighbours.add(topRight);

			Resource bottomLeft = env.getResource(x-1, y+1);
			if(bottomLeft != null) neighbours.add(bottomLeft);

			Resource bottomRight = env.getResource(x+1, y+1);
			if(bottomRight != null) neighbours.add(bottomRight);
		}
		
		if(shuffle) {
			Collections.shuffle(neighbours);
		}
		
		return neighbours;
	}
	
	public void setResource(String name, int amount) {
		resources.put(name, amount);
		env.activate(this);
	}
	
	public void addResource(String name) {
		addResource(name, 1);
	}
	
	public void addResource(String name, int amount) {

		int resourceAmount = getResource(name);
		resources.put(name, resourceAmount + amount);

		env.activate(this);
	}
	
	public int getResource(String name) {
		
		Integer resourceAmount = resources.get(name);
		if(resourceAmount != null) {
			return resourceAmount;
		}
		
		return 0;
	}
	
	public int getResources(String... names) {
		
		int sum = 0;
		
		for(int i=0; i<names.length; i++) {
			sum += getResource(names[i]);
		}
		
		return sum;
	}
	
	public boolean hasResource(String name) {
		return getResource(name) > 0;
	}
	
	@Override
	public Color getCellColor() {
		
		int intHeight = getTerrain();
		double height = intHeight;
		    
		int plants = getResource("plants");
		int _water = water + getResource("_water");

		int ashes = getResource("ashes");
		int fire = getResource("fire");

		int r = 0;
		int g = 0;
		int b = 0;
		
		if(getType() == 1) {
			
			return new Color(0, 0, 255);

		} else if(getType() == 2) {
			
			return new Color(255, 255, 255);
			
//		} else if(getResource("sink") > 0) {
//	
//			return new Color(255, 0, 0);
			
		} else if(fire > 0) {
			
			switch(Gaia.rand.nextInt(3)) {
				case 0:
					return new Color(255, 128, 0);
				case 1:
					return new Color(255, 0, 0);
				case 2:
					return new Color(255, 224, 0);
			}
			
		} else if(ashes > 0) {
			
			switch(Gaia.rand.nextInt(3)) {
				case 0:
					return new Color(128, 128, 128);
				case 1:
					return new Color(132, 132, 132);
				case 2:
					return new Color(136, 136, 136);
			}

		} else if(intHeight < env.getSeaLevel()) {

			int d = env.getSeaLevel() - intHeight;
			if(d > 8) {
				
				b = 240 - (d / 4);
				
			} else {
				
				b = 240 - d;
			}
			
		} else if(_water > 8) {
			
			b = 255 - _water;
			if(b <   0) b = 0;
			if(b > 255) b = 255;

			g = _water / 8;
			if(g <   0) g = 0;
			if(g > 255) g = 255;

		} else if(_water > 0) {

			b = 255 - (_water*_water);
			if(b <   0) b = 0;
			if(b > 255) b = 255;

			g = _water / 8;
			if(g <   0) g = 0;
			if(g > 255) g = 255;
			
		} else if(plants > 0) {

			g = (plants) + 160;
			
		} else {
			
			double f = env.getColorFactor();
			
			if(height > env.getTreeLine()) {
				
				r = (int)Math.rint(height * f) - 32;
				g = (int)Math.rint(height * f) - 32;
				b = (int)Math.rint(height * f) - 32;

			} else {
				
				r = (int)Math.rint(height * f) + 16;
				g = (int)Math.rint(height * f * 0.63) + 16;
				b = (int)Math.rint(height * f * 0.36) + 16;
			}
		}
		
		if(r > 255) r = 255;
		if(g > 255) g = 255;
		if(b > 255) b = 255;

		if(r <   0) r = 0;
		if(g <   0) g = 0;
		if(b <   0) b = 0;

		return new Color(r, g, b);
	}
	
	@Override
	public List<Effect> update(final long dt) {

		List<Effect> effects = new ArrayList<Effect>(20);
		
		if(getType() > 0) {

			effects.add(new Effect(this) {

				@Override
				public Effect effect() {
					affectedResource.addWater(env.getWaterSourceAmount());
					return null;
				}
			});
		}

		// add plants effect
		int humus = getResource("humus");
		if(getTerrain() < env.getTreeLine() && !hasWater() && humus > 0) {
			effects.add(new PlantsEffect(this));
		} else {
			setResource("plants", 0);
		}

		if(hasResource("plants") || hasResource("fire")) {
			effects.add(new FireEffect(this));
		}

		// remove ashes
		if(hasResource("ashes") && Gaia.rand.nextDouble() > 0.95) {
			addResource("ashes", -1);
		}

		// simulate water
		if(hasWater()) {
			
			setResource("_water", env.getWaterTrail());

			for(final Resource n : getNeighbours()) {

				effects.add(new Effect(n) {

					@Override public Effect effect() {

						Effect e = null;
						
						int localTerrain = getTerrain();
						int neighbourTerrain = affectedResource.getTerrain();

						int localWater = getWater();
						int neighbourWater = affectedResource.getWater();

						int combinedLocalHeight = localTerrain + localWater;
						int combinedNeighbourHeight = neighbourTerrain + neighbourWater;

						int combinedHeightDifference = combinedLocalHeight - combinedNeighbourHeight;
						e = new ErosionEffect(Resource.this, n, combinedHeightDifference);

						// flow speed dependent erosion
						if(localTerrain == neighbourTerrain) {

							// level terrain, equalize water height
							int d = ((localWater + neighbourWater) / 2);
							int m = (localWater + neighbourWater) % 2;

							setWater(d + m);
							affectedResource.setWater(d);
							
						} else {

							if(combinedLocalHeight > combinedNeighbourHeight) {
								
								if(localWater > neighbourWater) {
									
									int d = localWater - neighbourWater;
									
									if(d > 8) {

										int v = (d / 4) + (d % 4) - 1;

										affectedResource.addWater(v);
										addWater(-v);
										
									} else if(d > 4) {

										int v = (d / 2) + (d % 2) - 1;

										affectedResource.addWater(v);
										addWater(-v);

									} else {

										affectedResource.addWater(1);
										addWater(-1);
									}
								}
							}
						}

						return e;
					}
				});
			}

			// evaporization
			if(Gaia.rand.nextDouble() > (Math.pow(0.99, 1.0 / (double)getWater()))) {
				addWater(-1);
			}
			
		} else {
			addResource("_water", -1);			
		}
		
		// sea level never changes
		if(hasResource("sink")) {
			effects.add(new Effect(this) {
				@Override public Effect effect() {
					affectedResource.setWater(env.getSeaLevel() - affectedResource.getTerrain());
					return null;
				}
			});
		}
		
		if(effects.isEmpty()) {
			env.deactivate(this);
		}
		
		return effects;
	}
	
	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
	
	public Environment getEnvironment() {
		return env;
	}

	@Override
	public boolean contains(Point p) {
		
		int r = env.getCellSize();
		
		int px = p.x;
		int py = p.y;
		
		return (px > x*r) && (px < (x+1)*r && (py > y*r) && (py < (y+1)*r));
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTerrain() {
		return terrain;
	}

	public void setTerrain(int terrain) {
		this.terrain = terrain;
	}
	
	public void addTerrain(int amount) {
		this.terrain += amount;
	}

	public int getWater() {
		return water;
	}

	public void setWater(int water) {
		this.water = water;
		env.activate(this);
	}
	
	public void addWater(int amount) {
		this.water += amount;
		env.activate(this);
	}
	
	public boolean hasWater() {
		return water > 0;
	}
	
	public boolean higher(Resource res, int amount) {
		return getTerrain() - res.getTerrain() > amount;
	}
}
