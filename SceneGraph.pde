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