import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.google.common.collect.MinMaxPriorityQueue;

/**
 * This class implements the RANSAC algorithm.
 * 
 * It iteravely selects three points in the point set, determines the circle that belongs
 * to those three points, and then computes all the inliers. This is done using a pre-
 * set parameter epsilon, which is the thickness of the circle. At this point, no optimiztion
 * is done yet during the computation: circles are just circles and the models are not updated
 * to better fit the inliers.
 * @author Arie, Abe
 *
 */
class RANSAC {
	
	/** 
	 * Runs the algorithm described above.
	 * @param input the List of Points RANSAC will use
	 * @param iterations the number of trials RANSAC runs
	 * @param epsilon determines if a point is in the annulus if its distance to centre is in the interval [radius, radius*(1+epsilon)]
	 * @return the List of Circles found
	 */
	public static List<Model> doRansac(final List<Point> input, final int iterations, final double epsilon) {
		// this algorithm keeps track of the best n models, here: best 10.
		// this is an implementation of such a 'queue of 10 best models'.
		final MinMaxPriorityQueue<Model> models = 
				MinMaxPriorityQueue.orderedBy(new Comparator<Model>() {
					/** Compares two models by their score, which is roughly defined as
					 * the number of inliers scaled to the size of the circle.*/
					@Override
					public int compare(Model arg0,Model arg1) {
						return (int)Math.signum(arg1.getScore() - arg0.getScore());
					}
				}).maximumSize(10).create();
		
		for(int i=0; i<iterations; i++){
			// randomly select three Points
			// Points must be distinct!
			final int index1 = (int)(input.size() * Math.random());

			int index2;
			do{
				index2 = (int)(input.size() * Math.random());
			} while(index2 == index1);

			int index3;
			do{
				index3 = (int)(input.size() * Math.random());
			} while(index3 == index1 || index3 == index2);
			// ...and create a circle from these three points
			final Circle circle = Circle.fromPoints(input.get(index1),input.get(index2),input.get(index3));
			// create a new model and fill it with its inliers
			Model model=new Model(circle,epsilon);

			for(Point point : input)
				if(Model.inAnnulus(model, point))
					model.add(point);
			// offer the model to the list of best models. This .contains method
			// is guaranteed to run on a list of at most 10 models!
			if (!models.contains(model))
				models.offer(model);
		}
		// return an ordered list based on the model queue.
		final List<Model> finalModels = new ArrayList<>(models.size());
		while(!models.isEmpty()) {
			finalModels.add(models.poll());
		}
		return finalModels;
	}
	
	/** CSV reader method that reads points. The points are all on a separate line, 
	 * coordinates separated by a comma. */
	public static List<Point> readCSV(final String fileName) throws FileNotFoundException, IOException {
		final List<Point> result = new ArrayList<Point>();
		String line;
		try(final BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			while((line = br.readLine()) != null) {
				final String[] items = line.split(",");
				result.add(new Point(Double.parseDouble(items[0]),
						Double.parseDouble(items[1])));
			}
		}
		
		return result;
	}
	
	/** Prints a model in our specified format: all fields on one line, separated by ', '.*/
	private static String rawPrint(Model model) {
		Circle circle=model.circle;
		return circle.center.x+", "+circle.center.y+", "+circle.radius+", "+model.numberOfInliers;
	}

	/** Writes a list of models to file, using rawPrint on each model.*/
	public static void writeCSV(List<Model> models, String fileName,final boolean append) throws FileNotFoundException, IOException {
		try(final PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName),append),StandardCharsets.UTF_8))){
			for(Model model : models) 
				writer.println(rawPrint(model));
		}   
	} 

	/**
	 * This function first reads the data, then does RANSAC on each dataset,
	 * and finally writes the found circles to file.*/
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		int n_iters=-1;
		double epsilon=-1.0;
		String filename="default";
		
		boolean incorrectParameters=false;
		if(args.length != 3) {
			System.out.println("Incorrect call");
			incorrectParameters = true;
		} else {
			try {
				n_iters = Integer.parseInt(args[0]);
				epsilon = Double.parseDouble(args[1]);
				filename = args[2];
			} catch(NumberFormatException e){
				System.out.println("Could not parse all parameters");
				incorrectParameters = true;
			}
		}

		if(incorrectParameters) {
			System.out.println("ExampleRun needs 3 parameters: number_iterations epsilon filename");
			System.out.println("number_iterations (integer): how many triples RANSAC tries");
			System.out.println("epsilon (double): the thickness of the annulus RANSAC searches for");
			System.out.println("filename (string): this program reads from ../data/[filename].csv, and writes to ../data/[filename]_result.csv");
			return;
		}

		final List<Point> points = RANSAC.readCSV("../data/"+filename+".csv");
	  
		//Put all points in a 1xwidth box
		//Find bounding box
		double maxX=Double.MIN_VALUE, maxY=Double.MIN_VALUE, minX=Double.MAX_VALUE, minY=Double.MAX_VALUE;
		for(Point p : points) {
			if(p.x < minX) minX=p.x;
			if(p.x > maxX) maxX=p.x;
			if(p.y < minY) minY=p.y;
			if(p.y > maxY) maxY=p.y;
		}
		double width=maxX-minX;
		double height=maxY-minY;
		//Rescale points
		if(height > width) {
			for(Point p : points) {
				double tmp=p.x;
				p.x=((p.y+minY)/width);
				p.y=((tmp+minX)/width);
			}
		} else {
			for(Point p : points) {
				p.x=((p.x+minX)/height);
				p.y=((p.y+minY)/height);
			}
		}
		
		//Will find at most 10 models
		boolean appendToFile=false;
		for(int i=0; i<100; i++) {
			final List<Model> models = RANSAC.doRansac(points,n_iters,epsilon);
			final Model best = models.get(0);
			final Model updatedBest = best;//Model.improveAnnulusApprox(best);

			//Quit when circle is no circle at sigma = 0.05
			//System.out.println("p = "+1.0/best.getScore());
			if(best.getScore() < 1e10) {
				System.out.println("No more high quality annuli!");
				break;
			}

			System.out.println("Found circle " + (i+1));
			
			// the method returns a list, we use only the first, and unfortunately
			// writeCSV wants a list!
			models.clear();
			models.add(updatedBest);
			
			RANSAC.writeCSV(models,"../data/"+filename+"_result.csv",appendToFile);
			appendToFile=true;//Further found circles are appended to file
			
			// This is how we deal with multiple circles in the data: simply remove
			// the found circle from the data points!
			for(final Point p : models.get(0).inliers){
				points.remove(p);
			}
			if(points.size() < 3) {
				System.out.println("All points are in annuli!");
				break;
			}
		}
		System.out.println("RANSAC done! Results were written to ../data/"+filename+"_result.csv");
	}
}
