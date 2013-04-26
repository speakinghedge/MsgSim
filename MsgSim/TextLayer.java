/**
 * element that holds a horizontal or vertical text layer
 */

import processing.core.*;
import processing.data.XML;

class TextLayer extends SceneElement {
  
  int m_width;         //!< width of the text layer; default 100
  int m_height;        //!< height of the text layer; default 100 
  int m_orientation;   //!< orientation of the text layer; default 0 - vertical, 1 - horizontal
  
  String m_text;       //!< the text to show
  
  /* TODO: not used yet */
  int m_text_v_align; //-1: left, 0: center, 1: right
  int m_text_h_align; //-1: top, 0: center, 1: bottom
  
  int m_text_color;        //!< color of the text 
  int m_text_font_size;    //!< font size for the text
  boolean m_typewriter_enabled;   //!< add text to the textlayer character by character  
  int m_typewriter_interval_ms;  //!< milliseconds per character
  boolean m_typewriter_restart_endless; //!< restart typewriter if end of text reached
  
  boolean m_text_do_deep_check; //!< do deep check while rendering elements text, default true
  
  public TextLayer(PApplet parent, XML xml, IdResolver resolver) throws Exception {
   
    super(parent, xml, resolver);
    m_top_middle_pos = new Point(m_pos.getX() + (m_width/2), m_pos.getY(), m_pos.getZ());
  }
  
  void parse_element_attributes(XML xml) throws Exception {
      
    m_width = xml.getInt("width", 100);
    m_height = xml.getInt("height", 100);
    m_orientation = xml.getInt("orientation", 0);
    
    m_text = xml.getString("text", "").trim().replace("\\n", "\n");
    m_text_font_size = xml.getInt("tfsize", 12);
    
    m_text_v_align = xml.getInt("tvalign", 0); // ignored for now
    m_text_h_align = xml.getInt("thalign", 0); // ignored for now
    
    m_text_do_deep_check = xml.getInt("tddc", 1)==1?true:false;
    try {
      m_text_color = PApplet.unhex(xml.getString("tcolor", "ffff0000"));
    }
    catch(Exception e) {
      throw new Exception("Wrong fromat for attribute tcolor");
    }

    m_typewriter_enabled = xml.getInt("typewriter", 0)==0?false:true;
    m_typewriter_interval_ms = xml.getInt("typewriter_interval", 100); 
    m_typewriter_restart_endless = xml.getInt("typewriter_restart_endless", 0)==0?false:true;
    
  }
  
  /**
   * draw the text layer and the text if given
   */
  public void drawSpecificPart(int time) {
      
    p.fill(m_color);        
    p.textAlign(PApplet.LEFT);

    p.hint(m_do_deep_check == true ? PApplet.ENABLE_DEPTH_TEST : PApplet.DISABLE_DEPTH_TEST);      
         
    if ( m_orientation == 0 ) { //vertical
      
      p.beginShape(PApplet.QUADS);
      p.vertex(m_pos.getX(), m_pos.getY(), m_pos.getZ());
      p.vertex(m_pos.getX() + m_width, m_pos.getY(), m_pos.getZ());
      p.vertex(m_pos.getX() + m_width, m_pos.getY() + m_height, m_pos.getZ());
      p.vertex(m_pos.getX() , m_pos.getY() + m_height, m_pos.getZ());
      p.endShape(PApplet.CLOSE);
      
      if (m_text.length() != 0) {
        p.pushMatrix();
        p.translate(0, 0, m_pos.getZ() + 2);
        p.hint(m_text_do_deep_check == true ? PApplet.ENABLE_DEPTH_TEST : PApplet.DISABLE_DEPTH_TEST);
        p.textSize(m_text_font_size);
        p.textLeading(m_text_font_size);
        p.fill(m_text_color);  
        p.text(get_typewriter_text(time), m_pos.getX() + 10 , m_pos.getY() + 10, m_width - 20, m_height - 20);
        p.popMatrix();
      }
    
    } else { //horizontal
    
      p.beginShape(PApplet.QUADS);
      p.vertex(m_pos.getX(), m_pos.getY(), m_pos.getZ());
      p.vertex(m_pos.getX() + m_width, m_pos.getY(), m_pos.getZ());
      p.vertex(m_pos.getX() + m_width, m_pos.getY(), m_pos.getZ() + m_height);
      p.vertex(m_pos.getX() , m_pos.getY(), m_pos.getZ() + m_height);
      p.endShape(PApplet.CLOSE);

      if (m_text.length() != 0) {      
        p.pushMatrix();
        p.translate(m_pos.getX(), m_pos.getY() - 2, m_pos.getZ());
        p.rotateX(PApplet.HALF_PI);
        p.hint(m_text_do_deep_check == true ? PApplet.ENABLE_DEPTH_TEST : PApplet.DISABLE_DEPTH_TEST);
        p.textSize(m_text_font_size);
        p.textLeading(m_text_font_size);
        p.fill(m_text_color);        
        p.text(get_typewriter_text(time), 10 , 10 , m_width - 20, m_height - 20);
        p.popMatrix();
      }              
    }        
  }  
  
  private String get_typewriter_text(int time) {
    
    if (!m_typewriter_enabled) {
      return m_text;
    } else {
      if (m_typewriter_restart_endless) {
        return m_text.substring(0,((time - m_start_time) / m_typewriter_interval_ms)% (1+m_text.length()));
      } else {
        int t = ((time - m_start_time) / m_typewriter_interval_ms);
        if (t > m_text.length()) {
          return m_text;
        } else {
          return m_text.substring(0,((time - m_start_time) / m_typewriter_interval_ms));
        }
      }
    }
  }
  
  int getElementWidth() { return m_width; }
  Point getElementTopMiddlePosition() { return m_top_middle_pos; }  
  void initElement() throws Exception { /* NOP */ }
  boolean parentsVisible(int time) { return true; }
}
