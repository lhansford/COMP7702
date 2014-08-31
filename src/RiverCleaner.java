import java.util.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;

import problem.*;

/**
 * Main class of the RiverCleaner simulation for COMP7702 AS1
 */
public class RiverCleaner {
	private static final String DEFAULT_TESTCASE = "testcases/3ASV-fixed-x4.txt";
//	private static final String DEFAULT_TESTCASE = "testcases/7-ASV-x6.txt";
	
	private static final double BOOM_LENGTH = 0.05;
	
	private ProblemSpec prob;
	private Sampler sampler;
	
	/* ASV Specifics */
	private int numASV;						// The number of ASVs for the given problem
	private ASVConfig initialState;			// Starting state of problem
	private ASVConfig goalState;			// Goal state of problem
	
	/* Area Requirements */
	private double minArea;					// The minimum area to meet ( pi * (0.007 * (numASV-1)) )
	
	
	/**
	 * Constructor that uses the default problem
	 * @throws IOException 
	 */
	public RiverCleaner() throws IOException {
		this(DEFAULT_TESTCASE);
	}
	
	/**
	 * Constructor that accepts a user-defined problem
	 * 
	 * @param testcase	path to problem file
	 * @throws IOException 
	 */
	public RiverCleaner(String problem) throws IOException {
		// Use provided problem spec to help load/parse testcase
		prob = new ProblemSpec();
		prob.loadProblem(problem);
		
		numASV = prob.getASVCount();
		initialState = prob.getInitialState();
		goalState = prob.getGoalState();
		
		// Calculate the minimum area required
		minArea = Math.PI * (0.007 * (numASV - 1));
		
		// Create a new sampler
		sampler = new Sampler(numASV, BOOM_LENGTH, minArea, initialState, goalState, prob.getObstacles());
	}
}
