import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import com.google.common.collect.MinMaxPriorityQueue;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Voronoi;

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
			return circle.toString() + " with " + Integer.toString(numberOfInliers) + " number of inliers";
		}
	}
	
	/**
	 * 
	 * @param input the List of Points RANSAC will use
	 * @param iterations the number of trials RANSAC runs
	 * @param inlierRadius the distance from the center that is accepted as an inlier
	 * @param inlierRatio the number of inliers necessary for an accepted model, scaled to the number of inputs
	 * @return the List of Circles found
	 */
	private static List<Model> doRansac(final List<Point> input,final int iterations,final double inlierRadius,final double inlierRatio){
		final MinMaxPriorityQueue<Model> models = 
				MinMaxPriorityQueue.orderedBy(new Comparator<Model>(){
					@Override
					public int compare(Model arg0,Model arg1){
						// m1 with 6 inliers vs m2 with 4 inliers, m1 is better than m2,
						// so c(m1,m2) should yield positive, hence 6-4. But the prioqueue
						// wants the inverse comparator, therefore we use -1 to the original
						// comparison.
						return -1*(arg0.getNumberOfInliers() - arg1.getNumberOfInliers());
					}
				}).maximumSize(10).create();
		//Model bestModel = null;
		final Voronoi voronoiAlgorithm = new Voronoi(0); //TODO: 0 is the minDistance between points, valid?
		// determine bounding box!
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(final Point p : input){
			if (p.x < minX) minX = p.x;
			if (p.x > maxX) maxX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.y > maxY) maxY = p.y;
		}
		for(int i=0;i<iterations;i++){
			// randomly select three Points
			// Points must be distinct!
			final int index1 = (int)(input.size() * Math.random());
			int index2;
			do{
				index2 = (int)(input.size() * Math.random());
			}while(index2 == index1);
			int index3;
			do{
				index3 = (int)(input.size() * Math.random());
			}while(index3 == index1 || index3 == index2);
			Point[] randomPoints = new Point[3];
			randomPoints[0] = input.get(index1);
			randomPoints[1] = input.get(index2);
			randomPoints[2] = input.get(index3);
			// prepare argument for Voronoi algorithm
			final double[] xs = new double[3];
			final double[] ys = new double[3];
			int index = -1;
			for(final Point p : randomPoints){
				++index;
				xs[index] = p.x;
				ys[index] = p.y;
			}
			final List<GraphEdge> edgeList = voronoiAlgorithm.generateVoronoi(xs,ys,minX,maxX,minY,maxY);
			// the center of the circle lies at the intersection
			final Map<Point,Integer> edgesCross = new HashMap<>();
			// for all points in all edges, compute the number of occurances of each point.
			for(final GraphEdge edge : edgeList){
				for(final Point point : new Point[] {new Point(edge.x1,edge.y1),new Point(edge.x2,edge.y2)}){
					if(edgesCross.computeIfPresent(point,new BiFunction<Point,Integer,Integer>(){
						@Override
						public Integer apply(Point arg0,Integer arg1){
							return Integer.valueOf(arg1+1);
						}
					})==null)
						edgesCross.put(point,Integer.valueOf(1));
				}
			}
			// because we have three points, there is one unique point that is hit three times
			Point center = null;
			for(final Entry<Point,Integer> pair : edgesCross.entrySet()){
				if (pair.getValue() == Integer.valueOf(3))
					center = pair.getKey();
			}
			// radius is distance to closest point from center
			final double radius = Point.euclDistance(center,input.get(0));
			final Circle circle = new Circle(center,radius);
			// we now have the circle, now compute the inliers
			final List<Point> inliers = new ArrayList<>();
			for(final Point point : input){
				double distanceToCircle = Math.abs(Point.euclDistance(center,point));
				if (distanceToCircle < inlierRadius){
					inliers.add(point);
				}
			}
			final int numberOfInliers = inliers.size();
			// update stats if we found a better model!
			if (numberOfInliers > input.size()*inlierRatio){
				final Model model = new Model(circle,numberOfInliers);
				if (!models.contains(model))
					models.offer(model);
			}
		}
		final List<Model> finalModels = new ArrayList<>(models.size());
		while(!models.isEmpty()){
			finalModels.add(models.poll());
		}
		return finalModels;
	}
	
	private static List<Point> readCSV(final String fileName) throws FileNotFoundException, IOException {
		final List<Point> result = new ArrayList<Point>();
		String line;
		try(final BufferedReader br = new BufferedReader(new FileReader(fileName))){
			while((line = br.readLine()) != null){
				final String[] items = line.split(",");
				result.add(new Point(Double.parseDouble(items[0]),
						Double.parseDouble(items[1])));
			}
		}
		
		return result;
	}
	
	static void run() throws FileNotFoundException, IOException{
		final List<Point> points1 = readCSV("data/points1.5.csv");
		final List<Point> points2 = readCSV("data/points1.6.csv");
		
		final List<Model> models1 = doRansac(points1,5000,1,.015);
		final List<Model> models2 = doRansac(points2,5000,1,.015);
		
		System.out.println("Models found for points1:");
		System.out.println(models1.toString());
		System.out.println("\nModels found for points2:");
		System.out.println(models2.toString());
	}
	
	public static void main(final String[] args) throws FileNotFoundException, IOException{
		run();
	}
}
