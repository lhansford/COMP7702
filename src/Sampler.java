import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import kdtree.*;
import problem.ASVConfig;
import problem.Obstacle;
import tester.Tester;

public class Sampler {
	private int numASVs;						// The number of ASVs for the problem
	private double boomLength;					// The length of the booms between ASVs
	private double minArea;						// The minimum area to meet for a valid configuration
	private Random random = new Random();		// Used to generate random positions for states
	private boolean curveClockwise;				// True if generated states should curve clockwise
	private List<Obstacle> obstacles;
	
	private double MIN_OBSTACLE_SIZE = 0.001;   // The smallest width or height of the obstacles. TODO, get this on input load.
	private double WEIGHT_RANDOM = 0.1;
	private double WEIGHT_PASSAGE = 0.4;
	
	private Tester tester = new Tester();
	
	public Sampler(int numASVs, double boomLength, double minArea, ASVConfig initial, ASVConfig goal, List<Obstacle> obstacles) {
		this.numASVs = numASVs;
		this.minArea = minArea;
		this.boomLength = boomLength;
		this.obstacles = obstacles;
		
		// Work out the curve orientation of the initial and goal configurations
		boolean initialCurveClockwise = tester.isConvex(initial);
		boolean goalCurveClockwise = tester.isConvex(goal);
		if(initialCurveClockwise && goalCurveClockwise) {
			curveClockwise = initialCurveClockwise;
		} else {
			// Cannot solve?
			System.err.println("Initial and goal configurations have different curve orientations");
			System.exit(0);
		}
		
		generatePath(initial, goal);
//		Map<ASVConfig, PRMNode> prm = generatePRM(initial, goal);
//		findPath(prm, initial, goal);
	}
	
	// put initial config in tree
	// while goal q_new != goal
	//	get random configuration q
	//	q' = tree.nn(q)
	//	q_new = configuration on line qq'
	//	if line q_new to q' is free and dist(q', q_new) < D
	//		add q_new as child of q'
	
	private void buildRRT(ASVConfig initial, ASVConfig goal) {
		KDTree tree = new KDTree(2*numASVs);
		tree.insert(initial.getPositions(), initial);
		
		while(true) {
			// Get random config q
			ASVConfig q;
			Point2D initPos = getSample();
			while(initPos == null) {
				initPos = getSample();
			}
			q = generateRandomConfiguration(initPos);
			
			// Get q'
			ASVConfig qPrime = (ASVConfig) tree.nearest(q.getPositions());
			
			// Get qNew on line from q to q'
			List<Point2D> qPositions = q.getASVPositions();
			List<Point2D> qPrimePositions = qPrime.getASVPositions();
			
			double blend = Math.random() * 0.001;
			
			List<Point2D> qNewPositions = new ArrayList<Point2D>();
			
			for(int i = 0; i < qPositions.size(); i++) {
				Point2D qASV = qPositions.get(i);
				Point2D qPrimeASV = qPrimePositions.get(i);
				
				double x = qPrimeASV.getX() + blend * (qASV.getX() - qPrimeASV.getX());
				double y = qPrimeASV.getY() + blend * (qASV.getY() - qPrimeASV.getY());
				
				Point2D p = new Point2D.Double(x, y);
				
				qNewPositions.add(p);
				
				
			}
			
			
			
		}
	}
	
	private Map<ASVConfig, PRMNode> generatePRM(ASVConfig initial, ASVConfig goal) {
		Map<ASVConfig, PRMNode> prm = new HashMap<ASVConfig, PRMNode>();

		prm.put(initial, new PRMNode(initial));
		prm.put(goal, new PRMNode(goal));
		
		int con = 0;
		int maxConnections = 10;
		List<Point2D> points;
		PRMVisualiser visualHelper = new PRMVisualiser();
		
		visualHelper.addPoint(initial.getASVPositions().get(1));
		visualHelper.addPoint(goal.getASVPositions().get(1));
		
		while(prm.size() < 350) {
			ASVConfig conf = null;
			while(conf == null || !isValidConfiguration(conf)) {
				Point2D initPos = getSample();
				while(initPos == null) {
					initPos = getSample();
				}
				conf = generateRandomConfiguration(initPos);
			}
			
			
			// We now have a valid configuration
			
			// Add it to roadmap
			PRMNode node = new PRMNode(conf);
			prm.put(conf, node);
			int connections = 0;
			
			// Now we can generate candidate neighbours for the configuration
			// Find all nodes n already in the roadmap such that n != node and dist(n, node) <= minDist
			for(Iterator it = prm.values().iterator(); it.hasNext(); ) {
				// Current node of roadmap being processed
				PRMNode n = (PRMNode) it.next();
				
				// Check if it is close enough
				if(connections <= maxConnections && n != node && n.distanceTo(node) <= boomLength * 3) {
					// Check that the line between the two configurations is free
					if(configurationsConnected(conf, n.getConf())) {
						// Line is in free space, add connections
						con += 2;
						connections++;
						node.addConnection(n);
						n.addConnection(node);
						
						// Visualiser stuff:
						// Get points of ASV1
						Point2D p1 = conf.getASVPositions().get(0);
						Point2D p2 = n.getConf().getASVPositions().get(0);
						points = new ArrayList<Point2D>();
						points.add(p1); points.add(p2);
						visualHelper.addConnection(points, "");
						
//						if(n.getConf() == initial || conf == initial) {
//							System.err.println("Connected start node");
//							visualHelper.addConnection(points, "initial");
//						}
//						if(n.getConf() == goal || conf == goal) {
//							System.err.println("Connected goal node");
//							visualHelper.addConnection(points, "goal");
//						}
					}
				}
			}
			
			if(connections == 0) {
//				System.err.println(conf + " has no connections");
			}
		}
		
		for(Iterator it = prm.values().iterator(); it.hasNext(); ) {
			PRMNode n = (PRMNode) it.next();
			System.out.println(n.getConf());
		}
		
		System.out.println("Connections: " + con);
		
		
		
		visualHelper.addRectangles(obstacles);
		visualHelper.repaint();
		
		return prm;
		
	}
	
	public void findPath(Map<ASVConfig, PRMNode> prm, ASVConfig initial, ASVConfig goal) {
//		// First try to connect initial and goal config to the prm?
//		for(Iterator it = prm.values().iterator(); it.hasNext(); ) {
//			PRMNode n = (PRMNode) it.next();
//			
//			// Find a connection with line in free space
//			if(configurationsConnected(initial, n.getConf())) {
//				
//			}
//		}
		AStarSearch search = new AStarSearch(prm.get(initial), prm.get(goal));
		search.search();
	}
	
	/** Gets a random sample using weighted sampling strategies.
	 * @return Point2D sample
	 */
	private Point2D getSample(){
		Random random = new Random();
		Double randomDouble = random.nextDouble();
		if (randomDouble > WEIGHT_PASSAGE){
			return getSampleInPassage(boomLength * 5);
		}else if(randomDouble < WEIGHT_RANDOM){
			return getUniformSample();
		}
		return getSampleNearObstacle(boomLength);
	}
	
	/**	Finds an ASVConfig that is valid and connects to currentConfig.
	 * 
	 * @param currentConfig
	 * @return a new ASVConfig that connects to currentConfig
	 */
	private ASVConfig getNextConfiguration(ASVConfig currentConfig){
		ASVConfig newConfig = null;
		while(newConfig == null || !isValidConfiguration(newConfig) || !configurationsConnected(currentConfig, newConfig)){
			Point2D initPos = getSample();
			while(initPos == null){
				initPos = getSample();
			}
			newConfig = generateRandomConfiguration(initPos);
		}
		return newConfig;
	}
	
	
	/** Finds a path of ASVConfigs from intial to goal.
	 * 
	 * @param initial 	the initial ASVConfig
	 * @param goal 		the goal ASVConfig
	 */
	private void generatePath(ASVConfig initial, ASVConfig goal){
		List<ASVConfig> states = new ArrayList<ASVConfig>();
		states.add(initial);
		List<Point2D> points;
		PRMVisualiser visualHelper = new PRMVisualiser();
		
		while(!configurationsConnected(states.get(states.size()-1), goal)){
			states.add(getNextConfiguration(states.get(states.size()-1)));
			Point2D p1 = states.get(states.size() - 1).getASVPositions().get(0);
			Point2D p2 = states.get(states.size() - 2).getASVPositions().get(0);
			points = new ArrayList<Point2D>();
			points.add(p1); points.add(p2);
			visualHelper.addConnection(points, "");
		}
		states.add(goal);
		visualHelper.addRectangles(obstacles);
		visualHelper.repaint();
		try {
			PrintWriter writer = new PrintWriter("testcases/1.txt", "UTF-8");
			writer.println(states.size() + " 0.6");
			for(ASVConfig asv: states){
				writer.println(asv);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Generates a random state 
	 */
	public ASVConfig generateRandomConfiguration(Point2D initPos) {
		// List to eventually hold all of the generated ASV positions
		List<Point2D> positions = new ArrayList<Point2D>();
		double[] angles = new double[numASVs];
		
		positions.add(initPos);
		
		// Next generate all of the other ASV positions
		for(int i = 1; i < numASVs; i++) {
			/* We need to pick another random (x, y) point that is boomLength distance
			 * away from the previous point generated
			 */
			Point2D previous = positions.get(i - 1);

			// Choose a random angle
			double angle = Math.random() * (Math.PI * 2);
			
			if(i > 1) {
				if(curveClockwise) {
					while(angle > angles[i-1] && angle < ((angles[i-1] - Math.PI) + (Math.PI*2)) % Math.PI*2) {
//						System.out.println(angle + " " + angles[i-1] + " " + ((angles[i-1] - Math.PI) + (Math.PI*2)) % Math.PI*2);
						angle = Math.random() * (Math.PI * 2);
					}
				} else {
					while(angle < angles[i-1] && angle > ((angles[i-1] + Math.PI) + (Math.PI*2)) % Math.PI*2) {
//						System.out.println(angle);
						angle = Math.random() * (Math.PI * 2);
					}
				}
			}
			
			angles[i] = angle;
			
			// Choose the random points
			double x = previous.getX() + (Math.cos(angle) * boomLength);
			double y = previous.getY() + (Math.sin(angle) * boomLength);
			
			
			// Add it to the list
			positions.add(new Point2D.Double(x, y));
		}
		
		return new ASVConfig(positions);
	}

	private Point2D getUniformSample(){
		Random random = new Random();
		Point2D sample = new Point2D.Double(random.nextDouble(), random.nextDouble());
		while (isColliding(sample)){
			sample = new Point2D.Double(random.nextDouble(), random.nextDouble());
		}
		return sample;
	}
	
	private Point2D getSampleInPassage(double distance) {
		// Get q1
		Random random = new Random();
		Point2D sample = new Point2D.Double(random.nextDouble(), random.nextDouble());
		
		if(isColliding(sample)) {
			double angle = 0;
			while(angle < Math.PI*2) {
				// Choose the random points
				double x = sample.getX() + (Math.cos(angle) * distance);
				double y = sample.getY() + (Math.sin(angle) * distance);
				
				double midX = Math.abs((sample.getX() + x) / 2);
				double midY = Math.abs((sample.getY() + y) / 2);
				
				if(isColliding(new Point2D.Double(x, y)) && !isColliding(new Point2D.Double(midX, midY))) {
					// q2 and midpoint collide
					return new Point2D.Double(midX, midY);
				}
				
				angle += Math.PI/4;
			}
		}
		
		return null;
	}
	
	private Point2D getSampleNearObstacle(double distance) {
		Random random = new Random();
		Point2D sample = new Point2D.Double(random.nextDouble(), random.nextDouble());
		double angle = 0;
		if (isColliding(sample)){
			while(angle < Math.PI*2) {
				double x = sample.getX() + (Math.cos(angle) * distance);
				double y = sample.getY() + (Math.sin(angle) * distance);
				if(!isColliding(new Point2D.Double(x, y))){
					return sample;
				}
				angle += Math.PI/2;
			}
			return null;
		}else{
			while(angle < Math.PI*2) {
				double x = sample.getX() + (Math.cos(angle) * distance);
				double y = sample.getY() + (Math.sin(angle) * distance);
				if(isColliding(new Point2D.Double(x, y))){
					return new Point2D.Double(x, y);
				}
				angle += Math.PI/2;
			}
			return null;
		}
		
//		if (isColliding(sample)){
//			if (sample.getX() - distance >= 0) {
//				sample.setLocation(sample.getX() - distance, sample.getY());
//				if(!isColliding(sample)) {
//					 return sample;
//				}
//			}
//			if (sample.getX() + distance <= 1) {
//				sample.setLocation(sample.getX() + distance, sample.getY());
//				if(!isColliding(sample)) {
//					 return sample;
//				}
//			}
//			if (sample.getY() - distance >= 0) {
//				sample.setLocation(sample.getX(), sample.getY() - distance);
//				if(!isColliding(sample)) {
//					 return sample;
//				}
//			}
//			if (sample.getY() + distance <= 1) {
//				sample.setLocation(sample.getX(), sample.getY() + distance);
//				if(!isColliding(sample)) {
//					 return sample;
//				}
//			}
//		} else {
//			Point2D sample2 = new Point2D.Double(sample.getX(), sample.getY());
//			if (sample.getX() - distance >= 0) {
//				sample.setLocation(sample.getX() - distance, sample.getY());
//				if(isColliding(sample)) {
//					 return sample2;
//				}
//			}
//			if (sample.getX() + distance <= 1) {
//				sample.setLocation(sample.getX() + distance, sample.getY());
//				if(isColliding(sample)) {
//					 return sample2;
//				}
//			}
//			if (sample.getY() - distance >= 0) {
//				sample.setLocation(sample.getX(), sample.getY() - distance);
//				if(isColliding(sample)) {
//					 return sample2;
//				}
//			}
//			if (sample.getY() + distance <= 1) {
//				sample.setLocation(sample.getX(), sample.getY() + distance);
//				if(isColliding(sample)) {
//					 return sample2;
//				}
//			}
//		}
		//THIS IS PROBABLY TERRIBLE!
//		return getSampleNearObstacle(distance);	
//		return null;
	}
	
	private boolean isColliding(Point2D position){
		for (Obstacle obstacle: obstacles){
			if(obstacle.getRect().contains(position)){
				return true;
			}
		}
		return false;
	}
	
	/* ======= Configuration Verification ======= */
	
	/**
	 * Verifies whether a configuration is valid. 
	 * That is, the polygon formed by the connected chain:
	 * 		1) The polygon is convex
	 * 		2) The area of the polygon is at least minArea
	 * 		3) The direction of the polygon is the same as the initial and goal
	 * and:
	 * 		4) The entire configuration is within the [0, 1] bounds
	 * 		5) The entire configuration doesn't intersect with an obstacle
	 * 
	 * @param conf	the ASV configuration to check
	 */
	private boolean isValidConfiguration(ASVConfig conf) {
//		if(tester.isConvex((conf))) {
//			if(tester.hasEnoughArea(conf)) {
//				if(isClockwise(conf)) {
//					if(tester.hasCollision(conf, obstacles)) {
//						if(inBoundary(conf)) {
//							return true;
//						} else {
//							System.out.println("not in boundary");
//						}
//					} else {
//						System.out.println("collision");
//					}
//				} else {
//					System.out.println("wrong curve orientation");
//				}
//			} else {
//				System.out.println("doesn't meet area");
//			}
//			
//		} else {
//			System.out.println("not convex");
//		}
//		
//		return false;
		return  tester.isConvex((conf)) &&
				tester.hasEnoughArea(conf) &&
				tester.fitsBounds(conf) && 
//				isClockwise(conf) == curveClockwise &&
				!tester.hasCollision(conf, obstacles);
				
	}
	
	/**
	 * @param config1 An ASV configuration
	 * @param config2 An ASV configuration
	 * @return True if each ASV configuration can be connected in a straight 
	 * 			line without intersecting any obstacles, false otherwise
	 */
	private boolean configurationsConnected(ASVConfig config1, ASVConfig config2){
		for(int i = 0; i < config1.getASVCount(); i++){
			if(!pointsConnected(config1.getPosition(i), config2.getPosition(i))){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param position A Point2D representing an ASV
	 * @param position2 A Point2D representing an ASV
	 * @return True if points can connect without intersecting any obstacles,
	 * 			False otherwise.
	 */
	private boolean pointsConnected(Point2D pos1, Point2D pos2) {
		Point2D midpoint = new Point2D.Double((pos1.getX() +pos2.getX())/2, (pos1.getY() +pos2.getY())/2);
		if (isColliding(midpoint)){
			return false;
		}else if(Math.abs(pos1.getX() - pos2.getX()) < MIN_OBSTACLE_SIZE && Math.abs(pos1.getY() - pos2.getY()) < MIN_OBSTACLE_SIZE){
			return true;
		}else{
			return pointsConnected(pos1, midpoint) && pointsConnected(midpoint, pos2);
		}
	}

	/**
	 * Returns the area of the polygon formed by connecting the two ends of the 
	 * given ASV/boom chain
	 * 
	 * @param conf	the ASV configuration to calculate the area of
	 */
	private double getArea(ASVConfig conf) {
		List<Point2D> vertices = conf.getASVPositions();

		double area = 0;
		int last = vertices.size() - 1;
		
		for(int i = 0; i < vertices.size(); i++) {
			Point2D prev = vertices.get(last);
			Point2D curr = vertices.get(i);
			area += (prev.getX() + curr.getX()) * (prev.getY() - curr.getY());
			last = i;
		}
		
		return Math.abs(area/2);
	}

	/**
	 * Returns true if the given ASV configuration forms a convex polygon
	 * 
	 * @param conf	the ASV configuration to check
	 */
	private boolean isConvex(ASVConfig conf) {
		List<Point2D> vertices = conf.getASVPositions();
		
		double sumExternal = 0;
		
		for(int i = 0; i < vertices.size(); i++) {
			// Compare each pair of points
			Point2D p1 = vertices.get(i);
			Point2D p2 = vertices.get((i + 1) % vertices.size());
			Point2D p3 = vertices.get((i + 2) % vertices.size());
			
			// Calculate the angle between the two lines
			Line2D line1 = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
			Line2D line2 = new Line2D.Double(p2.getX(), p2.getY(), p3.getX(), p3.getY());

			double internal = Math.abs(getAngle(line1, line2));

			// Internal angle must be <= 180deg
			if(Math.abs(internal) > Math.PI) {
				return false;
			}
			
			// Add to the sum of external angles
			sumExternal += Math.PI - internal;
		}
		
		// Sum of external angles must be 360deg
		return sumExternal == (2*Math.PI);
	}
	
	/**
	 * Returns the angle between two given lines
	 * 
	 * @param line1		initial side
	 * @param line2		terminal side
	 */
	private double getAngle(Line2D line1, Line2D line2) {
		 double angle1 = Math.atan2(line1.getY1() - line1.getY2(), line1.getX1() - line1.getX2());
		 double angle2 = Math.atan2(line2.getY1() - line2.getY2(),  line2.getX1() - line2.getX2());
		 double angle = Math.abs((angle2 - angle1));
		 angle = Math.PI - angle;
		 return angle;
	}
	
	/**
	 * Returns true if the given configuration is within the bounds [0, 1]
	 * 
	 * @param conf	the ASV configuration to check
	 */
	private boolean inBoundary(ASVConfig conf) {
		for(Point2D p : conf.getASVPositions()) {
			if(p.getX() < 0 || p.getX() > 1 || p.getY() < 0 || p.getY() > 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds the 'turn' of the given ASV configuration
	 * A clockwise configuration has negative internal angles
	 * A counter-clockwise configuration has positive internal angles
	 */
	private boolean isClockwise(ASVConfig conf) {
		List<Point2D> vertices = conf.getASVPositions();
		
		int sum = 0;
		
		for(int i = 0; i < vertices.size(); i++) {
			Point2D p1 = vertices.get(i);
			Point2D p2 = vertices.get((i + 1) % vertices.size());
			sum += (p2.getX() - p1.getX()) * (p2.getY() + p1.getY());
		}
		return sum >= 0;
	}
}
