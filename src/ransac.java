import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class ransac {

    public class Point {
        double x, y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    ArrayList<Point> readCSV(String file) {
        ArrayList<Point> result=new ArrayList<Point>();
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] items=line.split(",");
                result.add(new Point(Double.parseDouble(items[0]), Double.parseDouble(items[1])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    void run() {
        System.out.println("We need to implement ransac!");
        ArrayList<Point> points1=readCSV("../data/points1.5.csv");
        ArrayList<Point> points2=readCSV("../data/points1.6.csv");
        
        System.out.println("Coordinates in points1:");
        for(Point a : points1) {
            System.out.println("( "+a.x+", "+a.y+" )");
        }
    }
    
    public static void main(String[] args) {
        ransac main=new ransac();
        main.run();
    }
}
