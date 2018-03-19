

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class client du jeux
 */
public class Client {

    /**
     * Fonction d'entrée de notre client
     * @param args Argument d'execution
     */
    public static void main(String[] args) {

        /** @type Socket Socket de communication avec le serveur */
        Socket socket;

        /** @type ObjectOutputStream flux de sortie pour les objets */
        ObjectOutputStream outToServer;

        /** @type ObjectInputStream flux d'entré pour les object */
        ObjectInputStream inFromServer;

        /** @type Requete requeque à envoyé et reçut */
        Requete ans, resp = null;

        /** @type Scanner scanner de l'entrer clavier */
        Scanner sc = new Scanner(System.in);
        
        /** @type Joueur joueur du client */
        Joueur j;

        /** @type String adresse ipV4 du serveur */
        String ipAddr = ipAddress();

        try {
            //demande d'ouverture d'une connexion sur le serveur de jeu et le numero de port 60000
            socket = new Socket(ipAddr, 60000);//args[0]

            //Initialisation des flux
            inFromServer = new ObjectInputStream(socket.getInputStream());
            outToServer = new ObjectOutputStream(socket.getOutputStream());

            // Récupération de la connexion serveur
            try{
               resp = (Requete) inFromServer.readObject(); 
            } catch (Exception e){
                System.err.println(e.getMessage());
            }
            
            // Suis-je bien connecté
            if(resp.intent.equalsIgnoreCase("connected")){
                System.out.println(resp.answer);

                // Debut de l'authenfication
                
                /** @type boolean phase authentification fini */
                boolean auth = false;
                do {
                    ans = authentification();

                    // Envoie de l'authenfication
                    outToServer.writeObject(ans);

                    // récupération de la réponse serveur
                    try{
                       resp = (Requete) inFromServer.readObject(); 
                    } catch (Exception e){
                        System.err.println(e.getMessage());
                    }

                    // Je voulait me connecter
                    if(ans.intent.equalsIgnoreCase("login")){
                        // Je suis connecter
                        if(resp.answer.equalsIgnoreCase("login-success")){
                            System.out.println("Authentification reussi");
                            j = resp.getJoueur();
                            auth = true;  
                        // La connexion à échouer  
                        }else if(resp.answer.equalsIgnoreCase("login-fail")){
                            System.out.println("Authentification échoué");
                        // Message inatendu du serveur
                        } else {
                            System.err.println("Erreur de communication avec le serveur");
                        }

                    // Je voulait m'inscrire    
                    }else if(ans.intent.equalsIgnoreCase("signup")){
                        // Inscription reussie
                        if(resp.answer.equalsIgnoreCase("signup-success")){
                            System.out.println("Inscription reussi");
                            j = resp.getJoueur();
                            auth = true;
                        // Message inatendu du serveur
                        } else {
                            System.err.println("Erreur de communication avec le serveur (ER001)");
                        }
                    }
                }while(!auth);
                // Fin de l'authentifcation

                pause();

                /** @type boolean souhaite rejouer */
                boolean rejouer = false;
                do{
                    System.out.println("Préparation du jeux");

                    // Demande au serveur de préparer un partie
                    resp = new Requete(null, "start", null, 0L);
                    outToServer.writeObject(resp);

                    try{
                       resp = (Requete) inFromServer.readObject(); 
                    } catch (Exception e){
                        System.err.println(e.getMessage());
                    }

                    // Lance la partie 
                    if(resp.answer.equalsIgnoreCase("start-ok")){

                        // phase de jeux
                        
                        /** @type Boolean le joueur à gagné */
                        boolean win = false;

                        /** @type String réponse du joueur */
                        String answer;
                        do {
                            System.out.flush();
                            System.out.println("Proposer une suite de 5 lettre :");
                            answer = sc.nextLine();
                            if(answer.length() == 5){

                                // Envoie un proposition
                                resp = new Requete(null, "try", answer, 0L);
                                outToServer.writeObject(resp);

                                try{
                                   resp = (Requete) inFromServer.readObject(); 
                                } catch (Exception e){
                                    System.err.println(e.getMessage());
                                }

                                System.out.println(resp.intent);
                                // Si je n'ai pas gagné
                                if(resp.intent.equalsIgnoreCase("answer")){
                                    
                                    /** @type String[] tableau d'informations */
                                    String[] tokens = resp.answer.split(":");

                                    System.out.println(tokens[1] + " lettre(s) sont bonnes et " + tokens[0] + " lettres sont bien placée(s) en "+resp.time+"s");
                                // Si j'ai gagnée
                                }else if(resp.intent.equalsIgnoreCase("game-success")){
                                    System.out.println("Bravo tu à trouvé en " + resp.time);
                                    win = true;
                                }else {
                                    System.err.println("Erreur de communication avec le serveur (ER003)");
                                }
                            }else {
                                System.err.println("Format de la réponse incorrect");
                            }
                            pause();
                        }while(!win);
                        // Fin de partie 
                } else {
                    System.err.println("Erreur de communication avec le serveur (ER002)");
                }
                System.out.flush();
                        
                // Demande de rejouer
                System.out.println("Voulez vous rejouer ? ");
                System.out.println("1. oui");
                System.out.println("2. non");
                System.out.print("Votre choix entre 1 et 2 : ");
                int choix = sc.nextInt();
                if(choix == 1){
                    rejouer = true;
                }else {
                    rejouer = false;
                }
            } while(rejouer);
                
            } else {
               System.err.println("Connexion au serveur echouée"); 
            }
            
            //fermeture de la connexion
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Demande de l'adresse ipV4
     * @return String adresse IPV4
     */
    public static String ipAddress(){

        /** @type String adresse Ipv4 entrez par le joueur */
        String ipAddr;

        /** @type boolean l'adresse ip et valid */
        boolean ipAddrValid;

        /** @type Scanner scan de l'entré clavier */
        Scanner sc = new Scanner(System.in);

        //Demande de l'adresse ip
        do {
            System.out.println("Entree l'adresse ip du serveur de jeu :");
            ipAddr = sc.nextLine();
            ipAddrValid = Client.validateIpAddress(ipAddr);
            if (!ipAddrValid) {
                System.err.println("Format de l'adresse ip invalid");
            }
            System.out.flush();
        } while (!ipAddrValid);

        return ipAddr;
    }

    /**
     * Procédure d'authntification du joueur
     * @return Requete requete à envoyé au serveur pour l'authentification
     */
    public static Requete authentification(){

        /** @type int choix fait par le joueur */
        int choix;

        /** @type Scanner scan de l'entré clavier */
        Scanner sc = new Scanner(System.in);

        //Demande d'authetification
        do {
            System.out.println("Choisissez la maniére de vous identifier :");
            System.out.println("1. Connexion");
            System.out.println("2. Inscription");
            System.out.println("Entrez 1 ou 2 pour votre choix :");
            choix = sc.nextInt();
            if (choix != 1 && choix != 2) {
                System.err.println("Format invalid");
            }
            System.out.flush();
        } while (choix != 1 && choix != 2);

        if(choix == 1){
            /** @type String license du joueur */
            int license = joueurLogin();

            /** @type Joueur joueur avec la clef de license */
            Joueur j = new Joueur(license);
            return new Requete(j, "login", null, 0L);
        } else {
            /** @type Joueur joueur avec les informations d'enregistrement */
            Joueur j = joueurRegister();
            return new Requete(j, "signup", null, 0L);
        }
    }

    /**
     * Connexion d'un joueur
     * @return int license du joueur
     */
    public static int joueurLogin(){

        /** @type int license entré par le joueur */
        int license;

        /** @type boolean si la license et valide */
        boolean licenseValid;

        /** @type Scanner scan de l'entré clavier */
        Scanner sc = new Scanner(System.in);
        //Demande de la license
        do {
            System.out.println("Entree votre clé de license :");
            license = sc.nextInt();
            if (license < 1 && license >=10000) {
                System.err.println("Format de la license invalid");
            }
            System.out.flush();
        } while (license < 1 && license >=10000);

        return license;
    }

    /**
     * Inscription d'un joueur
     * @return Joueur information du joueur à inscrire
     */
    public static Joueur joueurRegister(){

        /** @type String nom et prenom du joueur */
        String nom, prenom;

        /** @type boolean si le nom et prenom sont valide */
        boolean nomValid, prenomValid;

        /** @type Scanner scan de l'entré clavier */
        Scanner sc = new Scanner(System.in);
        
        //Demande du nom
        do {
            System.out.println("Entree votre nom :");
            nom = sc.nextLine();
            nomValid = Client.validateNomPrenom(nom);
            if (!nomValid) {
                System.err.println("Format de votre nom invalid");
            }
            System.out.flush();
        } while (!nomValid);

        //Demande du prenom
        do {
            System.out.println("Entree votre prenom :");
            prenom = sc.nextLine();
            prenomValid = Client.validateNomPrenom(prenom);
            if (!prenomValid) {
                System.err.println("Format de votre prenom invalid");
            }
            System.out.flush();
        } while (!prenomValid);

        // Joueur a partager avec le serveur.
        return new Joueur(nom, prenom, 0000); 
    }

    /**
     * Valide le bon format d'une adresse IPv4
     *
     * @param ipAddress adresse ip à valider
     * @return boolean
     */
    public static boolean validateIpAddress(String ipAddress) {
        
        /** @type String partie entiere de l'adresse ip */
        String[] tokens = ipAddress.split("\\.");

        if (tokens.length != 4) {
            return false;
        }

        for (String str : tokens) {
            int i = Integer.parseInt(str);
            if ((i < 0) || (i > 255)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valide le bon format d'un nom ou prénom
     *
     * @param chaine nom ou prénom à valider
     * @return boolean
     */
    public static boolean validateNomPrenom(String chaine) {
        if (chaine.length() <= 0) {
            return false;
        }
        if (chaine.matches(".*[0-9].*")) {
            return false;
        }

        return true;
    }

    /**
     * Mettre le programme en attente
     *
     * @return boolean
     */
    public static void pause() {

        /** @type Scanner scan de l'entré clavier */
        Scanner sc = new Scanner(System.in);
        System.out.println("Pour continuer appuyer sur une touche :");
        sc.nextLine();
        System.out.flush();
    }
}