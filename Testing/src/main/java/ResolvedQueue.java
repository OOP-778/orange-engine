

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class ResolvedQueue extends AbstractQueue<Integer> {

    private LinkedList<Integer> elements = new LinkedList<>();

    @Override
    public Iterator<Integer> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean offer(Integer Integer) {
        if(Integer == null) return false;
        elements.add(Integer);
        return true;
    }

    @Override
    public Integer poll() {
        Iterator<Integer> iter = elements.iterator();
        Integer t = iter.next();
        if(t != null){
            iter.remove();
            return t;
        }
        return null;
    }

    @Override
    public Integer peek() {
        return elements.getFirst();
    }
}
