/**
 * Class defining a circle by its centre and radius.
 * @author Arie
 *
 */
class Circle {
	/** The centre of this circle.*/
	Point center;
	/** The radius of this circle.*/
	double radius;
	
	/** Creates a new Circle using the provided circle.*/
	public Circle(Circle c){
		this.center = new Point(c.center);
		this.radius = c.radius;
	}
	
	/** Creates a new Circle from a given centre and radius.*/
	public Circle(Point center, double radius){
		this.center = new Point(center.x,center.y);
		this.radius = radius;
	}
	
	/** Returns whether a point is inside the circle. */
	public static boolean inCircle(Circle c, Point p) {
		double distanceToCircle = Math.abs(Point.euclDistance(c.center,p));
		return distanceToCircle < c.radius;
	}
	
	@Override public String toString(){
		return "Circle at (" + center.toString() + "), with radius " + Double.toString(radius);
	}
	
	@Override public boolean equals(final Object o){
		if (!(o instanceof Circle)) return false;
		Circle c = (Circle)o;
		return Double.compare(radius, c.radius) == 0 && center == c.center;
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + center.hashCode();
		long xSemiHash = Double.doubleToLongBits(radius);
		int xHash = (int) (xSemiHash ^ (xSemiHash >>> 32));
		result = result * 31 + xHash;
		return result;
	}
	
	/** Generates a Circle based on three points. This is a geometric algorithm 
	 * taken from http://paulbourke.net/geometry/circlesphere/.
	 * 
	 * The given points should not be perpendicular to some axis, because creating
	 * a circle from a line is nonsense!
	 * 
	 * @throws IllegalArgumentException if the points are perpendicular to some axis.*/
	public static Circle fromPoints(Point p1, Point p2, Point p3){
		if (!isPerpendicular(p1,p2,p3)) return calcCircle(p1,p2,p3);
		else if (!isPerpendicular(p1, p3, p2)) return calcCircle(p1, p3, p2);	
		else if (!isPerpendicular(p2, p1, p3)) return calcCircle(p2, p1, p3);	
		else if (!isPerpendicular(p2, p3, p1)) return calcCircle(p2, p3, p1);	
		else if (!isPerpendicular(p3, p2, p1)) return calcCircle(p3, p2, p1);	
		else if (!isPerpendicular(p3, p1, p2)) return calcCircle(p3, p1, p2);	
		else throw new IllegalArgumentException("Some of the points are perpendicular to some axis!");
	}
	
	/** Checks for the given points if they are perpendicular to x or y axis.*/
	private static boolean isPerpendicular(Point p1, Point p2, Point p3){
		// the original method works by comparing delta_x with a very small value
		// here I compare the two values using Double.compare
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
	private static Circle calcCircle(Point p1, Point p2, Point p3){
		// see original website for details about the implementation!
		final double yDeltaA = p2.y - p1.y;
		final double xDeltaA = p2.x - p1.x;
		final double yDeltaB = p3.y - p2.y;
		final double xDeltaB = p3.x - p2.x;
		
		if (Double.compare(p2.x,p1.x) == 0 && Double.compare(p3.y,p2.y) == 0){
			// this is the case where the three points lie on a triangle with
			// a right angle, and lie parallel to the axes.
			double circleX = .5 * (p2.x + p3.x);
			double circleY = .5 * (p1.y + p2.y);
			Point centerPoint = new Point(circleX,circleY);
			double radius = Point.euclDistance(centerPoint,p1);
			return new Circle(centerPoint,radius);
		}
		
		final double aSlope = yDeltaA / xDeltaA;
		final double bSlope = yDeltaB / xDeltaB;
		
		if (Double.compare(aSlope,bSlope) == 0)
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

