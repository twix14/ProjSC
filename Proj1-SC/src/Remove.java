import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class Remove implements Serializable{
	
	private String masterUser;
	private String user;
	private String path;

	private static final long serialVersionUID = 6721648574636359162L;

	public Remove(String rep, String user, String masterUser) {
		// TODO Auto-generated constructor stub
		this.user = user;
		this.path = masterUser + "/" + rep;
		this.masterUser = masterUser;
		
	}
	public void removeUser() throws IOException{
		
		File inputFile = new File(path + "/" + "share.txt");
		File tempFile = new File("myTempFile.txt");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String currentLine;

		while((currentLine = reader.readLine()) != null) {
		    if(currentLine.equals(user)) continue;
		    writer.write(currentLine);
		    writer.newLine();
		}
		writer.close(); 
		reader.close();
		inputFile.delete();
		tempFile.renameTo(inputFile);
		
	}
}

