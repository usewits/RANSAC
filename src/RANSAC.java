import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.MinMaxPriorityQueue;

class RANSAC {
	
	private static class Point {
		double x,y;
		
		public Point(final double x,final double y){
			this.x = x;
			this.y = y;
		}

		@Override public String toString(){
			return "Point at (" + Double.toString(x) + "," + Double.toString(y) + ")";
		}

		@Override public int hashCode(){
			int result = 17;
			long xSemiHash = Double.doubleToLongBits(x);
			int xHash = (int) (xSemiHash ^ (xSemiHash >>> 32));
			result = result * 31 + xHash;
			long ySemiHash = Double.doubleToLongBits(y);
			int yHash = (int)(ySemiHash ^ (ySemiHash>>>32));
			result = result * 31 + yHash;
			return result;
		}

		@Override public boolean equals(final Object o){
			if (!(o instanceof Point)) return false;
			final Point p = (Point)o;
			return Double.compare(p.x,this.x)==0 && Double.compare(p.y,this.y)==0;
		}

		public static double euclDistance(final Point first,final Point second){
			return Math.sqrt(
					Math.pow((first.x-second.x),2) + 
					Math.pow((first.y-second.y),2));
		}
	}
	
	private static final class Circle {
		private final Point center;
		private final double radius;

		public Circle(final Point center,final double radius){
			this.center = new Point(center.x,center.y);
			this.radius = radius;
		}

		public Point getCenter(){return new Point(center.x,center.y);}
		public double getRadius(){return radius;}
		@Override public String toString(){
			return "Circle at (" + center.toString() + "), with radius " + Double.toString(radius);
		}

		/** Algorithm stolen from http://paulbourke.net/geometry/circlesphere/*/
		public static Circle fromPoints(final Point p1,final Point p2,final Point p3){
			if (!isPerpendicular(p1,p2,p3)) return calcCircle(p1,p2,p3);
			else if (!isPerpendicular(p1, p3, p2)) return calcCircle(p1, p3, p2);	
			else if (!isPerpendicular(p2, p1, p3)) return calcCircle(p2, p1, p3);	
			else if (!isPerpendicular(p2, p3, p1)) return calcCircle(p2, p3, p1);	
			else if (!isPerpendicular(p3, p2, p1)) return calcCircle(p3, p2, p1);	
			else if (!isPerpendicular(p3, p1, p2)) return calcCircle(p3, p1, p2);	
			else throw new AssertionError("Some of the points are perpendicular to some axis!");
		}

		/** Check for the given points if they are perpendicular to x or y axis.*/
		private static boolean isPerpendicular(final Point p1,final Point p2,final Point p3){
			// the original method works by comparing delta_x with a very small value
			// here I compare the two values using Double.compare and act if they
			// are compare equal.
			final boolean yDeltaA = Double.compare(p2.y,p1.y) == 0;
			final boolean xDeltaA = Double.compare(p2.x,p1.x) == 0;
			final boolean yDeltaB = Double.compare(p3.y,p2.y) == 0;
			final boolean xDeltaB = Double.compare(p3.x,p2.x) == 0;
			// checking whether the line of the two points is vertical
			// three points are perpendicular to a certain axis if
			// there exists only! one couple of points that are on
			// one line. If there exist two such lines (if three points
			// are on a triangle with right angle aligned with the axes), 
			// then the points are not considered to be perpendicular 
			// to one axis.
			if (xDeltaA && yDeltaB) return false;
			if (yDeltaA) return true;
			if (yDeltaB) return true;
			if (xDeltaA) return true;
			if (xDeltaB) return true;
			return false;
		}

		/** Creates the circle using three points.*/
		private static Circle calcCircle(final Point p1,final Point p2,final Point p3){
			final double yDeltaA = p2.y - p1.y;
			final double xDeltaA = p2.x - p1.x;
			final double yDeltaB = p3.y - p2.y;
			final double xDeltaB = p3.x - p2.x;
			
			if (Double.compare(p2.x,p1.x)==0 && Double.compare(p3.y,p2.y) == 0){
				// this is the case where the three points lie on a triangle with
				// a right angle, and lie parallel to the axes.
				final double circleX = .5 * (p2.x + p3.x);
				final double circleY = .5 * (p1.y + p2.y);
				final Point centerPoint = new Point(circleX,circleY);
				final double radius = Point.euclDistance(centerPoint,p1);
				return new Circle(centerPoint,radius);
			}

			final double aSlope = yDeltaA / xDeltaA;
			final double bSlope = yDeltaB / xDeltaB;

			if (Double.compare(aSlope,bSlope)==0)
				throw new AssertionError("The points are colinear!");

			final double circleX = 
					(aSlope*bSlope*(p1.y - p3.y) + 
							bSlope*(p1.x+p2.x) - 
							aSlope*(p2.x+p3.x)) / 
							(2*(bSlope-aSlope));

			final double circleY = 
					-1*(circleX - (p1.x+p2.x)/2)/aSlope +  (p1.y+p2.y)/2;

			final Point centerPoint = new Point(circleX,circleY);
			final double radius = Point.euclDistance(centerPoint,p1);

			return new Circle(centerPoint,radius);
		}
	}
	
	private static final class Model {
		private final Circle circle;
		private final int numberOfInliers;

		public Model(final Circle circle,final int numberOfInliers){
			this.circle = circle;
			this.numberOfInliers = numberOfInliers;
		}

		public Circle getCircle(){return circle;}
		public int getNumberOfInliers(){return numberOfInliers;}
		@Override public String toString(){
			return circle.toString() + " with " + Integer.toString(numberOfInliers) + " inliers";
		}
	}

    private static double scoreModel(Model m) {
        return m.getNumberOfInliers()/m.circle.radius;
    }
	/**
	 * 
	 * @param input the List of Points RANSAC will use
	 * @param iterations the number of trials RANSAC runs
	 * @param inlierEpsilon: a point is in the anulus if its distance to centre is in [radius, radius*(1+inlierEpsilon)]
	 * @param inlierRatio the number of inliers necessary for an accepted model, scaled to the number of inputs
	 * @return the List of Circles found
	 */
	private static List<Model> doRansac(final List<Point> input, final int iterations, final double inlierEpsilon, final double inlierRatio) {
		final MinMaxPriorityQueue<Model> models = 
				MinMaxPriorityQueue.orderedBy(new Comparator<Model>() {
					@Override
					public int compare(Model arg0,Model arg1){
						// m1 with 6 inliers vs m2 with 4 inliers, m1 is better than m2,
						// so c(m1,m2) should yield positive, hence 6-4. But the prioqueue
						// wants the inverse comparator, therefore we use -1 to the original
						// comparison.
                        double comp=scoreModel(arg1) - scoreModel(arg0);
                        if(comp > 0)
						    return 1;
                        if(comp < 0)
                            return -1;
                        return 0;
					}
				}).maximumSize(10).create();
        //PriorityQueue<Model> models;
        
		for(int i=0;i<iterations;i++){
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

			final Circle circle = Circle.fromPoints(input.get(index1),input.get(index2),input.get(index3));
			// we now have the circle, now compute the inliers
			final List<Point> inliers = new ArrayList<>();
			for(final Point point : input){
				double distanceToCircle = Math.abs(Point.euclDistance(circle.getCenter(),point));
				if (distanceToCircle < circle.radius*(1+inlierEpsilon) && distanceToCircle > circle.radius){
					inliers.add(point);
				}
			}

			final int numberOfInliers = inliers.size();
			// update stats if we found a better model!
            // ALWAYS UPDATE SINCE DATA STRUCTURE WILL MAKE THIS NICE
			//if (numberOfInliers/circle.radius > input.size()*inlierRatio){
				final Model model = new Model(circle,numberOfInliers);
				if (!models.contains(model))
					models.offer(model);
			//}
		}
		final List<Model> finalModels = new ArrayList<>(models.size());
		while(!models.isEmpty()) {
			finalModels.add(models.poll());
		}
		return finalModels;
	}
	
	private static List<Point> readCSV(final String fileName) throws FileNotFoundException, IOException {
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
    
    private static String rawPrint(Model model) {
        Circle circle=model.circle;
        return circle.center.x+", "+circle.center.y+", "+circle.radius+", "+model.numberOfInliers;
    }

    private static void writeCSV(List<Model> models, String filename) throws FileNotFoundException, IOException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        for(Model model : models) 
		    writer.println(rawPrint(model));
        writer.close();	
    } 
    	
	static void run() throws FileNotFoundException, IOException {
        System.out.println("Reading CSVs...");

    
		final List<Point> points1 = readCSV("../data/points1.5.csv");
		final List<Point> points2 = readCSV("../data/points1.6.csv");
        System.out.println("RANSAC time! (1)");
		final List<Model> models1 = doRansac(points1,100000,.2,.015);
        System.out.println("RANSAC time! (2)");
		final List<Model> models2 = doRansac(points2,100000,.2,.015);
        writeCSV(models1, "../data/results1.5.csv");
        writeCSV(models2, "../data/results1.6.csv");
	}
	
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		run();
	}
}
