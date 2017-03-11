import java.io.Serializable;
import java.util.List;

public class Pull implements Serializable{

	private String repRem;
	List<Pair<String,Long>> files;
	private static final long serialVersionUID = -857533466048543308L;

	public Pull(String repRem) {
		this.repRem = repRem;
	}

	public List<Pair<String, Long>> getFiles() {
		return files;
	}
	
	public void addFiles(List<Pair<String,Long>> files){
		this.files = files;
	}
	
	public String getRep(){
		return repRem;
	}

}
