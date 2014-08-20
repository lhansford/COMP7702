package assignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JTree;

public class RiverCleaner {
	
	private static double BOOMLENGTH = 0.05;
	private static double MAXMOVEUNIT = 0.001;
	
	private List<ASV> asvList;
	private List<Obstacle> obstacleList;
	
	
	
	public RiverCleaner(String path){
		asvList = new ArrayList<ASV>();
		obstacleList = new ArrayList<Obstacle>();
		try {
			readInput(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(asvList.toString());
//		System.out.println(getUniformSamples(20000).size());
//		System.out.println(getSamplesNearObstacles(20000, (float) 0.005).size());
		List<Node> path2 = findPath(asvList.get(0), 0.01);
		for (Node n: path2){
			System.out.println(n.toString() + " " + n.toString() + " " + n.toString());
		}
		
	}
	
	private void readInput(String path) throws IOException {
		File file = new File(path);
		Reader reader = new FileReader(file);
		BufferedReader bReader = new BufferedReader(reader);
		int numAsvs = Integer.parseInt(bReader.readLine());
		String[] initialAsvPos = bReader.readLine().split("\\s+");
		String[] goalAsvPos = bReader.readLine().split("\\s+");
		for (int i = 0; i < numAsvs; i++){
			Position initialPos = new Position(Float.valueOf(initialAsvPos[i*2]), Float.valueOf(initialAsvPos[i*2+1]));
			Position goalPos = new Position(Float.valueOf(goalAsvPos[i*2]), Float.valueOf(goalAsvPos[i*2+1]));
			ASV asv = new ASV(initialPos, goalPos);
			asvList.add(asv);
		}
		int numObstacles = Integer.parseInt(bReader.readLine());
		for (int i = 0; i < numObstacles; i++){
			String[] bounds = bReader.readLine().split("\\s+");
			Position pos1 = new Position(Float.valueOf(bounds[0]), Float.valueOf(bounds[1]));
			Position pos2 = new Position(Float.valueOf(bounds[2]), Float.valueOf(bounds[3]));
			Position pos3 = new Position(Float.valueOf(bounds[4]), Float.valueOf(bounds[5]));
			Position pos4 = new Position(Float.valueOf(bounds[6]), Float.valueOf(bounds[7]));
			obstacleList.add(new Obstacle(pos1, pos2, pos3, pos4));
		}
		bReader.close();
	}
	
	private double minArea(int numAsvs){
		double rMin = 0.007 * (numAsvs - 1);
		return Math.PI * (rMin * rMin);
	}
	
	private List<Node> findPath(ASV asv, double distance){
		List<Position> samples = getUniformSamples(10000);
		List<Node> nodes = new ArrayList<Node>();
		samples.addAll(getSamplesNearObstacles(10000, 0.01));
		Node node = new Node(asv.getInitialPosition(), null);
		nodes.add(node);
		int i = 0;
		while (i < nodes.size() && !isConnected(node.getPosition(), asv.getGoalPosition())){
			node = nodes.get(i);
			for(int j = 0; j < samples.size(); j++){
//				System.out.println(getDistance(node.getPosition(), samples.get(j)));
				if(getDistance(node.getPosition(), samples.get(j)) < distance){
					nodes.add(new Node(samples.get(j), node));
					samples.remove(j);
				}
			}
			i++;
		}
		List<Node> path = new ArrayList<Node>();
		path.add(node);
		path.add(new Node(asv.getGoalPosition(), node));
		while(node.getPrevious() != null){
			path.add(0,node.getPrevious());
			node = node.getPrevious();
		}
		return nodes;
	}
	
	private Position getUniformSample(){
		Random random = new Random();
		Position sample = new Position(random.nextFloat(), random.nextFloat());
		while (isColliding(sample)){
			sample = new Position(random.nextFloat(), random.nextFloat());
		}
		return sample;
	}
	
	private Position getSampleNearObstacle(double distance){
		Random random = new Random();
		Position sample = new Position(random.nextFloat(), random.nextFloat());
		if (isColliding(sample)){
			if (sample.getX() - distance >= 0 && !isColliding(sample.translateLeft(distance))){
				return sample.translateLeft(distance);
			}
			if (sample.getX() + distance <= 1 && !isColliding(sample.translateRight(distance))){
				return sample.translateRight(distance);
			}
			if (sample.getY() - distance >= 0 && !isColliding(sample.translateUp(distance))){
				return sample.translateUp(distance);
			}
			if (sample.getY() + distance <= 1 && !isColliding(sample.translateDown(distance))){
				return sample.translateDown(distance);
			}
		} else {
			if (sample.getX() - distance >= 0 && isColliding(sample.translateLeft(distance))){
				return sample.translateLeft(distance);
			}
			if (sample.getX() + distance <= 1 && isColliding(sample.translateRight(distance))){
				return sample.translateRight(distance);
			}
			if (sample.getY() - distance >= 0 && isColliding(sample.translateUp(distance))){
				return sample.translateUp(distance);
			}
			if (sample.getY() + distance <= 1 && isColliding(sample.translateDown(distance))){
				return sample.translateDown(distance);
			}
		}
		//THIS IS PROBABLY TERRIBLE!
		return getSampleNearObstacle(distance);	
	}
	
	private List<Position> getUniformSamples(int n){
		List<Position> samples = new ArrayList<Position>();
		while(samples.size() < n){
			samples.add(getUniformSample());
		}
		return samples;
	}
	
	private List<Position> getSamplesNearObstacles(int n, double distance){
		List<Position> samples = new ArrayList<Position>();
		while(samples.size() < n){
			samples.add(getSampleNearObstacle(distance));
		}
		return samples;
	}
	
	private boolean isColliding(Position position){
		for (Obstacle obstacle: obstacleList){
			if(obstacle.inBounds(position)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isConnected(Position p1, Position p2){
		Position midpoint = new Position((p1.getX() +p2.getX())/2, (p1.getY() +p2.getY())/2);
		if (isColliding(midpoint)){
			return false;
		}else if(Math.abs(p1.getX() - p2.getX()) < 0.001 && Math.abs(p1.getY() - p2.getY()) < 0.001){
			return true;
		}else{
			return isConnected(p1, midpoint) && isConnected(midpoint, p2);
		}
	}
	
	private double getDistance(Position p1, Position p2){
		return Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2) + Math.pow((p2.getY() - p1.getY()), 2)); 
	}
	
	private void constructStateGraph(ASV asv){
		List<Node> nodes = new ArrayList<Node>();
		Node startNode = new Node(asv.getInitialPosition(), null);
		nodes.add(startNode);
	}

}
