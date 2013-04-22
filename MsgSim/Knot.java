/**
 * element that holds a knot  
 */

import processing.core.*;
import processing.data.XML;

class Knot extends Node {
    
  private int m_rx_counter, m_tx_counter;
  private String m_old_label_text;  //!< hold the static label text from base class
  private boolean m_show_rx;
  private boolean m_show_tx;  
  private String m_counter_unit;
  
  public Knot(PApplet parent, XML xml, IdResolver resolver) throws Exception {
   
    super(parent, xml, resolver);
  }
  
  void parse_node_element_attributes(XML xml) throws Exception { 
  
    if (m_show_rx = xml.getInt("rxce", 0) == 1?true:false) {
      m_label_line_offset += m_label_font_size;
    }
    if (m_show_tx = xml.getInt("txce", 0) == 1?true:false) {
      m_label_line_offset += m_label_font_size;
    }
    m_counter_unit = xml.getString("cunit", "").trim();
  }
  
  void initElement() throws Exception {
    m_old_label_text = m_label_text; 
  }
  
  Point getElementTopMiddlePosition() { return m_top_middle_pos; }  
  
  void draw_node(int time) {
    m_label_text = m_old_label_text;
    if (m_show_rx) {      
      m_label_text += "\nrx: "+ m_rx_counter + " " + m_counter_unit;
    }
    if (m_show_tx) {      
      m_label_text += "\ntx: "+ m_tx_counter + " " + m_counter_unit;
    }  
  }
  
  boolean parentsVisible(int time) { return true; }
  
  public void resetRXTXCounter() {
    m_rx_counter = m_tx_counter = 0;
  }
  public int incRXCounter(int size) { return m_rx_counter+=size; }
  public int incTXCounter(int size) { return m_tx_counter+=size; }
  public int getRXCounter() { return m_rx_counter; }
  public int getTXCounter() { return m_tx_counter; }
}
