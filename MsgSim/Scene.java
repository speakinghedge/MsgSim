import processing.core.*;
import processing.data.XML;
import java.util.*;

class Scene extends PAppletChild {
  
  String m_scene_name = "unnamed";
  int m_scene_width = 768;
  int m_scene_height = 1024;
  int m_scene_bcolor = 0;   
  boolean m_do_autocenter = false;
  Point m_autocenter_offset = new Point(0,0,0);
  
  int m_active_messages;           //!< active messages within the last run of draw 
  int m_remaining_messages;        //!< number of messages not activated yet
  int m_active_elements = 1;       //!< elemenst active or become active in future 
    
  List<SceneElement> m_scene_element_list = new ArrayList<SceneElement>(); //!< holds elements of the scene
  
  IdResolver m_resolver;  //!< resolver for scene specific element configuration
 
  /********************************************************************************************************************/
   
  public Scene(PApplet parent, String scene_file) throws Exception{
    
    super(parent);
    
    m_resolver = new IdResolver(this);
    
    XML xml = p.loadXML(scene_file);
    
    try {
      parse_scene(xml.getChild("setup"));
      parse_animation(xml.getChild("animation"));
      create_associations_and_init();
    }
    catch(Exception e) {
      throw new Exception("Failed to create scene. Reason:"+e.getMessage());
    }    
  }
  
  /********************************************************************************************************************/
  
  private void parse_scene(XML xml) throws Exception {
    
    if (null == xml) {
      throw new Exception("setup element in config missing");
    }
    
    m_scene_name = xml.getString("name", "unknown");
    m_scene_width = xml.getInt("width", 1024);
    m_scene_height = xml.getInt("height", 768);
    m_scene_bcolor = PApplet.unhex(xml.getString("bcolor", "ff000000"));

    m_do_autocenter  = xml.getInt("auto_center", 0)== 0?false:true;
    
    PApplet.println("read config for animation " + m_scene_name + " ("+m_scene_width+"x"+m_scene_height+").");
    
    XML[] scene_elements = xml.getChildren();
    
    for (XML elem : scene_elements) {
      
      String elem_name = elem.getName();
      
      if (elem_name.equals("img_layer")) {
        checkElementAndAdd(new ImageLayer(p, elem, m_resolver));
        
      } else if (elem_name.equals("txt_layer")) {
        checkElementAndAdd(new TextLayer(p, elem, m_resolver));
        
      } else if (elem_name.equals("knot")) {
        checkElementAndAdd(new Knot(p, elem, m_resolver));
        
      }  else if (elem_name.equals("path")) {
        checkElementAndAdd(new Path(p, elem, m_resolver));
      }
    }//__loop_scene_elements 
          
  }
  
  public int getWidth() { return m_scene_width; }
  public int getHeight() { return m_scene_height; }
  public int getBackgroundColor() { return m_scene_bcolor; }
  public String getName() { return m_scene_name; }
  
  /********************************************************************************************************************/
  
  private void parse_animation(XML xml) throws Exception {
    
    if (null == xml) {
      throw new Exception("animation element in config missing");      
    }
    
    XML[] anim_elements = xml.getChildren();
    
    for (XML elem : anim_elements) {
      
      String elem_name = elem.getName();
      
      if (elem_name.equals("msg")) {        
        checkElementAndAdd(new Message(p, elem, m_resolver));
        
      } else if (elem_name.equals("cam")) {
        //new CamMovement(p, elem);
        
      }
    } //__loop_animation_elements
  }
  
  /********************************************************************************************************************/
  
  /**
   * call after reading all elements / paths of the scene to create associations
   */
  private void create_associations_and_init() throws Exception {
   
   m_remaining_messages = 0;
   m_active_messages = 0; 
   try {     
     for (SceneElement elem : m_scene_element_list) {       
       //elem.initElement();
       if (elem.getClass().getName().equals("Message")) {
         m_remaining_messages++;
       }
     }
   } 
   catch (Exception e) {
     throw new Exception("create assoc and init failes cause of: " + e.getMessage());
   }
      
   if (m_do_autocenter) {
     auto_center_scene();
   }
  }
  
  /**
   * reinit
   */
  public void init() {
    try {
      create_associations_and_init();
    }
    catch (Exception e) {
      PApplet.println("error during init scene: " + e.getMessage());
    }
  }
  
  /********************************************************************************************************************/
    
  public int getActiveMessages() {
    return m_active_messages;
  }
  
  public int getActiveElements() {
    return m_active_elements;
  }
  
  public int getRemainingMessages() {
    return m_remaining_messages;
  }
    
  SceneElement getElementById(String id) {
    
    for (SceneElement elem : m_scene_element_list) {
      if (elem.getId().equals(id)) {
        return elem;
      }
    }
    return null;
  }
  
  /**
   * add a unique element to scene element list
   */  
  void checkElementAndAdd(SceneElement element) {
    
    if (isUniqueElementId(element.getId())) {
      m_scene_element_list.add( element );  
    } else {
      p.println("element id " + element.getId() + " not unique. element of type " + element.getClass().getName()+ " not created.");
    }
  }
    
  /**
   * @brief: every element needs a unique id - check all lists for given id
   * @param: id - to check
   * @return: true if unique id (id not known yet)
   */
  private boolean isUniqueElementId(String id) {
    for (SceneElement elem: m_scene_element_list) {
      if (elem.getId().equals(id)) {
        return false;
      }
    }
    
    return true;
  } 

  /**
   * @brief: draws the current scene based on given timebase
   * @param: time - simulation time in milliseconds
   * @rot: rotation angle of the y axis
   * @return: true if a message was created or destroyed
   */  
  boolean draw(int time, float rot_y) throws Exception {
    
    boolean msg_created = false,  msg_destroyed = false;
    m_active_messages = 0;
    m_remaining_messages = 0;
    m_active_elements = 0;
    
    p.translate(-m_autocenter_offset.getX(), -m_autocenter_offset.getY(),0);
    p.hint(PApplet.ENABLE_DEPTH_TEST);
        
    for (SceneElement element : m_scene_element_list) {
      
      try {
        element.draw(time, rot_y);
      }
      catch (Exception e) {
        throw new Exception(e);
      }
      
      if ( !element.isStaticElement() && (element.isActive(time) || time < element.m_start_time)) {
        m_active_elements++;
      }
      
      /* state handling for messages, states */      
      if (element.getClass().getName().equals("Message")) {
        
        if (element.isActive(time)) {
           m_active_messages++;
           
           if (! ((Message)element).getWasActiveFlag()) { // message activated
             ((Message)element).setWasActiveFlag(true);
             m_resolver.incTX(((Message)element).getFrom(),((Message)element).getSize());
             msg_created = true;
           }                      
        } else {
           
           if (time < element.m_start_time) { // becomes active in the future
             m_remaining_messages++;
             m_active_elements++;
           } else if (((Message)element).getWasActiveFlag()) { //was active but is inactive now
             m_resolver.incRX(((Message)element).getTo(),((Message)element).getSize());
             ((Message)element).setWasActiveFlag(false);
             msg_destroyed = true;
           }
           
        } //__element inactive
      }// __element is Message      
    } // __loop_scene_elements
    
    return msg_created || msg_destroyed;      
  }

  /**
   * calculate offset for x/y to center scene based on visible elements
   */  
  private void auto_center_scene() {
    
    Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE); 
    Point max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    
    for (SceneElement elem:m_scene_element_list) {
      min.setX(elem.m_pos.getX() < min.getX()?elem.m_pos.getX():min.getX());
      min.setY(elem.m_pos.getY() < min.getY()?elem.m_pos.getY():min.getY());
      max.setX(elem.m_pos.getX() > max.getX()?elem.m_pos.getX():max.getX());
      max.setY(elem.m_pos.getY() > max.getY()?elem.m_pos.getY():max.getY());
    }
    //PApplet.println("min_X:"+min.getX() + " max_x:" +  max.getX());
    
    if (max.getX() > 0 && min.getX() > 0) {
      m_autocenter_offset.setX( (max.getX() - min.getX()) / 2);
    } else if (max.getX() > 0 && min.getX() < 0) {
      m_autocenter_offset.setX( (max.getX() + min.getX()) / 2);
    } else {
      m_autocenter_offset.setX( (max.getX() - min.getX()) / 2);
    }
   
    if (max.getY() > 0 && min.getY() > 0) {
      m_autocenter_offset.setY( (max.getY() - min.getY()) / 2);
    } else if (max.getY() > 0 && min.getY() < 0) {
      m_autocenter_offset.setY( (max.getY() + min.getY()) / 2);
    } else {
      m_autocenter_offset.setY( (max.getX() - min.getY()) / 2);
    }       
  }
}
