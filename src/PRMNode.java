import java.util.*;
import problem.*;


public class PRMNode {
	private ASVConfig conf;
	private List<PRMNode> connections;
	
	public PRMNode(ASVConfig conf) {
		this.conf = conf;
		connections = new ArrayList<PRMNode>();
	}
	
	public ASVConfig getConf() {
		return conf;
	}

	public void addConnection(PRMNode node) {
		connections.add(node);
	}
	
	public List<PRMNode> getConnections() {
		return connections;
	}
	
	public double distanceTo(PRMNode n) {
		ASVConfig other = n.getConf();
		return conf.maxDistance(other);
	}
	
	public String toString() {
		return conf.toString();
	}
}
