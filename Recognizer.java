import java.awt.Point;
import java.io.*;

/**
 * This class performs unistroke handwriting recognition using an algorithm
 * known as "elastic matching."
 * 
 * @author Dave Berque
 * @version August, 2004 Slightly modified by David E. Maharry and Carl Singer
 *          10/27/2004
 * 
 */

public class Recognizer {
    public static final int STROKESIZE = 150;                                           
    private static final int NUMSTROKES = 10; 
    private Point[] userStroke; 
    private int nextFree;
    private Point[][] baseSet;
    /**
     * Constructor for the recognizer class. Sets up the arrays and loads the
     * base set from an existing data file which is assumed to have the right
     * number of points in it. The file is organized so that there are 150
     * points for stroke 0 followed by 150 points for stroke 1, ... 150 poinpts
     * for stroke 9. Each stroke is organized as an alternating series of x, y
     * pairs. For example, stroke 0 consists of 300 lines with the first line
     * being x0 for stroke 0, the next line being y0 for stroke 0, the next line
     * being x1 for stroke 0 and so on.
     */
    public Recognizer()
    {
        int row, col, stroke, pointNum, x, y;
        String inputLine;

        userStroke = new Point[STROKESIZE];
        baseSet = new Point[NUMSTROKES][STROKESIZE];

        try {
            FileReader myReader = new FileReader("strokedata.txt");
            BufferedReader myBufferedReader = new BufferedReader(myReader);
            for (stroke = 0; stroke < NUMSTROKES; stroke++)
                for (pointNum = 0; pointNum < STROKESIZE; pointNum++) {
                    inputLine = myBufferedReader.readLine();
                    x = Integer.parseInt(inputLine);
                    inputLine = myBufferedReader.readLine();
                    y = Integer.parseInt(inputLine);
                    baseSet[stroke][pointNum] = new Point(x, y);
                }
            myBufferedReader.close();
            myReader.close();
        }
        catch (IOException e) {
            System.out.println("Error writing to file.\n");
        }
    }

    
    /**
     * translate - Translates the points in the userStroke array by sliding them
     * as far to the upper-left as possible. It does this by finding the minX
     * value and the minY value. Then each point (x, y) is replaced with the
     * point (x-minX, y-minY). Note: you can use the translate method of the
     * Point class
     */
    public void translate()
    {
        int minX=findMinX();
        int minY=findMinY();
        for(int i=0; i<nextFree;i++)
        {   
            
            userStroke[i].x-=minX;
            userStroke[i].y-=minY;
        }
    }

    
    /**
     * scale - Scales the points in the user array by stretching the user's
     * stroke to fill the canvas as nearly as possible while maintaining the
     * aspect ratio of the stroke.
     */
    public void scale()
    {
        
        double scaleFactor;
        int a=findMaxX();
        int b=findMaxY();
        if(a>b)
        {
            scaleFactor=250.0/a;
            for(int i=0; i<nextFree;i++)
        {
             userStroke[i].x=(int)(userStroke[i].x*scaleFactor);
             userStroke[i].y=(int)(userStroke[i].y*scaleFactor);
        }
        }
        else if(b>a)
        {
            scaleFactor=250.0/b;
            for(int i=0; i<nextFree;i++)
        {
             userStroke[i].x=(int)(userStroke[i].x*scaleFactor);
             userStroke[i].y=(int)(userStroke[i].y*scaleFactor);
        }
        }
        
        
        
        
    }
    
    /**
     * insertOnePoint - inserts a new point between the two points that are the
     * farthest apart in the userStroke array. There must be at least two points
     * in the array
     */
    private void insertOnePoint()
    {
        int maxPosition = 0, newX, newY, distance;
        int maxDistance = (int) userStroke[0].distance(userStroke[1]);
        for(int i=1;i<nextFree-1;i++)
        {
            if(userStroke[i].distance(userStroke[i+1])>maxDistance)
            {
                maxDistance=(int)userStroke[i].distance(userStroke[i+1]);
            maxPosition=i;
            }
        }
        for (int i = nextFree; i > maxPosition + 1; i--)
            userStroke[i] = userStroke[i - 1];

        newX = (int) (userStroke[maxPosition].getX() + userStroke[maxPosition + 2]
                .getX()) / 2;
        newY = (int) (userStroke[maxPosition].getY() + userStroke[maxPosition + 2]
                .getY()) / 2;
        userStroke[maxPosition + 1] = new Point(newX, newY);

        nextFree++;
    }

    /**
     * normalizeNumPoints - Adds points to the userStroke by inserting points
     * repeatedly until there are STROKESIZE points in the stroke
     */
    public void normalizeNumPoints()
    {
        while (nextFree < STROKESIZE) {
            insertOnePoint();
        }
    }

    /**
     * computeScore Computes and returns a "score" that is a measure of how
     * closely the normalized userStroke array matches a given pattern array in
     * the baseset array. The score is the sum of the distances between
     * corresponding points in the userStroke array and the pattern array.
     * 
     * @param digitToCompare
     *            The index of the pattern in the baseset with which to compute
     *            the score
     */
    public int computeScore(int i)
    {
        int score=0;
        for(int j=0;j<nextFree;j++)
        {
            
            score=(int)(score+userStroke[j].distance(baseSet[i][j]));
        }
        return score;
    }

    /**
     * findMatch - Finds and returns the index (an int) of the base set pattern
     * which most closely matches the user stroke.
     */
    public int findMatch()
    {
        translate();
        scale();
        normalizeNumPoints();
        
        int minIndex=0;
        for(int i=1;i<10;i++)
        {
            if(computeScore(i)<computeScore(minIndex))
            minIndex=i;
            
        }
        return minIndex;          
    }

    /**
     * findMinX - returns the smallest x value in the userStroke array of points
     */
    public int findMinX()
    {
        int min=userStroke[0].x;
        for (int i=1;i<nextFree;i++)
        {
            if(userStroke[i].x<min)
            {
                min=userStroke[i].x;
            }
        }
        return min;
        
    }
    
    /**
     * findMinY - returns the smallest y value in the userStroke array of points
     */
    public int findMinY()
    {
        int min=userStroke[0].y;
        for (int i=1;i<nextFree;i++)
        {
            if(userStroke[i].y<min)
            {
                min=userStroke[i].y;
            }
        }
        return min;
        
    }
    
    /**
     * findMaxX - returns the largest x value in the userStroke array of points
     */
    public int findMaxX()
    {
        int max=userStroke[0].x;
        for (int i=1;i<nextFree;i++)
        {
            if(userStroke[i].x>max)
            {
                max=userStroke[i].x;
            }
        }
        return max;
        
    }
    
    /**
     * findMaxY - returns the largest y value in the userStroke array of points
     */
    public int findMaxY()
    {
        int max=userStroke[0].y;
        for (int i=1;i<nextFree;i++)
        {
            if(userStroke[i].y>max)
            {
                max=userStroke[i].y;
            }
        }
        return max;
        
    }
    
    public void resetUserStroke()
    {
        nextFree = 0;
    }

    public int numUserPoints()
    {
        return nextFree;
    }

    public int getUserPointX(int i)
    {
        if ((i >= 0) && (i < nextFree))
            return ((int) userStroke[i].getX());
        else {
            System.out.println("Invalid value of i in getUserPoint");
            return (0);
        }
    }

    public int getUserPointY(int i)
    {
        if ((i >= 0) && (i < nextFree))
            return ((int) userStroke[i].getY());
        else {
            System.out.println("Invalid value of i in getUserPoint");
            return (0);
        }
    }

    public void addUserPoint(Point newPoint)
    {
        if (nextFree < STROKESIZE) {
            userStroke[nextFree] = newPoint;
            nextFree++;
        }
    }
}
