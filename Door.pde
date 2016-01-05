public class Door extends Actor implements iCollidable {
	String location;
	int key;
	public Door(float x, float y, float width, float height, String location, int key){
		node = new SceneNode(x,y,width,height,this);
		this.location = location;
		this.key = key;
	}

	public boolean collides(float x, float y, float width, float height){
		if( abs(x - this.node.x) < (width/2.0)+(this.node.width/2.0) && abs(y - this.node.y) < (height/2.0)+(this.node.height/2.0))
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