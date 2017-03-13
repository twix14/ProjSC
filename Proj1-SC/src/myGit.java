import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class myGit {

	public static void main(String[] args) {
		myGit connect = new myGit();
		connect.connectToServer(args);
	}

	private void connectToServer(String[] args) {
		Socket socket = null;
		Result res = null;

		if(args[0].equals("-init")){
			if(makeRepositoryLocal(args[1]))
				System.out.print("-- O repositório " + args[1] + " foi criado localmente");
		} else {

			try {
				String[] temp = args[1].split(":");
				socket = new Socket(temp[0], 
						Integer.parseInt(temp[1]));
				System.out.println("A conectar...");
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}

			try{
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

				if(args.length == 4){ //Cria um novo utilizador

					System.out.println("-- O utilizador " + args[0] + " vai ser criado");
					if(user(args, out, in) == 1)
						System.out.println("-- O utilizador " + args[0] + " foi criado");
					else
						System.out.println("-- O utilizador ja existe!");
					out.writeBoolean(false); //indica que nao tem mais operacoes a fazer

				} else {

					if(user(args, out, in) == 1)
						System.out.println("-- O utilizador " + args[0] + " foi criado");
					out.writeBoolean(true); //indica que vai fazer operacao de pull,push...

					switch (args[4]) {
					case "-push":
						boolean isFile = args[5].contains(".");
						List<File> singleFile = new ArrayList<File>();
						List<Pair<String, Long>> single =  new ArrayList<Pair<String, Long>>();
						
						if(isFile){
							singleFile.add(new File(args[5]));
							single.add(new Pair<String, Long>(singleFile.get(0).getName(), singleFile.get(0).lastModified()));
						}
						File rep = new File(args[5]);
						List<Pair<String, Long>> files = isFile? single : getFiles(getFilesDir(rep));
						Push psh =  new Push(rep.getName().contains(".")? rep.getParent() : rep.getName(), 
								!args[5].contains("."), files);
						res = ClientStub.Instance.sendReceivePush(psh, out, in, isFile? singleFile : getFilesDir(rep), args[0]);
						//falta eliminar ficheiros que ja nao estejam no repositorio mas que estao no servidor
						
						break;
					case "-pull":
						String[] s = args[5].split("/");
						Pull pll;
						if(s.length == 1){
							pll = new Pull(args[0]+"/"+args[5], args[5],  !args[5].contains("."));
						}
						else{
							pll = new Pull(args[5], args[5], !args[5].contains("."));
						}
						res = ClientStub.Instance.sendReceivePull(pll, out, in);
						break;
					case "-share":
						Share shr = new Share(args[6], args[0], args[5]);
						res = ClientStub.Instance.sendReceiveShare(shr, out, in);
						break;
					case "-remove":
						Remove rm = new Remove(args[5], args[6], args[0]);
						res = ClientStub.Instance.sendReceiveRemove(rm, out, in);
						break;
					}
				}
				
				System.out.println(res);

				out.close();
				in.close();
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
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

	private int user(String[] args, ObjectOutputStream out, ObjectInputStream in) throws IOException {
		String pwd = "";
		Scanner keyboard = null;

		while(!args[3].equals(pwd)){
			keyboard = new Scanner(System.in);
			System.out.println("Confirmar password do utilizador " + args[0]);
			pwd = keyboard.nextLine();
			if(!pwd.equals(args[3]))
				System.out.println("As passwords nao correspondem");
		}

		keyboard.close();

		out.writeObject((String) args[0]);
		out.writeObject((String) args[3]);

		int result = 0;

		try {
			result = (int) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	private static boolean makeRepositoryLocal(String name) {
		File dir = new File(name);
		return dir.mkdir();
	}

}
