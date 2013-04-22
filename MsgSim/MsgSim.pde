import processing.opengl.*;
import javax.media.opengl.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;

String m_default_scene_file = "sample.xml";  //!<default scene to load on startup, set to "" to disable
String m_current_scene_file = ""; //<! file that represents the current scene

Scene m_scene = null;    //!< holds the scene (description and animation)

private int m_simulation_start_time = -1;    //!< time (in milliseconds) the simulation was started
int m_simulation_time = -1;          //!< time base of the simulation in milliseconds
boolean m_is_paused = false;         //!< animation is paused

boolean m_single_step_mode = false;  //!< pause animation after each msg create/destroy event
boolean m_show_help = false;         //!< hmm...
boolean m_show_scene_center = false; //!< show x/y/z axis 

Point m_mouse_pos = new Point(-1,-1,-1);
Point m_view_offset = new Point(0,0,0); //!< offset fro zoom, move left/right & up/down
float m_rot_x = 0, m_rot_y = 0;         //!< rotation around x/y-axis

void setup() {
    
  /* won't work under X11 */
  addMouseWheelListener(new MouseWheelListener() { 
    public void mouseWheelMoved(MouseWheelEvent mwe) { 
      mouseWheel(mwe.getWheelRotation());
  }});
 
  try {
    if (m_default_scene_file.length() > 0) {
      m_current_scene_file = m_default_scene_file;
      load_scene(m_default_scene_file); 
    }
  }
  catch(Exception e) {
    println("failed to load scene cause:"+ e.getMessage());    
    exit();
  }
  
  if (null != m_scene) {
    size(m_scene.getWidth(), m_scene.getHeight(), P3D);
  }
  frameRate(30);
}

//***load new scene************************************************
void load_scene(File scene_description_file) {
  try {
    load_scene(scene_description_file.getAbsolutePath());
  }
  catch (Exception e) {
    println("load scene failed.\n"+e.getMessage());
  }
  m_current_scene_file = scene_description_file.getAbsolutePath();
}
void load_scene(String scene_description_file_name) throws Exception{
  
  noLoop();
  try {Thread.sleep((long)500);} //make sure draw() is finished... 
  catch (InterruptedException ex) {println("Error!");}
  
  try {
    m_scene = new Scene(this, scene_description_file_name);
  } 
  catch (Exception e) {
    throw new Exception ("Failed to load scene cause of: " + e.getMessage());
  }
  
  m_is_paused = false;
  reset_view();
  init_simulation_time();
  
  loop();
}

void reset_view() {
  m_view_offset.setPos(0, 0, 0);
  m_rot_x = 0; m_rot_y = 0;
}

/*** time related ***************************************************/

void init_simulation_time() {
  m_simulation_start_time = millis();
  m_simulation_time = 0;
}

int getSimulationTime() {
    
  if ( m_is_paused || 
      (m_scene.getActiveMessages() == 0 && m_scene.getRemainingMessages() == 0 && m_scene.getActiveElements() == 0) /* stop after last message */
      ) {
    return m_simulation_time;
  } else {
    return (m_simulation_time = (millis() - m_simulation_start_time));
  }
}

/*** scene state related *********************************************/
void handlePause() {
  if (m_is_paused) {
    m_simulation_start_time = millis() - m_simulation_time;
  }
  m_is_paused = !m_is_paused;   
}

//***mouse&keyb-interaction****************************************
void mouseWheel(int delta) {
  m_view_offset.addZ(delta*10);  
}

int old_millis;
void keyPressed() {
  
  switch(key) {
    case 'l':
    case 'L':
      try {
        if (m_current_scene_file.length() > 0) {
          load_scene(m_current_scene_file);
        }
      }
      catch(Exception e) {
        println("failed to load scene cause:"+ e.getMessage());    
        exit();
      }
      break;
    case '+': //zoom out
      m_view_offset.addZ(10);
      break;
    case '-': //zoom in
      m_view_offset.addZ(-10);
      break; 
    case '0': //reset view point
      reset_view();
      break;
    case 'c':  //show scene center
    case 'C':
      m_show_scene_center = !m_show_scene_center;
      break;     
    case 's': //start/restart
    case 'S':
      init_simulation_time(); 
      m_is_paused = false;
      break;      
    case 'p': //pause / continue 
    case 'P':
      handlePause(); 
      break;
    case 'h': //show/hide help
    case 'H':
      m_show_help = !m_show_help;
      break;
    case 'q': //quit
    case 'Q':
      exit();
    case 'o': //load new scene
    case 'O':
      selectInput("select scene file to load", "load_scene");
      if (null != m_scene) {
        size(m_scene.getWidth(), m_scene.getHeight(), P3D);
      }
      break;
    case 't': //run till next msg event
    case 'T':
      m_single_step_mode = !m_single_step_mode;
      break;
  }
  
  if (keyCode == UP) {
    m_view_offset.addY(10);
  } else if (keyCode == DOWN) {
    m_view_offset.addY(-10);
  } else if (keyCode == LEFT) {
    m_view_offset.addX(-10);
  } else if (keyCode == RIGHT) {
    m_view_offset.addX(10);
  }
  draw();
}

void draw() {
    
  background(m_scene.getBackgroundColor());
  smooth(8);
  
  int t_base = getSimulationTime();
  
  if (null != m_scene) {
    
    show_status_text(t_base);
    
    pushMatrix();
    translate(width/2 + m_view_offset.getX(), height/2 + m_view_offset.getY(),  + m_view_offset.getZ());
    
    if (mousePressed) {
      
      if (mouseButton == LEFT) { //rotate x/y mouse move up/down left/right
        if (m_mouse_pos.getX() == -1) {
          m_mouse_pos.setPos(mouseX, mouseY, 0);
        } else {
          Point d = m_mouse_pos.calcDeltaAndSet(mouseX, mouseY, 0);
          m_rot_y -= d.getX() * (PI/180);
          m_rot_x += d.getY() * (PI/180);
        }    
      } else if (mouseButton == RIGHT) { //zoom over mouse-up-down move
        m_view_offset.addZ(m_mouse_pos.calcDeltaAndSet(0, mouseY, 0).getY()*10);
      }
    } else {
      m_mouse_pos.setPos(mouseX, mouseY, 0);
    }
    rotateX(m_rot_x);
    rotateY(m_rot_y);
    
    if (m_show_scene_center) {
      draw_scene_center();
    }
    
    noStroke();  
    lights();
    
    try {
      if (m_scene.draw( t_base, m_rot_y) && m_single_step_mode && !m_is_paused) {
        handlePause();       
      } 
    }
    catch(Exception e) {
      println("Failed to draw scene cause of: " + e.getMessage());
    }
  
    popMatrix();
    
  }// __has_valid_scene
}

void draw_scene_center() {
  
  textSize(24);
  strokeWeight(2);
  stroke(0xff00ff00);
  line (0, 0, -width, 0, 0, width);
  fill(0xff00ff00);
  text("-Z", 0, 0, -(width/2));
  text("Z", 0, 0, (width/2));  
  stroke(0xff0000ff);
  line (0, -height, 0, 0, height, 0);
  fill(0xff0000ff);
  text("-Y", 0, -(height/2), 0);
  text("Y", 0, (height/2), 0);  
  stroke(0xffff0000);
  line (-width, 0, 0, width, 0, 0);
  fill(0xffff0000);
  text("-X", -(width/2), 0, 0);
  text("X", (width/2), 0, 0);
}

void show_status_text(int time) {
  
  int remaining = m_scene.getRemainingMessages();
  
  fill(#009900);
  textSize(16);
  textAlign(LEFT);
  String single_step = m_single_step_mode?" [flow event step]":"";
  text("animation: " + m_scene.getName() + (m_is_paused?"\nstate: *** paused ***":remaining==0?"\nstate: finished" :"\nstate: running") + single_step , 20, 20);
  if (m_show_help) {
    text("left mouse button & move -> rotate X/Y\nmousewheel, right mouse button & move up/down or +/- -> zoom\ncursor up/down/left/right -> move\n0 -> center scene\ns -> start/restart animation\np -> pause\no -> load scene\nt -> toggle flow event step mode\nl - reload current scene file\nc - show x/y/z-axis\nq -> quit\nh -> close help", 20, height-240);
  } else {
    text("h -> show help", 20, height-20);
  }
  
  textAlign(RIGHT);
  
  text("t: " + time + " ms" +  
        "\nactive flows: " + m_scene.getActiveMessages() + 
        "\nremaining flows: " + remaining, width-20, 20);
}

