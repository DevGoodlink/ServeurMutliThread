import java.io.Serializable;

class Requete implements Serializable{
    final static long serialVersionUID =2L;
    Joueur j;
    public String intent;
    public String answer;
    public long time;
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