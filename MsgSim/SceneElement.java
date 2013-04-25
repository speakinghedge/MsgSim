import processing.core.*;
import processing.data.XML;

abstract class SceneElement extends PAppletChild {
  
  String m_id;              //!< unique id string for each element, auto-generated if not given
  static int m_id_counter = 0;
  
  Point m_pos;              //!< position of the element; default: 0,0,0
  Point m_top_middle_pos;   //!< point of the top-middle of the element 
  boolean m_do_deep_check;  //!< perform deep check during rendering for this element; default: true
  
  int m_start_time;         //!< simulation time in ms on that the element becomes visible; default: -1 - element is always visible
  int m_end_time;           //!< simulation time in ms on that the element disapears
  int m_time_visible;       //!< ms the element is visible (-1 if static)
  String m_on_on_start_of_id;    //!< become visible on start of element with given id
  String m_on_on_end_of_id;      //!< become visible on end of element with given id
  String m_off_on_start_of_id;   //!< hide on start of element with given id
  String m_off_on_end_of_id;     //!< hide on end of element with given id
  
  /* TODO: not used yet
  int m_fade_in_time;       //!< time the alpha channel is increased from 0 to 255, default: 0 (element simply appears at start time)
  int m_fade_out_time;      //!< time the alpha channel is decreased from 255 to 0, default: 0 (element simply disappears at end time)
  */ 
  
  int m_color;              //!< color of the element, default 0xffff0000 (red)
  
  /* labels are printet on top of the element */
  String m_label_text;           //!< label text, default empty string
  int m_label_line_offset;       //!< for multiline label contains number of pixels of additional offset 
  int  m_label_font_size;        //!< size of the label text, default 12 px
  int  m_label_color;            //!< color of the label text, default 0xffff0000 (red)
  int  m_label_position;         //!< position of the label text, -1: left, default 0: center, 1: right
  boolean m_label_do_deep_check; //!< do deep check while rendering elements label, default true 
  boolean m_label_rotate_y;      //!< keep the direction of the label for y axis 
  final int LABEL_ELEMENT_DIST = 10; //!< distance of the label to the object
  
  IdResolver m_resolver;          //!< service to resolve scene element ids to objects and values
  boolean m_dependencies_calculated = false; //!< flag for handling 2nd stage init - calculate time dependencies  

  final private int RECURSIVE_LIMIT = 100; //!< warning limit for recursive calls of calculateDependencies 
  
  // element specific methods
  abstract void parse_element_attributes(XML xml) throws Exception;
  abstract void drawSpecificPart(int sim_time);
  abstract int getElementWidth();
  abstract Point getElementTopMiddlePosition();
  abstract void initElement() throws Exception;
  abstract boolean parentsVisible(int time);    //!< if not all parents are visible -> return false -> don't draw element (used for paths, messages) 
  
  public SceneElement(PApplet parent, XML xml_desc, IdResolver resolver) throws Exception{
    
    super(parent);
    if (null == parent || null == xml_desc || null == resolver) {
      throw new Exception("parent, resolver or xml description is null.");
    }
    m_resolver = resolver;
    parse_basic_attributes(xml_desc);
    parse_element_attributes(xml_desc);
  }
  
  public String getId() { return m_id; }
  
  private void parse_basic_attributes(XML xml) throws Exception{
    
    m_id = xml.getString("id", "id_" + (m_id_counter++)).toLowerCase();
    
    try {
      m_pos = new Point(xml.getString("pos", "0,0,0"));
    }
    catch (Exception e) {
      throw(new Exception("Wrong format for attribute pos:" + e.getMessage()));
    }
    
    m_do_deep_check = xml.getInt("ddc", 1) == 1 ? true : false;
    
    m_start_time = xml.getInt("tstart", 0);
    m_end_time = xml.getInt("tend", 0);
    
    m_on_on_start_of_id = xml.getString("on_on_start_of", "").trim();
    m_on_on_end_of_id = xml.getString("on_on_end_of", "").trim();    
    m_off_on_start_of_id = xml.getString("off_on_start_of", "").trim();
    m_off_on_end_of_id = xml.getString("off_on_end_of", "").trim();
    
    /* TODO: not used yet
    m_fade_in_time = xml.getInt("f_in", 0);
    m_fade_out_time = xml.getInt("f_out", 0);
    */
    
    try {
      m_color = PApplet.unhex(xml.getString("color", "ffff0000"));
    }
    catch(Exception e) {
      throw new Exception("Wrong format for attribute color");
    }
    
    m_label_text = xml.getString("ltext", "").trim().replace("\\n", "\n");
    
    try {
      m_label_color = PApplet.unhex(xml.getString("lcolor", "ffff0000"));
    }
    catch(Exception e) {      
      throw new Exception("Wrong format for attribute lcolor");
    }
    
    m_label_font_size = xml.getInt("lfsize", 12);   
    m_label_line_offset = (m_label_text.split("\n").length - 1) * m_label_font_size;
    
    m_label_position = xml.getInt("lpos", 0);   
    m_label_do_deep_check = xml.getInt("lddc", 1)==1?true:false;
    
    m_label_rotate_y  = xml.getInt("lrot", 0)==1?true:false;
    
  }
  
  public boolean isStaticElement() { return m_start_time == 0 && m_end_time == 0; }
  public int getStartTime() { return m_start_time; }
  public int getEndTime() { return m_end_time; }
  public int getActiveTime(int time) {
   
   if (m_time_visible == -1 || isActive(time)) {
     return time - m_start_time;
   } else {
    return 0;
   } 
  }
  public boolean deepCheckEnabled() { return m_do_deep_check; }
  public Point getPos() { return m_pos; }
  public boolean isActive(int sim_time) {
   
   return isStaticElement() || 
          (m_end_time == 0 && sim_time >= m_start_time) ||       /* start at t_s and stay forever */
          (sim_time >= m_start_time && sim_time <= m_end_time);  /* start at t_s and end at t_e */ 
  }
  
  public boolean hasLabel() { return m_label_text.length() != 0;}
  public String getLabelText() { return m_label_text; }
  public int getLabelFontSize() { return m_label_font_size; }
  public int getLabelColor() { return m_label_color; }
  public int getLabelPos() { return m_label_position; }
  
  /**
   * 2nd stage init function - resolves scene dependencies
   */
  static int m_calculate_dependencies_entered = 0;               
  public void calculateDependencies() throws Exception {
    
    if (m_dependencies_calculated) {
      return;
    }
    
    m_dependencies_calculated = true;
    
    m_calculate_dependencies_entered++;
    if(m_calculate_dependencies_entered > RECURSIVE_LIMIT) {
      throw new Exception("I think there is some kind of circular dependency in the scene description, cause calculateDependencies was called >100 times recursive. check and if you think everything is right - change the RECURSIVE_LIMIT in class SceneElement.");      
    }
    
    int end_offset = 0;
        
    /* relative start time */
    if (m_on_on_start_of_id.length() > 0 && m_on_on_end_of_id.length() > 0) {
      throw new Exception("on_on_start_of and on_on_end_of given for element "+getId()+" but only one trigger possible");
    }
    
    if (m_on_on_start_of_id.length() > 0) {            
      m_start_time += m_resolver.getStartTimeOf(m_on_on_start_of_id);      
      end_offset = m_start_time;
    }
    if (m_on_on_end_of_id.length() > 0) {
      m_start_time += m_resolver.getEndTimeOf(m_on_on_end_of_id);
      end_offset = m_start_time;
    }
    
    if (m_start_time < 0) {
      throw new Exception("absolute start time for element "+getId()+" < 0.");
    }
    
    /* relative end time */
    if (m_off_on_start_of_id.length() > 0 && m_off_on_end_of_id.length() > 0) {
      throw new Exception("off_on_start_of and off_on_end_of given for element "+getId()+" but only one trigger possible");
    }
    
    if (m_off_on_start_of_id.length() > 0) {
      m_end_time += m_resolver.getStartTimeOf(m_off_on_start_of_id);
    } else if (m_off_on_end_of_id.length() > 0) {
      m_end_time += m_resolver.getEndTimeOf(m_off_on_end_of_id);
    } else if (m_end_time > 0 ) { /* end time specified - add offset from moved start time */
      m_end_time += end_offset;
    } 
    
    if (m_end_time < 0) {
      throw new Exception("absolute end time for element "+getId()+" < 0.");
    }
    
    if (m_end_time != 0 && m_start_time > m_end_time ) {
      throw new Exception("end time smaller then start time for message " + getId());
    }
  
    if (m_end_time == 0 && m_start_time == 0) { /* static element */
      m_time_visible = -1;
    } else if (m_end_time == 0 && m_start_time > 0) { /* element begins in t and stays forever */
      m_time_visible = Integer.MAX_VALUE;    
    } else {
      m_time_visible = m_end_time - m_start_time;
    }
    
    initElement();
    m_calculate_dependencies_entered--;
  }
  
  /**
   * by exporting this flag we could create a recursive init 
   * if we hit an element with false == m_dependencies_calculated 
   * -> just call calculate_dependencies
   * attention: pure circular dependencies won't work! 
   */
  public boolean dependenciesCalculated() {
    return m_dependencies_calculated;    
  }
  
  public void draw(int time, float rot_y) throws Exception {
    
    if (!m_dependencies_calculated) {
      calculateDependencies();      
    } 

    if ( !isActive(time) || !parentsVisible(time)) {
      return;
    }
    
    p.hint(m_do_deep_check == true ? PApplet.ENABLE_DEPTH_TEST : PApplet.DISABLE_DEPTH_TEST);
    drawSpecificPart(time);
    
    if (m_label_text.length() != 0) {
     drawLabel(rot_y);
   }
  }
    
  public void drawLabel(float rot_y) {
    
   Point pos = getElementTopMiddlePosition();
   
   p.pushMatrix();
   
   p.translate(pos.getX(), pos.getY(), pos.getZ());
   
   if (m_label_rotate_y) {
     p.rotate(-rot_y, 0, 1, 0);
   }
   
   p.textSize(m_label_font_size);
   p.fill(m_label_color);
   p.hint(m_label_do_deep_check == true ? PApplet.ENABLE_DEPTH_TEST : PApplet.DISABLE_DEPTH_TEST);
   
   p.textLeading(m_label_font_size);
   
   switch(m_label_position) {
     case 0: //center
       p.textAlign(PApplet.CENTER);
       p.text(m_label_text, 
                      (float)0, 
                      (float)- LABEL_ELEMENT_DIST - m_label_line_offset, 
                      (float)0);
       break;
     case 1: //right
       p.textAlign(PApplet.RIGHT);
       p.text(m_label_text, 
                      (float)(getElementWidth()/2) , 
                      (float)- LABEL_ELEMENT_DIST - m_label_line_offset, 
                      (float)0);
       break;
     case -1: //left
     default:
       p.textAlign(PApplet.LEFT);
       p.text(m_label_text, 
                      (float) - (getElementWidth()/2) , 
                      (float) - LABEL_ELEMENT_DIST - m_label_line_offset, 
                      0);                 
   }//__m_label_position
   
   p.popMatrix();
  }
  
};
