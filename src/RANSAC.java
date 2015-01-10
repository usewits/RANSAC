import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class RANSAC {
	
	private static class Point {
		double x,y;
		
		public Point(final double x,final double y){
			this.x = x;
			this.y = y;
		}
	}
	
	private static ArrayList<Point> readCSV(final String fileName) throws FileNotFoundException, IOException {
		final ArrayList<Point> result = new ArrayList<Point>();
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
		System.out.println("We need to implement ransac!");
		final ArrayList<Point> points1 = readCSV("../data/points1.5.csv");
		final ArrayList<Point> points2 = readCSV("../data/points1.6.csv");
		
		System.out.println("Coordinates in points1:");
		for(final Point a : points1)
			System.out.println("( " + a.x + ", " + a.y + " )");
	}
	
	public static void main(final String[] args) throws FileNotFoundException, IOException{
		run();
	}
}
