import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

public class ServerStub {

	public Result doPush(Push push, String user, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException{
		File rep = null;

		if(!userPath(push.getPath())){
			rep = new File(user + "/" + push.getPath());
			//if()
			if(push.isDir()){
				File f = new File(user);
				File share = new File(user + "/" + push.getPath() + "/" + "share.txt");

				if(!f.exists() && !share.exists()){
					f.mkdir();
				}

				if(!rep.exists()){
					rep.mkdir();
					share.createNewFile();
					//diretorio criado
				}
			}
		}
		FileUtilities fu = new FileUtilities();
		
		for(Pair<String, Long> file : push.getFiles()){
			String[] extension = file.getSt().split("\\.(?=[^\\.]+$)");
			if(!fu.checkFile(in, out)) //se o ficheiro nao estiver atualizado
				fu.downloadFile(in, out, rep + " " + extension[0] + " " +  extension[1], true);
		}
		
		for(File fl : rep.listFiles()){
			String[] file = fl.getName().split("////");
			if(!file[file.length-1].equals("share.txt")){
				String[] nameFile = file[file.length-1].split("_v[\\d]+\\.");
				boolean encontrou = false;
				for(Pair<String, Long> fileClient : push.getFiles()){
					String[] client = fileClient.getSt().split("\\.");
					if(client[0].equals(nameFile[0]))
						encontrou = true;
				}
				if(!encontrou)
					System.out.println(fl.delete()? "apagou " + file[file.length-1] : "");
			}	
		}
		
		return null;
	}
	
	public Result doPull(Pull pull, String user, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException{
		File rep = null;

		if(!userPath(pull.getRep())){
			rep = new File(pull.getRep());
			if(pull.isDir()){//Erro porque nao tenho aqui o metodo
				File f = new File(user);
				File share = new File(pull.getRep()+ "/" + "share.txt");

				if(!f.exists() && !share.exists()){
					f.mkdir();
				}

				if(!rep.exists()){
					rep.mkdir();
					share.createNewFile();
					//diretorio criado
				}
			}
		}
		FileUtilities fu = new FileUtilities();
		
		for(Pair<String, Long> file : pull.getFiles()){
			String[] extension = file.getSt().split("\\.(?=[^\\.]+$)");
			if(!fu.checkFile(in, out)) //se o ficheiro nao estiver atualizado
				fu.downloadFile(in, out, rep + " " + extension[0] + " " +  extension[1], true);
		}
		
		for(File fl : rep.listFiles()){
			String[] file = fl.getName().split("////");
			if(!file[file.length-1].equals("share.txt")){
				String[] nameFile = file[file.length-1].split("_v[\\d]+\\.");
				boolean encontrou = false;
				for(Pair<String, Long> fileClient : pull.getFiles()){
					String[] client = fileClient.getSt().split("\\.");
					if(client[0].equals(nameFile[0]))
						encontrou = true;
				}
				if(!encontrou)
					System.out.println(fl.delete()? "apagou " + file[file.length-1] : "");
			}	
		}
		
		return null;
	}
	
	public Result doShare(String pathDestiny, String userToShare) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(pathDestiny + "/" + "share.txt"));
		String line;
		boolean userE = false;
		Result res = null;
		
		while((line = reader.readLine()) != null){
			userE = line.equals(userToShare);
		}
		
		reader.close();
		
		if(!userE){
			BufferedWriter writer = new BufferedWriter(new FileWriter(pathDestiny + "/" + "share.txt", true)); 
			writer.write(userToShare);
			writer.newLine();
			writer.close();
			res = new Result("All good", true);
			return res; //a mudar para construtor!
		}
		
		return null;
	}
	
	public Result doRemove(String path, String user) throws IOException{
		
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
		
		return new Result("All good", true);
	}
	
	public boolean userPath(String string) throws IOException {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader("utilizadores.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		while((line = reader.readLine()) != null){
			String[] curr = line.split(" ");
			if(string.equals(curr[0]))
				return true;
		}

		return false;
	}
	
}
