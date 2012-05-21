package com.morgner.gaia;

import com.morgner.gaia.entity.HydroPlant;
import com.morgner.gaia.entity.PowerPlant;
import com.morgner.gaia.entity.Structure;
import com.morgner.gaia.entity.WaterPump;
import com.morgner.gaia.entity.Wire;
import com.morgner.gaia.util.FastMath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.border.BevelBorder;

/**
 *
 * @author Christian Morgner
 */
public class Gaia extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener {
	
	public static final Random rand = new Random(2);
	
	private Dimension buttonDimension = new Dimension(160, 25);
	private JToggleButton panButton = null;
	private JToggleButton elevateButton = null;
	private JToggleButton lowerButton = null;
	private JToggleButton smoothButton = null;
	private JToggleButton addWaterButton = null;
	private JToggleButton removeWaterButton = null;
	private JToggleButton addFireButton = null;
	private JToggleButton structureButton = null;
	private JComboBox structureSelector = null;
	
	private JLabel statusBar = null;
	
	private StringBuffer statusBuffer = new StringBuffer(200);
	private ScheduledExecutorService timer = null;
	private boolean running = false;
	private Canvas canvas = null;
	private Entity entity = null;
	
	private Environment environment = null;
	private int viewportWidth = 80;
	private int viewportHeight = 80;
	private boolean smooth = false;
	private boolean fire = false;
	private boolean plant = false;
	private boolean pump = false;
	private boolean structure = false;
	private boolean pan = false;
	private int width = 128;	// must be a power of 2 plus 1 to support terrain generation algo.
	private int height = 128;	// must be a power of 2 plus 1 to support terrain generation algo.
	private int cellSize = 10;
	private int level = -6;
	private int keyMask = 0;
	private int water = 0;
	private long dt = 50;
	private long gt = 20;
	
	private static final int LEFT   = 1;
	private static final int RIGHT  = 2;
	private static final int UP     = 4;
	private static final int DOWN   = 8;
	
	public Gaia() {
		
		super("Gaia v0.1", GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setUndecorated(true);
		
		setMinimumSize(new Dimension(200 + viewportWidth * cellSize, viewportHeight * cellSize));
		setMaximumSize(new Dimension(200 + viewportWidth * cellSize, viewportHeight * cellSize));
		
		timer = Executors.newScheduledThreadPool(2);
		
		environment = new Environment(cellSize, width, height, viewportWidth, viewportHeight);
		canvas = new Canvas(environment);
		
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		controlsPanel.setPreferredSize(new Dimension(194, viewportHeight * cellSize));

		ButtonGroup buttonGroup = new ButtonGroup();
		
		// toggle buttons
		panButton = addToggleButton(controlsPanel, buttonGroup, "Pan");
		lowerButton = addToggleButton(controlsPanel, buttonGroup, "Lower Terrain");
		elevateButton = addToggleButton(controlsPanel, buttonGroup, "Elevate Terrain");
		smoothButton = addToggleButton(controlsPanel, buttonGroup, "Smooth Terrain");
		addWaterButton = addToggleButton(controlsPanel, buttonGroup, "Add Water");
		removeWaterButton = addToggleButton(controlsPanel, buttonGroup, "Remove Water");
		addFireButton = addToggleButton(controlsPanel, buttonGroup, "Set Fire");
		structureButton = addToggleButton(controlsPanel, buttonGroup, "Add structure");
		
		structureSelector = new JComboBox();
		structureSelector.setPreferredSize(buttonDimension);
		structureSelector.setEditable(false);
		
		structureSelector.addItem(new StructureTemplate("Remove Structure",          null));
		structureSelector.addItem(new StructureTemplate("Wire",                      Wire.class));
		structureSelector.addItem(new StructureTemplate("Hydroelectric Power Plant", HydroPlant.class));
		structureSelector.addItem(new StructureTemplate("Water Pump",                WaterPump.class));
		
		controlsPanel.add(structureSelector);
		
		controlsPanel.add(Box.createRigidArea(buttonDimension));
		
		statusBar = new JLabel("Welcome to Gaia");
		statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(canvas, BorderLayout.CENTER);
		getContentPane().add(controlsPanel, BorderLayout.EAST);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		pack();

		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);

		canvas.createBufferStrategy(3);
		canvas.initialize();
		
		running = true;

		// start initialization
		timer.schedule(new Runnable() {
			@Override public void run() {
				
				environment.initialize(statusBar);

				// when initialization is finished, start update thread
				timer.schedule(new Runnable() {
					@Override public void run() {
						update();
					}
				}, dt, TimeUnit.MILLISECONDS);
			}
		}, 0, TimeUnit.MILLISECONDS);
		
		// start paint thread
		timer.schedule(new Runnable() {
			@Override public void run() {
				paint();
			}
		}, gt, TimeUnit.MILLISECONDS);
	}

	private JToggleButton addToggleButton(JPanel controlsPanel, ButtonGroup buttonGroup, String label) {
		
		JToggleButton button = new JToggleButton(label);
		button.setPreferredSize(buttonDimension);
		button.addActionListener(this);
		controlsPanel.add(button);
		buttonGroup.add(button);
		
		return button;
	}
	
	@Override
	public void finalize() throws Throwable
	{
		super.finalize();
		timer.shutdownNow();
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		switch(e.getKeyChar())
		{
			case 's':
				if(running) {
					running = false;
				} else {
					running = true;
					timer.schedule(new Runnable() {
						@Override public void run() {
							update();
						}
					}, dt, TimeUnit.MILLISECONDS);
				}
				break;
			
			case 'q':
				running = false;
				timer.shutdownNow();
				try { Thread.sleep(dt*2); } catch(Throwable t) {}
				System.exit(0);
				break;
				
			default:
				break;
		}
	}
	
	public void paint() {
		
		int dx = 0;
		int dy = 0;
		if((keyMask & LEFT)  == LEFT)   dx = -10;
		if((keyMask & RIGHT) == RIGHT)  dx =  10;
		if((keyMask & UP)    == UP)     dy = -10;
		if((keyMask & DOWN)  == DOWN)   dy =  10;
		
		if(dx != 0 || dy != 0) {
			environment.moveViewport(dx, dy);
		}

		try { canvas.paint(); } catch(Throwable t) { t.printStackTrace(); }

		if(running) {
			
			timer.schedule(new Runnable() {
				@Override public void run() {
					paint();
				}
			}, gt, TimeUnit.MILLISECONDS);
		}
		
	}
	
	public void update() {
		
		try { environment.update(dt); } catch(Throwable t) { }

		if(running) {

			try {
				timer.schedule(new Runnable() {
					@Override public void run() {
						update();
					}
				}, dt, TimeUnit.MILLISECONDS);
				
			} catch(Throwable t) {t.printStackTrace();}
		}
		
		statusBuffer.append(environment.getActiveResources().size());
		statusBuffer.append(" active resources, ");
		statusBuffer.append(environment.getAverageUpdateTime());
		statusBuffer.append(" ms update time");
				
		statusBar.setText(statusBuffer.toString());
		statusBuffer.setLength(0);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		switch(e.getKeyCode()) {

			case KeyEvent.VK_LEFT:
				keyMask |= LEFT;
				break;
				
			case KeyEvent.VK_RIGHT:
				keyMask |= RIGHT;
				break;
				
			case KeyEvent.VK_UP:
				keyMask |= UP;
				break;
				
			case KeyEvent.VK_DOWN:
				keyMask |= DOWN;
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		switch(e.getKeyCode()) {

			case KeyEvent.VK_LEFT:
				keyMask ^= LEFT;
				break;
				
			case KeyEvent.VK_RIGHT:
				keyMask ^= RIGHT;
				break;
				
			case KeyEvent.VK_UP:
				keyMask ^= UP;
				break;
				
			case KeyEvent.VK_DOWN:
				keyMask ^= DOWN;
				break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		Entity ent = environment.findEntity(e.getPoint());
		if(ent != null) {
			environment.center(ent.getX(), ent.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		if(entity == null) {
			entity = environment.findEntity(e.getPoint());
		}
		
		userInteraction();
		
		if(entity != null) {
			environment.setPanX(entity.getX());
			environment.setPanY(entity.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		entity = null;
		environment.setPanX(-1);
		environment.setPanY(-1);
		
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		entity = environment.findEntity(e.getPoint());

		if(pan) {
			if(entity != null) {
				environment.pan(entity.getX(), entity.getY());
			}
			
		} else {

			userInteraction();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		if(environment.getCellSize() >= environment.getInteractionZoomLevel()) {

			if(entity != null) {
				entity.setHover(false);
			}
			
			entity = environment.findEntity(e.getPoint());
			if(entity != null) {
				entity.setHover(true);
			}			
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		environment.zoom(e.getWheelRotation());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		structure = false;
		smooth = false;
		fire = false;
		pan = false;
		level = 0;
		water = 0;
		
		if(e.getSource().equals(panButton)) {
			pan = true;
		} else if(e.getSource().equals(lowerButton)) {
			level = -8;
		} else if(e.getSource().equals(elevateButton)) {
			level = 8;
		} else if(e.getSource().equals(smoothButton)) {
			smooth = true;
		} else if(e.getSource().equals(addWaterButton)) {
			water = 20;
		} else if(e.getSource().equals(removeWaterButton)) {
			water = -255;
		} else if(e.getSource().equals(addFireButton)) {
			fire = true;
		} else if(e.getSource().equals(structureButton)) {
			structure = true;
		}
	}
	
	private void userInteraction() {

		if(entity != null) {
			
			if(entity instanceof Resource) {

				Resource res = (Resource)entity;
				
				if(level != 0) {

					lower(res, level);
				}
				
				if(water != 0) {
					
					res.addResource(Resource.WATER, water);
					if(res.getResource(Resource.WATER) < 0) {
						res.setResource(Resource.WATER, 0);
					}
				}
				
				
				if(fire) {
					
					res.setResource(Resource.FIRE, 1);
				}
				
				if(smooth) {
					smooth(res, 0);
				}
				
				if(structure) {
					
					StructureTemplate template = (StructureTemplate)structureSelector.getSelectedItem();
					res.setStructure(template.createStructure(res));
				}
			}
			
		}
		
	}

	private void lower(final Resource res, final int level) {
		
		if(level == 0) {
			return;
		}

		Set<Resource> s = new LinkedHashSet<Resource>();
		
		for(Resource n1 : res.getNeighbours(false)) {
			
			for(Resource n2 : n1.getNeighbours(false)) {
				
				for(Resource n3 : n2.getNeighbours(false)) {
					
					if(!s.contains(n3)) {
						n3.addResource(Resource.TERRAIN, (level / 4));
					}
					s.add(n3);
				}
				if(!s.contains(n2)) {
					n2.addResource(Resource.TERRAIN, (level / 3));
				}
				s.add(n2);
			}
			if(!s.contains(n1)) {
				n1.addResource(Resource.TERRAIN, (level / 2));
			}
			s.add(n1);
		}

		res.addResource(Resource.TERRAIN, (level));
	}
	
	private void smooth(final Resource res, int depth) {
		
		if(depth > 1) {
			return;
		}

		int t = res.getResource(Resource.TERRAIN);
		int sum = 0;

		for (Resource n1 : res.getNeighbours()) {
			sum += n1.getResource(Resource.TERRAIN) - t;
		}

		res.addResource(Resource.TERRAIN, (FastMath.rint(((double) sum / 4.0) * 0.5)));
		
		for(Resource n : res.getNeighbours(false)) {
			smooth(n, depth+1);
		}
	}
	
	public static void main(String[] args) {
		
		Gaia e = new Gaia();
		e.setVisible(true);
	}
	
	private static class StructureTemplate {
		
		private String name = null;
		private Class type = null;
		
		public StructureTemplate(String name, Class type) {
			this.name = name;
			this.type = type;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public Structure createStructure(Resource resource) {
			
			try {
				return (Structure)type.getConstructor(Resource.class).newInstance(resource);
				
			} catch(Throwable t) { }
			
			return null;
		}
	}
}
