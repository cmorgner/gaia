package com.morgner.gaia;

import com.morgner.gaia.effect.ErosionEffect;
import com.morgner.gaia.effect.FireEffect;
import com.morgner.gaia.effect.PlantsEffect;
import com.morgner.gaia.util.FastMath;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author Christian Morgner
 */
public class Resource implements Entity {

	public static final int TERRAIN         =  0;
	public static final int WATER           =  1;
	public static final int WATER_TRAIL     =  2;
	public static final int FLOW            =  3;
	public static final int PLANTS          =  4;
	public static final int PLANTS_AGE      =  5;
	public static final int PLANTS_MAX_AGE  =  6;
	public static final int FIRE            =  7;
	public static final int ASH             =  8;
	public static final int MOISTURE        =  9;
	public static final int DEAD_PLANTS     = 10;
	public static final int HUMUS           = 11;
	
	public static final int RESOURCES = 12;
	
	private final int[] resources = new int[RESOURCES];
	
	private final List<Resource> directNeighbours = new LinkedList<Resource>();
	private final List<Resource> allNeighbours = new LinkedList<Resource>();
	
	private static final IntColor C01 = new IntColor(0x70, 0x50, 0x30);
	private static final IntColor C02 = new IntColor(0x74, 0x54, 0x34);
	private static final IntColor C03 = new IntColor(0x78, 0x58, 0x38);
	private static final IntColor C04 = new IntColor(0x7c, 0x5c, 0x3c);
	
	private static final IntColor C05 = new IntColor(0x80, 0x60, 0x40);
	private static final IntColor C06 = new IntColor(0x84, 0x64, 0x44);
	private static final IntColor C07 = new IntColor(0x88, 0x68, 0x48);
	private static final IntColor C08 = new IntColor(0x8c, 0x6c, 0x4c);
	
	private static final IntColor C09 = new IntColor(0x90, 0x70, 0x50);
	private static final IntColor C10 = new IntColor(0x94, 0x74, 0x54);
	private static final IntColor C11 = new IntColor(0x98, 0x78, 0x58);
	private static final IntColor C12 = new IntColor(0x9c, 0x7c, 0x5c);
	
	private static final IntColor C13 = new IntColor(0xa0, 0x80, 0x60);
	private static final IntColor C14 = new IntColor(0xa4, 0x84, 0x64);
	private static final IntColor C15 = new IntColor(0xa8, 0x88, 0x68);
	private static final IntColor C16 = new IntColor(0xac, 0x8c, 0x6c);
	
	private static final IntColor C17 = new IntColor(0xb0, 0x90, 0x70);
	private static final IntColor C18 = new IntColor(0xb4, 0x94, 0x74);
	private static final IntColor C19 = new IntColor(0xb8, 0x98, 0x78);
	private static final IntColor C20 = new IntColor(0xbc, 0x9c, 0x7c);
	
	private static final IntColor P = new IntColor(0xbb, 0xbb, 0xbb);
	private static final IntColor Q = new IntColor(0xc0, 0xc0, 0xc0);
	private static final IntColor R = new IntColor(0xc8, 0xc8, 0xc8);
	private static final IntColor S = new IntColor(0xcf, 0xcf, 0xcf);
	private static final IntColor T = new IntColor(0xd0, 0xd0, 0xd0);
	private static final IntColor U = new IntColor(0xd8, 0xd8, 0xd8);
	private static final IntColor V = new IntColor(0xdf, 0xdf, 0xdf);
	private static final IntColor W = new IntColor(0xff, 0xff, 0xff);

	private static final int green0 = 0x000000;
	private static final int green1 = 0x000400;
	private static final int green2 = 0x000800;
	
	public enum Direction {
		N, E, S, W, NE, SE, SW, NW
	}
	
	private static final IntColor[][] colorZones = new IntColor[][] {
	
		{ C01, add(C01, green1), add(C01, green2) },
		{ C02, add(C02, green1), add(C02, green2) },
		{ C03, add(C03, green1), add(C03, green2) },
		{ C04, add(C04, green1), add(C04, green2) },
		{ C05, add(C05, green1), add(C05, green2) },
		{ C06, add(C06, green1), add(C06, green2) },
		{ C07, add(C07, green1), add(C07, green2) },
		{ C08, add(C08, green1), add(C08, green2) },
		{ C09, add(C09, green1), add(C09, green2) },
		{ C10, add(C10, green1), add(C10, green2) },
		{ C11, add(C11, green1), add(C11, green2) },
		{ C12, add(C12, green1), add(C12, green2) },
		{ C13, add(C13, green1), add(C13, green2) },
		{ C14, add(C14, green1), add(C14, green2) },
		{ C15, add(C15, green1), add(C15, green2) },
		{ C16, add(C16, green1), add(C16, green2) },
		{ C17, add(C17, green1), add(C17, green2) },
		{ C18, add(C18, green1), add(C18, green2) },
		{ C19, add(C19, green1), add(C19, green2) },
		{ C20, add(C20, green1), add(C20, green2) },
		
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
	private boolean hover = false;
	private int iterations = 0;
	private int normalX = 0;
	private int normalY = 0;
	private int type = 0;
	private int x = 0;
	private int y = 0;
	
	public Resource(Environment env, int x, int y) {
		
		this.env = env;
		this.x = x;
		this.y = y;
		
		terrainHeightComparator = new Comparator<Resource>() {

			@Override public int compare(Resource o1, Resource o2) {

				int t1 = o1.getResource(TERRAIN);
				int t2 = o2.getResource(TERRAIN);

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
		return getNeighbours(false);
	}
	
	public List<Resource> getNeighbours(boolean directOnly) {

		if(directOnly) {
			return directNeighbours;
		}
		
		return allNeighbours;
	}
	
	public void setResource(int name, int amount) {
		resources[name] = amount;
		env.activate(this);
	}
	
	public void addResource(int name) {
		addResource(name, 1);
	}
	
	public void addResource(int name, int amount) {
		
		resources[name] += amount;
		env.activate(this);
	}
	
	public int getResource(int name) {
		return resources[name];
	}
	
	public int getResources(int... names) {
		
		int sum = 0;
		
		for(int i=0; i<names.length; i++) {
			sum += getResource(names[i]);
		}
		
		return sum;
	}
	
	public boolean hasResource(int name) {
		return resources[name] > 0;
	}
	
	private IntColor blend(IntColor dest, IntColor source) {

		int alpha = source.a;
		int neg   = 255 - alpha;
		
		int r = ((alpha * source.r) + (neg * dest.r)) / 255;
		int g = ((alpha * source.g) + (neg * dest.g)) / 255;
		int b = ((alpha * source.b) + (neg * dest.b)) / 255;
		
		return new IntColor(r, g, b, 255);
	}
	
	@Override
	public void drawCell(Graphics gr, int x, int y, int w, int h) {

		int _water = getResource(WATER) + getResource(WATER_TRAIL);
		int cellSize = env.getCellSize();
		
		if(cellSize >= 8) {
		
			double stepNum    = FastMath.rint(cellSize / 4);
			double stepWidth  = ((double)w / stepNum);
			double stepHeight = ((double)h / stepNum);
			
			for(int i=0; i<stepNum; i++) {
				
				for(int j=0; j<stepNum; j++) {
					
					int interpolatedHeight = getInterpolatedResource(Resource.TERRAIN, stepNum, i, j);

					IntColor color = getColorForTerrain(interpolatedHeight);

					color = blend(color, getColorForWater(getInterpolatedWater(stepNum, i, j)));
					color = blend(color, getColorForPlants(getInterpolatedResource(PLANTS, stepNum, i, j)));
					color = blend(color, getColorForHeight(interpolatedHeight));

					// shadow calculation
					{
						int tl = getInterpolatedResource(Resource.TERRAIN, stepNum, i-1, j);
						int tr = getInterpolatedResource(Resource.TERRAIN, stepNum, i+1, j);
						int tt = getInterpolatedResource(Resource.TERRAIN, stepNum, i, j-1) + top.getResource(PLANTS);
						int tb = getInterpolatedResource(Resource.TERRAIN, stepNum, i, j+1);

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
					color = blend(color, getColorForFire(getInterpolatedResource(FIRE, stepNum, i, j)));
					
					// increase gama for large zoom factors
					double gamma = 1.0 + (cellSize / 80.0);
					color.scale(gamma, gamma, gamma, 1.0);
					
					
					int fx = FastMath.rint(x+(i*stepWidth));
					int fy = FastMath.rint(y+(j*stepHeight));
					int fw = FastMath.ceil(stepWidth);
					int fh = FastMath.ceil(stepHeight);

					if(cellSize >= env.getInteractionZoomLevel()) {

						if(hover) {
							color = blend(color, new IntColor(64, 64, 64, 16));
						}
						
					}
					
					gr.setColor(new Color(color.r, color.g, color.b, 255));
					gr.fillRect(fx, fy, fw, fh);

					// draw grid
					if(cellSize >= env.getInteractionZoomLevel()) {

						color = blend(color, new IntColor(64, 64, 64, 16));
						gr.setColor(new Color(color.r, color.g, color.b, 255));

						if(i == 0) gr.drawLine(fx, fy, fx, fy+fh);
						if(j == 0) gr.drawLine(fx, fy, fx+fw, fy);
					}
				}
			}
			
		} else {

			IntColor color = getColorForTerrain();
			
			if(_water > 0) {
				color = blend(color, getColorForWater(_water));
			}
					
			if(hasResource(PLANTS)) {
				color = blend(color, getColorForPlants(getResource(PLANTS)));
			}

			color = blend(color, getColorForHeight());
			color = blend(color, getColorForHighlights().scale(1.0, 1.0, 1.0, 0.25));
			color = blend(color, getColorForShadows().scale(1.0, 1.0, 1.0, 0.25));

			gr.setColor(new Color(color.r, color.g, color.b, 255));
			gr.fillRect(x, y, w, h);
		}
	}
	
	private int getInterpolatedWater(double step, int i, int j) {
		
		int topValue         = top.getResource(WATER) + top.getResource(WATER_TRAIL);
		int rightValue       = right.getResource(WATER) + right.getResource(WATER_TRAIL);
		int bottomValue      = bottom.getResource(WATER) + bottom.getResource(WATER_TRAIL);
		int leftValue        = left.getResource(WATER) + left.getResource(WATER_TRAIL);
		int centerValue      = getResource(WATER) + getResource(WATER_TRAIL);

		double interpolationValue = env.getWaterInterpolationFactor();
		
		double deltaTop    = ((double)(centerValue - topValue   )) / (step * interpolationValue);
		double deltaRight  = ((double)(centerValue - rightValue )) / (step * interpolationValue);
		double deltaLeft   = ((double)(centerValue - leftValue  )) / (step * interpolationValue);
		double deltaBottom = ((double)(centerValue - bottomValue)) / (step * interpolationValue);

		double d = 0.0;
		int s2 = step % 2 == 0 ? FastMath.rint((step) / 2.0) : FastMath.rint((step-1) / 2.0);

		double leftI   = i < s2 ?  s2 - i : 0;
		double topJ    = j < s2 ?  s2 - j : 0;
		double rightI  = i > s2 ?  i - s2 : 0;
		double bottomJ = j > s2 ?  j - s2 : 0;
		
		d -= (leftI   * deltaLeft);
		d -= (topJ    * deltaTop);
		d -= (rightI  * deltaRight);
		d -= (bottomJ * deltaBottom);
		
		return FastMath.rint(centerValue + d);
	}
	
	private int getInterpolatedResource(int name, double step, int i, int j) {

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
		int s2 = step % 2 == 0 ? FastMath.rint((step) / 2.0) : FastMath.rint((step-1) / 2.0);

		int leftI   = i < s2 ?  s2 - i : 0;
		int topJ    = j < s2 ?  s2 - j : 0;
		int rightI  = i > s2 ?  i - s2 : 0;
		int bottomJ = j > s2 ?  j - s2 : 0;
		
		d -= (leftI   * deltaLeft);
		d -= (topJ    * deltaTop);
		d -= (rightI  * deltaRight);
		d -= (bottomJ * deltaBottom);

		return FastMath.rint(centerValue + d);
	}

	@Override
	public void update(Collection<Effect> effects, final long dt) {

		int treeLine = env.getTreeLine();
		int humus = getResource(HUMUS);
		int water = getResource(WATER);

		if(type > 0) {
			addResource(WATER, env.getWaterSourceAmount());
		}

		// add plants effect
		if(getResource(TERRAIN) < treeLine && water == 0 && humus > 0 && hasResource(MOISTURE)) {
			
			effects.add(new PlantsEffect(this));
			
		} else {
			
			setResource(PLANTS, 0);
		}

		if(hasResource(PLANTS) || hasResource(FIRE)) {
			effects.add(new FireEffect(this));
		}

		// remove ashes
		if(hasResource(ASH) && Gaia.rand.nextDouble() > 0.95) {
			addResource(ASH, -1);
		}

		// simulate water
		if(water > 0) {
			
			setResource(WATER_TRAIL, env.getWaterTrail());
			setResource(MOISTURE, 255);

			Collections.sort(allNeighbours, terrainHeightComparator);
			for(final Resource n : allNeighbours) {

				effects.add(new Effect(n) {

					@Override public void effect() {

						int localTerrain = getResource(TERRAIN);
						int neighbourTerrain = affectedResource.getResource(TERRAIN);

						int localWater = getResource(WATER);
						int neighbourWater = affectedResource.getResource(WATER);

						int combinedLocalHeight = localTerrain + localWater;
						int combinedNeighbourHeight = neighbourTerrain + neighbourWater;

						if(localTerrain == neighbourTerrain) {

							// level terrain, equalize water height
							int d = ((localWater + neighbourWater) / 2);
							int m = (localWater + neighbourWater) % 2;

							setResource(WATER, d + m);
							affectedResource.setResource(WATER, d);
							
						} else {

							if(combinedLocalHeight > combinedNeighbourHeight) {
								
								if(combinedLocalHeight > neighbourTerrain) {
									
									if(localWater > 0) {
										
										int dh = combinedLocalHeight - neighbourTerrain;
										int amount = FastMath.min(dh, localWater);

										int v = amount / 8;
										int d = amount % 8;

										affectedResource.addResource(WATER, v+d);
										addResource(WATER, -(v+d));
										
										addResource(FLOW, -(v+d));
										affectedResource.addResource(FLOW, (v+d));

										new ErosionEffect(Resource.this).effect();
									}
									
								} else if(localWater > neighbourWater) {
									
									int d = localWater - neighbourWater;
									
									if(d > 8) {

										int v = (d / 4) + (d % 4) - 1;

										affectedResource.addResource(WATER, v);
										addResource(WATER, -v);
										
									} else if(d > 4) {

										int v = (d / 2) + (d % 2) - 1;

										affectedResource.addResource(WATER, v);
										addResource(WATER, -v);
										
									} else {

										affectedResource.addResource(WATER, 1);
										addResource(WATER, -1);
									}
								}
								
							}
						}
					}
				});
			}

			// evaporization
			if(Gaia.rand.nextDouble() > 0.99) {
				addResource(WATER, -1);
			}
			
		} else {

			if(hasResource(MOISTURE) || hasResource(WATER_TRAIL)) {

				env.activate(this);
				
				if(Gaia.rand.nextDouble() > 0.9) {

					if(hasResource(MOISTURE)) {
						addResource(MOISTURE, -1);
					}
				}

				if(hasResource(WATER_TRAIL)) {
					addResource(WATER_TRAIL, -1);
				}
			}
		}

		// moisture effect randomly
		if(Gaia.rand.nextDouble() > 0.95) {

			effects.add(new Effect(this) {
				@Override public void effect() {

					int moisture = affectedResource.getResource(MOISTURE);
					int m = 0;

					for(Resource n : affectedResource.getNeighbours()) {

						int nm = n.getResource(MOISTURE);
						if(n.higher(affectedResource, -32)) {
							m += ((nm - moisture));
						}
					}

					int v = (m/8 + m%8);

					affectedResource.addResource(MOISTURE, v);
				}
			});
		}
						
		// continuous calculation of surface normals (spread calculation time)
		env.calculateNormal(this);

		if(type == 0 && !hasResource(WATER_TRAIL) && !hasResource(WATER) && !hasResource(PLANTS)) {
			env.deactivate(this);
			
			effects.add(new Effect(this) {
				@Override public void effect() {
					affectedResource.getEnvironment().calculateNormal(affectedResource);
					for(Resource n : affectedResource.getNeighbours(false)) {
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
	
	public boolean higher(Resource res, int amount) {
		return getResource(TERRAIN) - res.getResource(TERRAIN) > amount;
	}

	public boolean isSink() {
		return isSink;
	}

	public void setSink(boolean isSink) {
		this.isSink = isSink;
	}
	
	public int getSunExposure() {
		return FastMath.rint((double)(-getNormalX()-getNormalY()) * 0.5);
	}
	
	private IntColor getColorForWater(int water) {

		if(water > 0) {
			
			int b = 180 - (water / 4);
			if(b <  64) b =  64;
			if(b > 255) b = 255;

			int a = 32 + (water * 2);
			if(a <   0) a =   0;
			if(a > 255) a = 255;

			return new IntColor(0, 0, b, a);
		}
		
		return new IntColor(0, 0, 0, 0);
	}
	
	private IntColor getColorForPlants(int plants) {

		if(plants > 0) {

			int g = 128 + (plants * 2);
			if(g <  64) g =  64;
			if(g > 255) g = 255;

			int a = 128 + (plants * 2);
			if(a <   0) a =   0;
			if(a > 255) a = 255;

			return new IntColor(0, a, 0, a);
		}
		
		return new IntColor(0, 0, 0, 0);
	}
	
	private IntColor getColorForFire(int fire) {

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

			return new IntColor(r, g, b, a);
		}
		
		return new IntColor(0, 0, 0, 0);
	}
	
	private IntColor getColorForTerrain() {
		return getColorForTerrain(getResource(TERRAIN));
	}
	
	private IntColor getColorForTerrain(int height) {

		int h = FastMath.rint(((double)height / (double)env.getMaxHeight()) * heightZoneCount) - 1;
		int m = FastMath.rint(((double)getResource(MOISTURE) / (double)255.0) * moistZoneCount) - 1;		
		
		return getColorForZone(h, m);
		
	}
	
	private IntColor getColorForHeight() {
		return getColorForHeight(getResource(TERRAIN));
	}
	
	private IntColor getColorForHeight(int height) {
		
		int x = FastMath.rint(((double)height / (double)env.getMaxHeight()) * 128.0) + 128;
		if(x <   0) x =   0;
		if(x > 255) x = 255;
		
		return new IntColor(0, 0, 0, 255-x);
	}
	
	private IntColor getColorForShadows() {
		int dark = getNormalX()+getNormalY();
		return getColorForShadows(dark);
	}
	
	private IntColor getColorForShadows(int d) {
		
		int dark = FastMath.rint((double)d * env.getInclinationBrightnessFactor() * 0.4);
		if(dark <   0) dark =   0;
		if(dark > 255) dark = 255;

		return new IntColor(0, 0, 0, dark);
	}
	
	private IntColor getColorForHighlights() {
		int light = -getNormalX()-getNormalY();
		return getColorForHighlights(light);
	}
	
	private IntColor getColorForHighlights(int l) {
		
		int light = FastMath.rint((double)l * env.getInclinationBrightnessFactor() * 0.4);
		if(light <   0) light =   0;
		if(light > 255) light = 255;

		return new IntColor(255, 255, 255, light);
		
	}
	
	private IntColor getColorForZone(int height, int moisture) {
		
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
	
	public int getNeighbourResources(int name, Direction... which) {
		
		int sum = 0;
		
		for(Direction d : which) {
			sum += getNeighbour(d).getResource(name);
		}
		
		return sum;
	}
	
	public int getNeighbourWater(Direction... which) {
		
		int sum = 0;
		
		for(Direction d : which) {
			sum += getNeighbour(d).getResource(WATER);
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

	@Override
	public void setHover(boolean hover) {
		this.hover = hover;
	}
	
	private static class IntColor {
		
		public int r = 0;
		public int g = 0;
		public int b = 0;
		public int a = 0;
		
		public IntColor(int r, int g, int b) {
			this(r, g, b, 255);
		}
		
		public IntColor(int r, int g, int b, int a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			
			validate();
		}
		
		public IntColor scale(double f) {
			
			this.r = FastMath.rint(r * f);
			this.g = FastMath.rint(g * f);
			this.b = FastMath.rint(b * f);
			this.a = FastMath.rint(a * f);
			
			validate();
			
			return this;
		}
		
		public IntColor scale(double fr, double fg, double fb, double fa) {
			
			this.r = FastMath.rint(r * fr);
			this.g = FastMath.rint(g * fg);
			this.b = FastMath.rint(b * fb);
			this.a = FastMath.rint(a * fa);
			
			validate();
			
			return this;
		}
		
		private void validate() {
			
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
	
	private static IntColor add(IntColor color, int rgb) {
		
		int r = color.r + ((rgb >> 16) & 0xff);
		int g = color.g + ((rgb >>  8) & 0xff);
		int b = color.b + ((rgb      ) & 0xff);

		if(r < 0) r = 0;
		if(g < 0) g = 0;
		if(b < 0) b = 0;
		
		if(r > 255) r = 255;
		if(g > 255) g = 255;
		if(b > 255) b = 255;
		
		return new IntColor(r, g, b);
	}
}
