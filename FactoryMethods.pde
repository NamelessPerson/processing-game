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