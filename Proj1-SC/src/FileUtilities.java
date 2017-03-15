import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public enum FileUtilities {
	INSTANCE;

	public void downloadFile(ObjectInputStream in, ObjectOutputStream out, String file, boolean server) throws IOException, ClassNotFoundException{
		int dim = 0;
		String version = null;

		if(server){
			version = (String) in.readObject();
		}
		else{
			version = ".";
		}


		dim = in.readInt();
		String[] args = file.split(" ");
		FileOutputStream fos = new FileOutputStream(args[0] + "/" + args[1] + version + args[2]);

		int count;
		int temp = dim;
		byte[] buffer = new byte[1024];

		while ((count = in.read(buffer, 0, temp < 1024 ? temp : 1024)) > 0){
			fos.write(buffer, 0, count);
			temp -= count;
			fos.flush();
		}

		fos.close();
	}

	public String sendReceiveCheckFile(ObjectInputStream in, ObjectOutputStream out, Pair<String, Long> pair, String rep) throws IOException, ClassNotFoundException {
		out.writeObject((String) pair.getSt());
		out.writeLong(pair.getNd());
		out.writeObject((File) new File(rep));

		return (String) in.readObject();
	}

	public boolean checkFile(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException{
		String file = (String) in.readObject();
		long size = in.readLong();
		File rep = (File) in.readObject();

		String[] extension = file.split("\\.(?=[^\\.]+$)");

		String version = fileVersion(size, rep, extension[0], extension[1]);
		out.writeObject((String) version);

		return version.equals("up-to-date");
	}

	/**
	 * Verificar se o ficheiro que esta a ser recebido eh mais recente do que 
	 * aquele que esta no servidor, caso contrario devolve "up-to-date"
	 */
	public String fileVersion(long lastModified, File rep, String name, String extension) {
		File[] versions = getNewestVersion(rep, name, extension);
		if(versions == null || versions.length == 0)
			return "_v0.";
		else if(lastModified > versions[versions.length-1].lastModified()){ //ficheiro mais recente do que aquele que esta no servidor
			return "_v" + versions.length + ".";
		} else return "up-to-date";
	}

	public File[] getNewestVersion(File dir, String nameF, String extension) {
		return dir.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name)
			{	
				return name.startsWith(nameF) && name.endsWith("." + extension);
			}
		});
	}

	public void uploadFile(ObjectInputStream in, ObjectOutputStream out, File file, String version, boolean server) throws IOException, ClassNotFoundException{

		if(!server)
			out.writeObject((String) version); //so no cliente a rever
		out.writeInt((int)file.length());

		FileInputStream fil =  new FileInputStream(file);
		InputStream fis = new BufferedInputStream(fil);

		byte[] bytes = new byte[1024];

		int temp = (int)file.length();
		int count = 1024;

		while ((count = fis.read(bytes, 0, temp < 1024 ? temp : 1024)) > 0) {
			out.write(bytes, 0, count);
			temp -= count;
			out.flush();
		}
		fis.close();
	}

	public boolean checkUserPermission(String userToCheck, String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path + "/" + "share.txt"));
		String line;

		while((line = reader.readLine()) != null){
			if(line.equals(userToCheck)){
				reader.close();
				return true;
			}
		}

		reader.close();
		return false;
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
