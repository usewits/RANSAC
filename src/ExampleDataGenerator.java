import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * This class is used solely for generating a stylised sample of some circles. All
 * generated circles lie completely in a 1 by 1 grid. The number of circles and the
 * number of points per circle are adaptable by providing the necessary command-line
 * arguments. This class functionality will write the generated points to file.
 * @author Arie, Abe
 *
 */
public class ExampleDataGenerator {
	
	//Generates noise and anuli within unit square.
	/**
	 * Generates random points based on the provided noise parameter, the number of 
	 * circles to be generated and the number of points per circle. The point data
	 * will be written to ../data/examplePoints.csv. The algorithm guarantees that
	 * all points lie within the unit square, that is, the square of 1 by 1. Circles
	 * fall completely within the square.
	 */
	public static void main(final String[] args) throws FileNotFoundException {
		int numAnuli = -1;
		int pointsPerAnulus = -1;
		int numNoise = -1;
		
		// extract variables from args, this is just conversion logic... Read on!
		boolean incorrectParameters = false;
		if(args.length != 3) {
			System.out.println("Incorrect call");
			incorrectParameters = true;
		} else {
			try {
				numAnuli = Integer.parseInt(args[0]);
				pointsPerAnulus = Integer.parseInt(args[1]);
				numNoise = Integer.parseInt(args[2]);
			} catch(NumberFormatException e){
				System.out.println("Could not parse all parameters");
				incorrectParameters = true;
			}
		}
		
		if(incorrectParameters) {
			System.out.println("ExampleDataGenarator needs 3 integer parameters: number_anuli number_points_per_anulus number_noise_points");
			return;
		}
		
		// here's where the fun starts!
		try(final PrintWriter writer = new PrintWriter("../data/examplePoints.csv")){
			//Generate noise
			System.out.println("Generating "+numNoise+" noise points");
			for(int i=0;i<numNoise;i++){
				writer.println(Math.random() + "," + Math.random());
			}
			//Generate anuli
			for(int i=0;i<numAnuli;i++){
				final double circleX = Math.random();
				final double circleY = Math.random();
				final double circleRadius = Math.random()*.9+.1; // make sure we get large enough circles
				final double circleEpsilon = Math.random()*0.01+0.001;
				// create a 'perfect' model for this data. Then sample random points and check if
				// they lie within this model. If yes: random point on circle found!
				final Model m = new Model(new Circle(new Point(circleX,circleY),circleRadius),circleEpsilon);
				int pointsFound = 0;
				
				double R = circleRadius * (1 + circleEpsilon);
				//Check if anulus is entirely inside unit square: then we can show a 'full' circle.
				if(circleX - R < 0 || circleX + R > 1 || circleY - R < 0 || circleY + R > 1) {
					i--;
					continue;
				}
				
				System.out.println("Generating anulus " + (i+1) + "/" + numAnuli);
				
				//Add points using rejection method. Slow, but fast enough for the purpose and reliable.
				while(pointsFound < pointsPerAnulus){
					// sample only within bounding square of this circle
					final double x = circleX+(Math.random()-.5)*2*R;
					final double y = circleY+(Math.random()-.5)*2*R;
					
					//Add point if it is within anulus
					if(Model.inAnulus(m,new Point(x,y))){
						writer.println(x + "," + y);
						pointsFound++;
					}
				}
			}
		}
	}
}
