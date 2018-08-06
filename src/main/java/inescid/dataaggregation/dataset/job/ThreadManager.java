package inescid.dataaggregation.dataset.job;

import java.util.HashSet;

public class ThreadManager {
	HashSet<Thread> threads=new HashSet<>();
	
	public void addThreads(Thread... threadsToAdd) {
		for(Thread t: threadsToAdd)
			threads.add(t);
	}
	
	public void removeThreads(Thread... threadsToRemove) {
		for(Thread t: threadsToRemove)
			threads.remove(t);
	}
	
	public void waitForFinish(boolean forceFinishOnDeadlock) {		
		boolean running=true;
		while(running) {
			running=false;
			if(!running)
				for(Thread t: threads)
					if(t.isAlive()) {
						running=true;
						break;
					}
		}
	}

	public void waitForFinishOfThreads() {		
		boolean running=true;
		while(running) {
			running=false;
			for(Thread t: threads)
				if(t.isAlive()) {
					running=true;
					break;
				}
		}
	}
	
	public boolean isAlive() {
		for(Thread t: threads)
			if(t.isAlive())
				return true;
		return false;
	}
	
	public String printState() {
		StringBuilder sb=new StringBuilder();
		sb.append("Threads:\n");
		for(Thread t: threads)
			sb.append(" - ").append(t.getState().name()).append("\n");
		return sb.toString();
	}

	public void startThreads() {
		for(Thread t: threads)
			t.start();
	}

	public void interruptThreads() {
		for(Thread t: threads)
			t.interrupt();
	}
}
