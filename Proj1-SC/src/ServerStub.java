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
import java.util.ArrayList;
import java.util.List;

public enum ServerStub {
	Instance;
	
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

		for(Pair<String, Long> file : push.getFiles()){
			String[] extension = file.getSt().split("\\.(?=[^\\.]+$)");
			if(!FileUtilities.INSTANCE.checkFile(in, out)) //se o ficheiro nao estiver atualizado
				FileUtilities.INSTANCE.downloadFile(in, out, rep + " " + extension[0] + " " +  extension[1], true);
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
		List <File> files = getFilesDir(new File(pull.getRep()));
		pull.addFiles(getFiles(files));
		out.writeObject(pull);

		int i = 0;
		List<Pair<String, Long>> fls = pull.getFiles();

		for(File fl : files){
			String version;
			Pair<String, Long> file = fls.get(i);
			if(!(version = FileUtilities.INSTANCE.sendReceiveCheckFile(in, out, file, pull.getLocRep())).equals("up-to-date"))
				if(!fl.getName().equals("share.txt"))
					FileUtilities.INSTANCE.uploadFile(in, out, fl, version);
			i++;
		}
		return new Result("", true);
	}

	private List<Pair<String, Long>> getFiles(List<File> files){
		List<Pair<String, Long>> result = new ArrayList<Pair<String, Long>>();

		for(File fl : files)
			result.add(new Pair<String, Long>(fl.getName(), fl.lastModified()));

		return result;
	}

	private List<File> getFilesDir(File rep){
		List<File> result = new ArrayList<File>();
		for(File fl : rep.listFiles())
			result.add(fl);
		return result;
	}
	public Result doShare(Share share) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(share.pathDestiny() + "/" + "share.txt"));
		String line;
		boolean userE = false;

		while((line = reader.readLine()) != null){
			userE = line.equals(share.getUserToShare());
		}

		reader.close();

		if(!userE){
			BufferedWriter writer = new BufferedWriter(new FileWriter(share.pathDestiny() + "/" + "share.txt", true)); 
			writer.write(share.getUserToShare());
			writer.newLine();
			writer.close();
			String[] s = share.pathDestiny().split("/");
			return new Result("-- O repositorio " + s[1] + " foi partilhado com o utilizador " + share.getUserToShare() , true); //a mudar para construtor!
		}

		return new Result("Erro: O utilizador" + share.getUserToShare() + " não existe", false);
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
		
		String[] s = path.split("/");
		return new Result("-- O utilizador " + user + " foi removido do repositório " + s[1], true);
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
