/**
 * element that holds a horizontal or vertical image layer
 * images must be put to a data directory within the sketch or given by an absolute path (local/url)
 * alpha is only supported for gif & png
 */

import processing.core.*;
import processing.data.XML;

class ImageLayer extends SceneElement {
  
  int m_width;             //!< width of the image layer; defaults to the image width, if set -> scale image
  int m_height;            //!< height of the image layer; default to the image height, if set -> scale image
  int m_orientation;       //!< orientation of the text layer; default 0 - vertical, 1 - horizontal
  int m_alpha;             //!< transparency, 0 -> total, 255 -> full visible, default: 255
  int m_fade_in_time;      //!< time in ms to run brightness from 0 to 255, default: 0
  float m_fade_in_per_ms;
  int m_fade_out_time;     //!< time in ms to run brightness from 255 to 0, default: 0
  float m_fade_out_per_ms;
  
  PImage m_image;       //<! holds the image
  
  public ImageLayer(PApplet parent, XML xml, IdResolver resolver) throws Exception {
   
    super(parent, xml, resolver);
    m_top_middle_pos = new Point(m_pos.getX() + (m_width/2), m_pos.getY(), m_pos.getZ());
  }
  
  void parse_element_attributes(XML xml) throws Exception {
      
    m_width = xml.getInt("width", -1);
    m_height = xml.getInt("height", -1);
    m_orientation = xml.getInt("orientation", 0);
    
    m_alpha = xml.getInt("alpha", 255);
    m_fade_in_time = xml.getInt("t_fade_in", 0);
    m_fade_out_time = xml.getInt("t_fade_out", 0);
    
    m_fade_in_per_ms = (float)(m_fade_in_time != 0 ? 255.0/m_fade_in_time:0.0);
    m_fade_out_per_ms = (float)(m_fade_out_time != 0 ? 255.0/m_fade_out_time:0);
    
    if ( (m_time_visible > 0) && ((m_fade_in_per_ms + m_fade_out_per_ms) > m_time_visible) ) {
      throw new Exception("invalid attribute(s). t_fade_in + t_fade_out > time the image is visible.");
    }
    
    if ( xml.getString("src","").length() == 0) {
      throw new Exception("empty or missing attribute src for img_layer.");
    }
    
    m_image = p.loadImage(xml.getString("src",""));
    
    if (null == m_image) {
      throw new Exception("failed to load image " + xml.getString("src",""));
    }
    
    if (m_width < 0) {
      m_width = m_image.width;
    }
    
    if (m_height < 0) {
      m_height = m_image.height;
    }
  }
  
  /**
   * draw the image layer
   */
  public void drawSpecificPart(int time) {
    
    int toa = getActiveTime(time); 
    float t = 255;  //tint
    
    p.pushMatrix();
    p.translate(m_pos.getX(), m_pos.getY(), m_pos.getZ());
    if (m_orientation == 1) {
      p.rotateX(PApplet.HALF_PI);
    }
        
    if (m_fade_in_time != 0 &&  toa < m_fade_in_time) {
      t = (m_fade_in_per_ms*toa);      
    } else if (m_fade_out_time != 0 &&  time >= m_end_time - m_fade_in_time) {
      t = m_fade_out_per_ms*(m_time_visible - toa);      
    }
    
    p.noFill();
    if (m_stroke_width > 0) 
    {
      p.stroke(m_stroke_color);
      p.strokeWeight(m_stroke_width);
      p.rect((float)0, (float)0, (float)m_width, (float)m_height);
    } else {
      p.noStroke();
    }
    
    p.fill(255,255,255);    
    p.tint(t, t, t, t>m_alpha?m_alpha:t);
    
    p.image(m_image, 0, 0, m_width, m_height);
    p.popMatrix();
  }
    
  int getElementWidth() {
    return m_width;
  }

  Point getElementTopMiddlePosition() {
    return m_top_middle_pos;
  }  
  
  void initElement() throws Exception { /* NOP */ }
  boolean parentsVisible(int time) { return true; }
};
