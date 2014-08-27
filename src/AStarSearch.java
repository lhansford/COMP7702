import java.util.*;

/**
 *	Implementation of A* Search
 */
public class AStarSearch {
	// Queue of states to be processed
	private Queue<Node> queue;
	
	// Map of previously visited states
	private Map<PRMNode, PRMNode> visited;
	
	// The node/state being currently processed
	private Node currNode;
	
	// Start and goal states
	private PRMNode root;
	private PRMNode goal;
	
	private boolean goalFound;
	
	public AStarSearch(PRMNode root, PRMNode goal) {
		this.root = root;
		this.goal = goal;
		
		queue = new PriorityQueue<Node>();
		visited = new HashMap<PRMNode, PRMNode>();
	}
	
	public void search() {
		long startTime = System.currentTimeMillis();
		// Insert root node into PQ
		queue.add(new Node(root, 0.0, 0, null));
		
		while(!queue.isEmpty()) {
			currNode = queue.remove();
			PRMNode state = currNode.getState();
			
			// Check if node has been visited
			if(visited.containsKey(state)) {
				// Visited node, don't process it
				continue;
			}
			
			// Not goal state, so mark as visited and put successors in pq
			visited.put(state, currNode.getPred());
			
			// Check if this state is our goal state
			if(state.equals(goal)) {
				goalFound = true;
				break;
			}
			
			for(PRMNode s : state.getConnections()) {
				if(!visited.containsKey(s)) {
					Node n = new Node(s, currNode.getCost() + state.distanceTo(s), 0, state);
					queue.add(n);
				}
			}
		}
		
		if(goalFound) {
			// Got to the goal - print the path
			System.out.println("\nFound goal");
			
			System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + "ms");
			
			System.out.println("Cost: " + currNode.getCost());
			
			List<PRMNode> path = new ArrayList<PRMNode>();
			PRMNode s = currNode.getState();
			path.add(s);
			while((s = visited.get(s)) != null) {
				path.add(s);
			}
			Collections.reverse(path);
			
			for(Iterator it = path.iterator(); it.hasNext(); ) {
				PRMNode n = (PRMNode) it.next();
				System.out.println(n);
			}
//			System.out.println(path);
			
		} else {
			System.out.println("Could not find goal");
		}
	}
}
