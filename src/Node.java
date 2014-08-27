
public class Node implements Comparable<Node>{
	private PRMNode state;
	private double cost;
	private double heuristicEstimate;
	private PRMNode pred;
	
	public Node(PRMNode state, double cost, double hEstimate, PRMNode pred) {
		this.state = state;
		this.cost = cost;
		this.heuristicEstimate = hEstimate;
		this.pred = pred;
	}
	
	public PRMNode getState() {
		return state;
	}
	
	public double getCost() {
		return cost;
	}
	
	public double getHeuristic() {
		return heuristicEstimate;
	}
	
	public PRMNode getPred() {
		return pred;
	}

	@Override
	public int compareTo(Node o) {
		return Double.compare(this.cost + this.heuristicEstimate, o.cost + o.heuristicEstimate);
	}
	
	public String toString() {
		return state.toString();
	}
}