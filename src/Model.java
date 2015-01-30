import java.util.List;
import java.util.ArrayList;

/**
 * A model stores information about the circle it encodes, the epsilon used and the
 * number of inliers that are covered by this circle. The RANSAC algorithm returns
 * the best model found, and these are compared using the number of inliers.
 * @author Arie, Abe
 *
 */
class Model {
	/** The circle this model proposes.*/
	Circle circle;
	/** The epsilon, or error margin, used for determining the inliers.*/
	double epsilon;
	/** The list of inliers that this model covers.*/
	final ArrayList<Point> inliers;
	int numberOfInliers;
	
	/** Creates a new Model from another model. */
	public Model(final Model m) {
		this.circle=new Circle(m.circle);
		this.epsilon=m.epsilon;
		this.inliers=new ArrayList<>(m.inliers);
		this.numberOfInliers=m.numberOfInliers;
	}
	
	/** Creates a new Model from a given circle and an epsilon. 
	 * This model has no initial inliers.*/
	public Model(final Circle circle, double epsilon){
		this.circle = circle;
		this.epsilon = epsilon;
		this.inliers = new ArrayList<>();
		this.numberOfInliers = inliers.size();
	}
	
	/** Creates a new Model based on a given circle, epsilon and list of inliers.*/
	public Model(final Circle circle, double epsilon, List<Point> inliers){
		this.circle = circle;
		this.epsilon = epsilon;
		this.inliers = new ArrayList<>(inliers);
		this.numberOfInliers = inliers.size();
	}
	
	/** Determines if a given point is covered by the annulus of the model.*/
	public static boolean inAnulus(Model m, Point p) {
		double distanceToCircle = Math.abs(Point.euclDistance(m.circle.center,p));
		return (distanceToCircle < m.circle.radius*(1+m.epsilon) && distanceToCircle > m.circle.radius);
	}
	
	/** Adds a new inlier point to the model.*/
	public void add(Point p) {
		inliers.add(p);
		numberOfInliers=inliers.size();
	}
	
	@Override public String toString(){
		return circle.toString() + " with " + Integer.toString(numberOfInliers) + " inliers";
	}
	
	@Override public boolean equals(final Object o){
		if (!(o instanceof Model)) return false;
		Model m = (Model)o;
		return circle == m.circle && Double.compare(epsilon, m.epsilon) == 0;
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + circle.hashCode();
		long epsilonSemiHash = Double.doubleToLongBits(epsilon);
		int epsilonHash = (int) (epsilonSemiHash ^ (epsilonSemiHash >>> 32));
		result = result * 31 + epsilonHash;
		return result;
	}
	
	public double getScore() {
		//return numberOfInliers/(Math.pow(circle.radius,2)*epsilon*(2+epsilon));//Minimize surface area
		//return Math.pow(numberOfInliers,0.9)/circle.radius;//Minimize radius
		
		//Statistically good score:
		int n=numberOfInliers;
		double e=epsilon;
		double w=circle.radius;
		double A=1.;
		double Qn=  n*(e+n-1) *
				Math.pow(2*Math.PI, n-1) * 
				Math.pow(Math.abs(Math.pow(w,2))/A, n-1) * 
				Math.pow(e, n-2);
		return 1/Qn;
	}
	
	public void improveAnulusMonteCarlo(int nIterations) {
		double t=1000.;
		for(int i=0; i<nIterations; i++) {
			t*=0.95;
			Circle oldCircle=new Circle(circle);
			double oldEpsilon=epsilon;
			double oldScore=getScore();
			
			epsilon*=(1.+(Math.random()-.5)*2./100.);
			circle.radius*=(1.+(Math.random()-.5)*2./100.);
			circle.center.x*=(1.+(Math.random()-.5)*2./100.);
			circle.center.y*=(1.+(Math.random()-.5)*2./100.);
			
			numberOfInliers=0;
			for(Point p : inliers)
				if(inAnulus(this,p))
					numberOfInliers++; 
			
			double newScore=getScore();
			double deltaScore=newScore-oldScore;
			if(Math.exp(-deltaScore/t) > Math.random() || numberOfInliers < inliers.size()) {//Reject new model;
			circle=oldCircle;
			epsilon=oldEpsilon;
			}
		}
	}
	
	public static Model improveAnulusApprox(final Model m) {
		Model result=new Model(m);
		for(int i=0;   i<m.inliers.size(); i++)
			for(int j=i+1; j<m.inliers.size(); j++)
				for(int k=j+1; k<m.inliers.size(); k++) {
					Circle circ=Circle.fromPoints(m.inliers.get(i), m.inliers.get(j), m.inliers.get(k));
					double close=-1.0;
					double far=0.0;
					int nInside=0;
					int nOutside=0;
					for(int p=0; p<m.inliers.size(); p++) {
						if(p == i || p == j || p == k) {
							nInside++;
							nOutside++;
						}
						
						if(Circle.inCircle(circ,m.inliers.get(p)))
							nInside++;
						else
							nOutside++;
						
						double dist=Point.euclDistance(circ.center, m.inliers.get(p));
						if(dist < close) close=dist;
						if(dist > far || far < 0.0) far=dist;
					}
					
					boolean circIsInside = (nOutside == m.inliers.size());
					boolean circIsOutside = (nInside == m.inliers.size());
					if(circIsInside || circIsOutside) {
						Model current;
						if(circIsInside) {
							current=new Model(circ, far/circ.radius - 1, m.inliers);
						} else {
							double epsilon=circ.radius/close - 1.0;
							circ.radius=close;
							current=new Model(circ, epsilon, m.inliers);
						}
						if(current.getScore() > result.getScore()) {
							result=current;
						}
					}
				}
		return result;
	}
	
}

