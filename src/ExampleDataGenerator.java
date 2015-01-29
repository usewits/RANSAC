import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class ExampleDataGenerator {
	
	public final static void main(final String[] args) throws FileNotFoundException {
		try(final PrintWriter writer = new PrintWriter("data\\examplePoints.csv")){
			//for(int i=0;i<150;i++){
				//writer.println(Math.random() + "," + Math.random());
			//}
			for(int i=0;i<4;i++){
				System.out.println("Generating circle " + i);
				final double cx = Math.random();
				final double cy = Math.random();
				final double cr = Math.random();
				final double ce = Math.random()/100;
				System.err.println("using ce of " + ce + " for this circle");
				final Model m = new Model(new Circle(new Point(cx,cy),cr),ce);
				System.err.println("using model " + m);
				int pointsFound = 0;
				while(pointsFound < 30){
					final double x = Math.random();
					final double y = Math.random();
					if(Model.inAnulus(m,new Point(x,y))){
						writer.println(x + "," + y);
						pointsFound++;
					}
				}
				System.out.println("Done with circle " + i);
			}
		}
	}
}
