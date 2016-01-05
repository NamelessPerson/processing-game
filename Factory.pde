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