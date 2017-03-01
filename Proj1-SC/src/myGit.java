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
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
			
			try{
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					
				if(args.length == 3){ //Cria um novo utilizador
					
					System.out.println("-- O utilizador " + args[0] + " vai ser criado");
					if(user(args, out, in) == 1)
						System.out.println("-- O utilizador " + args[0] + " foi criado");
					else
						System.out.println("-- A password nao corresponde ao utilizador");
					
				} else {
					
					switch (args[4]) {
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
