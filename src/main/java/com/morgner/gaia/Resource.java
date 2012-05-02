package com.morgner.gaia;

import com.morgner.gaia.effect.ErosionEffect;
import com.morgner.gaia.effect.FireEffect;
import com.morgner.gaia.effect.PlantsEffect;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author Christian Morgner
 */
public class Resource implements Entity {

	private final Map<String, Integer> resources = new HashMap<String, Integer>();
	private final List<Resource> directNeighbours = new LinkedList<Resource>();
	private final List<Resource> allNeighbours = new LinkedList<Resource>();
	private final List<Effect> effects = new ArrayList<Effect>(20);
		
	private Comparator<Resource> terrainHeightComparator = null;
	private Environment env = null;
	private boolean isSink = false;
	private int normalX = 0;
	private int normalY = 0;
	private int terrain = 0;
	private int water = 0;
	private int type = 0;
	private int x = 0;
	private int y = 0;
	
	public Resource(Environment env, int x, int y) {
		
		this.env = env;
		this.x = x;
		this.y = y;
		
		terrainHeightComparator = new Comparator<Resource>() {

			@Override public int compare(Resource o1, Resource o2) {

				int t1 = o1.getTerrain();
				int t2 = o2.getTerrain();

				if(t1  < t2) return -1;
				if(t1  > t2) return  1;

				return 0;
			}
		};
	}
	
	public void initialize() {
		
		Resource top = env.getResource(x, y-1);
		directNeighbours.add(top);
		allNeighbours.add(top);

		Resource left = env.getResource(x-1, y);
		directNeighbours.add(left);
		allNeighbours.add(left);

		Resource right = env.getResource(x+1, y);
		directNeighbours.add(right);
		allNeighbours.add(right);

		Resource bottom = env.getResource(x, y+1);
		directNeighbours.add(bottom);
		allNeighbours.add(bottom);

		Resource topLeft = env.getResource(x-1, y-1);
		allNeighbours.add(topLeft);

		Resource topRight = env.getResource(x+1, y-1);
		allNeighbours.add(topRight);

		Resource bottomLeft = env.getResource(x-1, y+1);
		allNeighbours.add(bottomLeft);

		Resource bottomRight = env.getResource(x+1, y+1);
		allNeighbours.add(bottomRight);
	}
	
	public List<Resource> getNeighbours() {
		return getNeighbours(false, true);
	}
	
	public List<Resource> getNeighbours(boolean directOnly, boolean shuffle) {

		List<Resource> list = null;
		if(directOnly) {
			
			list = directNeighbours;
			
		} else {
			
			list = allNeighbours;
		}
			
		if(shuffle) {
			
			Collections.shuffle(list);
		}
		
		return list;
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
	public void drawCell(Graphics gr, int x, int y, int w, int h) {
		
		int plants = getResource("plants");
		int _water = water + getResource("_water");

		int ashes = getResource("ashes");
		int fire = getResource("fire");

		Color color = getColorForTerrain();
		color = blend(color, getColorForHeight());
		color = blend(color, getColorForHighlights());
		color = blend(color, getColorForShadows());

		if(fire > 0) {
			
			int r = 0;
			int g = 0;
			int b = 0;
			
			switch(Gaia.rand.nextInt(3)) {
				case 0:
					r = 255;
					g = 128;
					b = 0;
					break;

				case 1:
					r = 255;
					break;

				case 2:
					
					r = 255;
					b = 224;
					break;
			}

			color = blend(color, new Color(r, g, b, 255));
		} 
		
		if(ashes > 0) {
			
			int r = 0;
			int g = 0;
			int b = 0;
			
			switch(Gaia.rand.nextInt(3)) {
				case 0:
					r = 128;
					g = 128;
					b = 128;
					break;

				case 1:
					r = 132;
					g = 132;
					b = 132;
					break;

				case 2:
					r = 136;
					g = 136;
					b = 136;
					break;
			}


			color = blend(color, new Color(r, g, b, 64));
		}

		if(_water > 0) {

			int b = 170 - (water / 4);
			if(b <  64) b =  64;
			if(b > 255) b = 255;
			
			int a = 64 + (water * 2);
			if(a <   0) a =   0;
			if(a > 255) a = 255;
			
			color = blend(color, new Color(0, 0, b, a));
		}
		
		if(plants > 0) {

			int g = (plants) + 160;
			if(g <  64) g =  64;
			if(g > 255) g = 255;
			
			color = blend(color, new Color(0, g, 0, 32));
		}

		draw(gr, x, y, w, h, color);
	}
	
	public Color blend(Color dest, Color source) {

		double alpha = (double)source.a / 255.0;
		double neg   = 1.0 - alpha;
		
		int r = (int)Math.rint((alpha * (double)source.r) + (neg * (double)dest.r));
		int g = (int)Math.rint((alpha * (double)source.g) + (neg * (double)dest.g));
		int b = (int)Math.rint((alpha * (double)source.b) + (neg * (double)dest.b));
		
		return new Color(r, g, b, 255);
	}
	
	private void draw(Graphics gr, int x, int y, int w, int h, Color color) {
		
		gr.setColor(new java.awt.Color(color.r, color.g, color.b, 255));
		gr.fillRect(x, y, w, h);
	}

	/*
	private void drawRound(Graphics gr, int x, int y, int w, int h, Color color) {
		
		int cellSize = env.getCellSize();
		
		gr.setColor(new java.awt.Color(color.r, color.g, color.b, 255));
		gr.fillRoundRect(x, y, w, h, cellSize, cellSize);
	}
	*/
	
	@Override
	public List<Effect> update(final long dt) {

		effects.clear();
		
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
		if(getTerrain() < env.getTreeLine() && !hasWater() && humus > 0 && hasResource("moisture")) {
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
			setResource("moisture", 255);

			Collections.sort(allNeighbours, terrainHeightComparator);
			for(final Resource n : allNeighbours) {

				effects.add(new Effect(n) {

					@Override public Effect effect() {

						Effect e = null;
						
						int localTerrain = getTerrain();
						int neighbourTerrain = affectedResource.getTerrain();

						int localWater = getWater();
						int neighbourWater = affectedResource.getWater();

						int combinedLocalHeight = localTerrain + localWater;
						int combinedNeighbourHeight = neighbourTerrain + neighbourWater;

						if(localTerrain == neighbourTerrain) {

							// level terrain, equalize water height
							int d = ((localWater + neighbourWater) / 2);
							int m = (localWater + neighbourWater) % 2;

							setWater(d + m);
							affectedResource.setWater(d);
							
						} else {

							if(combinedLocalHeight > combinedNeighbourHeight) {
								
								if(combinedLocalHeight > neighbourTerrain) {
									
									if(localWater > 0) {
										int dh = combinedLocalHeight - neighbourTerrain;
										int amount = Math.min(dh, localWater);

										int v = amount / 8;
										int d = amount % 8;

										affectedResource.addWater(v+d);
										addWater(-(v+d));

										e = new ErosionEffect(affectedResource, v+d);
									}
									
								} else if(localWater > neighbourWater) {
									
									int d = localWater - neighbourWater;
									
									if(d > 8) {

										int v = (d / 4) + (d % 4) - 1;

										affectedResource.addWater(v);
										addWater(-v);

										e = new ErosionEffect(affectedResource, v);
										
									} else if(d > 4) {

										int v = (d / 2) + (d % 2) - 1;

										affectedResource.addWater(v);
										addWater(-v);

										e = new ErosionEffect(affectedResource, v);

									} else {

										affectedResource.addWater(1);
										addWater(-1);

										e = new ErosionEffect(affectedResource, 1);
									}
								}
								
							}
						}

						localTerrain = getTerrain();
						neighbourTerrain = affectedResource.getTerrain();

						localWater = getWater();
						neighbourWater = affectedResource.getWater();

						combinedLocalHeight = localTerrain + localWater;
						combinedNeighbourHeight = neighbourTerrain + neighbourWater;

						if(combinedLocalHeight == combinedNeighbourHeight) {

							// sedimentation if water level is the same
							if(localTerrain > neighbourTerrain && localWater > 16) {

								if(Gaia.rand.nextDouble() > 0.9) {
									affectedResource.addTerrain(localWater / 16);
								}

							} else if(localTerrain < neighbourTerrain && neighbourWater > 16) {

								if(Gaia.rand.nextDouble() > 0.9) {
									addTerrain(neighbourWater / 16);
								}

							}
						}
						
						return e;
					}
				});
			}

			// evaporization
			if(Gaia.rand.nextDouble() > (Math.pow(0.999, 1.0 / (double)getWater()))) {
				addWater(-1);
			}
			
		} else {

			if(Gaia.rand.nextDouble() > 0.9) {
				addResource("moisture", -1);
			}
			
			addResource("_water", -1);

			if(hasResource("_water") || hasResource("moisture")) {
			}
		}
		
		effects.add(new Effect(this) {
			@Override public Effect effect() {

				int moisture = affectedResource.getResource("moisture");
				boolean alive = affectedResource.getType() > 0;
				int m = 0;

				for(Resource n : affectedResource.getNeighbours()) {
					int nm = n.getResource("moisture");
					m += nm - moisture;
					
					alive |= nm > 0;
				}

				affectedResource.addResource("moisture", m/8);

				if(!alive) {
					env.deactivate(affectedResource);
				}
				
				return null;
			}
		});
		
		// continuous calculation of surface normals (spread calculation time)
		env.calculateNormal(this);
		
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

	@Override
	public boolean isAlive() {
		return env.isActive(this);
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
		if(terrain < 0) {
			terrain = 0;
		}
	}
	
	public void addTerrain(int amount) {
		if(!isSink()) {
			this.terrain += amount;
		}
	}

	public int getWater() {
		return water;
	}

	public void setWater(int water) {
		
		if(!isSink()) {

			this.water = water;
			if(water < 0) {
				water = 0;
			}
			env.activate(this);
		}
	}
	
	public void addWater(int amount) {
		
		if(!isSink()) {
			
			this.water += amount;
			if(water < 0) {
				water = 0;
			}
			env.activate(this);
		}
	}
	
	public boolean hasWater() {
		return water > 0;
	}
	
	public boolean higher(Resource res, int amount) {
		return getTerrain() - res.getTerrain() > amount;
	}

	public boolean isSink() {
		return isSink;
	}

	public void setSink(boolean isSink) {
		this.isSink = isSink;
	}
	
	public int getSunExposure() {
		return (int)Math.rint((double)(-getNormalX()-getNormalY()) * 0.5);
	}
	
	private Color getColorForTerrain() {
		
		double height = getTerrain();

		int h = (int)Math.rint((height / (double)env.getMaxHeight()) * 4.0) - 1;
		int m = (int)Math.rint(((double)getResource("moisture") / (double)255.0) * 6.0) - 1;

		return getColorForZone(h, m);
		
	}
	
	private Color getColorForHeight() {
		
		int height = getTerrain();
		int x = (int)Math.rint(height * env.getColorFactor());
		if(x <   0) x =   0;
		if(x > 255) x = 255;
		
		int v = env.getShadowBrightness();

		return new Color(v, v, v, 255-x);
	}
	
	private Color getColorForShadows() {
		
		int dark = getNormalX()+getNormalY();
		dark = (int)Math.rint((double)dark * env.getInclinationBrightnessFactor());
		if(dark <   0) dark =   0;
		if(dark > 255) dark = 255;

		return new Color(32, 32, 32, dark);
		
	}
	
	private Color getColorForHighlights() {
		
		int light = -getNormalX()-getNormalY();
		light = (int)Math.rint((double)light * env.getInclinationBrightnessFactor() * 0.4);
		if(light <   0) light =   0;
		if(light > 255) light = 255;

		return new Color(255, 255, 255, light);
		
	}
	
	private static final Color A = new Color(0x775533);	// brown
	private static final Color B = new Color(0x664422);	// darker brown
	private static final Color C = new Color(0x555522);	// slightly green
	private static final Color D = new Color(0x334411);	// darker green
	private static final Color E = new Color(0x7a5a3a);	// lighter brown
	
	private static final Color F = new Color(0x666666);
	private static final Color G = new Color(0x666666);
	private static final Color H = new Color(0x666666);
	private static final Color I = new Color(0x666666);
	private static final Color J = new Color(0x666666);
	private static final Color K = new Color(0x666666);
	
	private static final Color[][] colorZones = new Color[][] {
	
		{ A, A, A, B, C, D },
		{ A, A, A, B, B, C },
		{ E, A, A, B, B, B },
		{ F, G, H, I, J, K }

	};
	
	private Color getColorForZone(int height, int moisture) {
		
		if(height < 0) height = 0;
		if(height > 3) height = 3;
		if(moisture < 0) moisture = 0;
		if(moisture > 5) moisture = 5;

		return colorZones[height][moisture];
	}

	public int getNormalX() {
		return normalX;
	}

	public void setNormalX(int normalX) {
		this.normalX = normalX;
	}

	public int getNormalY() {
		return normalY;
	}

	public void setNormalY(int normalY) {
		this.normalY = normalY;
	}
	
	private static class Color {
		
		public int r = 0;
		public int g = 0;
		public int b = 0;
		public int a = 0;
		
		public Color(int r, int g, int b, int a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
		
		public Color(int rgb) {
			this.r = (rgb >> 16) & 0xff;
			this.g = (rgb >>  8) & 0xff;
			this.b = rgb & 0xff;
			this.a = 0xff;
		}
	}
}
