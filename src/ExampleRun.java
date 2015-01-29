import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class ExampleRun {
	
	public final static void main(final String[] args) throws IOException {
		try(final PrintWriter writer = new PrintWriter("data\\exampleModels.csv")){
			writer.close();
		}
		ExampleDataGenerator.main(new String[] {});
		final List<Point> points = RANSAC.readCSV("data\\examplePoints.csv");
		for(int i=0;i<4;i++){
			System.out.println("Doing RANSAC for circle " + i);
			final List<Model> models = RANSAC.doRansac(points,1000000,.006,0.1);
			System.out.println("Done for " + i);
			final Model best = models.get(0);
			final Model updatedBest = best;//Model.improveAnulusApprox(best);
			models.clear();
			models.add(updatedBest);
			RANSAC.writeCSV(models,"data\\exampleModels.csv",true);
			for(final Point p : models.get(0).inliers){
				points.remove(p);
			}
		}
	}
	
}
