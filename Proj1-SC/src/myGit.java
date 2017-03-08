import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class myGit {

	public static void main(String[] args) {
		myGit connect = new myGit();
		connect.connectToServer(args);
	}

	private void connectToServer(String[] args) {
		Socket socket = null;

		if(args[0].equals("-init")){
			if(makeRepositoryLocal(args[1]))
				System.out.print("-- O repositório myrep foi criado localmente");
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
					Object op = null;

					switch (args[4]) {
					case "-push":
						File[] file = new File[1];
						boolean isFile = args[5].contains(".");
						if(isFile){
							String[] path = args[5].split("/");
							file[0] = new File(path[path.length-1]);
						}
						op =  new Push(args[5], !args[5].contains("."), 
								isFile? file : getFilesDir(new File(args[5])));
						break;
					case "-pull":
						op = new Pull();
						break;
					case "-share":
						op = new Share(args[6], args[0], args[5]);						
						break;
					case "-remove":
						op = new Remove(args[5], args[6], args[0]);
						break;
					}
					sendObj(op, out, in);
				}

				out.close();
				in.close();
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private File[] getFilesDir(File rep){
		return rep.listFiles();
	}

	private void sendObj(Object obj, ObjectOutputStream out, ObjectInputStream in) throws IOException{
		out.writeObject(obj);
		if(obj instanceof Share){
			Share temp = (Share) obj;
			if(!in.readBoolean())
				System.out.println("Erro: o utilizador " + temp.getUserToShare() + " não existe");
			else{
				System.out.println("-- O repositório myrep foi partilhado com o utilizador " + temp.getUserToShare());
			}
		}
		try {
			System.out.println(in.readObject().toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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
