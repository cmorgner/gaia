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
	
	private static final Color A = new Color(0x70, 0x50, 0x30);
	private static final Color B = new Color(0x75, 0x55, 0x35);
	private static final Color C = new Color(0x7a, 0x5a, 0x3a);
	private static final Color D = new Color(0x80, 0x60, 0x40);
	private static final Color E = new Color(0x85, 0x65, 0x45);
	private static final Color F = new Color(0x8a, 0x6a, 0x4a);
	private static final Color G = new Color(0x90, 0x70, 0x50);
	private static final Color H = new Color(0x95, 0x75, 0x55);
	private static final Color I = new Color(0x9a, 0x7a, 0x5a);
	private static final Color J = new Color(0xa0, 0x80, 0x60);
	private static final Color K = new Color(0xa5, 0x85, 0x65);
	private static final Color L = new Color(0xaa, 0x8a, 0x6a);
	private static final Color M = new Color(0xb0, 0x90, 0x70);
	private static final Color N = new Color(0xb5, 0x95, 0x75);
	private static final Color O = new Color(0xba, 0x9a, 0x7a);
	private static final Color P = new Color(0xbb, 0xbb, 0xbb);
	private static final Color Q = new Color(0xc0, 0xc0, 0xc0);
	private static final Color R = new Color(0xc8, 0xc8, 0xc8);
	private static final Color S = new Color(0xcf, 0xcf, 0xcf);
	private static final Color T = new Color(0xd0, 0xd0, 0xd0);
	private static final Color U = new Color(0xd8, 0xd8, 0xd8);
	private static final Color V = new Color(0xdf, 0xdf, 0xdf);
	private static final Color W = new Color(0xff, 0xff, 0xff);

	private static final int green0 = 0x000000;
	private static final int green1 = 0x000400;
	private static final int green2 = 0x000800;
	
	public enum Direction {
		N, E, S, W, NE, SE, SW, NW
	}
	
	private static final Color[][] colorZones = new Color[][] {
	
		{ A, add(A, green1), add(A, green2) },
		{ B, add(B, green1), add(B, green2) },
		{ C, add(C, green1), add(C, green2) },
		{ D, add(D, green1), add(D, green2) },
		{ E, add(E, green1), add(E, green2) },
		{ F, add(F, green1), add(F, green2) },
		{ G, add(G, green1), add(G, green2) },
		{ H, add(H, green1), add(H, green2) },
		{ I, add(I, green1), add(I, green2) },
		{ J, add(J, green1), add(J, green2) },
		{ K, add(K, green1), add(K, green2) },
		{ L, add(L, green1), add(L, green2) },
		{ M, add(M, green1), add(M, green2) },
		{ N, add(N, green1), add(N, green2) },
		{ O, add(O, green1), add(O, green2) },
		{ P, add(P, green0), add(P, green0) },
		{ Q, add(Q, green0), add(Q, green0) },
		{ R, add(R, green0), add(R, green0) },
		{ S, add(S, green0), add(S, green0) },
		{ T, add(T, green0), add(T, green0) },
		{ U, add(U, green0), add(U, green0) },
		{ V, add(V, green0), add(V, green0) },
		{ W, add(W, green0), add(W, green0) }

	};
	
	private static final int heightZoneCount = colorZones.length;
	private static final int moistZoneCount  = colorZones[0].length;
		
	private Comparator<Resource> terrainHeightComparator = null;
	private Environment env = null;
	private boolean isSink = false;
	private Resource bottom = null;
	private Resource right = null;
	private Resource left = null;
	private Resource top = null;
	private Resource bottomLeft = null;
	private Resource bottomRight = null;
	private Resource topLeft = null;
	private Resource topRight = null;
	private int iterations = 0;
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
		
		top = env.getResource(x, y-1);
		directNeighbours.add(top);
		allNeighbours.add(top);

		left = env.getResource(x-1, y);
		directNeighbours.add(left);
		allNeighbours.add(left);

		right = env.getResource(x+1, y);
		directNeighbours.add(right);
		allNeighbours.add(right);

		bottom = env.getResource(x, y+1);
		directNeighbours.add(bottom);
		allNeighbours.add(bottom);

		topLeft = env.getResource(x-1, y-1);
		allNeighbours.add(topLeft);

		topRight = env.getResource(x+1, y-1);
		allNeighbours.add(topRight);

		bottomLeft = env.getResource(x-1, y+1);
		allNeighbours.add(bottomLeft);

		bottomRight = env.getResource(x+1, y+1);
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
	
	private Color blend(Color dest, Color source) {

		double alpha = (double)source.a / 255.0;
		double neg   = 1.0 - alpha;
		
		int r = (int)Math.rint((alpha * (double)source.r) + (neg * (double)dest.r));
		int g = (int)Math.rint((alpha * (double)source.g) + (neg * (double)dest.g));
		int b = (int)Math.rint((alpha * (double)source.b) + (neg * (double)dest.b));
		
		return new Color(r, g, b, 255);
	}
	
	@Override
	public void drawCell(Graphics gr, int x, int y, int w, int h) {

		int _water = this.water + getResource("_water");
		int cellSize = env.getCellSize();
		
		Color hc = getColorForHighlights();
		Color sc = getColorForShadows();
		
		if(cellSize > 6) {
		
//			double stepNum = Math.rint(Math.sqrt(cellSize));
			double stepNum = Math.rint(cellSize / 4);
			
			double stepWidth  = ((double)w / stepNum);
			double stepHeight = ((double)h / stepNum);
			
			for(int i=0; i<stepNum; i++) {
				
				for(int j=0; j<stepNum; j++) {
					
					int interpolatedHeight = getInterpolatedTerrain(stepNum, i, j);

					Color color = getColorForTerrain(interpolatedHeight);

					color = blend(color, getColorForWater(getInterpolatedWater(stepNum, i, j)));
					color = blend(color, getColorForPlants(getInterpolatedResource("plants", stepNum, i, j)));
					color = blend(color, getColorForHeight(interpolatedHeight));

					// shadow calculation
					{
						int tl = getInterpolatedTerrain(stepNum, i-1, j);
						int tr = getInterpolatedTerrain(stepNum, i+1, j);
						int tt = getInterpolatedTerrain(stepNum, i, j-1) + top.getResource("plants");
						int tb = getInterpolatedTerrain(stepNum, i, j+1);

						int shadowX = tl - tr;
						int shadowY = tt - tb;

						int nx = -shadowX;
						int ny = shadowY;
						
						int dark = nx+ny;
						int light = -nx-ny;

						color = blend(color, getColorForHighlights(light));
						color = blend(color, getColorForShadows(dark));
					}

					// fire
					color = blend(color, getColorForFire(getInterpolatedResource("fire", stepNum, i, j)));
					
					
					gr.setColor(new java.awt.Color(color.r, color.g, color.b, 255));
					
					int fx = (int)Math.rint(x+(i*stepWidth));
					int fy = (int)Math.rint(y+(j*stepHeight));
					int fw = (int)Math.ceil(stepWidth);
					int fh = (int)Math.ceil(stepHeight);
					
					gr.fillRect(fx, fy, fw, fh);
				}
			}
			
		} else {

			Color color = getColorForTerrain();
			
			if(_water > 0) {
				color = blend(color, getColorForWater(_water));
			}
					
			if(hasResource("plants")) {
				color = blend(color, getColorForPlants(getResource("plants")));
			}
			color = blend(color, getColorForHeight());
			color = blend(color, hc);
			color = blend(color, sc);

			gr.setColor(new java.awt.Color(color.r, color.g, color.b, 255));
			gr.fillRect(x, y, w, h);
		}
		
//		gr.setColor(java.awt.Color.BLACK);
//		gr.drawRect(x, y, w, h);
	}
	
	private int getInterpolatedTerrain(double step, int i, int j) {
		
		int topValue         = top.getTerrain();
		int rightValue       = right.getTerrain();
		int bottomValue      = bottom.getTerrain();
		int leftValue        = left.getTerrain();
		int centerValue      = terrain;

		double interpolationValue = env.getWaterInterpolationFactor();
		
		double deltaTop    = ((double)(centerValue - topValue   )) / (step*interpolationValue);
		double deltaRight  = ((double)(centerValue - rightValue )) / (step*interpolationValue);
		double deltaLeft   = ((double)(centerValue - leftValue  )) / (step*interpolationValue);
		double deltaBottom = ((double)(centerValue - bottomValue)) / (step*interpolationValue);

		double d = 0.0;
		int s2 = step % 2 == 0 ? (int)Math.rint((step) / 2.0) : (int)Math.rint((step-1) / 2.0);

		int leftI   = i < s2 ?  s2 - i : 0;
		int topJ    = j < s2 ?  s2 - j : 0;
		int rightI  = i > s2 ?  i - s2 : 0;
		int bottomJ = j > s2 ?  j - s2 : 0;
		
		d -= (leftI   * deltaLeft);
		d -= (topJ    * deltaTop);
		d -= (rightI  * deltaRight);
		d -= (bottomJ * deltaBottom);
		
		return (int)Math.rint(centerValue + d);
	}
	
	private int getInterpolatedWater(double step, int i, int j) {
		
		int topValue         = top.getWater() + top.getResource("_water");
		int rightValue       = right.getWater() + right.getResource("_water");
		int bottomValue      = bottom.getWater() + bottom.getResource("_water");
		int leftValue        = left.getWater() + left.getResource("_water");
		int centerValue      = water + getResource("_water");

		double interpolationValue = env.getWaterInterpolationFactor();
		
		double deltaTop    = ((double)(centerValue - topValue   )) / (step * interpolationValue);
		double deltaRight  = ((double)(centerValue - rightValue )) / (step * interpolationValue);
		double deltaLeft   = ((double)(centerValue - leftValue  )) / (step * interpolationValue);
		double deltaBottom = ((double)(centerValue - bottomValue)) / (step * interpolationValue);

		double d = 0.0;
		int s2 = step % 2 == 0 ? (int)Math.rint((step) / 2.0) : (int)Math.rint((step-1) / 2.0);

		double leftI   = i < s2 ?  s2 - i : 0;
		double topJ    = j < s2 ?  s2 - j : 0;
		double rightI  = i > s2 ?  i - s2 : 0;
		double bottomJ = j > s2 ?  j - s2 : 0;
		
		d -= (leftI   * deltaLeft);
		d -= (topJ    * deltaTop);
		d -= (rightI  * deltaRight);
		d -= (bottomJ * deltaBottom);
		
		return (int)Math.rint(centerValue + d);
	}
	
	private int getInterpolatedResource(String name, double step, int i, int j) {

		int topValue    = top.getResource(name);
		int rightValue  = right.getResource(name);
		int bottomValue = bottom.getResource(name);
		int leftValue   = left.getResource(name);
		int centerValue = getResource(name);

		double interpolationValue = env.getWaterInterpolationFactor();
		
		double deltaTop    = ((double)(centerValue - topValue   )) / (step*interpolationValue);
		double deltaRight  = ((double)(centerValue - rightValue )) / (step*interpolationValue);
		double deltaLeft   = ((double)(centerValue - leftValue  )) / (step*interpolationValue);
		double deltaBottom = ((double)(centerValue - bottomValue)) / (step*interpolationValue);

		double d = 0.0;
		int s2 = step % 2 == 0 ? (int)Math.rint((step) / 2.0) : (int)Math.rint((step-1) / 2.0);

		int leftI   = i < s2 ?  s2 - i : 0;
		int topJ    = j < s2 ?  s2 - j : 0;
		int rightI  = i > s2 ?  i - s2 : 0;
		int bottomJ = j > s2 ?  j - s2 : 0;
		
		d -= (leftI   * deltaLeft);
		d -= (topJ    * deltaTop);
		d -= (rightI  * deltaRight);
		d -= (bottomJ * deltaBottom);

		return (int)Math.rint(centerValue + d);
	}

	@Override
	public void update(Collection<Effect> effects, final long dt) {

		int treeLine = env.getTreeLine();
		int humus = getResource("humus");

		if(type > 0) {
			addWater(env.getWaterSourceAmount());
		}

		// add plants effect
		if(getTerrain() < treeLine && water == 0 && humus > 0 && hasResource("moisture")) {
			
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
		if(water > 0) {
			
			setResource("_water", env.getWaterTrail());
			setResource("moisture", 255);

			Collections.sort(allNeighbours, terrainHeightComparator);
			for(final Resource n : allNeighbours) {

				effects.add(new Effect(n) {

					@Override public void effect() {

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
										
										addResource("flow", -(v+d));
										affectedResource.addResource("flow", (v+d));

										new ErosionEffect(Resource.this).effect();
									}
									
								} else if(localWater > neighbourWater) {
									
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
					}
				});
			}

			// evaporization
			if(Gaia.rand.nextDouble() > 0.99) {
				addWater(-1);
			}
			
		} else {

			if(hasResource("moisture") || hasResource("_water")) {

				env.activate(this);
				
				if(Gaia.rand.nextDouble() > 0.9) {

					if(hasResource("moisture")) {
						addResource("moisture", -1);
					}
				}

				if(hasResource("_water")) {
					addResource("_water", -1);
				}
			}
		}

		// moisture effect randomly
		if(Gaia.rand.nextDouble() > 0.95) {

			effects.add(new Effect(this) {
				@Override public void effect() {

					int moisture = affectedResource.getResource("moisture");
					int m = 0;

					for(Resource n : affectedResource.getNeighbours()) {

						int nm = n.getResource("moisture");
						if(n.higher(affectedResource, -32)) {
							m += ((nm - moisture));
						}
					}

					int v = (m/8 + m%8);

					affectedResource.addResource("moisture", v);
				}
			});
		}
						
		// continuous calculation of surface normals (spread calculation time)
		env.calculateNormal(this);

		if(type == 0 && !hasResource("_water") && !hasWater() && !hasResource("plants")) {
			env.deactivate(this);
			
			effects.add(new Effect(this) {
				@Override public void effect() {
					affectedResource.getEnvironment().calculateNormal(affectedResource);
					for(Resource n : affectedResource.getNeighbours(false, false)) {
						n.getEnvironment().calculateNormal(n);
					}
				}
			});
		}
		
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
		
		return (px >= x*r) && (px < (x+1)*r && (py >= y*r) && (py < (y+1)*r));
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
		env.activate(this);
		if(terrain < 0) {
			terrain = 0;
		}
	}
	
	public void addTerrain(int amount) {
		if(!isSink()) {
			this.terrain += amount;
			env.activate(this);
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
	
	private Color getColorForWater(int water) {

		if(water > 0) {
			
			int b = 180 - (water / 4);
			if(b <  64) b =  64;
			if(b > 255) b = 255;

			int a = 32 + (water * 2);
			if(a <   0) a =   0;
			if(a > 255) a = 255;

			return new Color(0, 0, b, a);
		}
		
		return new Color(0, 0, 0, 0);
	}
	
	private Color getColorForPlants(int plants) {

		if(plants > 0) {

			int g = 128 + (plants * 2);
			if(g <  64) g =  64;
			if(g > 255) g = 255;

			int a = 128 + (plants * 2);
			if(a <   0) a =   0;
			if(a > 255) a = 255;

			return new Color(0, a, 0, a);
		}
		
		return new Color(0, 0, 0, 0);
	}
	
	private Color getColorForFire(int fire) {

		if(fire > 0) {

			int r = 0;
			int g = 0;
			int b = 0;
			
			int a = 64 + (fire * 2);
			if(a <   0) a =   0;
			if(a > 255) a = 255;

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

			return new Color(r, g, b, a);
		}
		
		return new Color(0, 0, 0, 0);
	}
	
	private Color getColorForTerrain() {
		return getColorForTerrain(getTerrain());
	}
	
	private Color getColorForTerrain(int height) {

		int h = (int)Math.rint(((double)height / (double)env.getMaxHeight()) * heightZoneCount) - 1;
		int m = (int)Math.rint(((double)getResource("moisture") / (double)255.0) * moistZoneCount) - 1;		
		
		return getColorForZone(h, m);
		
	}
	
	private Color getColorForHeight() {
		return getColorForHeight(getTerrain());
	}
	
	private Color getColorForHeight(int height) {
		
		int x = (int)Math.rint(((double)height / (double)env.getMaxHeight()) * 128.0) + 128;
		if(x <   0) x =   0;
		if(x > 255) x = 255;
		
		return new Color(0, 0, 0, 255-x);
	}
	
	private Color getColorForShadows() {
		int dark = getNormalX()+getNormalY();
		return getColorForShadows(dark);
	}
	
	private Color getColorForShadows(int d) {
		
		int dark = (int)Math.rint((double)d * env.getInclinationBrightnessFactor());
		if(dark <   0) dark =   0;
		if(dark > 255) dark = 255;

		return new Color(0, 0, 0, dark);
	}
	
	private Color getColorForHighlights() {
		int light = -getNormalX()-getNormalY();
		return getColorForHighlights(light);
	}
	
	private Color getColorForHighlights(int l) {
		
		int light = (int)Math.rint((double)l * env.getInclinationBrightnessFactor() * 0.4);
		if(light <   0) light =   0;
		if(light > 255) light = 255;

		return new Color(255, 255, 255, light);
		
	}
	
	private Color getColorForZone(int height, int moisture) {
		
		if(height < 0) height = 0;
		if(moisture < 0) moisture = 0;

		if(height >= heightZoneCount) height = heightZoneCount-1;
		if(moisture >= moistZoneCount) moisture = moistZoneCount-1;

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
	
	public int getNeighbourResources(String name, Direction... which) {
		
		int sum = 0;
		
		for(Direction d : which) {
			sum += getNeighbour(d).getResource(name);
		}
		
		return sum;
	}
	
	public int getNeighbourWater(Direction... which) {
		
		int sum = 0;
		
		for(Direction d : which) {
			sum += getNeighbour(d).getWater();
		}
		
		return sum;
	}
	
	public Resource getNeighbour(Direction which) {
		
		switch(which) {
			
			case N:
				return top;
				
			case E:
				return right;
				
			case S:
				return bottom;
				
			case W:
				return left;
				
			case NE:
				return topRight;
				
			case SE:
				return bottomRight;
				
			case SW:
				return bottomLeft;
				
			case NW:
				return topLeft;
		}

		return null;
	}
	
	private static class Color {
		
		public int r = 0;
		public int g = 0;
		public int b = 0;
		public int a = 0;
		
		public Color(int r, int g, int b) {
			this(r, g, b, 255);
		}
		
		public Color(int r, int g, int b, int a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			
			if(this.r <   0) this.r =   0;
			if(this.r > 255) this.r = 255;
	
			if(this.g <   0) this.g =   0;
			if(this.g > 255) this.g = 255;

			if(this.b <   0) this.b =   0;
			if(this.b > 255) this.b = 255;

			if(this.a <   0) this.a =   0;
			if(this.a > 255) this.a = 255;
		}
	}
	
	private static Color add(Color color, int rgb) {
		
		int r = color.r + ((rgb >> 16) & 0xff);
		int g = color.g + ((rgb >>  8) & 0xff);
		int b = color.b + ((rgb      ) & 0xff);

		if(r < 0) r = 0;
		if(g < 0) g = 0;
		if(b < 0) b = 0;
		
		if(r > 255) r = 255;
		if(g > 255) g = 255;
		if(b > 255) b = 255;
		
		return new Color(r, g, b);
	}
}
