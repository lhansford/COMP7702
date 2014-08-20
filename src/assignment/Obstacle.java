package assignment;

import java.util.ArrayList;
import java.util.List;

public class Obstacle {
	
	private double x;
	private double y;
	private double width;
	private double height;

	public Obstacle(Position pos1, Position pos2, Position pos3, Position pos4) {
		double x1 = pos1.getX();
		double x2 = pos1.getX();
		double y1 = pos1.getY();
		double y2 = pos1.getY();
		List<Position> bounds = new ArrayList<Position>();
		bounds.add(pos2);
		bounds.add(pos3);
		bounds.add(pos4);
		for (Position position: bounds){
			double posX = position.getX();
			double posY = position.getY();
			if (posX < x1){
				x1 = posX;
			}else if(posX > x2){
				x2 = posX;
			}
			if (posY < y1){
				y1 = posY;
			}else if(posY > y2){
				y2 = posY;
			}
		}
		x = x1;
		y = y1;
		width = x2 - x1;
		height = y2 - y1;
	}
	
	@Override
	public String toString(){
		return "blergh:";
//		return "[" + x.toString() + ", " + bounds.get(1).toString() + ", "
//				 + bounds.get(2).toString() + ", " + bounds.get(3).toString() + "]";
	}
	
	public boolean inBounds(Position position){
		if (position.getX() >= x &&
				position.getX() <= x + width &&
				position.getY() >= y &&
				position.getY() <= y + height){
			return true;
		}
		return false;
	}

}
