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
    /**
     * 
     * @param input the List of Points RANSAC will use
     * @param iterations the number of trials RANSAC runs
     * @param epsilon: a point is in the anulus if its distance to centre is in [radius, radius*(1+epsilon)]
     * @param inlierRatio the number of inliers necessary for an accepted model, scaled to the number of inputs
     * @return the List of Circles found
     */
    private List<Model> doRansac(final List<Point> input, final int iterations, final double epsilon, final double inlierRatio) {
        final MinMaxPriorityQueue<Model> models = 
                MinMaxPriorityQueue.orderedBy(new Comparator<Model>() {
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

            final Circle circle = Circle.fromPoints(input.get(index1),input.get(index2),input.get(index3));
            
            Model model=new Model(circle,epsilon);

            for(Point point : input)
                if(Model.inAnulus(model, point))
                    model.add(point);

            if (!models.contains(model))
                models.offer(model);
        }
        final List<Model> finalModels = new ArrayList<>(models.size());
        while(!models.isEmpty()) {
            finalModels.add(models.poll());
        }
        return finalModels;
    }
    
    private List<Point> readCSV(final String fileName) throws FileNotFoundException, IOException {
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
    
    private String rawPrint(Model model) {
        Circle circle=model.circle;
        return circle.center.x+", "+circle.center.y+", "+circle.radius+", "+model.numberOfInliers;
    }

    private void writeCSV(List<Model> models, String filename) throws FileNotFoundException, IOException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        for(Model model : models) 
            writer.println(rawPrint(model));
        writer.close();    
    } 

    void run() throws FileNotFoundException, IOException {
        System.out.println("Reading CSVs...");
    
        
        final List<Point> points1 = readCSV("../data/points1.5.csv");
        final List<Point> points2 = readCSV("../data/points1.6.csv");
        System.out.println("RANSAC time! (1)");
        final List<Model> models1 = doRansac(points1,100000,.2,.015);
        System.out.println("RANSAC time! (2)");
        final List<Model> models2 = doRansac(points2,100000,.2,.015);
        

        System.out.println("Improvement! (1)");
        for(Model m : models1) {
            //m.improveAnulusMonteCarlo(10000);
            //WRITE TO NEW LIST TODO
            m=(Model.improveAnulusApprox(m));
        }
        System.out.println("Writing CSVs..");
        writeCSV(models1, "../data/results1.5.csv");
        writeCSV(models2, "../data/results1.6.csv");
        System.out.println("All done!");
    }
    
    public static void main(final String[] args) throws FileNotFoundException, IOException {
        RANSAC ransac=new RANSAC();
        ransac.run();
    }
}
