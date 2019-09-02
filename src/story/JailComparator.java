package story;

import java.io.Serializable;
import java.util.Comparator;

public class JailComparator implements Comparator<Jail>, Serializable {
    @Override
    public int compare(Jail first, Jail second){
        return first.getName().compareTo(second.getName());
    }
}
