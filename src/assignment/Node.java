package assignment;

import java.util.List;

public class Node {
	
	private Position position;
	private Node previousNode;

	public Node(Position p, Node pNode){
		position = p;
		previousNode = pNode;
	}
	
	public Position getPosition(){
		return position;
	}
	
	public Node getPrevious(){
		return previousNode;
	}
	
	
	@Override
	public String toString(){
		return String.format("%.3f", position.getX()) + " " + String.format("%.3f", position.getY());
	}
	
}
