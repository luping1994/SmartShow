package com.suntrans.smartshow.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class MyObserver  {
	public interface Observer{
		public void update();
		public void updateState();
	}
	List<Observer> observers=new ArrayList<Observer>();

	public void addObserver(Observer observer){
		if(observer==null){
			throw new RuntimeException();
		}
		if(!observers.contains(observer)){
			observers.add(observer);
		}
	}

	public void removeObserver(Observer observer){
		if(observer==null){
			throw new RuntimeException();
		}
		if(observers.contains(observer)){
			observers.remove(observer);
		}
	}
	public void notifyObservers(){
		for(Observer observer:observers){
			observer.update();
		}
	}
	public void notifyState(){
		for(Observer observer:observers){
			observer.updateState();
		}
	}
	
}
