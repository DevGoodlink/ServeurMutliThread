import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Exception;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class Server extends Thread{
    int nbClient=0;
    boolean game=true;

    public void run() {
        System.err.println("Lancement du run serveur    ");
        try {
			ServerSocket ss = new ServerSocket(60000);
			while (nbClient < 10 && game) {
				Socket s = ss.accept();
                ++nbClient;
                new Game(s,nbClient).start();
			}
			ss.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			
        }
    }
    public static void main(String[] args) {
		new Server().start();
        System.out.println("     Lancement du serveur par le main   ");
    }
/**
 * La classe Game représente un jeu qui se lance à chaque connexion d'un client
 * Chaque jeu a sa propre socket pour communiquer avec le joueur
 * Un objet joueur est associé à  chaque jeu
 * 
 */
    class Game extends Thread {
        private Socket socket;
        Joueur j;
		int cltNumber;
        long tempsJeu;
        String mot;
		boolean game = true;
        //Joueur j;
        
        public Game(Socket s,int nbClient){
            socket =s;
            tempsJeu=0L;
            cltNumber=nbClient;
        }

        public void run(){
            Requete req;
            Requete resp;
            System.out.println("   lancement du jeu pour le client n° = "+nbClient);
            try 
            {
                String ipAddress = socket.getRemoteSocketAddress().toString();
                ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                String modle = "Bievenue, vous êtes connecté au serveur de jeux";
                resp = new Requete(null,"first","connected :\n"+ modle,0L); //afficher la liste des score à la connexion
                toClient.writeObject(resp);
                Thread.sleep(5000);
                long debut = System.currentTimeMillis(), fin = System.currentTimeMillis();
                long temps = fin - debut;
                while (( req = (Requete) fromClient.readObject()) != null) 
                {
                    fin = System.currentTimeMillis();
                    temps = (fin - debut) / 1000L;
                    tempsJeu+=temps;
                    if (req.intent.equalsIgnoreCase("login")){
                        //récupération de la licence nécessaire à l'authentification
                        Joueur j=LoginAndRegister(true,req.getJoueur());
                        if(j==null){
                            resp=new Requete(j,null ,"login-fail" , temps);
                        }
                        else
                        {
                            j.nbrJeux+=1;
                            resp=new Requete(j,null ,"login-success" , temps);
                            this.j=j;
                        }
                    }
                    if (req.intent.equalsIgnoreCase("signup")){
                        Joueur j=LoginAndRegister(false,req.getJoueur());
                        if(j!=null){
                            resp=new Requete(j,null,"signup-success", temps);
                            this.j=j;
                        }else{
                            resp=new Requete(null,null,"signup-fail", temps);
                        }
                        
                    }
                    if(req.intent.equalsIgnoreCase("start")){
                        this.mot=genererMot();
                        temps=0;
                        resp=new Requete(null, null, "start-ok", temps);
                    }
                    if(req.intent.equalsIgnoreCase("try")){
                        int[] res = verifier(mot, req.intent);
                        if(res[0]==5 && res[1]==5)
                            resp=new Requete(null, null,"game-success", temps);
                        else
                            resp=new Requete(null, null,""+res[0]+":"+res[1], temps);
                        //Enregistrement de score
                        if (tempsJeu<60)j.score+=10;
                        else if(tempsJeu>60 && tempsJeu<60*3)j.score+=5;
                        else if(tempsJeu>60*3 && tempsJeu<60*5)j.score+=2;
                        else if(tempsJeu>60*5)j.score+=1;

                        resultatsInOut(false,j );//enregistrement des résultats sur le fichier
                    }
                    if(req.intent.equalsIgnoreCase("abondon")){
                        this.j.score-=5;
                    }
                    else
                        resp=new Requete(null, null, "ambigus or unknown", temps);

                    toClient.writeObject(resp);
                    debut = System.currentTimeMillis();
                }
                  socket.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
                --nbClient;
            }
            
        }

    }
    
    
    /**
     * Permet de récupérer l'historique des résultats des joueurs ou 
     * d'inscrire les nouveaux résultats d'un joueur;
     * @param lectureEcriture true pour lecture des résultat et false pour l'écriture d'un nouveau score
     * @param j Joueur null si lecture de résultat / joueur avec les nouveaux résultats à enregistrer
     * 
     */
    public static synchronized String resultatsInOut(boolean lectureEcriture,Joueur j){
        StringBuilder chaine=new StringBuilder("Résultats : \n");
        FileOutputStream fos;ObjectOutputStream oos;
        FileInputStream fis;ObjectInputStream ois;
        List<Joueur> playerLst = new ArrayList();
        

        try{
            //remplir le fichier par un élément de test
            fis=new FileInputStream("players.txt");
            ois= new ObjectInputStream(fis);
            playerLst=(List<Joueur>)ois.readObject();
            ois.close();
            fis.close();
            if(playerLst.size()==0){
                fos=new FileOutputStream("players.txt");
                oos= new ObjectOutputStream(fos);
                playerLst.add(new Joueur("test","test",1111));
                oos.writeObject(playerLst);
                oos.close();
                fos.close();
            }
            if(lectureEcriture){//si je ve récupérer les résultats
                //Tri des objets joueur selon le score par ordre décroissant
                playerLst = playerLst.stream().sorted(
                    Comparator.comparing(Joueur::getScore).reversed()
                    ).collect(Collectors.toList());
                //écriture de la liste des scores de tous les joueurs
                playerLst.stream().forEach(e->chaine.append(e+"\n"));
                return chaine.toString();
            }else{//récupération du fichier players pour écriture
                fos=new FileOutputStream("C:\\players.txt");
                oos= new ObjectOutputStream(fos);
                //récupération du joueur en cours
                for(int i=0;i<playerLst.size();++i){
                    Joueur player =playerLst.get(i);
                    if(player.licence==j.licence){
                        //je le supprime de la liste
                        playerLst.set(i,j);}
                }
                
                oos.writeObject(playerLst);
                oos.close();
                fos.flush();
            }
            return null;//dans le cas de l'écriture
          }catch(Exception ioe)
          {ioe.printStackTrace();return null;}
    }
    /**
     * fonction qui permet de faire le login et l'enregistrement
     * les deux pour éviter un accés simultané au fichier par plusieurs thread.
     * @param l true pour login ou false pour Sign up
     * @param j Objet joueur
     * @return Joureur
     */
    public static synchronized Joueur LoginAndRegister(boolean l,Joueur j)throws Exception {
        
        List<Joueur> lst = new ArrayList<>();
        //ouverture du fichier players.txt pour lecture
        File f = new File("players.txt");
        if(f.exists() && !f.isDirectory()) { 
            FileInputStream fis=new FileInputStream("players.txt");
            ObjectInputStream ois= new ObjectInputStream(fis);
            lst=(List<Joueur>)ois.readObject();
            ois.close();
            fis.close();
        }else{
            //création du fichier
            FileOutputStream fos=new FileOutputStream("players.txt");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(lst);
            oos.close();
            fos.close();
        }
        if(l){//login
            System.out.println("Login launched for j = "+j.nom);
            Joueur foundJoueur=null;
            foundJoueur = lst.stream().filter(e->e.equals(j)).findFirst().get();
            if(foundJoueur==null){
                System.out.println("[fail authentification]");
                return null;//retourn null
            }
            System.out.println("[success authentification]");               
            return foundJoueur;//retourn l'objet trouvé dans la base authentification avec succés
        }else{
            System.out.println("Signup launched for j = "+j.nom);
            //enregistrement
            //test si le joueur est présent
            boolean joueurPresent = lst.stream().filter(e->e.nom.equalsIgnoreCase(j.nom) && e.prenom.equalsIgnoreCase(j.prenom)).findFirst().isPresent();
            if(!joueurPresent){//le cas ou il n'est pas présent on lui attribue un numéro de licence aléatoire
                j.licence= new Random().nextInt(10000);
                lst.add(j);
                FileOutputStream fos=new FileOutputStream("players.txt");
                ObjectOutputStream oos= new ObjectOutputStream(fos);
                oos.writeObject(lst);
                oos.close();
                fos.close();
                System.out.println("[joueur créé licence = "+j.licence+"]");
                return j;
            }else{
                System.out.println("[Joueur existant]");
                return null;
            }
        }              
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
	private static synchronized int[] verifier(String mot, String proposition) {
		int[] resultat = { 0, 0 };
		for (int i = 0; i < 5; i++) {
			if (mot.charAt(i) == proposition.charAt(i))
				resultat[0]++;//incrémenter nombre de lettres correctes
			if (mot.contains("" + proposition.charAt(i)))
				resultat[1]++;//incrémenter nombre de lettre existantes
		}
		return resultat;
    }
}