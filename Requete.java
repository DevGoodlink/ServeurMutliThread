import java.io.Serializable;


class Requete implements Serializable{
    final static long serialVersionUID = 1L;
    Joueur j;
    String intent;
    String answer;
    long time;
    public Requete(){}
    public Requete(Joueur j, String intent,String answer,long time){
        this.j=j;
        this.intent = intent;
        this.answer=answer;
        this.time=time;
    }
    public Joueur getJoueur(){
        return this.j;
    }
    @Override
    public String toString() {
        return intent;
    }


}