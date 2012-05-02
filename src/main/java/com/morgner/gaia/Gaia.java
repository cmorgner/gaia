package com.morgner.gaia;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Christian Morgner
 */
public class Gaia extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ChangeListener {
	
	public static final Random rand = new Random(2);
	
	private Dimension buttonDimension = new Dimension(160, 25);
	private JToggleButton elevateButton = null;
	private JToggleButton lowerButton = null;
	private JToggleButton addWaterButton = null;
	private JToggleButton removeWaterButton = null;
	private JToggleButton addFireButton = null;
	
	private JSlider waterSourceAmountSlider = null;
	private JSlider waterTrailSlider = null;
	private JSlider plantsSlider = null;
	private JSlider shadowBrightnessSlider = null;
	private JSlider inclinationBrightnessSlider = null;
	
	private ScheduledExecutorService timer = null;
	private boolean running = false;
	private Canvas canvas = null;
	private Entity entity = null;
	
	private Environment environment = null;
	private int viewportWidth = 100;
	private int viewportHeight = 100;
	private boolean fire = false;
	private int width = 513;
	private int height = 513;
	private int cellSize = 10;
	private int level = -6;
	private int keyMask = 0;
	private int water = 0;
	private long dt = 20;
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
		
		timer = Executors.newScheduledThreadPool(10);
		
		environment = new Environment(cellSize, width, height, viewportWidth, viewportHeight);
		canvas = new Canvas(environment);
		
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		controlsPanel.setPreferredSize(new Dimension(194, viewportHeight * cellSize));

		ButtonGroup buttonGroup = new ButtonGroup();
		
		// toggle buttons
		lowerButton = addToggleButton(controlsPanel, buttonGroup, "Lower Terrain");
		elevateButton = addToggleButton(controlsPanel, buttonGroup, "Elevate Terrain");
		addWaterButton = addToggleButton(controlsPanel, buttonGroup, "Add Water");
		removeWaterButton = addToggleButton(controlsPanel, buttonGroup, "Remove Water");
		addFireButton = addToggleButton(controlsPanel, buttonGroup, "Set Fire");
		
		controlsPanel.add(Box.createRigidArea(buttonDimension));
		
		// sliders
		waterTrailSlider = addSlider(controlsPanel, "Water trail length", 2, 20, 10);
		waterSourceAmountSlider = addSlider(controlsPanel, "Water source strength", 0, 50, 1);
		plantsSlider = addSlider(controlsPanel, "Plant growth", 0, 6, 1);
		shadowBrightnessSlider = addSlider(controlsPanel, "Shadow brightness", 0, 255, 128);
		inclinationBrightnessSlider = addSlider(controlsPanel, "Slope shadow brightness", 0, 800, 260);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(canvas, BorderLayout.CENTER);
		getContentPane().add(controlsPanel, BorderLayout.EAST);
		
		pack();

		/*
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice dev      = env.getDefaultScreenDevice();
		
		try {
			
			DisplayMode mode        = null;
			
			for(DisplayMode m : dev.getDisplayModes()) {
				
				System.out.println(m.getWidth() + "x" + m.getHeight() + "@" + m.getBitDepth() + " " + m.getRefreshRate());
				
				if(m.getWidth() == 800 && m.getHeight() == 600 && m.getRefreshRate() == 75) {
					mode = m;
				}
			}
			
			dev.setFullScreenWindow(this);
			dev.setDisplayMode(mode);
			
		} catch(Throwable t) {
			
			t.printStackTrace();
			dev.setFullScreenWindow(null);
		}
		*/
		
		
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		
		canvas.createBufferStrategy(2);
		
		running = true;

		// start initialization
		timer.schedule(new Runnable() {
			@Override public void run() {
				
				environment.initialize();

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

	private JSlider addSlider(JPanel controlsPanel, String label, int min, int max, int value) {
		
		JSlider slider = new JSlider(min, max, value);
		slider.setPreferredSize(buttonDimension);
		slider.addChangeListener(this);
		controlsPanel.add(new JLabel(label));
		controlsPanel.add(slider);

		return slider;
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
					update();
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
		
		try {
			canvas.paint();
			
		} catch(Throwable t) {
		}

		if(running) {
			
			timer.schedule(new Runnable() {
				@Override public void run() {
					paint();
				}
			}, gt, TimeUnit.MILLISECONDS);
		}

		int dx = 0;
		int dy = 0;
		if((keyMask & LEFT)  == LEFT)   dx = -1;
		if((keyMask & RIGHT) == RIGHT)  dx =  1;
		if((keyMask & UP)    == UP)     dy = -1;
		if((keyMask & DOWN)  == DOWN)   dy =  1;
		
		if(dx != 0 || dy != 0) {
			environment.pan(dx, dy);
		}
		
	}
	
	public void update() {
		
		environment.update(dt);
		
		if(running) {

			try {
				timer.schedule(new Runnable() {
					@Override public void run() {
						update();
					}
				}, dt, TimeUnit.MILLISECONDS);
				
			} catch(Throwable t) {t.printStackTrace();}
		}
		
	}
	
	/*
	public static int div(int level, double div) {
		return (int)Math.floor((double)level / div);
	}
	* */
	
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
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		if(entity == null) {
			entity = environment.findEntity(e.getPoint());
		}
		
		userInteraction();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		entity = null;
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
		userInteraction();
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		environment.zoom(e.getWheelRotation());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		fire = false;
		level = 0;
		water = 0;
		
		if(e.getSource().equals(lowerButton)) {
			level = -6;
		} else if(e.getSource().equals(elevateButton)) {
			level = 6;
		} else if(e.getSource().equals(addWaterButton)) {
			water = 2;
		} else if(e.getSource().equals(removeWaterButton)) {
			water = -255;
		} else if(e.getSource().equals(addFireButton)) {
			fire = true;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		
		if(e.getSource().equals(waterTrailSlider)) {
			
			environment.setWaterTrail(waterTrailSlider.getValue());
			
		} else if(e.getSource().equals(waterSourceAmountSlider)) {
			
			environment.setWaterSourceAmount(waterSourceAmountSlider.getValue());
			
		} else if(e.getSource().equals(plantsSlider)) {
			
			environment.setPlantsFactor(plantsSlider.getValue());
			
		} else if(e.getSource().equals(shadowBrightnessSlider)) {
			
			environment.setShadowBrightness(shadowBrightnessSlider.getValue());
			
		} else if(e.getSource().equals(inclinationBrightnessSlider)) {
			
			double f = (double)inclinationBrightnessSlider.getValue() / 100.0;
			environment.setInclinationBrightnessFactor(f);
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
					
					res.addWater(water);
				}
				
				
				if(fire) {
					
					res.setResource("fire", 1);
				}
			}
			
		}
		
	}

	private void lower(final Resource res, final int level) {
		
		if(level == 0) {
			return;
		}

		res.addTerrain(level);
		
		for(Resource n1 : res.getNeighbours(false, false)) {
			n1.addTerrain(level / 2);
		}
		
		
	}
	
	public static void main(String[] args) {
		
		Gaia e = new Gaia();
		e.setVisible(true);
	}
}
