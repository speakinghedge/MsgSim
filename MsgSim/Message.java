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
      
  float m_step_per_ms; //!< 1/total_active_time - part of 0..1 to move for a given time
  boolean m_invert_direction; //!< true if the movement is defined in the oposite direction as the path definition was done
    
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
    m_invert_direction = !m_from_id.equals(m_parent_path.m_from_id);       
    m_step_per_ms = (float)1.0 / m_time_visible;
    
    m_was_active = false;    
  }
  
  public void setWasActiveFlag(boolean was_active) { m_was_active = was_active; }
  public boolean getWasActiveFlag() { return m_was_active; }
   
  public boolean isFinished(int time) { return time > m_end_time; } 

  /**
   * calculate the position based on current simulation time
   */
  void draw_node(int time) {
    
    if (m_invert_direction) {
      m_pos.setPos(m_parent_path.getPathPos((m_time_visible - getActiveTime(time)) * m_step_per_ms));
    } 
    else
   {
     m_pos.setPos(m_parent_path.getPathPos(getActiveTime(time) * m_step_per_ms));
   } 
    
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
