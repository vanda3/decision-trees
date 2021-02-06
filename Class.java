import java.util.*;

public class Class{
    String name;
    int n_obs;
    ArrayList<Integer> pos;
    Class(String name, int line){
        this.name=name;
        this.pos=new ArrayList<Integer>();
        if(line!=-1){
            this.n_obs=1;
            pos.add(line);
        }
        else
            this.n_obs=0;
    }
}