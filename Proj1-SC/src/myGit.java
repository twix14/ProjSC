import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

public class myGit {

	public static void main(String[] args) {
		myGit connect = new myGit();
		connect.connectToServer(args);
	}
	
	private void connectToServer(String[] args) {
		Socket socket = null;
		
		/**
		 * Codigos createuser - 0
		 */
		
		try {
			String[] temp = args[1].split(":");
			socket = new Socket(temp[0], 
					Integer.parseInt(temp[1]));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		try{
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			
			if(args[0].equals("-init"))
				if(makeRepositoryLocal(args[1]))
					System.out.print("-- O repositório myrep foi criado localmente");
			else if(args.length == 3){ //Cria um novo utilizador
				user(args, out, in);	
			}else {
				switch (args[3]) {
					case "-push": break;
					
					case "-pull": break;
					
					case "-share": break;
					
					case "-remove": break;
				}
			}
			
			out.close();
			in.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}	
	
	private void user(String[] args, ObjectOutputStream out, ObjectInputStream in) throws IOException {
		//ir buscar lista de utilizadores primeiro, value - utilizador, key - password
		List<Entry<String, String>> users = getUsers(out, in);
		
		//ve se o utilizador ja existe no caso de ser
		for(Entry<String,String> e : users){
			if(e.getValue().equals(args[0]))
				return;
		}
		
		System.out.println("O utilizador " + args[0] + " vai ser criado");
		
		String pwd = "";
		Scanner keyboard = null;
		
		while(!args[2].equals(pwd)){
			keyboard = new Scanner(System.in);
			System.out.println("Confirmar password do utilizador " + args[3]);
			pwd = keyboard.nextLine();
			if(!pwd.equals(args[2]))
				System.out.println("As passwords nao correspondem");
		}
		
		keyboard.close();
		
		createUser(args[3], pwd, out, in);
		System.out.println("O utilizador " + args[0] + " foi criado");
	}

	private List<Entry<String, String>> getUsers(ObjectOutputStream out, ObjectInputStream in) throws IOException {
		List<Entry<String, String>> result = new ArrayList<Entry<String, String>>();
		//rever implementacao para ser feita map?
		
		//diz que quer a lista de utilizadores
		out.writeInt(1);
		
		while(true){
			//envia 1 se ainda tem utilizadores e 0 se ja nao tem
			if(in.readInt() == 0)
				break;
			else{
				String name = "", pwd = "";
				
				try {
					name = (String) in.readObject();
					pwd = (String) in.readObject();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				
				Entry<String, String> e = new myEntry<String, String>(name,pwd);
				result.add(e);
			}
		}
		
		return result;
	}

	private void createUser(String name, String pwd, ObjectOutputStream out, ObjectInputStream in) throws IOException {
		//diz que vai criar um utilizador
		out.writeInt(0);
		
		out.writeObject(name);
		out.writeObject(pwd);
		
		boolean ok = in.readBoolean();
		
		if(ok) 
			System.out.println("OK");
		else System.out.println("ERRO");
	}

	private static boolean makeRepositoryLocal(String name) {
		File dir = new File(name);
		return dir.mkdir();
	}
	
}
