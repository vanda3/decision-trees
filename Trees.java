import java.util.*;
import java.io.*;
import java.math.BigDecimal;


class Trees{
    static String attribs[];
    static ArrayList<Attribute> ats = new ArrayList<Attribute>();
    static String[][] obs;
    static String[][] test;
    static boolean is_test=false;
    static int ob_size=0, at_size=0, ts_size=0;
    public static void main(String args[]){
        String file;
        Scanner scan = new Scanner(System.in);
        
        System.out.println("Qual dos datasets pretende usar?");
        System.out.println("1) Restaurant");
        System.out.println("2) Weather");
        System.out.println("3) Iris");
        System.out.println("4) Outra");
        int choice=scan.nextInt();
        System.out.println();
        while(choice!=1 && choice!=2 && choice !=3 && choice!=4){
            System.out.println("Resposta inválida!");
            System.out.println();
            System.out.println("Qual dos datasets pretende usar?");
            System.out.println("1) Restaurant");
            System.out.println("2) Weather");
            System.out.println("3) Isis");
            System.out.println("4) Outro .csv (sem extensão)");
            choice=scan.nextInt();
            System.out.println();
        }
        if(choice==1)
            file="restaurant";
        else if(choice==2)
            file="weather";
        else if(choice==3)
            file="iris";
        else
            file=scan.next();
        file+=".csv";
        
        obs=parse_data(file);
        ID3 id3=new ID3(ats);
        
        System.out.println("O que pretende fazer?");
        System.out.println("1) Inserir ficheiro teste");
        System.out.println("2) Imprimir àrvore");
        choice=scan.nextInt();
        System.out.println();
        while(choice!=1 && choice!=2){
            System.out.println("Resposta inválida!");
            System.out.println();
            System.out.println("O que pretende fazer?");
            System.out.println("1) Inserir ficheiro teste");
            System.out.println("2) Imprimir àrvore");
            choice=scan.nextInt();
            System.out.println();
        }
        if(choice==1){
            System.out.println("O conteúdo do ficheiro deverá encontrar-se no mesmo formato que o dataset original, exceptuando a primeira linha que contém os atributos.\nInsira o nome do ficheiro .csv (sem extensão):");
            file=scan.next();
            file+=".csv";
            is_test=true;
            System.out.println();
            test=parse_data(file);
            id3.classification(test);
            System.out.println();
        }
        if(choice==2){
            System.out.println();
            id3.print();
            System.out.println();
        }
        
    }
    
    
    //////////////////////////////////////////////////////////////////
    // PROCESSAR FICHEIRO
    static String[][] parse_data(String file){
        at_size=0;
        ob_size=0;
        String line = "", cvsSplit = ",";
        int i=0, j;
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new FileReader(file));
            
            // TESTE
            if(is_test){
                // remover ultimo atributo - classifier
                ats.remove(ats.size()-1);
                for(Attribute att:ats){
                    att.values=new ArrayList<String>();
                    for(Class cl: att.classes){
                        cl.n_obs=0;
                        cl.pos=new ArrayList<Integer>();
                    }
                }
                //adicionar espaço para ID
                ats.add(0,new Attribute(""));
            }
            
            // TRAINING
            else{
                String attributes = br.readLine();
                String attribs [] = attributes.split(cvsSplit);
                for(String s: attribs){
                    ats.add(new Attribute(s));
                    i++;
                }
            }
            at_size=ats.size();
            while ((line = br.readLine()) != null) {
                String[] ob = line.split(cvsSplit);
                ob_size++;
                for(j=1;j<at_size;j++){
                    ats.get(j).values.add(ob[j]);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        ats.remove(0);
        at_size=ats.size();
        if(is_test)
            organizeTestData();
        else
            organizeData();
        

        String obs1[][]= new String[ob_size][at_size];
        for(i=0; i<ob_size; i++){
            for(j=0; j<at_size; j++){
                obs1[i][j]=ats.get(j).values.get(i);
            }
        }
        return obs1;
    }
    
    ////////////////////////////////////////////////////////////////////
    // ORGANIZAR DADOS EM CLASSES
    // CONTA NR DE CLASSES EM NOMINAIS, ORGANIZA VALORES EM INTERVALOS EM NUMERAIS
    static void organizeData(){
        int nDifVals = 0, i, j, classes=0;
        ArrayList<String> difVals;
        //System.out.println("at_size "+at_size+" ob_size "+ob_size);
        for(i=0; i<at_size; i++){
            difVals=new ArrayList<String>();
            for(String s: ats.get(i).values)
                if(!difVals.contains(s))
                    difVals.add(s);
            // VALORES NUMÈRICOS - ORGANIZAR EM INTERVALOS
            if(ats.get(i).values.get(0).matches("[0-9]+\\.?[0-9]*")){ 
                
                classes=sturgeRule();
                if(difVals.size()>classes){
                    ats.get(i).is_interval=true;
                    ats.get(i).values=makeIntervals(difVals.get(0), i, classes);
                }
            }
        }
        // ORGANIZAR ATRIBUTOS POR CLASSES
        for(i=0; i<at_size; i++){
            difVals=new ArrayList<String>();
            for(j=0; j<ob_size; j++){
                String s=ats.get(i).values.get(j);
                if(!difVals.contains(s) && !ats.get(i).is_interval){
                    difVals.add(s);
                    ats.get(i).classes.add(new Class(s,j));
                }
                else
                    ats.get(i).addObs(s,j);
            }
            /*ats.get(i).n_classes=ats.get(i).classes.size();
            System.out.println("Nome: "+ats.get(i).name+" n_classes: "+ats.get(i).n_classes);
            for(Class s: ats.get(i).classes)
                System.out.println("Classe: "+s.name+" obs: "+s.n_obs);*/
        }    
    }
    
    
    ///////////////////////////////////////////////////////////////////
    // ORGANIZAR ATRIBUTOS NUMERICOS EM INTERVALOS PRE DEFINIDOS
    static void organizeTestData(){
        int i,j,actual;
        ArrayList<String> vals=new ArrayList<String>();;
        float prev, d, st=0;
        int st1=0, prev1, d1;
        for(Attribute at: ats){
            vals=new ArrayList<String>();
            if(at.is_interval){
                if(at.is_dec){
                    for(j=0; j<at.values.size(); j++){
                        st=Float.parseFloat(at.values.get(j));
                        prev=at.dec.get(0);
                        for(i=1; i<at.dec.size(); i++){
                            d=at.dec.get(i);
                            if(i==1 && st<prev){
                                vals.add("<"+prev);
                                break;
                            }
                            else if(i==(at.dec.size()-1) && st>=d){
                                vals.add(">="+d);
                                break;
                            }
                            else if(st>=prev && st<d){
                                vals.add(prev+"-"+d);
                                break;
                            }
                             prev=d;
                        }  
                    }
                }
                else{
                    for(j=0; j<at.values.size(); j++){
                        st1=Integer.parseInt(at.values.get(j));
                        prev1=at.num.get(0);
                        for(i=1; i<at.num.size(); i++){
                            d1=at.num.get(i);
                            if(i==1 && st1<prev1){
                                vals.add("<"+prev1);
                                break;
                            }
                            else if(i==(at.num.size()-1) && st1>=d1){
                                vals.add(">="+d1);
                                break;
                            }
                            else if(st1>=prev1 && st1<d1){
                                vals.add(prev1+"-"+d1);
                                break;
                            }
                             prev1=d1;
                        }  
                    }
                }
                for(Class cl: at.classes){
                    for(i=0; i<vals.size(); i++){
                        String v=vals.get(i);
                    }        
                }
                at.values=new ArrayList<String>();
                for(String x: vals){
                    at.values.add(x);
                }
            }
        }
        for(Attribute g: ats){
            for(i=0; i<g.values.size(); i++){
                for(Class c:g.classes){
                    if(g.values.get(i).equals(c.name))
                        g.addObs(c.name,i);
                }
            }
        }
        
         /*for(i=0; i<at_size; i++){
            System.out.println("Nome: "+ats.get(i).name+" n_classes: "+ats.get(i).n_classes);
            for(Class s: ats.get(i).classes)
                System.out.println("Classe: "+s.name+" obs: "+s.n_obs);
        } */
        
    }
    
    ///////////////////////////////////////////////////////////////////
    // VALORES NUMERICOS - CRIAR INTERVALOS
    static ArrayList<String> makeIntervals(String first, int pos, int n_classes){
        boolean is_dec=false;
        int i=0, j, n_vals=ob_size;
        String s="",vals_s[]=new String[n_vals];
        ArrayList<String> temp=new ArrayList<String>();

        if(first.matches("[0-9]+\\.[0-9]+"))
            is_dec=true;
        
        // DECIMAIS
        if(is_dec){
            float interval[]=new float[n_classes];
            ats.get(pos).is_dec=true;
            float a, b, diff, last=0, actual=0;
            float min=Float.MAX_VALUE, max=Float.MIN_VALUE;
            float vals[]= new float[n_vals];
            
            for(String s1: ats.get(pos).values){
                vals[i]= Float.parseFloat(s1);
                i++;
            }
            for(i=0; i<n_vals; i++){
                if(vals[i]>max)
                    max=vals[i];
                if(vals[i]<min)
                    min=vals[i];
            }    
            a=min;
            diff=(max-min)/n_classes;
            BigDecimal bd = new BigDecimal(diff);
            bd = bd.setScale(1,
                BigDecimal.ROUND_HALF_UP);
            float new_diff=bd.floatValue();
            
            b=min+new_diff;
            
            for(j=0; j<n_classes; j++){
                //a com 1 casa decimal
                bd = new BigDecimal(a);
                bd = bd.setScale(1,
                BigDecimal.ROUND_HALF_UP);
                a=bd.floatValue();
                //b com 1 casa decimal
                bd = new BigDecimal(b);
                bd = bd.setScale(1,
                BigDecimal.ROUND_HALF_UP);
                b=bd.floatValue();
                if(j!=(n_classes-1))
                    ats.get(pos).dec.add(b);
                for(i=0; i<n_vals; i++){
                    if(j==0 && vals[i]<b)
                        vals_s[i]="<"+b;
                    if(vals[i]>=a && vals[i]<b && j>0 && j!=(n_classes-1))
                        vals_s[i]=a+"-"+b;
                    if(j==(n_classes-1) && vals[i]>=a && vals[i]<=max)
                        vals_s[i]=">="+a;
                }
                a=b;
                b+=new_diff;
            }
            
            // CRIA CLASSES
            for(i=0; i<ats.get(pos).dec.size(); i++){
                actual=ats.get(pos).dec.get(i);
                if(i==0)
                    s="<"+actual;
                else if(i==(ats.get(pos).dec.size()-1)){
                    s=last+"-"+actual;
                    ats.get(pos).classes.add(new Class(s,-1));
                    s=">="+actual;
                }
                else
                    s=last+"-"+actual;
                last=actual;
                ats.get(pos).classes.add(new Class(s,-1));
            }
        }
        
        // INTEIROS
        else{
            int a1, b1, diff1, value1, last=0, actual=0;
            int min1=Integer.MAX_VALUE, max1=Integer.MIN_VALUE;
            int vals1[]= new int[n_vals];
            for(String s1: ats.get(pos).values){
                vals1[i]= Integer.parseInt(s1);
                i++;
            }
            for(i=0; i<n_vals; i++){
                if(vals1[i]>max1)
                    max1=vals1[i];
                if(vals1[i]<min1)
                    min1=vals1[i];
            }
            a1=min1;
            diff1=(max1-min1)/n_classes;
            b1=diff1+min1;
            for(j=0; j<n_classes; j++){
                if(j!=(n_classes-1))
                    ats.get(pos).num.add(b1);
                for(i=0; i<n_vals; i++){
                    if(j==0 && vals1[i]<b1)
                        vals_s[i]="<"+b1;
                    if(vals1[i]>=a1 && vals1[i]<b1 && j>0 && j!=(n_classes-1))
                        vals_s[i]=a1+"-"+b1;
                    if(j==(n_classes-1) && vals1[i]>=a1 && vals1[i]<=max1)
                        vals_s[i]=">="+a1;
                }
                a1=b1;
                b1+=diff1;
            }
            
            // CRIA CLASSES
            for(i=0; i<ats.get(pos).num.size(); i++){
                actual=ats.get(pos).num.get(i);
                if(i==0)
                    s="<"+actual;
                else if(i==(ats.get(pos).num.size()-1)){
                    s=last+"-"+actual;
                    ats.get(pos).classes.add(new Class(s,-1));
                    s=">="+actual;
                }
                else
                    s=last+"-"+actual;
                last=actual;
                ats.get(pos).classes.add(new Class(s,-1));
            }
        } // FIM INTEIROS
        
        for(j=0; j<n_vals; j++)
            temp.add(vals_s[j]);
        return temp;
    }
    
    /////////////////////////////////////////////////////////////////
    // ESTIMA NR DE CLASSES
    static int sturgeRule(){
        double c=3.322*Math.log10(ob_size)+1;
        return (int) c;
    }
}