/** Class defining a point as two coordinates.*/
class Point {
	/** The coordinates of this point.*/
	double x,y;
	
	/** Creates a new Point based on an existing point.*/
	public Point(Point p){
		this.x = p.x;
		this.y = p.y;
	}
	
	/** Creates a new Point given an x and y.*/
	public Point(double x,double y){
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
	
	/** Calculates the euclidean distance between two points.*/
	public static double euclDistance(final Point first,final Point second){
		return Math.sqrt(
				Math.pow((first.x-second.x),2) + 
				Math.pow((first.y-second.y),2));
	}
}

