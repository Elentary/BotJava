package entity;

/**
 * Created by amareelez on 23.12.16.
 */
public class Amazon {
    public final int id;
    public int row;
    public int column;

    public Amazon(int row, int column, int id) {
        this.row = row;
        this.column = column;
        this.id = id;
    }

    public void updateCoords(int x, int y) {
        this.row = x;
        this.column = y;
    }

}
