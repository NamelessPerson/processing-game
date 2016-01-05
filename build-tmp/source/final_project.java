import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import shiffman.box2d.*; 
import org.jbox2d.collision.shapes.*; 
import org.jbox2d.common.*; 
import org.jbox2d.dynamics.*; 
import java.util.*; 
import java.lang.reflect.Method; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class final_project extends PApplet {

/*
================================
	Main Arduino File

	Acts as the scene manager in this game;
================================
*/










Box2DProcessing box2d;

int displayWidth = 900;
int displayHeight = 500;

Serial myPort;
int arduino_input;

InputHandler Input;
SimpleSceneGraph SceneGraph;
SimpleSceneGraph MenuGUI;
PlayerManager Player;

private Factory factory;
private int level;
private GameState state;

final static float GRAVITY = .2f;


public void setup(){
	//Processing Setup
	//background(100, 1);
	size(displayWidth, displayHeight, P3D);
	ortho(0, displayWidth, 0, displayHeight);
	if(frame != null) 
		frame.setResizable(true);
	textSize(32);
	rectMode(CENTER);
	
	//Engine Setup
	state = GameState.PLAYING;
	factory = new Factory(this);
	Input = new InputHandler();
	Player = new PlayerManager();
	
	// Initialize box2d physics and create the world
	box2d = new Box2DProcessing(this);
	box2d.createWorld();
	// We are setting a custom gravity
	box2d.setGravity(0, -2);
	
	setupFactory();
	factory.generateLevel("json/level_1.json");

	try{
		myPort = new Serial(this, Serial.list()[0], 9600);
	}catch(Exception e){
		System.err.println("Controller Disabled");
	}

}

public void draw(){
	displayWidth = width;
	displayHeight = height;
	int dt = millis();

	if(state == GameState.STARTING && dt > 500){
		//MenuGUI = factory.menu();
		state = GameState.MAIN_MENU;
	}
	
	if(myPort != null && myPort.available() > 0) {  // If data is available,
  		arduino_input = (int)(myPort.readChar());         // read it and store it in arduino_input
  	}

  	Input.tick();
  	if(SceneGraph != null)SceneGraph.tick(dt);
  	else{
  		println("Where's my graph??");
  		System.exit(1);
  	}

  	clear();
	background(100, 1);

  	if(state == GameState.PLAYING && SceneGraph != null)SceneGraph.renderScene();
	//else if(state == GameState.MAIN_MENU && MenuGUI != null)MenuGUI.renderScene();

}

public void keyPressed(){
	if(key == '\n' && state == GameState.MAIN_MENU){
		state = GameState.PLAYING;
	}
	if(key == 'a' && state == GameState.PLAYING){
		Input.update(KEY.LEFT, true);
	}
	if(key == 'd' && state == GameState.PLAYING){
		Input.update(KEY.RIGHT, true);
	}
	if(key == ' ' && state == GameState.PLAYING){
		Input.update(KEY.JUMP, true);
	}
	if(key == 'w' && state == GameState.PLAYING){
		Input.update(KEY.UP, true);
	}
	if(key == 'r' && state == GameState.PLAYING){
		factory.generateLevel(factory.last);
	}
}

public void keyReleased(){
	if(key == 'a' && state == GameState.PLAYING){
		Input.update(KEY.LEFT, false);
	}
	if(key == 'd' && state == GameState.PLAYING){
		Input.update(KEY.RIGHT, false);
	}
	if(key == ' ' && state == GameState.PLAYING){
		Input.update(KEY.JUMP, false);
	}
	if(key == 'w' && state == GameState.PLAYING){
		Input.update(KEY.UP, false);
	}
}
public class Actor implements iDrawable{

	public SceneNode node;

	public Actor(float x, float y){
		node = new SceneNode(x,y,1,1,this);
	}

	public Actor(){
		node = new SceneNode(0,0,1,1,this);
	}

	public void draw(){

	}

}

public class Controller implements iTickable{
	public Actor actor;

	public Controller(float x, float y){
		actor = new Actor(x, y);
	}

	public Controller(){
		actor = new Actor();
	}

	public void preTick(int dt){

	}

	public void tick(int dt){

	}

	public void postTick(int dt){

	}
	public Actor getActor(){
		return actor;
	}

}
class Camera{
	float height, width;
	SceneNode focus;

	public float getX(){
		float x = 0;
		if(focus != null) x = focus.x;
		if(x - (width/2) < 0) x = width/2;
		if(x + (width/2) > SceneGraph.width) x = SceneGraph.width-(width/2);
		return x;
	}

	public float getY(){
		float y = 0;
		if(focus != null) y = focus.y;
		if(y - (height/2) < 0) y = height/2;
		if(y + (height/2) > SceneGraph.height) y = SceneGraph.height-(height/2);
		//println("y: "+y);
		return y;
	}
}
public class Door extends Actor implements iCollidable {
	String location;
	int key;
	public Door(float x, float y, float width, float height, String location, int key){
		node = new SceneNode(x,y,width,height,this);
		this.location = location;
		this.key = key;
	}

	public boolean collides(float x, float y, float width, float height){
		if( abs(x - this.node.x) < (width/2.0f)+(this.node.width/2.0f) && abs(y - this.node.y) < (height/2.0f)+(this.node.height/2.0f))
			return true;
		return false;
	}

	public void draw(){
		pushMatrix();
		translate(node.x, node.y, 5);
		fill(56, 33, 17);
		stroke(82);
		rectMode(CENTER);
		rect(0, 0, node.width, node.height);
		popMatrix();
	}

}
public class Factory {
	protected HashMap<String, Method> classMap;
	private Object caller;
	String last;

	public Factory(Object ref){
		caller = ref;
	}

	public void setClassMap(HashMap<String, Method> map){
		this.classMap = (HashMap<String, Method>)map.clone();
		
	}

	public void generateLevel(String json_file){
		last = json_file;
		JSONObject level;

		try {
			level = loadJSONObject(json_file);
			SceneGraph = new SimpleSceneGraph(level.getInt("level_width", 1), level.getInt("level_height", 1));
			SceneGraph.setCamera(level.getInt("camera_width", 1), level.getInt("camera_height", 1));

			Set<String> set = level.getJSONObject("inhabitants").keys();
			Iterator<String> iter = set.iterator();
			while(iter.hasNext()){
				String s = iter.next();

				if(classMap.get(s) != null){
					JSONArray arr = level.getJSONObject("inhabitants").getJSONArray(s);
					for(int i = 0; i < arr.size(); i++)
						classMap.get(s).invoke(caller, arr.getJSONObject(i));
				}
				else{
					System.out.println("Class "+s+" not registered.");
					throw new Exception();
				}
			}

		} catch (Exception e) {
			System.out.println("SHIT");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
public void setupFactory() {
	HashMap<String, Method> map = new HashMap<String, Method>();
	try{
		map.put("player", this.getClass().getDeclaredMethod("playerFactory", JSONObject.class));
		map.put("platform", this.getClass().getDeclaredMethod("platformFactory", JSONObject.class));
		map.put("lamp", this.getClass().getDeclaredMethod("lampFactory", JSONObject.class));
		map.put("door", this.getClass().getDeclaredMethod("doorFactory", JSONObject.class));
		map.put("key", this.getClass().getDeclaredMethod("keyFactory", JSONObject.class));
	}catch(Exception e){
		e.printStackTrace();
	}
	
	factory.setClassMap(map);
}

public void playerFactory(JSONObject json){
	println("Generating Player");
	float x = json.getFloat("x", 0);
	float y = json.getFloat("y", 0);
	PlayerController p = new PlayerController(x,y);
	SceneGraph.addTickable(p);
	SceneGraph.addSceneNode(p.getActor().node);
}

public void platformFactory(JSONObject json){
	println("Generating Factory");
	float x = json.getFloat("x", 0);
	float y = json.getFloat("y", 0);
	float width = json.getFloat("width", 1);
	float height = json.getFloat("height", 1);
	float depth = json.getFloat("depth", 1);	
	int r = json.getInt("r", 0);
	int g = json.getInt("g", 0);
	int b = json.getInt("b", 0);

	SceneGraph.addSceneNode(new Platform(x,y,width,height,depth,r,g,b).node);
}

public void lampFactory(JSONObject json){
	println("Generating Lamp");
	float x = json.getFloat("x", 0);
	float y = json.getFloat("y", 0);
	float width = json.getFloat("width", 1);
	float height = json.getFloat("height", 1);
	boolean swing = json.getBoolean("swing", false);
	LampController l = new LampController(x,y,width,height,swing);
	SceneGraph.addTickable(l);
	SceneGraph.addSceneNode(l.getActor().node);
}

public void doorFactory(JSONObject json){
	println("Generating Door");
	float x = json.getFloat("x", 0);
	float y = json.getFloat("y", 0);
	int key = json.getInt("key", 0);
	float width = json.getFloat("width", 1);
	float height = json.getFloat("height", 1);
	String location = json.getString("location", "");
	SceneGraph.addSceneNode(new Door(x,y,width,height,location, key).node);
}

public void keyFactory(JSONObject json){
	int key = json.getInt("num", 0);
	if(!Player.keys[key]){
		println("Generating Key");
		float x = json.getFloat("x", 0);
		float y = json.getFloat("y", 0);
		SceneGraph.addSceneNode(new Key(x,y,key).node);		
	}
	else println("Already Got key: "+ key);
}
public class InputHandler {

	HashMap<KEY, Boolean> keys;
	HashMap<KEY, ArrayList<KeyListener>> listeners;
	boolean controller[];

	public InputHandler() {
		controller = new boolean[8];
		keys = new HashMap<KEY, Boolean>();
		listeners = new HashMap<KEY, ArrayList<KeyListener>>();
	}

	public void update(KEY k, boolean pressed){
		if(keys.get(k) == null) keys.put(k, false);
		if(keys.get(k) != pressed){
			keys.put(k, pressed);
			
		} 
	}

	public void tick(){
		 Iterator it = keys.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        if((Boolean)(pair.getValue()) == true){
	        	for(KeyListener kl : listeners.get((KEY)pair.getKey()))
					kl.keyPressed((KEY)pair.getKey());
	        }
	        else{
	        	for(KeyListener kl : listeners.get((KEY)pair.getKey()))
					kl.keyReleased((KEY)pair.getKey());
	        	it.remove(); // avoids a ConcurrentModificationException
	        }
	    }
	}

	public void addListener(KeyListener kl, KEY k){
		if(listeners.get(k) == null) listeners.put(k, new ArrayList<KeyListener>());
		listeners.get(k).add(kl);
	}
	public void removeListener(KeyListener kl, KEY k){
		listeners.get(k).remove(kl);
	}
	public void update(int contollerState){
		
	}
}
class Key extends Actor implements iCollidable{
	int num;

	public Key(float x, float y, int num){
		node = new SceneNode(x,y,10,30,this);
		this.num = num;
	}

	public boolean collides(float x, float y, float width, float height){
		if( abs(x - this.node.x) < (width/2.0f)+(this.node.width/2.0f) && abs(y - this.node.y) < (height/2.0f)+(this.node.height/2.0f))
			return true;
		return false;
	}

	public void draw(){
		pushMatrix();
		translate(node.x, node.y, 5);
		fill(235, 223, 34);
		stroke(82);
		rectMode(CENTER);
		rect(0, 0, node.width, node.height);
		popMatrix();
	}

}
public class LampActor extends Actor{
	PImage lamp_sprite;
	float angle;
	float maxAngle = 10;
	float step = .01f;
	public  LampActor(float x, float y, float width, float height) {
		angle = random(-maxAngle, maxAngle);
		step = random(.01f, .02f);
		lamp_sprite = loadImage("res/spotlight.png");
		node = new SceneNode(x,y,width, height, this);
	}
	public void draw(){
		pushMatrix();
		imageMode(CORNER);
		translate(node.x-(node.width/2), node.y, 20);
		rotate(radians(angle), 0,0,1);
		image(lamp_sprite, 0, 0, node.width, node.height);
		popMatrix();
	}
	public void swing(){
		angle+= step;
		if(angle > 10 || angle < -10) step*=-1;
	}

}
public class LampController extends Controller {
	boolean swing;
	public LampController(float x, float y, float width, float height, boolean swing){
		actor = new LampActor(x,y, width, height);
		this.swing = swing;
		SceneGraph.addTickable(this);
	}
	public void tick(int dt){
		if(swing) ((LampActor)actor).swing();
	}

}
public class MenuGui implements iDrawable{
	int renderID;
	public void draw(){
		textAlign(CENTER);
      	fill(255);
      	text("Press Enter", 1, 1);
	}
}
public class Platform extends Actor implements iCollidable{
	int r,g,b;
	float draw_depth;

	public Platform(float x, float y){
		node = new SceneNode(x,y,1,1,this);
	}

	public Platform(float x, float y, float width, float height) {
		node = new SceneNode(x,y,width, height,this);
	}
	public Platform(float x, float y, float width, float height, int grey){
		node = new SceneNode(x,y,width, height,this);
		r = grey;
		g = grey;
		b = grey;
	}
	public Platform(float x, float y, float width, float height, int r, int g, int b) {
		node = new SceneNode(x,y,width, height,this);
		this.r = r;
		this.g = g;
		this.b = b;
	}
	public Platform(float x, float y, float width, float height, float draw_depth, int r, int g, int b) {
		node = new SceneNode(x,y,width, height,this);
		this.r = r;
		this.g = g;
		this.b = b;
		this.draw_depth = draw_depth;
	}

	public boolean collides(float x, float y, float width, float height){
		if( abs(x - this.node.x) < (width/2.0f)+(this.node.width/2.0f) && abs(y - this.node.y) < (height/2.0f)+(this.node.height/2.0f))
			return true;
		return false;
	}

	public void draw(){
		fill(r,g,b);
		pushMatrix();
		rectMode(CENTER);
		translate(node.x, node.y, draw_depth);
		rect(0, 0, node.width, node.height);
		popMatrix();
	}
}
public class PlayerActor extends Actor implements iCollidable{
	float direction = 1;
	
	PlayerState moveState = PlayerState.IDLE;
	PlayerState actionState = PlayerState.IDLE;
	
	int walk_index;
	PImage walk_sprite[];
	PImage jump_sprite;
	PImage push_sprite;

	float yVel = 0;
	float max_yVel = 6;
	final float moveVel = .7f;
	boolean flag = false;
	int dt;

	public PlayerActor(float x, float y) {
		node = new SceneNode(x,y, 10, 50, this);
		walk_sprite = new PImage[4];
		walk_sprite[0] = loadImage("res/base_sprite.png");
		walk_sprite[1] = loadImage("res/extend_sprite.png");
		walk_sprite[2] = loadImage("res/base_sprite.png");
		walk_sprite[3] = loadImage("res/contracted_sprite.png");

		jump_sprite = loadImage("res/jump_sprite.png");
		push_sprite = loadImage("res/push_sprite.png");
		dt = millis();
	}

	public void draw(){
		fill(255);
		noStroke();
		imageMode(CENTER);
		pushMatrix();
		translate(node.x, node.y, 0);
		scale(direction, 1, 1);
		//rect(0,0, node.width, node.height);
		if(actionState == PlayerState.JUMPING || actionState == PlayerState.FALLING) image(jump_sprite, 0, 0, node.width, node.height);
		else if(actionState == PlayerState.PUSHING) image(push_sprite, 0, 0, node.width, node.height);
		else{
			if(moveState == PlayerState.IDLE) image(walk_sprite[0], 0, 0, node.width, node.height);
			else {
				image(walk_sprite[walk_index], 0, 0, node.width, node.height);
				if(millis() - dt > 300){
					walk_index =(walk_index+1)%4;
					dt = millis();
				}
			}
		}
		popMatrix();

	}

	public void update(){
		if(moveState == PlayerState.RIGHT) node.x+=moveVel;
		if(moveState == PlayerState.LEFT) node.x-=moveVel;
		

		if(actionState == PlayerState.JUMPING || actionState == PlayerState.FALLING){
			if(yVel + GRAVITY < max_yVel)yVel+=GRAVITY;
			if(yVel > 0) actionState = PlayerState.FALLING;
			node.y+=yVel;
		}
		if(actionState == PlayerState.IDLE && yVel != 0) yVel = 0;
	}
	public PVector nextMove(){
		PVector next = new PVector(node.x, node.y);
		if(moveState == PlayerState.RIGHT) next.x+=moveVel;
		if(moveState == PlayerState.LEFT) next.x-=moveVel;

		next.y+=yVel+GRAVITY;
		
		return next;
	}
	public boolean collides(float x, float y, float width, float height){
		return false;
	}
	public void jump(){
		actionState = PlayerState.JUMPING;
		yVel-=max_yVel;
	}
}

public class PlayerController extends Controller implements KeyListener {
	PlayerActor actor;
	int lastTime;
	boolean up = false;

	public PlayerController(float x, float y) {
		actor = new PlayerActor(x, y);
		SceneGraph.setCameraFocus(actor.node);
		Input.addListener(this, KEY.LEFT);
		Input.addListener(this, KEY.RIGHT);
		Input.addListener(this, KEY.JUMP);
		Input.addListener(this, KEY.UP);
	}

	public void tick(int dt){
		((PlayerActor)actor).flag = false;
		PVector nextMove = actor.nextMove();
		for(SceneNode s : SceneGraph.getColliders(actor.node, nextMove)){
			//println("Curr: "+actor.node.x+", "+actor.node.y+", Next: "+nextMove.x + ", "+nextMove.y);
			if(s.actor instanceof Platform){
				//println(actor.node.x+", "+actor.node.width/2.0+", "+s.x+", "+s.width/2.0);
				//Horizontal
				if(s.x - (s.width/2.0f) >= actor.node.x+(actor.node.width/2.0f) || s.x+(s.width/2.0f) <= actor.node.x-(actor.node.width/2.0f)){
					if(((PlayerActor)actor).actionState == PlayerState.IDLE){
						((PlayerActor)actor).actionState = PlayerState.PUSHING;
						((PlayerActor)actor).flag = true;
					}
					//collide right
					if(nextMove.x < s.x && nextMove.x+(actor.node.width/2.0f) > s.x - (s.width/2.0f)){
						actor.node.x = s.x - (s.width/2.0f) - (actor.node.width/2.0f);
						((PlayerActor)actor).moveState = PlayerState.IDLE;
					}
					//collide left
					else if(nextMove.x > s.x && nextMove.x-(actor.node.width/2.0f) < s.x + (s.width/2.0f)){
						actor.node.x = s.x + (s.width/2.0f) + (actor.node.width/2.0f);
						((PlayerActor)actor).moveState = PlayerState.IDLE;
					}
				}
				//Vertical
				else{
					//collide bottom
					if(nextMove.y < s.y && nextMove.y+(actor.node.height/2.0f) > s.y - (s.height/2.0f)){
						actor.node.y = s.y - (s.height/2.0f) - (actor.node.height/2.0f);
						if(!((PlayerActor)actor).flag) ((PlayerActor)actor).actionState = PlayerState.IDLE;
						((PlayerActor)actor).flag = true;
					}
					//collide top
					else if(nextMove.y > s.y && nextMove.y-(actor.node.height/2.0f) < s.y + (s.height/2.0f)){
						actor.node.y = s.y + (s.height/2.0f) + (actor.node.height/2.0f);
						((PlayerActor)actor).actionState = PlayerState.FALLING;
						((PlayerActor)actor).flag = true;
					}
				}
			}
			if(s.actor instanceof Door && up){
				if(Player.keys[((Door)s.actor).key])factory.generateLevel(((Door)s.actor).location);
			}
			if(s.actor instanceof Key){
				Player.keys[((Key)s.actor).num] = true;
				SceneGraph.removeSceneNode(s);
			}
		}
		if(!((PlayerActor)actor).flag && ((PlayerActor)actor).actionState != PlayerState.JUMPING && ((PlayerActor)actor).actionState != PlayerState.PUSHING) ((PlayerActor)actor).actionState = PlayerState.FALLING;
		actor.update();
	}

	public void keyPressed(KEY k){
		//println(k);
		if(k == KEY.RIGHT){
			((PlayerActor)actor).moveState = PlayerState.RIGHT;
			((PlayerActor)actor).direction = -1;
		}
		else if(k == KEY.LEFT){
			((PlayerActor)actor).moveState = PlayerState.LEFT;
			((PlayerActor)actor).direction = 1;
		}
		else if(k == KEY.JUMP && ((PlayerActor)actor).actionState == PlayerState.IDLE) ((PlayerActor)actor).jump();
		else if(k == KEY.UP ) up = true;
	}

	public void keyReleased(KEY k){
		//println("released: "+k);
		if(k == KEY.RIGHT || k == KEY.LEFT) ((PlayerActor)actor).moveState = PlayerState.IDLE;
		else if(k == KEY.UP ) up = false;
	}
	public PlayerActor getActor(){
		return actor;
	}
}
public class PlayerManager{
	boolean keys[];

	public PlayerManager () {
		keys = new boolean[10];
		keys[0] = true;
	}

	public void draw(){
		for(int i = 1, j = 1; i < keys.length; i++){
			if(keys[i]){
				pushMatrix();
				translate(displayWidth - (j*40), 10, 50);
				fill(235, 223, 34);
				stroke(82);
				rectMode(CENTER);
				rect(0, 0, 10, 30);
				popMatrix();
				j++;
			}
		}
	}
}
public class SimpleSceneGraph {

	public ArrayList<SceneNode> nodes;
	private ArrayList<iTickable> tickables;
	public int width, height;
	private Camera cam;
	PGraphics lights;

	PShader INVERT_ALPHA;
	
	public SimpleSceneGraph(int width, int height) {
		this.width = width;
		this.height = height;
		lights = createGraphics(width, height);

		nodes = new ArrayList<SceneNode>();
		tickables = new ArrayList<iTickable>();
		cam = new Camera();
		INVERT_ALPHA = loadShader("alpha.glsl");
	}

	public void setCameraFocus(SceneNode node){
		cam.focus = node;
	}

	public void setCamera(float width, float height){
		cam.width = width;
		cam.height = height;
	}

/*================================================================*/

	public void addSceneNode(SceneNode node){
		nodes.add(node);
	}
	public void addAllSceneNodes(List<SceneNode> nodes){
		for(SceneNode n : nodes){
			nodes.add(n);
		}

	}
	public boolean removeSceneNode(SceneNode node){
		return nodes.remove(node);
	}

/*================================================================*/

	public ArrayList<SceneNode> getColliders(SceneNode node){
		ArrayList<SceneNode> rtn = new ArrayList<SceneNode>();
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i) != node && nodes.get(i).actor instanceof iCollidable && ((iCollidable)(nodes.get(i).actor)).collides(node.x, node.y, node.width, node.height))
				rtn.add(nodes.get(i));
		return rtn;
	}
	public ArrayList<SceneNode> getColliders(float x, float y, float width, float height){
		ArrayList<SceneNode> rtn = new ArrayList<SceneNode>();
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i).actor instanceof iCollidable && ((iCollidable)(nodes.get(i).actor)).collides(x,y,width,height))
				rtn.add(nodes.get(i));
		return rtn;
	}
	//
	public ArrayList<SceneNode> getColliders(SceneNode node, PVector pos){
		ArrayList<SceneNode> rtn = new ArrayList<SceneNode>();
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i) != node && nodes.get(i).actor instanceof iCollidable && ((iCollidable)(nodes.get(i).actor)).collides(pos.x, pos.y, node.width, node.height))
				rtn.add(nodes.get(i));
		return rtn;
	}
/*================================================================*/
	public void renderScene(){
		pushMatrix();
		lights = createGraphics(displayWidth, displayHeight);
		lights.beginDraw();
		//lights.background(255,255,255,255);
		lights.background(255,255,255, 0);

		scale((float)displayWidth/cam.width, (float)displayHeight/cam.height);
		translate(-(cam.getX()-(cam.width/2)), -(cam.getY()-(cam.height/2)));
		for(int i = 0; i < nodes.size(); i++)
			nodes.get(i).draw();
		fill(255, 255, 255, 255);
		//rect(0,0,displayWidth,displayHeight);
		popMatrix();
		//mask(lights);

		//blend(lights, 0,0, lights.width, lights.height, 0,0, displayWidth, displayHeight, SUBTRACT);
		//lights.filter(INVERT_ALPHA);
		image(lights, 0, 0, lights.width, lights.height);

		/*loadPixels();
		lights.loadPixels();
		for(int x = 0; x < displayWidth*displayHeight; x++){
			if(lights.pixels[x] != color(0)){
			 pixels[x] = color(0,0,0);
			}
		}
		updatePixels();*/
		Player.draw();


	}
/*================================================================*/
	public void addTickable(iTickable t) {
		tickables.add(t);
	}

	public void addController(Controller c) {
		tickables.add(c);
		addSceneNode(c.actor.node);
	}

	public void removeTickable(iTickable t) {
		tickables.remove(t);
	}
	public void tick(int dt){
		for(int i = 0; i < tickables.size(); i++)
			tickables.get(i).preTick(dt);
		for(int i = 0; i < tickables.size(); i++)
			tickables.get(i).tick(dt);
		for(int i = 0; i < tickables.size(); i++)
			tickables.get(i).postTick(dt);
	}
}
public class SceneNode{
	public float x;
	public float y;
	public float width;
	public float height;

	protected iDrawable actor;

	public SceneNode(float x, float y, float width, float height, iDrawable actor){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.actor = actor;

	}

	public void draw(){
		actor.draw();
	}
}
interface iCollidable{
	public boolean collides(float x, float y, float width, float height);
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "final_project" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
