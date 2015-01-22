
class Model {
    Circle circle;
    int numberOfInliers;

    public Model(final Circle circle,final int numberOfInliers){
        this.circle = circle;
        this.numberOfInliers = numberOfInliers;
    }

    @Override public String toString(){
        return circle.toString() + " with " + Integer.toString(numberOfInliers) + " inliers";
    }

    public double getScore() {
        return numberOfInliers/circle.radius;
    }
}

