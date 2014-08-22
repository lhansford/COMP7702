import java.io.IOException;

/**
 * Kick off point for River Cleaner simulation
 * Simply creates a RiverCleaner object and starts the search
 */
public class Main {
	
	/**
	 * Takes the location of the problem to run as a command line arg
	 * Specified problem is optional, else a default is used
	 */
	public static void main(String [] args) {
		try {
			if(args.length == 0) {
				RiverCleaner r = new RiverCleaner();
			} else {
				String testcase = args[0];
				RiverCleaner r = new RiverCleaner(testcase);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
