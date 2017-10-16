package ListView;

/**
 * Created by Mat on 12/03/2016.
 *
 * DirectionRow, custom ListView item
 */

public class DirectionRow {
    private int imageID;
    private String direction;

    public DirectionRow(int i ,String dir){
        imageID = i;
        direction = dir;
    }

    public int getImageID(){return imageID;}

    public String getDirection(){return direction;}

    @Override
    public String toString(){
        return direction;
    }

}
