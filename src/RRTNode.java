import java.util.*;
import problem.ASVConfig;

public class RRTNode {
	private ASVConfig conf;
	private List<ASVConfig> children;
	
	public RRTNode(ASVConfig conf) {
		this.conf = conf;
		children = new ArrayList<ASVConfig>();
	}
	
	public void addChild(ASVConfig child) {
		children.add(child);
	}
	
	public List<ASVConfig> getChildren() {
		return children;
	}
	
	public ASVConfig getConf() {
		return conf;
	}
}
