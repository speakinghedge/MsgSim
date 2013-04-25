/**
 * element that holds a path = connection between two knots
 */


import processing.core.*;
import processing.data.XML;

class Path extends SceneElement {
  
  Point m_start;
  Point m_end;
  
  Knot m_knot_from;
  Knot m_knot_to;
  
  String m_from_id;
  String m_to_id;

  int m_stroke_width;  //!< diameter of the path  
  
  int m_width;
  Point m_path_middle;
  
  int m_type;   //!< type of the path, 0 - straight, 1 - arc
  Point m_arc_path_center; //!< center point of the curve
  
  public Path(PApplet parent, XML xml, IdResolver resolver) throws Exception {
    
    super(parent, xml, resolver);
    
  }
  
  public boolean goesFromTo(String from_id, String to_id) {
    return m_from_id.equals(from_id) && m_to_id.equals(to_id);
  }
  
  void parse_element_attributes(XML xml) throws Exception {
    
    m_from_id = xml.getString("from", "").trim().toLowerCase();
    m_to_id = xml.getString("to", "").trim().toLowerCase();
    
    if (m_from_id.length() == 0 || m_to_id.length() == 0 ) {
      throw new Exception("missing from or to for path with id "  +  getId());        
    }
      
    m_stroke_width = xml.getInt("width", 1);
    
    m_type = xml.getInt("type", 0);
    m_arc_path_center = new Point(xml.getString("cpos", "0,0,0")); // center point of the curve
  }
  
  void initElement() throws Exception {
    
    if (null == (m_knot_from = m_resolver.getKnot(m_from_id))) {      
      throw new Exception("invalid start knot given for path with id " + getId());
    }
    m_start = m_knot_from.m_pos;
    
    
    if (null == (m_knot_to = m_resolver.getKnot(m_to_id))) {
      throw new Exception("invalid end knot given for path with id " + getId());
    }
    m_end = m_knot_to.m_pos;
    
    m_width = 0;
    if (m_label_position != 0) {
      throw new Exception("invalid label position given for path  " + getId() + ". only center position allowed");      
    }
    
    m_path_middle = new Point( m_start.getX() + ((m_end.getX() - m_start.getX())/2), 
                                  m_start.getY() + ((m_end.getY() - m_start.getY())/2),
                                  m_start.getZ() + ((m_end.getZ() - m_start.getZ())/2));
    
    m_top_middle_pos = getPathPos(0.5);
    
    p.bezierDetail(40);

  }
  
  public void drawSpecificPart(int time) {
    
    switch(m_type) {
      case 0: //straight
        p.strokeWeight(m_stroke_width);
        p.stroke(m_color);
        p.line(m_start.getX(), m_start.getY(), m_start.getZ(),
                      m_end.getX(), m_end.getY(), m_end.getZ());
        p.noStroke();
        break;
      case 1: //bezir        
        p.strokeWeight(m_stroke_width);
        p.stroke(m_color);
        p.noFill();
        p.beginShape();        
        p.vertex(m_start.getX(), m_start.getY(), m_start.getZ());        
        p.bezierVertex(m_arc_path_center.getX(), m_arc_path_center.getY(), m_arc_path_center.getZ(),
                       m_arc_path_center.getX(), m_arc_path_center.getY(), m_arc_path_center.getZ(),
                       m_end.getX(), m_end.getY(), m_end.getZ()); //first point
        p.endShape();  
        p.noStroke();
        break;
    }      
  }
  
  int getElementWidth() { return m_width; }
  
  Point getElementTopMiddlePosition() {
    return m_top_middle_pos;
  }
  
  Point getPathPos(double t) {
    
    switch (m_type) {
      default:
      case 0:
            return new Point( (int)(m_start.getX() + ((m_end.getX() - m_start.getX())*t)), 
                              (int)(m_start.getY() + ((m_end.getY() - m_start.getY())*t)),
                              (int)(m_start.getZ() + ((m_end.getZ() - m_start.getZ())*t)));
      case 1:  
            return new Point( (int) p.bezierPoint((float)m_start.getX(), (float)m_arc_path_center.getX(), (float)m_arc_path_center.getX(), (float)m_end.getX(), (float)t),
                              (int) p.bezierPoint((float)m_start.getY(), (float)m_arc_path_center.getY(), (float)m_arc_path_center.getY(), (float)m_end.getY(), (float)t),
                              (int) p.bezierPoint((float)m_start.getZ(), (float)m_arc_path_center.getZ(), (float)m_arc_path_center.getZ(), (float)m_end.getZ(), (float)t));
    } 
  }

  boolean parentsVisible(int time) {
    return m_knot_from.isActive(time) && m_knot_to.isActive(time);
  }  
}
