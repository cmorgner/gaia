package com.morgner.gaia;

import com.morgner.gaia.entity.Animal;
import com.morgner.gaia.entity.Player;
import com.morgner.gaia.util.FastMath;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;
import javax.swing.JLabel;

/**
 *
 * @author Christian Morgner
 */
public class Environment {

	private final Set<Resource> pendingAdditionResources = new LinkedHashSet<Resource>();
	private final Set<Resource> pendingRemovalResources = new LinkedHashSet<Resource>();
	private final Set<Resource> activeResources = new LinkedHashSet<Resource>();
	private final List<Effect> effects = new LinkedList<Effect>();
	private List<Entity> entities = new LinkedList<Entity>();
	private Resource[][] resources = null;
	private Player player = null;
	private int cellSize = 0;
	private int width = 0;
	private int height = 0;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	private int panX = 0;
	private int panY = 0;
	private int iterations = 0;
	private int initialCellSize = 0;
	private int initialViewportWidth = 0;
	private int initialViewportHeight = 0;
	private long averageUpdateTime = 0;
	
	private int terrainGenerationIterations = 50;
	private int terrainGenerationConstant =	100;
	private int terrainSmoothingIterations = 25;
	private double terrainSmoothingConstant = 0.5;
	
	private int waterSourceAmount = 2;
	private int waterSources = 10;
	private int waterTrail = 8;
	private int seaLevel = 0;
	private int seaWaterHeight = 10;
	private double seaLevelFactor = 0.0;
	
	private int minHeight = 0;
	private int maxHeight = 1000;
	private int treeLine = 0;
	private int interactionZoomLevel = 40;
	private int maxZoomLevel = 50;

	private int plantsFactor = 1;
	private int shadowBrightness = 128;
	private double treeLineFactor = 0.675;
	
	private double inclinationBrightnessFactor = 6.0;
	private double waterInterpolationFactor = 1.8;
	
	public Environment(int r, int width, int height, int viewportWidth, int viewportHeight) {

		this.cellSize = r;
		this.width = width;
		this.height = height;
		this.initialCellSize =  cellSize;
		this.initialViewportWidth = viewportWidth;
		this.initialViewportHeight = viewportHeight;

		resources = new Resource[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				resources[i][j] = new Resource(this, i, j);
			}
		}
		
		// initialize resources (performance optimization)
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				resources[i][j].initialize();
			}
		}

		player = new Player(this, width * 2, height * 2);
	}

	public void initialize(JLabel statusBar) {

		long start = System.currentTimeMillis();
		
		statusBar.setText("Generating terrain..");
		
		try {

			// randomize terrain
			for (int i = 0; i < terrainGenerationIterations; i++) {

				genTerrain1(0, 0, width, 0, i);
				genTerrain2(0, 0, width, 0, i);
			}

		} catch (Throwable t) {

			t.printStackTrace();
		}

		statusBar.setText("Smoothing terrain..");
		
		// smooth terrain
		for (int a = 0; a < terrainSmoothingIterations; a++) {

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {

					Resource res = resources[i][j];

					int t = res.getResource(Resource.TERRAIN);
					int sum = 0;

					for (Resource n1 : res.getNeighbours()) {
						sum += n1.getResource(Resource.TERRAIN) - t;
					}

					final int val = (int) FastMath.rint(((double) sum / 4.0) * terrainSmoothingConstant);

					effects.add(new Effect(res) {

						@Override
						public void effect() {
							affectedResource.addResource(Resource.TERRAIN, val);
						}
					});
				}
			}

			for (Effect e : effects) {
				e.effect();
			}

			effects.clear();
		}

		// find minimum and maximum terrain height
		minHeight = 1000000;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int terrain = resources[i][j].getResource(Resource.TERRAIN);
				if (terrain < minHeight) {
					minHeight = terrain;
				}
				if (terrain > maxHeight) {
					maxHeight = terrain;
				}
			}
		}

		// align terrain height to 0
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				resources[i][j].addResource(Resource.TERRAIN, -minHeight);
			}
		}

		maxHeight -= minHeight;
		minHeight = 0;

		// set tree line (max height of plant growth
		treeLine = (int) FastMath.rint((double) maxHeight * treeLineFactor);
		seaLevel = (int) FastMath.rint((double) maxHeight * seaLevelFactor);

		// add sea level flag
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				
				//resources[i][j].addResource(Resource.TERRAIN, Gaia.rand.nextInt(7) - 3);
				int terrain = resources[i][j].getResource(Resource.TERRAIN);

				
				
				if (terrain < treeLine && terrain >= seaLevel) {
					resources[i][j].setResource(Resource.HUMUS, 5);
				}

				if (terrain <= seaLevel) {

					resources[i][j].setResource(Resource.WATER, seaLevel - terrain);
//					resources[i][j].setSink(true);
				}
			}
		}

		statusBar.setText("Adding water sources..");		
		
		// add water sources
		int minWaterSourceHeight = treeLine;
		int sources = 0;
		while (sources < waterSources) {

			int tries = 0;
			while (tries++ < 10000) {
				Resource res = getResource(Gaia.rand.nextInt(width), Gaia.rand.nextInt(height));
				if (res != null) {
					if (res.getResource(Resource.TERRAIN) > minWaterSourceHeight) {
						res.setType(1);
						res.addResource(Resource.WATER, 2);
						sources++;
						break;
					}

				}
			}
			minWaterSourceHeight -= 1;
		}

		statusBar.setText("Calcuating initial terrain normals..");
		
		calculateNormals(true);
		
		// add animal
		for(int i=0; i<0; i++) {
			entities.add(new Animal(this, Gaia.rand.nextInt(width), Gaia.rand.nextInt(height)));
		}
		
		System.out.println((System.currentTimeMillis() - start) + " ms");
	}

	public Resource getResource(int x, int y) {
		return resources[wrapX(x)][wrapY(y)];
	}

	public void update(long dt) {

		long start = System.currentTimeMillis();
		
		// collect effects from environment
		synchronized(getActiveResources()) {
			
			for (Resource res : getActiveResources()) {
				res.update(effects, dt);
			}
		}

		synchronized(getActiveResources()) {
			getActiveResources().removeAll(pendingRemovalResources);
		}
		
		// collect effects from entities
		for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
			Entity entity = it.next();
			entity.update(effects, dt);
			if(!entity.isAlive()) {
				it.remove();
			}
		}
		
		player.update(effects, dt);
		
		for(Effect e : effects) {
			e.effect();
		}
		
		effects.clear();

		synchronized(getActiveResources()) {
			getActiveResources().addAll(pendingAdditionResources);
		}
		
		pendingAdditionResources.clear();
		pendingRemovalResources.clear();
		
		averageUpdateTime += (System.currentTimeMillis() - start);
		averageUpdateTime /= 2;
	}

	public void draw(Graphics g) {

		viewportWidth  = FastMath.rint((double)initialViewportWidth * ((double)initialCellSize / (double)cellSize));
		viewportHeight = FastMath.rint((double)initialViewportHeight * ((double)initialCellSize / (double)cellSize));

		checkViewport();
		
		int vW = viewportWidth + 2;
		int vH = viewportHeight + 2;
				
		for (int i = 0; i < vW; i++) {
			for (int j = 0; j < vH; j++) {

				int x = i + viewportX;
				int y = j + viewportY;

				if (x >= 0 && x < width && y >= 0 && y < height) {
					resources[x][y].drawCell(g, i * cellSize, j * cellSize, cellSize, cellSize);
				}
			}
		}
		
		// draw entities
		for(Entity entity : entities) {
			
			int x = (entity.getX() - viewportX) * cellSize;
			int y = (entity.getY() - viewportY) * cellSize;

			entity.drawCell(g, x, y, cellSize, cellSize);
		}

		// draw player
//		{
//			int x = (player.getX() - getViewportX()) * cellSize;
//			int y = (player.getY() - getViewportY()) * cellSize;
//
//			player.drawCell(g, x, y, cellSize, cellSize);
//		}
	}

	public void zoom(int amount) {

		cellSize -= amount;

		if (cellSize < 2) {
			
			cellSize = 2;
			
		} else if (cellSize > maxZoomLevel) {
			
			cellSize = maxZoomLevel;
		}

		viewportWidth  = FastMath.rint((double)initialViewportWidth * ((double)initialCellSize / (double)cellSize));
		viewportHeight = FastMath.rint((double)initialViewportHeight * ((double)initialCellSize / (double)cellSize));

		viewportX = FastMath.rint(player.getScaledX() - ((double) viewportWidth / 2.0));
		viewportY = FastMath.rint(player.getScaledY() - ((double)viewportHeight / 2.0));

		checkViewport();
		
		// System.out.println(cellSize);
	}

	public void moveViewport(int dx, int dy) {
		player.translate(dx, dy);
	}

	public void pan(int mx, int my) {

		if(panX >= 0 && panY >= 0) {
			
			int dx = (panX - mx);
			int dy = (panY - my);

			viewportX += dx;
			viewportY += dy;

			checkViewport();

			player.setPosition(viewportX + (viewportWidth / 2), viewportY + (viewportHeight / 2));
		}
	}

	public void center(int dx, int dy) {
		player.setPosition(dx, dy);
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
	
	public void checkViewport() {

		if (viewportX + viewportWidth > width) {
			viewportX = width - viewportWidth;
		}
		if (viewportY + viewportHeight > height) {
			viewportY = height - viewportHeight;
		}

		if(viewportX < 0) viewportX = 0;
		if(viewportY < 0) viewportY = 0;
	}

	public Entity findEntity(Point pos) {

		for (Entity e : entities) {
			if (e.contains(pos)) {
				return e;
			}
		}

		Point localPos = new Point(pos.x + (getViewportX() * cellSize), pos.y + (getViewportY() * cellSize));

		int vW = viewportWidth;
		int vH = viewportHeight;
				
		for (int i = 0; i < vW; i++) {
			for (int j = 0; j < vH; j++) {

				int x = i + viewportX;
				int y = j + viewportY;

				if (resources[x][y].contains(localPos)) {
					return resources[x][y];
				}
			}
		}

		return null;
	}

	public void addEntity(Entity entity) {
		entities.add(entity);
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

		Resource topLeft = getResource(x1, y1);
		Resource topRight = getResource(x2, y2);
		Resource bottomLeft = getResource(x3, y3);
		Resource bottomRight = getResource(x4, y4);

		int sum = 1;

		sum += topLeft.getResource(Resource.TERRAIN);
		sum += topRight.getResource(Resource.TERRAIN);
		sum += bottomLeft.getResource(Resource.TERRAIN);
		sum += bottomRight.getResource(Resource.TERRAIN);

		sum = (int) FastMath.rint((double) sum / 4.0) + randomFactor(depth, iteration);

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

		Resource topLeft = getResource(x1, y1);
		Resource topRight = getResource(x2, y2);
		Resource bottomLeft = getResource(x3, y3);
		Resource bottomRight = getResource(x4, y4);

		int sum = 0;

		sum += topLeft.getResource(Resource.TERRAIN);
		sum += topRight.getResource(Resource.TERRAIN);
		sum += bottomLeft.getResource(Resource.TERRAIN);
		sum += bottomRight.getResource(Resource.TERRAIN);

		sum = (int) FastMath.rint((double) sum / 4.0) + randomFactor(depth, iteration);

		return sum;
	}

	private void diamondStep(int x, int y, int radius, int depth, int iteration) {

		int r2 = div2(radius);

		getResource((x + r2), (y + r2)).setResource(Resource.TERRAIN, averageDiamond(x, y, radius, depth, iteration));
	}

	private void squareStep(int x, int y, int radius, int depth, int iteration) {

		int r2 = div2(radius);

		getResource((x + r2), (y)).setResource(Resource.TERRAIN, averageSquare(x, y, radius, depth, iteration));

	}

	private void genTerrain1(int x, int y, int radius, int depth, int iteration) {

		if (radius < 1) {
			return;
		}

		int r2 = div2(radius);

		diamondStep(x, y, radius, depth, iteration);

		genTerrain1(x, y, r2, depth + 1, iteration);
		genTerrain1(x + r2, y, r2, depth + 1, iteration);
		genTerrain1(x, y + r2, r2, depth + 1, iteration);
		genTerrain1(x + r2, y + r2, r2, depth + 1, iteration);

	}

	private void genTerrain2(int x, int y, int radius, int depth, int iteration) {

		if (radius == 0) {
			return;
		}

		int r2 = div2(radius);

		squareStep(x - r2, y + r2, radius, depth, iteration); // links
		squareStep(x + r2, y + r2, radius, depth, iteration); // rechts
		squareStep(x, y, radius, depth, iteration); // oben
		squareStep(x, y + radius, radius, depth, iteration); // unten

		genTerrain2(x, y, r2, depth + 1, iteration);
		genTerrain2(x + r2, y, r2, depth + 1, iteration);
		genTerrain2(x, y + r2, r2, depth + 1, iteration);
		genTerrain2(x + r2, y + r2, r2, depth + 1, iteration);

	}

	private int wrapX(int x) {

		if (x < 0) {
			return width + x - 1;
		}
		if (x >= width) {
			return (x - width);
		}

		return x;
	}

	private int wrapY(int y) {


		if (y < 0) {
			return height + y - 1;
		}
		if (y >= height) {
			return (y - height);
		}

		return y;
	}

	private int div2(int val) {

		return val / 2;
		//return FastMath.rint((double)val / 2.0);
	}

	private int randomFactor(int depth, int iteration) {

		int log = (int) FastMath.rint(FastMath.log(width) / FastMath.log(2.0)) + 1;

		int v = (terrainGenerationConstant * (terrainGenerationIterations - iteration)) * (log - depth);

		return (Gaia.rand.nextInt(v + 1)) - (v / 2);

	}

	public int getTreeLine() {
		return treeLine;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public double getColorFactor() {

		return 255.0 / (double) maxHeight;
	}

	public double getColorFactor(double num) {

		return num / (double) maxHeight;
	}

	public int getSeaLevel() {
		return seaLevel;
	}

	public int getSeaWaterHeight() {
		return seaWaterHeight;
	}

	public boolean isActive(Resource res) {
		return getActiveResources().contains(res) || res.hasStructure();
	}
	
	public void activate(Resource res) {
		if (!activeResources.contains(res)) {
			pendingAdditionResources.add(res);
		}
	}

	public void deactivate(Resource res) {
		if (getActiveResources().contains(res) && !res.hasStructure()) {
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

	public void calculateNormals() {
		calculateNormals(false);
	}
	
	public void calculateNormals(boolean calculateAll) {

		for (int x = 0; x < width; x++) {
			for (int y = 0;	y < height; y++) {
		
				Resource res = getResource(x, y);

				// optimization: calculate normals only for active resources
				if(calculateAll || res.isAlive()) {
					
					calculateNormal(res);
				}
			}
		}
	}
	
	public void calculateNormal(Resource res) {

		int x = res.getX();
		int y = res.getY();
		
		Resource left   = getResource(x-1,   y);
		Resource right  = getResource(x+1,   y);
		Resource top    = getResource(  x, y-1);
		Resource bottom = getResource(  x, y+1);

		int tl =   left.getResource(Resource.TERRAIN);
		int tr =  right.getResource(Resource.TERRAIN);
		int tt =    top.getResource(Resource.TERRAIN) + (top.getResource(Resource.PLANTS) * 4);
		int tb = bottom.getResource(Resource.TERRAIN);

		int sx = tl - tr;
		int sy = tt - tb;

		res.setNormalX(-sx);
		res.setNormalY(sy);
	}

	public double getInclinationBrightnessFactor() {
		return inclinationBrightnessFactor;
	}

	public void setInclinationBrightnessFactor(double inclinationBrightnessFactor) {
		this.inclinationBrightnessFactor = inclinationBrightnessFactor;
	}

	public int getShadowBrightness() {
		return shadowBrightness;
	}

	public void setShadowBrightness(int shadowBrightness) {
		this.shadowBrightness = shadowBrightness;
	}

	public int getViewportX() {
		return viewportX;
	}

	public int getViewportY() {
		return viewportY;
	}

	public void setPanX(int panX) {
		this.panX = panX;
	}

	public void setPanY(int panY) {
		this.panY = panY;
	}
	
	public void setWaterInterpolationFactor(double f) {
		this.waterInterpolationFactor = f;
	}
	
	public double getWaterInterpolationFactor() {
		return waterInterpolationFactor;
	}

	public int getMaxZoomLevel() {
		return maxZoomLevel;
	}

	public int getInteractionZoomLevel() {
		return interactionZoomLevel;
	}

	public long getAverageUpdateTime() {
		return averageUpdateTime;
	}

	public Set<Resource> getActiveResources() {
		return activeResources;
	}
}
