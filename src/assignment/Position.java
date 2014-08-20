package assignment;

public class Position {
	
	private double x;
	private double y;
	
	public Position(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public double getX(){
		return x;
	}

	public double getY(){
		return y;
	}
	
	public Position translateUp(double distance){
		return new Position(x, y - distance);
	}
	
	public Position translateDown(double distance){
		return new Position(x, y + distance);
	}
	
	public Position translateLeft(double distance){
		return new Position(x - distance, y);
	}
	
	public Position translateRight(double distance){
		return new Position(x - distance, y);
	}
	
	@Override
	public String toString(){
		return "(" + String.valueOf(x) + ", " + String.valueOf(y) + ")";
	}
}
