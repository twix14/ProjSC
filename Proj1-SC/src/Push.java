import java.io.Serializable;
import java.util.List;

public class Push implements Serializable{

	private String path;
	private boolean dir;
	private List<Pair<String, Long>> files;
	
	private static final long serialVersionUID = 2355826059562172987L;

	//0 directorio 1 ficheiro 
	public Push(String args, boolean dir, List<Pair<String, Long>> files) {
		this.path = args;
		this.dir = dir;
		this.files = files;
	}

	public String getPath(){
		return path;
	}
	
	public List<Pair<String, Long>> getFiles(){
		return files;
	}
	
	public boolean isDir(){
		return dir;
	}
	
}
