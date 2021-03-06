import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Exception;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Classe server c'est un thread avec deux propriétés nbClient correspondant au nombre de client
 * qui se connecte sur le serveur, à chaque connexion on lance un thread game
 * le serveur ne peux accueillir plus de 10 clients à la fois soit 10 threads
 * à la déconnexion d'un client la propriété nbClient est décrémenté de 1
 */
class Server extends Thread{
    int nbClient=0;
    boolean game=true;
/**
 * la méthode run pour gérer l'arrivée des clients
 */
    public void run() {
        System.err.println("Lancement du run serveur    ");
        try {
			ServerSocket ss = new ServerSocket(60000);
			while (nbClient <= 10 && game) {
				Socket s = ss.accept();
                ++nbClient;
                new Game(s,nbClient).start();
			}
			ss.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			
        }
    }
    /**
     * Main du serveur pour lancer le serveur
     */
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
        /**
         * Constructeur de la classe game prend un socket et un numéro de client au lancement.
         * initialise le temps de jeu à 0
         * 
         * */        
        public Game(Socket s,int nbClient){
            socket =s;
            tempsJeu=0L;
            cltNumber=nbClient;
        }
/**
 * Méthode run pour lancer le jeu elle utilise une requete d'envoie et une requete à la reception
 * pour échanger avec le client
 */
        public void run(){
            Requete req;
            Requete resp;
            System.out.println("   lancement du jeu pour le client n° = "+nbClient);
            try 
            {
                String ipAddress = socket.getRemoteSocketAddress().toString();
                ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                String modle = "Bievenue, vous êtes connecté au serveur de jeux \n"+resultatsInOut(true,null);
                resp = new Requete(null,"connected","connected :\n"+ modle,0L); //afficher la liste des score à la connexion
                toClient.writeObject(resp);
                Thread.sleep(5000);
                long debut = System.currentTimeMillis(), fin = System.currentTimeMillis();
                long temps = fin - debut;
                while (( req = (Requete) fromClient.readObject()) != null) 
                {
                    //notre compteur temps s'arrête à la reception d'une requête client
                    fin = System.currentTimeMillis();
                    temps = (fin - debut) / 1000L;
                    //on incrémente le temps global du jeu pour le joueur qui sera base de calcul du score
                    tempsJeu+=temps;
                    //lorsque le client sollécite un login
                    if (req.intent.equalsIgnoreCase("login")){
                        //récupération de la licence nécessaire à l'authentification
                        Joueur j=LoginAndRegister(true,req.getJoueur());
                        if(j==null){
                            resp=new Requete(j,null ,"login-fail" , temps);
                        }
                        else
                        {//si le login passe on enregistre la référence du joueur authentifié  
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
                        int[] res = verifier(mot, req.answer);
                        if(res[0]==5 && res[1]==5){
                            if (tempsJeu<60){
                                j.score+=10;
                                resultatsInOut(false,j );//enregistrement des résultats sur le fichier
                            }
                            else if(tempsJeu>60 && tempsJeu<60*3){
                                j.score+=5;
                                resultatsInOut(false,j );//enregistrement des résultats sur le fichier
                            }
                            else if(tempsJeu>60*3 && tempsJeu<60*5){
                                j.score+=2;
                                resultatsInOut(false,j );//enregistrement des résultats sur le fichier
                            }
                            else if(tempsJeu>60*5)
                            {
                                j.score+=1;
                                resultatsInOut(false,j );//enregistrement des résultats sur le fichier
                            }
                            resp=new Requete(j, "game-success","game-success", temps);
                        }
                        else
                            resp=new Requete(null, "answer",""+res[0]+":"+res[1], temps);
                    }
                    if(req.intent.equalsIgnoreCase("abondon")){
                        this.j.score-=5;
                    }
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
        System.out.println("enregistrement des résultats");

        try{
            File f = new File("players.txt");
            if(f.exists() && !f.isDirectory()) { 
                fis=new FileInputStream("players.txt");
                ois= new ObjectInputStream(fis);
                playerLst=(List<Joueur>)ois.readObject();
                ois.close();
                fis.close();
                System.out.println("liste des données chargée en mémoire");
            }
            if(playerLst.size()==0){
                System.out.println("taille de la liste ==0");
                fos=new FileOutputStream("players.txt");
                oos= new ObjectOutputStream(fos);
                playerLst.add(new Joueur("test","test",1111));
                oos.writeObject(playerLst);
                oos.close();
                fos.close();
            }
            if(lectureEcriture){//si je ve récupérer les résultats
                //Tri des objets joueur selon le score par ordre décroissant
                System.out.println("Tri des résultats sur la liste");
                playerLst = playerLst.stream().sorted(
                    Comparator.comparing(Joueur::getScore).reversed()
                    ).collect(Collectors.toList());
                //écriture de la liste des scores de tous les joueurs
                playerLst.stream().forEach(e->chaine.append(e+"\n"));
                System.out.println("Resultats envoyé au client");
                return chaine.toString();
               
            }else{
                System.out.println("récupération du fichier players pour écriture");
                fos=new FileOutputStream("players.txt");
                oos= new ObjectOutputStream(fos);
                //récupération du joueur en cours
                System.out.println("récupération du joueur n licence ="+j.licence);

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
    public static synchronized Joueur LoginAndRegister(boolean l,Joueur j){
        
        List<Joueur> lst = new ArrayList<>();
        try {
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
                System.out.println("Login launched for licence = "+j.licence);
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Méthode du serveur pour générer une conbinaison ordonnée de 5 lettres
     * @return String chaine de taille 5
     */
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
    /**
     * Methode verifier la proposition du joueur au mot secret
     * @param mot String 
     * @param proposition String
     * @return int[] tableau  de taille 2 {nombre de lettres correctes,nombre de lettre existantes}
     */
	private static synchronized int[] verifier(String mot, String proposition) {
        System.out.println("mot = "+mot+" prop = "+proposition);
		int[] resultat = { 0, 0 };
		for (int i = 0; i < 5; ++i) {
			if (mot.charAt(i) == proposition.charAt(i))
				resultat[0]+=1;//incrémenter nombre de lettres correctes
			if (mot.contains("" + proposition.charAt(i)))
				resultat[1]+=1;//incrémenter nombre de lettre existantes
		}
		return resultat;
    }
}