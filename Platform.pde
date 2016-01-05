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
		if( abs(x - this.node.x) < (width/2.0)+(this.node.width/2.0) && abs(y - this.node.y) < (height/2.0)+(this.node.height/2.0))
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