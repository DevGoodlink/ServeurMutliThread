import java.io.Serializable;
class Joueur implements Serializable{

    /** @type long numéro de serie définie pour le partage entre client et serveur */
    final static long serialVersionUID =1L;
   
    /** @type String Nom et prenom du joueur */
    String nom,prenom;

    /** @type String license du joueur */
    int licence=0;

    /** @type int Score du joueur */
    int score;

    /** @type int nombre de partie joué */
    int nbrJeux=0;

    /** @type boolean le joueur et connectée */
    boolean CONNECTED = false;
    
    /**
     * Constructeur de joueur
     * @param  licence license du joueur
     */
    public Joueur(int licence) {
        this.licence=licence;
    }
    
    /**
     * Constructeur par defautl du joueur
     */
    public Joueur() {
        super();
    }
    
    /**
     * Cnostructeur complet du joueur
     * @param  nom     Nom du joueur
     * @param  prenom  Prenom du joueur
     * @param  licence License du joueur
     */
    public Joueur(String nom,String prenom,int licence){
        this.nom=nom;
        this.prenom=prenom;
        this.licence=licence;
    }
   
    @Override
    public String toString() {
        return "nom : "+this.nom+" prenom "+this.prenom+" score = "+this.score +" nombre de jeux = "+nbrJeux;
    }
   
    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }
   
    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof Joueur)) return false;
        Joueur j = (Joueur)obj;
        return this.licence==j.licence;
    }
}