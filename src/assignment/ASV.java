package assignment;

public class ASV {
	
	private Position initialPos;
	private Position goalPos;

	public ASV(Position initialPos, Position goalPos) {
		this.initialPos = initialPos;
		this.goalPos = goalPos;
	}
	
	public Position getInitialPosition(){
		return initialPos;
	}
	
	public Position getGoalPosition(){
		return goalPos;
	}
	
	@Override
	public String toString(){
		return "The current position is " + initialPos.toString() +
				". The goal position is " + goalPos.toString();
	}
}
