package story;

import java.io.Serializable;

public class Thing extends Creature  implements Serializable {

    public Thing(){
        super();
    }
    public Thing(String n){
        super(n);
    }
    public Thing(int x, int y){
        super(x, y);
    }
    public Thing(String n,  int x, int y){
        super(n, x, y);
    }
}
