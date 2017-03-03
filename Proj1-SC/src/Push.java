import java.io.File;
import java.io.Serializable;

public class Push implements Serializable{

	private String path;
	private boolean dir;
	private File[] files;
	
	private static final long serialVersionUID = 2355826059562172987L;

	//0 directorio 1 ficheiro 
	public Push(String args, boolean dir, File[] files) {
		this.path = args;
		this.dir = dir;
		this.files = files;
	}

	public String getPath(){
		return path;
	}
	
	public File[] getFiles(){
		return files;
	}
	
	public boolean isDir(){
		return dir;
	}
	
}
