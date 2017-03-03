import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class Remove implements Serializable{

	private static final long serialVersionUID = 6721648574636359162L;

	public Remove() {
		// TODO Auto-generated constructor stub
		
		
	}
	public void removeUser(String path, String user) throws IOException{
		
		File inputFile = new File("share.txt");
		File tempFile = new File("myTempFile.txt");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String currentLine;

		while((currentLine = reader.readLine()) != null) {
		    // trim newline when comparing with lineToRemove
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
