import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class ExampleDataGenerator {

    //Generates noise and anuli within unit square. 	
	public final static void main(final String[] args) throws FileNotFoundException {
        int numAnuli = -1;
        int pointsPerAnulus = -1;
        int numNoise = -1;

        boolean incorrectParameters=false;
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

		try(final PrintWriter writer = new PrintWriter("../data/examplePoints.csv")){
            //Generate noise
			System.out.println("Generating "+numNoise+" noise points");
			for(int i=0;i<numNoise;i++){
				writer.println(Math.random() + "," + Math.random());
			}
            //Generate anuli
			for(int i=0;i<numAnuli;i++){
				final double cx = Math.random();
				final double cy = Math.random();
				final double cr = Math.random()*.9+.1;
				final double ce = Math.random()*0.01+0.001;
				final Model m = new Model(new Circle(new Point(cx,cy),cr),ce);
				int pointsFound = 0;
                
                double R = cr * (1 + ce);
                //Check if anulus is entirely inside unit square
                if(cx - R < 0 || cx + R > 1 || cy - R < 0 || cy + R > 1) {
                    i--;
                    continue;
                }

				System.out.println("Generating anulus " + (i+1) + "/" + numAnuli);

                //Add points using rejection method. Slow, but fast enough for the purpose and reliable.
				while(pointsFound < pointsPerAnulus){
					final double x = cx+(Math.random()-.5)*2*R;
					final double y = cy+(Math.random()-.5)*2*R;

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
