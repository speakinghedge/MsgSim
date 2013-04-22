import processing.core.*;
import processing.data.XML;

class Message extends Node {
  
  Point m_start;
  Point m_end;
  
  Path m_parent_path = null;
  
  private String m_from_id;//!< source of the msg
  private String m_to_id;  //!< target of the msg
  private int m_size;      //!< size of the message; default 1
    
  boolean m_was_active; //!< used for external state handling
    
  float m_x_per_ms, m_y_per_ms, m_z_per_ms; //!< movement per ms for x/y/z direction
    
  public Message(PApplet parent, XML xml, IdResolver resolver) throws Exception {
                
    super(parent, xml, resolver);
    
  }
  
  void parse_node_element_attributes(XML xml) throws Exception {
    
    m_from_id = xml.getString("from", "").trim().toLowerCase();
    m_to_id = xml.getString("to", "").trim().toLowerCase();
    m_size = xml.getInt("size", 1);
      
    if (m_from_id.length() == 0 || m_to_id.length() == 0 ) {
      throw new Exception("missing from or to for message with id "  +  getId());        
    }    
  }
    
  void initElement() throws Exception {
    
    /* start / end position */
    if (null == (m_start = m_resolver.getPosOfKnot(m_from_id))) {
      throw new Exception("invalid attribute 'from' for message with id " + getId());
    }
    if (null == (m_end = m_resolver.getPosOfKnot(m_to_id))) {
      throw new Exception("invalid attribute 'to' for message with id " + getId());
    }
    
    /* get associated path */
    if ( null == (m_parent_path = m_resolver.getPath(m_from_id, m_to_id))) {
      throw new Exception("missing path for message " + getId());
    }
   
    m_pos.setPos(m_start);
    
    m_x_per_ms = ((float)m_end.getX() - m_start.getX()) / m_time_visible;
    m_y_per_ms = ((float)m_end.getY() - m_start.getY()) / m_time_visible;
    m_z_per_ms = ((float)m_end.getZ() - m_start.getZ()) / m_time_visible;
    
    m_was_active = false;    
  }
  
  public void setWasActiveFlag(boolean was_active) { m_was_active = was_active; }
  public boolean getWasActiveFlag() { return m_was_active; }
   
  public boolean isFinished(int time) { return time > m_end_time; } 

  /**
   * calculate the position based on current simulation time
   */
  void draw_node(int time) { 
    
    m_pos.setPos((int)(m_start.getX() + (getActiveTime(time) * m_x_per_ms)), 
                 (int)(m_start.getY() + (getActiveTime(time) * m_y_per_ms)),
                 (int)(m_start.getZ() + (getActiveTime(time) * m_z_per_ms)));
    
  }  
    
  Point getElementTopMiddlePosition() {
     return calcTopMiddlePosition();
  }
  
  boolean parentsVisible(int time) { 
    return m_parent_path.isActive(time);
  }
  
  String getFrom() { return m_from_id; };
  String getTo() { return m_to_id; };
  int getSize() { return m_size; };
}
