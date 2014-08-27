import javax.swing.JFrame;
import javax.swing.JPanel;
import problem.Obstacle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

public class PRMVisualiser extends JPanel {
	
	private int panelWidth = 750;
	private int panelHeight = 750;

	private List<Rectangle2D> obstacles = new ArrayList<Rectangle2D>();
	private List<ArrayList<Point2D>> connections = new ArrayList<ArrayList<Point2D>>();
	private List<Point2D> points = new ArrayList<Point2D>();
	
	private Random random = new Random();
	private JFrame frame;
	
	private Point2D startPoint, endPoint;
	
	/**
	 * Constructor
	 */
	public PRMVisualiser() {
		frame = new JFrame("PRM Visualiser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(panelWidth, panelHeight);
		frame.add(this);
		frame.setVisible(true);
	}
	
	/**
	 * Draws obstacles specified by problem input
	 */
	public void addRectangles(List<Obstacle> obstacles) {
		for(Obstacle o : obstacles) {
			this.obstacles.add(o.getRect());
		}
	}
	
	/**
	 * Draws a line between the two given points
	 */
	public void addConnection(List<Point2D> input, String type) {
		connections.add(new ArrayList<Point2D>());
		connections.get(connections.size() - 1).addAll(input);
		if(type.equals("initial")) {
			startPoint = input.get(0);
		}
		if(type.equals("goal")) {
			endPoint = input.get(0);
		}
	}
	
	public void addPoint(Point2D input) {
		points.add(input);
	}
	
	/**
	 * Call this to refresh the window
	 */
	public void repaint() {
		if (frame != null) {
			frame.repaint();
		}
	}
	
	
	/**
	 * Display everything
	 */
	public void paint(Graphics g) {
		panelHeight = this.getHeight();
		panelWidth = this.getWidth();
		
		g.setColor(Color.white);
		g.fillRect(0, 0, panelWidth, panelHeight);

		// Draw obstacles
		for (Rectangle2D r : obstacles) {
			int x = (int) (r.getX() * panelWidth);
			int y = (int) (r.getY() * panelHeight);
			int width = (int) (r.getWidth() * panelWidth);
			int height = (int) (r.getHeight() * panelHeight);
			g.setColor(Color.red);
			g.fillRect(x, panelHeight - y - height, width, height);
		}
		
		Random rand = new Random();
		
		// Draw connections
		for (int i = 0; i < connections.size(); i++) {
			
			// Pick a random colour
			float red = rand.nextFloat();
			float green = rand.nextFloat();
			float blue = rand.nextFloat();
			Color randomColor = new Color(red, green, blue);
			g.setColor(randomColor);
			
			for (int j = 0; j < connections.get(i).size() - 1; j++) {
				Point2D p1 = connections.get(i).get(j);
				Point2D p2 = connections.get(i).get(j + 1);
				int x1 = (int) (p1.getX() * panelWidth);
				int y1 = (int) (p1.getY() * panelHeight);
				int x2 = (int) (p2.getX() * panelWidth);
				int y2 = (int) (p2.getY() * panelHeight);	
				g.drawLine(x1, panelHeight - y1, x2, panelHeight - y2);
				
				if(p1.equals(startPoint)) {
//					g.drawOval(x1, (int) (panelHeight - p1.getY() * panelHeight), 10, 10);
				}
			}
		}
		
		for(int i = 0; i < points.size(); i++) {
			g.setColor(Color.BLACK);
			
			Point2D p1 = points.get(i);
			
			// Draw point
			int x1 = (int) (p1.getX() * panelWidth);
			int y1 = (int) (p1.getY() * panelHeight);
			
//			g.drawOval(x1, y1, 15, 15);
		}
	}
}