import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Exception;
import java.util.regex.Pattern;

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
            try {
                String ipAddress = socket.getRemoteSocketAddress().toString();
                //PrintWriter toClient = new PrintWriter(socket.getOutputStream(),true);
                //toClient.write("Hello Client "+ cltNumber);
                ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                //toClient.writeObject(new Joueur("Nice try"));
                ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                String modle = "Bievenue, vous êtes connecté au serveur de mastermind réalisé par sbai et perez";
                //Ajouter la liste des score à la connexion
                toClient.writeObject(new Requete(null,null,"connected :\n"+ modle));
                long debut = System.currentTimeMillis(), fin = System.currentTimeMillis();
                long temps = fin - debut;
                
                while (( req = (Requete) fromClient.readObject()) != null) {
                    fin = System.currentTimeMillis();
                    temps = (fin - debut) / 1000L;
                    tempsJeu+=temps;
                    if (req.intent.equalsIgnoreCase("auth")){
                        //récupération de la licence nécessaire à l'authentification
                        Joueur j=LoginAndRegister(true,req.getJoueur());
                        resp=new Requete(j,null ,j.licence=0?"auth-success":"auth-fail" , temps);
                        this.j=j;
                    }
                    if (req.intent.equalsIgnoreCase("register")){
                        Joueur j=LoginAndRegister(false,req.getJoueur());
                        resp=new Requete(j,null,"register-success", temps);
                        this.j=j;
                    }
                    if(req.intent.equalsIgnoreCase("start")){
                        this.mot=genererMot();
                        temps=0;
                        resp=new Requete(null, null, "start-ok", time);
                    }
                    if(req.intent.equalsIgnoreCase("try")){
                        int[] res = verifier(mot, req.intent);
                        if(res[0]==5 && res[1]==5)
                            resp=new Requete(null, null,"game-success", time);
                        else
                            resp=new Requete(null, null,""+res[0]+":"+res[1], time);
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
                        resp=new Requete(null, null, "ambigus or unknown", time);

                    toClient.writeObject(resp);
                    debut = System.currentTimeMillis();
                }
                  
                  socket.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            
        }

    }
    
    
    /**
     * Permet de récupérer l'historique des résultats des joueurs ou d'inscrire les nouveaux résultats d'un joueur;
     */
    public static synchronized String resultatsInOut(boolean lectureEcriture,Joueur j){
        StringBuilder chaine=new StringBuilder("Résultats : \n");
        FileOutputStream fos;
        ObjectOutputStream oos;
        List<Joueur> playerLst = new ArrayList();
        try{
            fos= new FileOutputStream("players");
            oos= new ObjectOutputStream(fos);
            playerLst = (List<Joueur>) ois.readObject();
            //traitement sur l'objet user list en lecture
            if(lectureEcriture){
                playerLst.stream().forEach(e->chaine.append(e+"\n"));
            }else{
                for(int i=0;i<playerLst.size();++i){
                    Joueur player =playerLst.get(i);
                    if(player.licence==j.licence){
                        playerLst.remove(i);
                    }
                }
                playerLst.add(j)
            }
            //oos.writeObject(userList);
            oos.close();
            fos.close();
          }catch(Exception ioe){
               ioe.printStackTrace();
           }
    }
    /**
     * fonction qui permet de faire le login et l'enregistrement
     * les deux pour éviter un accés simultané au fichier par plusieurs thread.
     */
    public static synchronized Joueur LoginAndRegister(boolean l,Joueur j) {
        System.out.println("Login launched");
        List<Joueur> lst = new ArrayList<>();
        lst.add(new Joueur("a","b","111"));
        lst.add(new Joueur("c","d","222"));
        lst.add(new Joueur("e","f","333"));
        lst.add(new Joueur("g","h","444"));
        Joueur foundJoueur;
        if(l){//login
            foundJoueur = lst.stream().filter(e->e.equals(j)).findFirst().get();
            //return oj;
            if(!oj){
                System.out.println("[fail authentification]");
                return j;
            }
            System.out.println("[success authentification]");               
            return foundJoueur;
        }else{//enregistrement
            //test si le joueur est présent
            oj = lst.stream().filter(e->e.nom.equalsIgnoreCase(oj.nom) && e.prenom.equalsIgnoreCase(oj.prenom)).findFirst().isPresent();
            if(!oj){//le cas ou il n'est pas présent
                j.licence= new Random().nextInt(9999);
                lst.add(j);
                System.out.println("[joueur créé]");
                return j;
            }else{
                System.out.println("[Joueur existant]");
                return j;
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