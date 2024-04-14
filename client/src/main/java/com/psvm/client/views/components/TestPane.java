package com.psvm.client.views.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestPane extends JPanel {

	private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

	private JLabel label;

	public TestPane() {
		setBorder(new EmptyBorder(16, 16, 16, 16));
		label = new JLabel("Nothing happening here, just waiting for stuff");
		setLayout(new GridBagLayout());
		add(label);

		startNextWorker();
	}

	protected void startNextWorker() {
		ExecutorWorker worker = new ExecutorWorker(new ExecutorWorker.Observer() {
			@Override
			public void workerDidUpdate(String message) {
				label.setText(message);
			}
		});
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (worker.getState() == SwingWorker.StateValue.DONE) {
					worker.removePropertyChangeListener(this);
					startNextWorker();
				}
			}
		});
		service.schedule(worker, 1500, TimeUnit.MILLISECONDS);
	}

}

class ExecutorWorker extends SwingWorker<Void, String> {

	public interface Observer {
		public void workerDidUpdate(String message);
	}

	private Random rnd = new Random();
	private Observer observer;

	public ExecutorWorker(Observer observer) {
		this.observer = observer;
	}

	@Override
	protected Void doInBackground() throws Exception {
		publish(String.valueOf(rnd.nextInt(1500)));
		return null;
	}

	@Override
	protected void process(List<String> chunks) {
		for (String messages : chunks) {
			observer.workerDidUpdate(messages);
		}
	}

}