package com.morgner.gaia;

import java.awt.Graphics;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Christian Morgner
 */
public class Environment {

	private final Set<Resource> pendingAdditionResources = new LinkedHashSet<Resource>();
	private final Set<Resource> pendingRemovalResources = new LinkedHashSet<Resource>();
	private final Set<Resource> activeResources = new LinkedHashSet<Resource>();
	private final Queue<Effect> effects = new ConcurrentLinkedQueue<Effect>();
	private List<Entity> entities = new LinkedList<Entity>();
	private Resource[][] resources = null;
	private int cellSize = 0;
	private int width = 0;
	private int height = 0;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportX0 = 0;
	private int viewportY0 = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	
	private int terrainGenerationIterations = 25;
	private int terrainGenerationConstant = 50;
	private int terrainSmoothingIterations = 4;
	
	private int waterSourceAmount = 2;
	private int waterSources = 20;
	private int waterTrail = 20;
	private int minHeight = 0;
	private int maxHeight = 0;
	private int treeLine = 0;
	private int seaLevel = 0;
	private int seaWaterHeight = 10;
	private int plantsFactor = 1;
	
	private double treeLineFactor = 0.75;
	private double seaLevelFactor = 0.1;
	
	public Environment(int r, int width, int height, int viewportWidth, int viewportHeight) {
		
		this.cellSize = r;
		this.width = width;
		this.height = height;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		
		resources = new Resource[width][height];

		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				resources[i][j] = new Resource(this, i, j);
			}
		}
	}
	
	public void initialize() {
		
		List<Effect> effects = new LinkedList<Effect>();

		try {
			
			// randomize terrain
			for(int i=0; i<terrainGenerationIterations; i++) {
				
				genTerrain1(0, 0, width, 0, i);
				genTerrain2(0, 0, width, 0, i);
			}

		} catch(Throwable t) {
			
			t.printStackTrace();
		}

		// smooth terrain
		for(int a=0; a<terrainSmoothingIterations; a++) {
			
			for(int i=0; i<width; i++) {
				for(int j=0; j<height; j++) {

					Resource res = resources[i][j];

					int t = res.getTerrain();
					int sum = 0;

					for(Resource n1 : res.getNeighbours()) {
						sum += n1.getTerrain() - t; 
					}

					final int val = (int)Math.rint(((double)sum / 4.0) * 0.1);

					effects.add(new Effect(res) {
						@Override public Effect effect() {
							affectedResource.addTerrain(val);
							return null;
						}
					});
				}
			}

			for(Effect e : effects) {
				e.effect();
			}

			effects.clear();
		}
		
		// find minimum and maximum terrain height
		minHeight = 1000000;
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				int terrain = resources[i][j].getTerrain();
				if(terrain < minHeight) {
					minHeight = terrain;
				}
				if(terrain > maxHeight) {
					maxHeight = terrain;
				}
			}
		}
		
		// align terrain height to 0
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				
				resources[i][j].addTerrain(-minHeight);
			}
		}
		
		maxHeight -= minHeight;
		minHeight = 0;
		
		System.out.println("minHeight: " + minHeight + ", maxHeight: " + maxHeight);
		
		// set tree line (max height of plant growth
		treeLine = (int)Math.rint((double)maxHeight * treeLineFactor);
		seaLevel = (int)Math.rint((double)maxHeight * seaLevelFactor);

		// add sea level flag
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				
				int terrain = resources[i][j].getTerrain();

				if(terrain < treeLine && terrain >= seaLevel) {
					resources[i][j].setResource("humus", 5);
				}
				
				if(terrain <= seaLevel) {
					
					resources[i][j].setResource("sink", 1);
					resources[i][j].setWater(seaLevel - terrain);
				}
			}
		}
		
		// add water sources
		int minWaterSourceHeight = treeLine;
		int sources = 0;
		while(sources < waterSources) {
			
			int tries = 0;
			while(tries++ < 10000) {
				Resource res = getResource(Gaia.rand.nextInt(width), Gaia.rand.nextInt(height));
				if(res != null) {
					if(res.getTerrain() > minWaterSourceHeight) {
						res.setType(1);
						res.addWater(2);
						sources++;
						break;
					}

				}			
			}
			minWaterSourceHeight -= 1;
		}
	}
	
	public Resource getResource(int x, int y) {
		return resources[wrapX(x)][wrapY(y)];
	}

	public void update(long dt) {
		
		// collect effects from environment
		for(Resource res : activeResources) {
			effects.addAll(res.update(dt));
		}

		// collect effects from entities
		for(Entity entity : entities) {
			List<Effect> e = entity.update(dt);
			effects.addAll(e);
		}
		
		// apply effects on environment
		while(effects.peek() != null) {

			Effect e = effects.poll();
			
			Effect secondary = e.effect();
			if(secondary != null) {
				effects.add(secondary);
			}
		}
		
		activeResources.removeAll(pendingRemovalResources);
		activeResources.addAll(pendingAdditionResources);
		pendingAdditionResources.clear();
		pendingRemovalResources.clear();
	}
	
	public void draw(Graphics g) {

		// update viewport
		int tmpX = viewportX;
		int tmpY = viewportY;
		
		viewportX += (int)Math.rint((double)(viewportX - viewportX0) * 0.1);
		viewportY += (int)Math.rint((double)(viewportY - viewportY0) * 0.1);
		
		viewportX0 = tmpX;
		viewportY0 = tmpY;
		
		if(viewportX < 0) viewportX = 0;
		if(viewportY < 0) viewportY = 0;
		
		if(viewportX + viewportWidth > width) viewportX = width - viewportWidth;
		if(viewportY + viewportHeight > height) viewportY = height - viewportHeight;


		for(int i=0; i<viewportWidth+3; i++) {
			for(int j=0; j<viewportHeight+3; j++) {
				
				int x = i + viewportX;
				int y = j + viewportY;

				if(x >= 0 && x < width && y >= 0 && y < height) {
					
					g.setColor(resources[x][y].getCellColor());
					g.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
				}
			}
		}
	}
	
	public void zoom(int amount) {
		
		int oldSize = cellSize;
		cellSize -= amount * 2;
		
		if(cellSize < 4) {
			cellSize = 4;
		} else if(cellSize > 30) {
			cellSize = 30;
		}
		
		// scale viewport size accordingly
		double f = (double)oldSize / (double)cellSize;
		
		if(f != 1.0) {
			
			int oldViewportWidth = viewportWidth;
			int oldViewportHeight = viewportHeight;

			viewportWidth = (int)Math.rint((double)viewportWidth * f);
			viewportHeight = (int)Math.rint((double)viewportHeight * f);

			int offsetX = (int)Math.rint((double)(viewportWidth - oldViewportWidth) / 2.0);
			int offsetY = (int)Math.rint((double)(viewportHeight - oldViewportHeight) / 2.0);
			
			viewportX -= offsetX;
			viewportY -= offsetY;
			viewportX0 -= offsetX;
			viewportY0 -= offsetY;
		}
		
	}
	
	public void pan(int dx, int dy) {
		
		viewportX += dx * (30.0 / (double)cellSize);
		viewportY += dy * (30.0 / (double)cellSize);
		
		if(viewportX < 0) viewportX = 0;
		if(viewportY < 0) viewportY = 0;
		
		if(viewportX + viewportWidth > width) viewportX = width - viewportWidth;
		if(viewportY + viewportHeight > height) viewportY = height - viewportHeight;
		
	}

	public int getCellSize() {
		return cellSize;
	}

	public void setCellSize(int size) {
		this.cellSize = size;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public Entity findEntity(Point pos) {
		
		for(Entity e : entities) {
			if(e.contains(pos)) {
				return e;
			}
		}

		Point localPos = new Point(pos.x + (viewportX * cellSize), pos.y + (viewportY * cellSize));
		
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				
				if(resources[i][j].contains(localPos)) {
					return resources[i][j];
				}
			}
		}

		return null;
	}
	
	// terrain generation
	private int averageDiamond(int x, int y, int radius, int depth, int iteration) {
		
		int x1 = (x);
		int y1 = (y);
		
		int x2 = (x + radius);
		int y2 = (y);
		
		int x3 = (x);
		int y3 = (y + radius);
		
		int x4 = (x + radius);
		int y4 = (y + radius);
		
		Resource topLeft =     getResource(x1, y1);
		Resource topRight =    getResource(x2, y2);
		Resource bottomLeft =  getResource(x3, y3);
		Resource bottomRight = getResource(x4, y4);
		
		int sum = 0;

		sum += topLeft.getTerrain();
		sum += topRight.getTerrain();
		sum += bottomLeft.getTerrain();
		sum += bottomRight.getTerrain();

		sum = (int)Math.rint((double)sum / 4.0) + randomFactor(depth, iteration);

		return sum;
	}
	
	private int averageSquare(int x, int y, int radius, int depth, int iteration) {
		
		int r2 = div2(radius);
		
		int x1 = (x);
		int y1 = (y);
		
		int x2 = (x + radius);
		int y2 = (y);
		
		int x3 = (x + r2);
		int y3 = (y - r2);
		
		int x4 = (x + r2);
		int y4 = (y + r2);
		
		Resource topLeft =     getResource(x1, y1);
		Resource topRight =    getResource(x2, y2);
		Resource bottomLeft =  getResource(x3, y3);
		Resource bottomRight = getResource(x4, y4);
		
		int sum = 0;

		sum += topLeft.getTerrain();
		sum += topRight.getTerrain();
		sum += bottomLeft.getTerrain();
		sum += bottomRight.getTerrain();

		sum = (int)Math.rint((double)sum / 4.0) + randomFactor(depth, iteration);

		return sum;
	}
	
	private void diamondStep(int x, int y, int radius, int depth, int iteration) {
		
		int r2 = div2(radius);
		
		getResource((x + r2), (y + r2)).setTerrain(averageDiamond(x, y, radius, depth, iteration));
	}
	
	private void squareStep(int x, int y, int radius, int depth, int iteration) {

		int r2 = div2(radius);
		
		getResource((x + r2), (y)).setTerrain(averageSquare(x, y, radius, depth, iteration));
		
	}
	
	private void genTerrain1(int x, int y, int radius, int depth, int iteration) {
		
		if(radius < 1) {
			return;
		}
		
		int r2 = div2(radius);

		diamondStep(x, y, radius, depth, iteration); 

		genTerrain1(     x,      y, r2, depth+1, iteration);
		genTerrain1(x + r2,      y, r2, depth+1, iteration);
		genTerrain1(     x, y + r2, r2, depth+1, iteration);
		genTerrain1(x + r2, y + r2, r2, depth+1, iteration);
		
	}
	
	private void genTerrain2(int x, int y, int radius, int depth, int iteration) {
		
		if(radius == 0) {
			return;
		}
		
		int r2 = div2(radius);

		squareStep(x - r2,     y + r2,  radius, depth, iteration); // links
		squareStep(x + r2,     y + r2,  radius, depth, iteration); // rechts
		squareStep(     x,          y,  radius, depth, iteration); // oben
		squareStep(     x, y + radius,  radius, depth, iteration); // unten
		
		genTerrain2(     x,      y, r2, depth+1, iteration);
		genTerrain2(x + r2,      y, r2, depth+1, iteration);
		genTerrain2(     x, y + r2, r2, depth+1, iteration);
		genTerrain2(x + r2, y + r2, r2, depth+1, iteration);
		
	}
	
	private int wrapX(int x) {

		if(x < 0) return width + x - 1;
		if(x >= width) return (x - width);

		return x;
	}
	
	private int wrapY(int y) {
		

		if(y < 0) return height + y - 1;
		if(y >= height) return (y - height);

		return y;
	}

	private int div2(int val) {
		
		return val / 2;
		//return (int)Math.rint((double)val / 2.0);
	}
	
	private int randomFactor(int depth, int iteration) {
		
		int log = (int)Math.rint(Math.log(width)/Math.log(2.0)) + 1;
		
		int v = (terrainGenerationConstant * (terrainGenerationIterations - iteration)) * (log - depth);
		
		return (Gaia.rand.nextInt(v+1)) - (v/2);
		
	}

	public int getTreeLine() {
		return treeLine;
	}
	
	public int getMaxHeight() {
		return maxHeight;
	}
	
	public double getColorFactor() {
		
		return 1.0 / ((double)maxHeight / 255.0);
	}
	
	public int getSeaLevel() {
		return seaLevel;
	}
	
	public int getSeaWaterHeight() {
		return seaWaterHeight;
	}
	
	public void activate(Resource res) {
		if(!activeResources.contains(res)) {
			pendingAdditionResources.add(res);
			
			/*
			// add neighbours too
			for(Resource n : res.getNeighbours()) {
				if(!activeResources.contains(n)) {
					pendingAdditionResources.add(n);
				}
			}
			 * 
			 */
		}
	}
	
	public void deactivate(Resource res) {
		if(activeResources.contains(res)) {
			pendingRemovalResources.add(res);
		}
	}

	public int getWaterTrail() {
		return waterTrail;
	}

	public void setWaterTrail(int waterTrail) {
		this.waterTrail = waterTrail;
	}

	public int getWaterSourceAmount() {
		return waterSourceAmount;
	}

	public void setWaterSourceAmount(int waterSourceAmount) {
		this.waterSourceAmount = waterSourceAmount;
	}

	public int getPlantsFactor() {
		return plantsFactor;
	}

	public void setPlantsFactor(int plantsFactor) {
		this.plantsFactor = plantsFactor;
	}
}
