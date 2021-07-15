package gsxconfig;

import javax.swing.JProgressBar;

public class JobRunner implements Progress {
	private boolean cancelled;
	private JProgressBar pb;
	
	public JobRunner(JProgressBar pb) {
		this.pb=pb;
	}
	
	public void execute(final Executable job, final Callback finished) {
		setCancelled(false);
		final Progress progress = this;
		Thread thread = new Thread() {
			public void run() {
				String msg = null;
				try {
					msg = job.execute(progress);
				} catch (Exception e) {
					msg = e.getMessage();
					if (msg==null) msg = e.toString();
					if (msg==null) msg = "Error";
					e.printStackTrace();
				}
				if (finished!=null) finished.call(msg);
				set(0d);
			}
		};
		thread.start();
	}
	
	public void set(double p) {
		if (pb==null) return; 
		pb.setValue((int)(100*p)); 
		pb.validate();
	}

	public void setCancelled(boolean b) {cancelled=b;}
	public boolean isCancelled() {return cancelled;}
	
}
