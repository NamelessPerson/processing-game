/*
================================
	Main File

	Acts as the scene manager in this game;
================================
*/

import processing.serial.*;
import shiffman.box2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

import java.util.*;
import java.lang.reflect.Method;

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

final static float GRAVITY = .2;


void setup(){
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

void draw(){
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

void keyPressed(){
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
	if(key == 't' && state == GameState.PLAYING){
		Player = new PlayerManager();
		factory.generateLevel("json/level_1.json");
	}
}

void keyReleased(){
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