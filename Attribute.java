import java.util.*;


public class Attribute{
    String name;
    int n_classes;
    boolean is_dec;
    boolean is_interval;
    ArrayList<Float> dec;
    ArrayList<Integer> num;
    ArrayList<String> values;
    ArrayList<Class> classes;
    Attribute(String name){
        this.name=name;
        this.is_dec=false;
        this.is_interval=false;
        this.values=new ArrayList<String>();
        this.dec=new ArrayList<Float>();
        this.num=new ArrayList<Integer>();
        this.classes=new ArrayList<Class>();
    }
    // Adiciona linha em que uma dada classe foi observada e incrementa o número de observações dessa mesma classe
    void addObs(String class_name, int line){
        for(Class s: classes){
            if(s.name.equals(class_name)){
                s.n_obs++;
                s.pos.add(line);
            }
        }
    } 
}