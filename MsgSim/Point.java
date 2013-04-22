import processing.core.*;

class Point {
  
  int pos_x;
  int pos_y;
  int pos_z;

  public Point() {
    pos_x = pos_y = pos_z = 0;
  }

  public Point(int x, int y, int z) {
    setPos(x, y, z);
  }
  
  public Point(String pos_str) throws Exception {

    String[] pos = pos_str.split(",");
    
    if (pos.length != 3) {      
      throw new Exception("invalid position string - only " + pos.length + " elements given but need 3");
    }
    
    pos_x = Integer.parseInt(pos[0]);
    pos_y = Integer.parseInt(pos[1]);
    pos_z = Integer.parseInt(pos[2]); 
  }
  
  public int getX() { return pos_x; }
  public int getY() { return pos_y; }
  public int getZ() { return pos_z; }
  
  public void setX(int x) { pos_x = x; }
  public void setY(int y) { pos_y = y; }
  public void setZ(int z) { pos_z = z; }
  public void addX(int x) { pos_x += x; }
  public void addY(int y) { pos_y += y; }
  public void addZ(int z) { pos_z += z; }
  
  public void setPos(int x, int y, int z) {
    pos_x = x; pos_y = y; pos_z = z;
  }
  
  public void setPos(Point other) {
    pos_x = other.pos_x; pos_y = other.pos_y; pos_z = other.pos_z;
  }
  
  public Point getPos() { return this; }
  
  Point calcDeltaAndSet(int x, int y, int z) {
    Point d = new Point(pos_x - x, pos_y - y, pos_z - z);
    pos_x = x; 
    pos_y = y; 
    pos_z = z;
    return d;
  }
  
  public void dump() {
    PApplet.println("X:"+pos_x+"\nY:"+pos_y+"\nz:"+pos_z);    
  }
}
