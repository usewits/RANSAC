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

	//Generates noise and annuli within unit square. 	
	public final static void main(final String[] args) throws FileNotFoundException {
		int numAnnuli = -1;
		int pointsPerAnnulus = -1;
		int numNoise = -1;

		boolean incorrectParameters=false;
		if(args.length != 3) {
			System.out.println("Incorrect call");
			incorrectParameters = true;
		} else {
			try {
				numAnnuli = Integer.parseInt(args[0]);
				pointsPerAnnulus = Integer.parseInt(args[1]);
				numNoise = Integer.parseInt(args[2]);
			} catch(NumberFormatException e){
				System.out.println("Could not parse all parameters");
				incorrectParameters = true;
			}
		}

		if(incorrectParameters) {
			System.out.println("ExampleDataGenarator needs 3 integer parameters: number_annuli number_points_per_annulus number_noise_points");
			return;
		}

		try(final PrintWriter writer = new PrintWriter("../data/examplePoints.csv")){
			//Generate noise
			System.out.println("Generating "+numNoise+" noise points");
			for(int i=0;i<numNoise;i++){
				writer.println(Math.random() + "," + Math.random());
			}
			//Generate annuli
			for(int i=0;i<numAnnuli;i++){
				final double circleX = Math.random();
				final double circleY = Math.random();
				final double circleRadius = Math.random()*.9+.1; // make sure we get large enough circles
				final double circleEpsilon = Math.random()*0.01+0.001;
				// create a 'perfect' model for this data. Then sample random points and check if
				// they lie within this model. If yes: random point on circle found!
				final Model m = new Model(new Circle(new Point(circleX,circleY),circleRadius),circleEpsilon);
				int pointsFound = 0;
				
				double R = circleRadius * (1 + circleEpsilon);
				//Check if annulus is entirely inside unit square
				if(circleX - R < 0 || circleX + R > 1 || circleY - R < 0 || circleY + R > 1) {
					i--;
					continue;
				}

				System.out.println("Generating annulus " + (i+1) + "/" + numAnnuli);

				//Add points using rejection method. Slow, but fast enough for the purpose and reliable.
				while(pointsFound < pointsPerAnnulus){
					final double x = circleX+(Math.random()-.5)*2*R;
					final double y = circleY+(Math.random()-.5)*2*R;

					//Add point if it is within annulus
					if(Model.inAnnulus(m,new Point(x,y))){
						writer.println(x + "," + y);
						pointsFound++;
					}
				}
			}
		}
	}
}
