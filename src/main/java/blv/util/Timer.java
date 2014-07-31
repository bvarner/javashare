package blv.util;

import java.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Timer extends Thread {
	ActionListener target;
	int delay;
	
	
	public Timer(ActionListener target, int delay){
		this.target = target;
		this.delay = delay;
	}
	
	
	
	public void run(){
		while (true){
			if (delay > 0){
				try {
					sleep(delay);
					target.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Timer"));
				} catch (Exception e){
					break;
				}
			} else {
				// Will end the thread.
				break;
			}
		}
	}
}
