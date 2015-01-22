
class Circle {
    Point center;
    double radius;

    public Circle(Point center, double radius){
        this.center = new Point(center.x,center.y);
        this.radius = radius;
    }

    @Override public String toString(){
        return "Circle at (" + center.toString() + "), with radius " + Double.toString(radius);
    }

    /** Algorithm stolen from http://paulbourke.net/geometry/circlesphere/*/
    public static Circle fromPoints(Point p1, Point p2, Point p3){
        if (!isPerpendicular(p1,p2,p3)) return calcCircle(p1,p2,p3);
        else if (!isPerpendicular(p1, p3, p2)) return calcCircle(p1, p3, p2);	
        else if (!isPerpendicular(p2, p1, p3)) return calcCircle(p2, p1, p3);	
        else if (!isPerpendicular(p2, p3, p1)) return calcCircle(p2, p3, p1);	
        else if (!isPerpendicular(p3, p2, p1)) return calcCircle(p3, p2, p1);	
        else if (!isPerpendicular(p3, p1, p2)) return calcCircle(p3, p1, p2);	
        else throw new AssertionError("Some of the points are perpendicular to some axis!");
    }

    /** Check for the given points if they are perpendicular to x or y axis.*/
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

