import java.io.Serializable;


class Requete implements Serializable{
    
    /** @type long numéro de serie de la classe */
    final static long serialVersionUID = 1L;

    /** @type Joueur joueur à envoyé */
    Joueur j;

    /** @type String intention de la requete */
    String intent;

    /** @type String contenue texte de la requete */
    String answer;

    /** @type liong temps de jeux */
    long time;

    /** Constructeur de base */
    public Requete(){}

    /**
     * Constructeur complet
     * @param  j      Joueur
     * @param  intent intention
     * @param  answer contenue texte
     * @param  time   temps
     */
    public Requete(Joueur j, String intent,String answer,long time){
        this.j=j;
        this.intent = intent;
        this.answer=answer;
        this.time=time;
    }

    /**
     * Getter du joueur
     * @return le joueur
     */
    public Joueur getJoueur(){
        return this.j;
    }

    @Override
    public String toString() {
        return intent;
    }

}