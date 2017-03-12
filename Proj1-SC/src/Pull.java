import java.io.Serializable;
import java.util.List;

public class Pull implements Serializable{

	private String repRem;
	List<Pair<String,Long>> files;
	private boolean isDir;
	private static final long serialVersionUID = -857533466048543308L;

	public Pull(String repRem, boolean isDir) {
		this.repRem = repRem;
		this.isDir = isDir;
	}

	public List<Pair<String, Long>> getFiles() {
		return files;
	}
	
	public boolean isDir(){
		return isDir;
	}
	public void addFiles(List<Pair<String,Long>> files){
		this.files = files;
	}
	
	public String getRep(){
		return repRem;
	}

}
