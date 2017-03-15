import java.io.Serializable;

public class Result implements Serializable{
	private boolean ok;
	private String s;
	private boolean created;
	
	private static final long serialVersionUID = 4722700293377523852L;
	
	public Result (String s, boolean ok){
		this.s = s;
		this.ok = ok;
		this.created = false;
	}

	public void setOk(){
		this.ok = true;
	}
	public void setCreated(){
		this.created = true;
	}
	public void setS(String s){
		this.s = this.s + s; 
	}
	

	public boolean allGood() {
		return ok;
	}
	public boolean created(){
		return created;
	}
	@Override
	public String toString(){
		return s;
	}
	
}
