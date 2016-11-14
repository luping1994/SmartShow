package Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Looney on 2016/10/27.
 */

public class SixSensorManager {

    boolean changed = false;
    public interface Observer{
        public void update();
    }
    List<Observer> observers = new ArrayList<Observer>();

    public void addObserver(Observer observer){
        if (observer==null){
            throw new RuntimeException();
        }
        if (!observers.contains(observer)){
            observers.add(observer);
        }
    }
    public boolean hasChanged() {
        return changed;
    }
    protected void clearChanged() {
        changed = false;
    }
    public void notifyObservers(){
        int size = 0;
        Observer[] arrays = null;
        synchronized (this) {
            if (hasChanged()) {
                clearChanged();
                size = observers.size();
                arrays = new Observer[size];
                observers.toArray(arrays);
            }
        }
        if (arrays != null) {
            for (Observer observer : arrays) {
                observer.update();
            }
        }
    }
}
