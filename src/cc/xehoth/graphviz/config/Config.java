package cc.xehoth.graphviz.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Config implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean isAutoPainting = false;

	private String content = null;

	private boolean isDirected = false;

	private boolean hasWeight = false;

	private boolean saveLastData = true;

	public boolean isAutoPainting() {
		return isAutoPainting;
	}

	public void setAutoPainting(boolean isAutoPainting) {
		this.isAutoPainting = isAutoPainting;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isDirected() {
		return isDirected;
	}

	public void setDirected(boolean isDirected) {
		this.isDirected = isDirected;
	}

	public boolean isHasWeight() {
		return hasWeight;
	}

	public void setHasWeight(boolean hasWeight) {
		this.hasWeight = hasWeight;
	}

	public boolean isSaveLastData() {
		return saveLastData;
	}

	public void setSaveLastData(boolean saveLastData) {
		this.saveLastData = saveLastData;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static Config getData() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.dat"));
		Config config = (Config) ois.readObject();
		ois.close();
		ois = null;
		BufferedReader br = new BufferedReader(new FileReader("config"));
		String s = br.readLine();
		if (s.trim().endsWith("true")) {
			config.isAutoPainting = true;
		} else {
			config.isAutoPainting = false;
		}
		s = br.readLine();
		if (s.trim().endsWith("true")) {
			config.saveLastData = true;
		} else {
			config.saveLastData = false;
		}
		br.close();
		br = null;
		return config;
	}

	public static void saveData(Config config) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.dat"));
		oos.writeObject(config);
		oos.flush();
		oos.close();
		oos = null;
	}

	/*
	 * public static void main(String[] args) { try { saveData(new Config()); }
	 * catch (IOException e) { e.printStackTrace(); } }
	 */
}
