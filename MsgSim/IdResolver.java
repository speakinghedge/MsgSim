/**
 * class that resolves element ids to positions/times/etc for specific class types
 * the only reason using this class is to not expose internals od Scene to
 * other classes and by this to keep the amount of needed code small
 */
 import processing.core.*;

class IdResolver {
 
  Scene m_scene;
 
  public IdResolver(Scene scene) throws Exception{
    
    if (null == scene) {
      throw new Exception("given scene point to null.");
    }
    m_scene = scene;
  }
  
  private SceneElement getElement(String id) throws Exception {
    try {
      return getElement(id, "");
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  private SceneElement getElement(String id, String type) throws Exception {
    
    for (SceneElement elem : m_scene.m_scene_element_list) {
      if (elem.m_id.equals(id) && (type.length()==0 || elem.getClass().getName().equals(type))) {
        if (!elem.dependenciesCalculated()) {
          try {
            elem.calculateDependencies();
          }
          catch (Exception e) {
            throw new Exception(e); 
          }
        }// __calc_deps_if_needed
        return elem;
      }// __check_id
    }// __loop_elements
    throw new Exception("no element with id "+id+" found.");
  }
  
  int incTX(String id, int size) throws Exception {
    
    try {
      return ((Knot)getElement(id, "Knot")).incTXCounter(size);
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  int incRX(String id, int size) throws Exception {
    
    try {
      return ((Knot)getElement(id, "Knot")).incRXCounter(size);
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  Knot getKnot(String id)  throws Exception {
    
    try {
      return (Knot)getElement(id, "Knot");
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  Point getPosOfKnot(String id) throws Exception{
        
    try {
      return ((Knot)getElement(id, "Knot")).m_pos;
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  int getEndTimeOf(String id) throws Exception {
    
    try {
      return getElement(id).m_end_time;
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  int getStartTimeOf(String id) throws Exception {
    
    try {
      return getElement(id).m_start_time;
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  int getStartTimeOfMessage(String id) throws Exception {
    
    try {
      return getElement(id, "Message").m_start_time;
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  int getEndTimeOfMessage(String id) throws Exception {
    
    try {
      return getElement(id, "Message").m_end_time;
    }
    catch(Exception e) {
      throw new Exception(e);
    }
  }
  
  Path getPath(String from_id,String to_id) throws Exception {
    
    for (SceneElement elem : m_scene.m_scene_element_list) {
      if (elem.getClass().getName().equals("Path")) {
        if ( ((Path)elem).goesFromTo(from_id, to_id) || ((Path)elem).goesFromTo(to_id, from_id)) {
          if (!elem.dependenciesCalculated()) {
            try {
           elem.calculateDependencies();
          }
          catch (Exception e) {
            throw new Exception(e); 
          }
          }
          return (Path)elem;
        }
      } 
    }
    throw new Exception("no matching path from " + from_id + " to " + to_id + " found."); 
  }
 
 
} 
