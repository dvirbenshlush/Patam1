package test;


public class StatLib {


    // simple average
    public static float avg(float[] x) {
        float avg = 0;
        for (float v : x) {
            avg += v;
        }
        avg /= x.length;
        return avg;
    }

    // returns the variance of X and Y
    public static float var(float[] x) {
        float variance = 0;
        for (int i = 0; i < x.length; i++) {
            variance += Math.pow(x[i] - avg(x), 2);
        }
        variance /= x.length;
        return variance;
    }

    // returns the covariance of X and Y
    public static float cov(float[] x, float[] y) {
        float covar = 0;
        for (int i = 0; i < x.length; i++) {
            covar += (x[i] - avg(x)) * (y[i] - avg(y));
        }
        covar /= x.length;
        return covar;
    }


    // returns the Pearson correlation coefficient of X and Y
    public static float pearson(float[] x, float[] y) {
        float covar = cov(x, y);
        float vXvY = (float) Math.sqrt(var(x) * var(y));
        return covar / vXvY;
    }

    // performs a linear regression and returns the line equation
    public static Line linear_reg(Point[] points) {
        float a, b;
        Line Y;
        float[] x = new float[points.length];
        float[] y = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            x[i] = points[i].x;
            y[i] = points[i].y;
        }
        a = (cov(x, y) / var(x));
        b = avg(y) - a * (avg(x));
        Y = new Line(a, b);
        return Y;
    }

    // returns the deviation between point p and the line equation of the points
    public static float dev(Point p, Point[] points) {
        Line Y = linear_reg(points);
        return dev(p, Y);
    }

    // returns the deviation between point p and the line
    public static float dev(Point p, Line l) {
        float res = Math.abs(p.y - l.f(p.x));
        return res;
    }
}
