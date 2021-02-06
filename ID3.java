import java.io.*;
import java.util.*;

class Atributo{
    String name;
    ArrayList<String> values;
    ArrayList<String> classes;
    Atributo(String name, ArrayList<String> values , ArrayList<String> classes){
        this.name=name;
        this.classes=classes;
        this.values=values;
    }
}

class Label{
    int count;
    int label;
    Label(int label, int count){
        this.label=label;
        this.count=count;
    }
}

class Node{
    int id;
    Node parent;
    String attribute;
    String label;
    String klass;
    boolean isLeaf;
    int count;
    ArrayList<Atributo> ats;
    ArrayList<Node> children;
    Node (String attribute){
        this.children = new ArrayList<Node>();
        this.attribute=attribute;
        this.klass="";
        this.isLeaf=false;
        this.label="";
        this.count=0;
    }
    void addClass(String klass){
        this.klass=klass;
    }
    void addLabel(String label){
        this.label=label;
    }
    void addAttribute(String attribute){
        this.attribute=attribute;
    }
    void addList(ArrayList<Atributo> ats){
       ArrayList<Atributo> temp=new ArrayList<Atributo>();
        for(Atributo a: ats){
            String name=a.name;
            ArrayList<String> obs= new ArrayList<String>();
            for(int i=0; i<a.values.size(); i++){
                obs.add(a.values.get(i));
            }
            ArrayList<String> classes=a.classes;
            temp.add(new Atributo(name,obs,classes));
        }
        this.ats=temp;
    }
    void addChild(Node child) {
        child.parent = this;
        children.add(child);
    }
}

public class ID3{
    Node root;
    ArrayList<String> at_names;
    ID3 (ArrayList<Attribute> ats){
        this.at_names=new ArrayList<String>();
        ArrayList<Atributo> temp=new ArrayList<Atributo>();
        for(Attribute a: ats){
            String name=a.name;
            at_names.add(name);
            ArrayList<String> obs= a.values;
            ArrayList<String> classes=new ArrayList<String>();
            for(Class c:a.classes){
                classes.add(c.name);
            }
            temp.add(new Atributo(name,obs,classes));
        }
        int chosenAttr=nextAttribute(temp);
        String chosenAttri=temp.get(chosenAttr).name;
        root= new Node(chosenAttri);
        root.id=chosenAttr;
        root.addList(temp);
        Label l=getLabel(temp);
        String mostFrequent=root.ats.get(root.ats.size()-1).classes.get(l.label);
        root.addLabel(mostFrequent);
        algorithm(chosenAttr, root, temp);
        
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // ID3
    void algorithm(int chosen_id, Node node,  ArrayList<Atributo> a){
        if(a.size()==0 || a.get(a.size()-1).values.size()==0){
            return;
        }
        if(a.size()==1){
            String parentLabel=node.label;
            node.addLabel(node.parent.label);
            node.count=node.ats.get(0).values.size();
            if(node.count==node.parent.ats.get(0).values.size()){
                node.parent.count=node.count;
                node.parent.isLeaf=true;
            }
            node.isLeaf=true;
        }
        int targetIndex = a.size()-1;
        String chosenAttribute = a.get(chosen_id).name;
        //CRIA NOS PARA CADA UMA DAS CLASSES DO NO PARENT
        for(String c: a.get(chosen_id).classes){
            ArrayList<Atributo> temp=new ArrayList<Atributo>();
            for(Atributo at: a){
                String name=at.name;
                ArrayList<String> obs= new ArrayList<String>();
                for(int i=0; i<at.values.size(); i++){
                    obs.add(at.values.get(i));
                }
                ArrayList<String> classes=at.classes;
                temp.add(new Atributo(name,obs,classes));
            }
            temp=cleanData(chosen_id, c, temp);
            Node child=new Node(null);
            child.addClass(c);
            child.addList(temp);
            node.addChild(child);
        }
        
        for(Node child: node.children){
            if(!child.isLeaf){
                Label l=getLabel(child.ats);
                // mesmo valor para todos
                if(l.label==-1){
                    String any=child.ats.get(child.ats.size()-1).values.get(0);
                    child.addLabel(any);
                    child.count=l.count;
                    child.isLeaf=true;
                }
                // 0 exemplos, label do pai
                else if(l.label==-2){
                    String parentLabel=node.label;
                    child.addLabel(parentLabel);
                    child.count=l.count;
                    child.isLeaf=true;
                }
                // várias classes presentes, label da classe mais frequente, continua
                else{
                    String mostFrequent=child.ats.get(child.ats.size()-1).classes.get(l.label);
                    child.addLabel(mostFrequent);
                    child.ats.remove(chosen_id);
                    ArrayList<Atributo> temp2=child.ats;
                    int chosenAttr=nextAttribute(temp2);
                    String chosenAttri=temp2.get(chosenAttr).name;
                    for(int x=0; x<at_names.size(); x++){
                        String x1=at_names.get(x);
                        if(x1.equals(chosenAttri)){
                            child.id=x;
                            break;
                        }
                    }
                    child.addAttribute(chosenAttri);
                    algorithm(chosenAttr,child,child.ats);
                }
                if(child.count==child.parent.ats.get(0).values.size()){
                    child.parent.count=child.count;
                    child.parent.isLeaf=true;
                }
            }
        }
        int count=0;
        String first=node.children.get(0).label;
        for(Node child: node.children){
            if(child.isLeaf && child.label.equals(first))
                count++;
        }
        if(count==node.children.size()){
            node.isLeaf=true;
            node.count=node.ats.get(0).values.size();
            node.label=first;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // APAGA CLASSES DIFERENTES DA QUE VAI SER TESTADA
    ArrayList<Atributo> cleanData(int chosen_at, String className, ArrayList<Atributo> temp){
        int targetIndex = temp.size()-1;
        int temp_size=temp.get(chosen_at).values.size();
        int k=0;
        while(k<temp_size){
            String s= temp.get(chosen_at).values.get(k);
            if(!s.equals(className)){
                for(int m=0; m<temp.size(); m++){
                    temp.get(m).values.remove(k);
                }
            }
            else
                k++;
            temp_size=temp.get(chosen_at).values.size();
        }
        return temp;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // MEDE INFO GAIN, ESCOLHE PROXIMO NO DA ARVORE - MENOR ENTROPIA
	int nextAttribute(ArrayList<Atributo> a){
        int i, at_num, chosen=0, count=0;
        float temp_gain, max=Float.MIN_VALUE;
        for(i=0; i<a.size()-1; i++){
            temp_gain=gain(i,a);
            //System.out.println("ATTRIBUTE: "+a.get(i).name+" GANHO: "+temp_gain);
            if(temp_gain>max){
                chosen=i;
                max=temp_gain;
            }
        } 
        //System.out.println("CHOSEN: "+a.get(chosen).name);
        return chosen;
    }
    
    float gain(int index, ArrayList<Atributo> a){
        int i, j, size=0, samples=0, totalSamples;
        float entropy=0, ganho=0, temp_entropy=0;
        int targetIndex=a.size()-1;
        size=a.get(targetIndex).values.size();
        HashMap<String, HashMap<String, Integer>> matrix = new HashMap<String, HashMap<String, Integer>>();
        for(String c: a.get(index).classes){
            matrix.put(c,new HashMap<String,Integer>());
            for(String c1: a.get(targetIndex).classes)
                matrix.get(c).put(c1,0);
        }
        for(i=0; i<a.get(index).values.size(); i++){
            String cl=a.get(index).values.get(i);
            String tar=a.get(targetIndex).values.get(i);
            int temp=matrix.get(cl).get(tar)+1;
            matrix.get(cl).put(tar,temp);
        }
        // ENTROPIA
        samples=0;
        for(String c1: a.get(targetIndex).classes){
            for(i=0; i<a.get(targetIndex).values.size(); i++){
                if(c1.equals(a.get(targetIndex).values.get(i)))
                    samples++;
            }
            if(samples>0)
                entropy+=-((float)samples/(float)size) * log2(samples,size);
            samples=0;
        }
        //System.out.println("ENTROPY: "+entropy);
        
        // INFO GAIN
        totalSamples=0;
        for(String c1:a.get(index).classes){
            // tamanho da amostra para uma dada classe 
            for(String c2:a.get(targetIndex).classes){
                totalSamples+=matrix.get(c1).get(c2);
            }
            // Entropia da classe
            for(String c2:a.get(targetIndex).classes){
                samples=matrix.get(c1).get(c2);
                if(samples>0)
                    temp_entropy+=-((float)samples/(float)totalSamples) * log2(samples,totalSamples);
                //System.out.println("SAMPLES: "+samples+" TEMP_ENTROPY"+temp_entropy+" TOTALSAMPLES: "+totalSamples);
            }
            ganho-=temp_entropy*((float)totalSamples/(float)size);
            //System.out.println("CLASSE: "+c1+" GANHO: "+ganho+" SAMPLES DA CLASSE: "+totalSamples+" SIZE "+size);
            totalSamples=0;
            temp_entropy=0;
        }
          
        return entropy+ganho;
    }
    
    float log2(int sample, int size) {
        float n= (float)sample/(float)size;
        float log= (float)Math.log(n) / (float)Math.log(2);
        return log;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // DEVOLVE LABEL
    Label getLabel(ArrayList<Atributo> a){
        int i=0, most_frequent=0, count=0, targetIndex=a.size()-1, max=Integer.MIN_VALUE;
        ArrayList<String> targetClasses=a.get(targetIndex).classes;
        ArrayList<String> target=a.get(targetIndex).values;
        int size= a.get(targetIndex).values.size(), n_classes=targetClasses.size();
        // 0 OBSERVAÇOES, USA LABEL DO PAI
        if(size==0)
            return new Label(-2,0);
        // 1 OBSERVAÇAO
        if(size==1)
            return new Label(-1,1);
        // VE QUAL CLASSE TARGET TEM MAIS OBSERVAÇOES
        for(i=0; i<n_classes; i++){
            String n1=targetClasses.get(i);
            count=0;
            for(int j=0; j<size; j++){
                String n2=target.get(j);
                if(n1.equals(n2))
                count++;
            }
            if(count>max){
                most_frequent=i;
                max=count;
            }
            count=0;
        }
        // MESMO VALOR PARA TODAS AS OBSERVAÇOES
        if(max==size)
            return new Label(-1,max);
        else
            return new Label(most_frequent,max);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // IMPRIME ARVORE
    void print(){
        int count=1;
        System.out.println("<"+root.attribute+">");
        for(Node child: root.children){
            if(child.isLeaf)
                System.out.println("\t"+child.klass+" : "+child.label+" ("+child.count+")");
            else{
                System.out.println("\t"+child.klass+" : ");
                print(child,1);
                System.out.println();
            }
        }
    }
    void print(Node node,int level){
        String tab=addTab(level);
        System.out.println(tab+"<"+node.attribute+">");
        tab=addTab(level+1);
        for(Node child: node.children){
            if(child.isLeaf)
                System.out.println(tab+child.klass+" : "+child.label+" ("+child.count+")");
            else{
                System.out.println(tab+child.klass+" : ");
                print(child, (level+2));
                System.out.println();
            }
        }
    }
    
    String addTab(int level){
        String tab="";
        for(int i=0; i<level+1; i++){
            tab+="\t";
        }
        return tab;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // CLASSIFICA OBSERVAÇOES TESTE
    void classification(String[][] test){
        ArrayList<ArrayList<String>> a= new ArrayList<ArrayList<String>>();
        Node node=root;
        int i, j, count=0;
        for(i=0; i<test.length; i++){
            ArrayList<String> temp= new ArrayList<String>();
            for(j=0; j<test[0].length; j++){
                temp.add(test[i][j]);
            }
            a.add(temp);
        }
        for(ArrayList<String> z: a){
            count++;
            System.out.print("Sample "+count+": ");
            for(Node child: root.children){
                if(child.klass.equals(z.get(root.id)) && !child.isLeaf)
                    classify(child, z);
                else if(child.klass.equals(z.get(root.id)) && child.isLeaf)
                    System.out.println(child.label);
            }
        }
    }
    void classify(Node node, ArrayList<String> z){
        for(Node child: node.children){
            if(child.klass.equals(z.get(node.id)) && !child.isLeaf)
                classify(child, z);
            else if(child.klass.equals(z.get(node.id)) && child.isLeaf){
                System.out.println(child.label);
                return;
            }
        }
    }
}