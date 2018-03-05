package server;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ServerMT extends Thread {
	private int numeroClient=0;
	private Thread[] tab = new Thread[10]; 
	public void run() {
		System.out.println("Lancement serveur");
		try {
			ServerSocket ss = new ServerSocket(60000);
			
			while (numeroClient < 10) {
				// A mettre la condition sur le nombre d'utilisateur
				Socket s = ss.accept();
				tab[numeroClient]=new Game(s);
				tab[numeroClient].start();
				++numeroClient;
			}
			ss.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			
		}
		
	}
	

	class Game extends Thread {
		private Socket socket;
		int cltNumber;
		long tempsJeu;
		boolean game = true;
		/**
		 *
		 * @param s le socket serveur
		 * @param numeroClient le numéro client attribué par le serveur
		 * @param tempsJeu est initialisé à 0 seconde
		 */

		public Game(Socket s) {
			System.out.println("LANCEMENT D'UNE NOUVELLE PARTIE");
			this.socket = s;
			this.cltNumber = numeroClient;//la inner classe a accés aux membres de la classe parent
			this.tempsJeu=0;
		}

		private Joueur inJoueur(socket s){
			ObjectOutputStream outToClient;
            ObjectInputStream inFromClient;

            outToClient = new ObjectOutputStream(s.getOutputStream());
		   	Thread.sleep(1000);
		   	outToClient.writeUTF("wait-Joueur");
		   	outToClient.flush();

		   	Thread.sleep(1000); 
		   	inFromClient = new ObjectInputStream (s.getInputStream());
		   	Joueur j = (Joueur) inFromClient.readObject();

		   	return j;
		}

		private void outJoueur(Joueur j, socket s){
			ObjectOutputStream outToClient;
            ObjectInputStream inFromClient;

            outToClient = new ObjectOutputStream(s.getOutputStream());
		   	Thread.sleep(1000);
		   	outToClient.writeObject(j);
		   	outToClient.flush();
		}

		public void run() {
			System.out.println("INITIALISATION");
			try {
				// instanciation des objets
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				OutputStream os = socket.getOutputStream();
				PrintWriter pw = new PrintWriter(os, true);

				ObjectOutputStream outToClient;
            	ObjectInputStream inFromClient;

            	Joueur j;
				
				System.out.println("RECUPERATION DE L'ADRESSE IP");
				// récupération de l'adresse IP
				String ip = socket.getRemoteSocketAddress().toString();
				System.out.println("CLIENT " + numeroClient + " connecté ip  =" + ip);

				//Authentification avec le client
				outToClient = new ObjectOutputStream(s.getOutputStream());
			   	Thread.sleep(1000);
			   	outToClient.writeUTF("Auth-att");
			   	outToClient.flush();

			   	do {
			   		Thread.sleep(1000); 
				   	inFromClient = new ObjectInputStream (s.getInputStream());
				   	String rep= inFromClient.readUTF();

				   	if(rep.equalsIgnoreCase("send-Joueur")){
				   		j = inJoueur();
				   	}else if(rep.equalsIgnoreCase("joueur-infos")){
				   		outJoueur(j, s);
				   	}else if(rep.equalsIgnoreCase("login")){
				   		if(exists(j)){
				   			
				   		}
				   	}
				   	
				   	
				} while (rep.equalsIgnoreCase("disconnect"));

			   	

			   	 if(login(j)){
			   	 	// Envoi au serveur que le joueur exist
			   	 	outToClient = new ObjectOutputStream(s.getOutputStream());
				   	Thread.sleep(1000);
				   	outToClient.writeUTF("Auth-User-exist");
				   	outToClient.flush();

				   	Thread.sleep(1000); 
				   	inFromClient = new ObjectInputStream (s.getInputStream());
				   	String response = inFromClient.readUTF();
				   	
				   	if(response == "User-info"){
				   		// Envoi du joueur enregistré
					   	j = getJoueur(j);
					   	outToClient = new ObjectOutputStream(s.getOutputStream());
					   	Thread.sleep(1000);
					   	outToServer.writeObject(j);
					   	outToClient.flush();
				   	}
				   	
			   	 } else {
				   	saveNewUser(j);
				   	outToClient = new ObjectOutputStream(s.getOutputStream());
				   	Thread.sleep(1000);
				   	outToClient.writeUTF("Auth-User-new");
				   	outToClient.flush();
			   	 }

				//reponse pour valider l'authentification
				outToClient = new ObjectOutputStream(s.getOutputStream());
				if(j!=null){
		                    outToClient.writeUTF(new String("Auth-ok"));
		                    outToClient.flush();
				}

				pw.println("BIENVENU VOUS ETES LE CLIENT NUM " + numeroClient);
				
				pw.println("PRET A JOUER AVEC MOI?[O/n]");
				String answer = br.readLine();
				System.out.println(answer);
				if (!answer.equalsIgnoreCase("n")) {
					System.out.println("USER A ACCEPTE DE JOUER");
					pw.println("GENERATION DU MOT ALEATOIRE ...");
					System.out.println("GENERATION DU MOT ALEATOIRE");
					
					String word = genererMot();
					pw.println("C'EST PARTIE ! A TOI DE JOUER : ");
					System.out.println("DEBUT DE LA PARTIE, CLIENT NUMERO = " + numeroClient+" A "+new Date());
					System.out.println("Mot généré : " + word);
					
					
					String rep;// la réponse
					String req;// la requete
					int[] resultat;

					long debut = System.currentTimeMillis(), fin = System.currentTimeMillis();
					long temps = fin - debut;
					
					while (game) {
						// l'utilisateur ne doit pas rentrer une valeur nulle

						while ((req = br.readLine()) != null) {
							fin = System.currentTimeMillis();
							temps = (fin - debut) / 1000L;
							pw.println("VOUS AVEZ MIS " + temps+" SECONDES POUR REPONDRE");
							tempsJeu+=temps;
							// si le client veut sortir du jeu il doit juste tapper QUIT
							answer=req;
							if (answer.compareToIgnoreCase("QUIT") == 0) {
								rep = "C'EST TRISTE DE VOUS VOIR PARTIR !";
								leaving();
							}
							// Test sur le nombre de caractères qui doit être égale à 5
							if (req.length() == 5) {
								resultat = verifierNbreLettreCorrecte(word, req);
								if (resultat[0] == 5) 
								{ // toutes les lettres sont à leurs places
									rep = "BRAVO "+ cltNumber +" TU A TROUVE LE MOT A DEVINER C'EST BIEN : " + word;
									game = false;
								} 
								else if (resultat[1] == 5 && resultat[0] != 5) 
								{
									// toutes les lettres sont là mais pas dans l'ordre
									rep = "TU A TROUVE TOUTES LES LETTRES, MAINTNANT IL FAUT LES METTRE DANS L'ORDRE!!";
								} 
								else 
								{ // les autres cas de figure
									rep = "NBRE DE LETTRES DANS L'ORDRE + " + resultat[0]
											+ "\nNBRE DE LETTRES TROUVEES = " + resultat[1] + " TENTE ENCORE.";
								}
							} else {
								// si nombre de caracères est trop grand ou insuffisant
								rep = "NOMBRE DE CARACTERE < OU > A 5 VOUS AVEZ ENVOYE " + req.length()
										+ " CARACTERES.";
							}
							pw.println(rep);
							if(!game) {
								leaving();
							}
							debut = System.currentTimeMillis();
						}
					}
				}
				else {
					pw.println("SAD TO SEE YOU LEAVING");
					leaving();
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			System.out.println("FIN DU GAME");
		}
		/**
		 * leaving() méthode pour mettre fin au jeu
		 * décrémente le nombre de client enregistré
		 * met le thread en sleep pendant un seconde
		 * interrompt le thread
		 * ferme le socket pour la partie en cours
		 * 
		 */
		private void  leaving() {
			numeroClient--;
			game=false;
			try {
				Thread.sleep(1000);
				this.interrupt();
				this.socket.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			
		}
	}

	public static void main(String[] args) {
		new ServerMT().start();
		System.out.println("Après start");
	}
	
	private static synchronized String genererMot() {
		Set<String> chaine = Collections.synchronizedSet(new HashSet<String>());
		int aleatoire = 0;
		String mot = "";
		String[] lettre = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
				"S", "T", "U", "V", "W", "X", "W", "Y", "Z" };
		Random r = new Random();
		while (chaine.size() < 5) {
			aleatoire = r.nextInt(27);
			System.out.println(lettre[aleatoire]);
			if (chaine.add(new String(lettre[aleatoire])))
				mot += lettre[aleatoire];
		}
		return mot;
	}

	private synchronized int[] verifierNbreLettreCorrecte(String mot, String proposition) {
		int[] resultat = { 0, 0 };
		for (int i = 0; i < 5; i++) {
			if (mot.charAt(i) == proposition.charAt(i))
				resultat[0]++;
			if (mot.contains("" + proposition.charAt(i)))
				resultat[1]++;
		}
		return resultat;
	}
	private boolean login() {
		
		return false;
	}
	private void saveNewUser() {
		
	}
	private String[] scores() {
		return new String[] {};
	}
	private void saveScore() {
		
	}
}
