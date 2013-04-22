import processing.core.*;
import processing.data.XML;

abstract class Node extends SceneElement {
 
  int m_type;
  private Point m_dimensions;
  
  public Node(PApplet parent, XML xml, IdResolver resolver) throws Exception {
    super(parent, xml, resolver);    
  }
  
  abstract void parse_node_element_attributes(XML xml) throws Exception;
  abstract void draw_node(int time);
    
  void parse_element_attributes(XML xml) throws Exception {
      
    m_type = xml.getInt("type", 0);
    m_dimensions = null;
    
    switch(m_type) {
      case 0: //sphere
      case 1: //box
        m_dimensions  = new Point(xml.getInt("radius", 25), 0, 0);        
        break;
      case 2: //quad
        m_dimensions  = new Point(xml.getString("dim", "20,30,40")); // width, height, deept,         
        break;
    }
    
    calcTopMiddlePosition();
    
    try {
      parse_node_element_attributes(xml);
    }
    catch (Exception e) {
      throw new Exception(e);
    }
  }
  
  Point calcTopMiddlePosition() {
    
    switch(m_type) {
      case 0: //sphere
        m_top_middle_pos = new Point(m_pos.getX(), m_pos.getY() - m_dimensions.getX(), m_pos.getZ());
        break;
      case 1: //box
        m_top_middle_pos = new Point(m_pos.getX(), m_pos.getY() - (m_dimensions.getX()/2), m_pos.getZ());
        break;
      case 2: //quad        
        m_top_middle_pos = new Point(m_pos.getX(), m_pos.getY() -  (m_dimensions.getY()/2), m_pos.getZ());
        break;
    }
    return m_top_middle_pos;
  }
    
  /**
   * draw the Node
   */
  public void drawSpecificPart(int time) {
    
    p.noStroke();
    p.fill(m_color);
    
    p.pushMatrix();
    
    /* first do node specific stuff (drawing/position calculation) */
    draw_node(time);
    p.translate(m_pos.getX(), m_pos.getY(), m_pos.getZ());
    
    switch(m_type) {
      case 0: //sphere
        p.sphere((float)(m_dimensions.getX()));
        break;
      case 1: //box
        p.box(m_dimensions.getX());
        break;
      case 2: //quad
        p.box(m_dimensions.getX(), m_dimensions.getY(), m_dimensions.getZ()); 
        break;
      
    }
    p.popMatrix(); 
  }
  
  int getElementWidth() {
    switch(m_type) {
      case 0: //sphere
      case 1: //box
        return m_dimensions.getX();
      case 2:
        return m_dimensions.getX();
      default:
        return -1;
     }
  }
      
}
