package gy2023;

import javafx.util.Pair;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;


import javax.swing.*;

public class Client extends JFrame {

	private static final int boxWidth = 280;
	private static final int boxHeight = 340;
	private int row = 0;
	private int col = 0;
	private int picWidth = 15;
	private int picHeight = 15;
	private int timeDown = 0;
	private int sumTime = 1000;
	private int mineSum = 40;
	private int mineRemained;
	private int otherSum;
	private paintMinePanel mp;
	private String gameJudge = "start";
	private boolean firstClickJudge = true;
	private String name;
	private int id;
	private JMenuBar mB;
	private JMenu m;
	private JMenuItem nB;
	private JMenuItem oB;
	private JMenuItem sB;
	private JMenuItem eB;
	private JMenuItem rB;
	private ArrayList<PaintMine> paintMineList = new ArrayList<PaintMine>();
	private maintainThread mtT;
	private Socket socket;
    private customMouseListener ctM;
    private boolean winJudge = false;
	private static final long serialVersionUID = 1L;

	public Client() {
		makeMenu();
		setTitle("Minesweeper");
		setSize(boxWidth, boxHeight);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		setVisible(true);
		initState();
		mp = new paintMinePanel();
		add(mp);
		ctM = new customMouseListener(this);
		mp.addMouseListener(ctM);
		mtT = new maintainThread();
		mtT.start();
		try {
           socket = new Socket("localhost", 8000);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null ,e.getMessage());
			System.exit(0);
		}
	}

	public Client(ArrayList<Object> status){
		makeMenu();
		setTitle("Minesweeper");
		setSize(boxWidth, boxHeight);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		setVisible(true);
		initState((ArrayList<PaintMine>) status.get(7));
		mp = new paintMinePanel();
		add(mp);
		timeDown = (int)status.get(1);
		sumTime = (int) status.get(2);
		mineRemained = (int) status.get(3);
		otherSum = (int) status.get(4);
		gameJudge = (String) status.get(5);
		firstClickJudge = (boolean) status.get(6);
		ctM = new customMouseListener(this);
		mp.addMouseListener(ctM);
		mtT = new maintainThread();
		mtT.start();
		try {
			socket = new Socket("localhost", 8000);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null ,e.getMessage());
			System.exit(0);
		}
	}

	private void makeMenu() {
		m =new JMenu("File");
		mB =new JMenuBar();
		nB =new JMenuItem("New");
		oB =new JMenuItem("Open");
		sB =new JMenuItem("Save");
		rB = new JMenuItem("Top5");
		eB =new JMenuItem("Exit");
		nB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				dispose();
				new Client();
			}
		});

		oB.addActionListener(e->{
			try {
				mtT.sleepFlag = true;
				OpenBox od = new OpenBox();
				od.setVisible(true);
				if(od.id.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Archive Number can not be empty !");
				}
				else{
					socket.sendUrgentData(0xFF);
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
					ArrayList<Object> statusForSend = new ArrayList<>();
					int num = Integer.parseInt(od.id.getText());
					statusForSend.add(num);
					toServer.writeObject(statusForSend);
					toServer.flush();
					ArrayList<Object> status = new ArrayList<>();
					Object objectFromServer = fromServer.readObject();
					if(objectFromServer instanceof String){
						JOptionPane.showMessageDialog(null, "Archive Number does not exist !");
					}
					else{
						status = (ArrayList<Object>) objectFromServer;
						dispose();
						new Client(status);
					}

				}
			mtT.interrupt();
			} catch (IOException | ClassNotFoundException ioException) {
				ioException.printStackTrace();
			} catch (NumberFormatException e1){
				JOptionPane.showMessageDialog(null, "Wrong number format!");
				System.exit(1);
			}

		});

		sB.addActionListener(e-> {
			try {
				if (this.gameJudge.equals("lose") || this.gameJudge.equals("win")){

					JOptionPane.showMessageDialog(null, "This game state cannot be saved !");
				}
				else{
					socket.sendUrgentData(0xFF);
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
					mtT.sleepFlag = true;
					ArrayList<Object> status = new ArrayList<>();
					status.add("default");
					status.add(timeDown);
					status.add(sumTime);
					status.add(mineRemained);
					status.add(otherSum);
					status.add(gameJudge);
					status.add(firstClickJudge);
					status.add(paintMineList);
					toServer.writeObject(status);
					toServer.flush();
					Integer id = (Integer) fromServer.readObject();
					this.id = id;
					JOptionPane.showMessageDialog(null, "Save Completed, please remember your \n Archive Number: " + id);
					mtT.interrupt();
				}

			} catch (IOException | ClassNotFoundException exception){
				JOptionPane.showMessageDialog(null, "Connection Error! Please make sure" +
						"server is open");
			}
		});

		eB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});

		rB.addActionListener(e->{
			topBox rd = new topBox();
			try {
				socket.sendUrgentData(0xFF);
				ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
				mtT.sleepFlag = true;
				ArrayList<Object> tempForSend = new ArrayList<>();
				tempForSend.add("Show Top 5");
				toServer.writeObject(tempForSend);
				ArrayList<Object> realResList = (ArrayList<Object>)fromServer.readObject();
				Pair<String, Integer> pair = null;
				List<Pair<String,Integer>> resList = Collections.synchronizedList(new ArrayList<>());

				for(Object o : realResList){
					ArrayList<Object> ele = (ArrayList<Object>) o;
					pair = new Pair<String,Integer>((String) ele.get(0), (Integer) ele.get(1));

					if(resList.size() == 0){
						resList.add(pair);
					}
					else{
						boolean insertFlag = false;
						for(int i=0; i<resList.size(); i++){
							if(resList.get(i).getValue() <= (Integer)ele.get(1)){
								Integer index = resList.indexOf(resList.get(i));
								resList.add(index, pair);
								insertFlag = true;
								break;
							}
						}
						if(insertFlag == false){
							resList.add(pair);
						}
					}
				}
				rd.ta.append("Name\tScore\n");
				if(resList.size()<5){
					for(int i = 0; i < resList.size(); i++){
						Pair<String, Integer> r = resList.get(i);
						rd.ta.append(r.getKey().toString() + "\t"+(Integer.parseInt(r.getValue().toString()))+"\n");
					}
				}
				else{
					for(int i=0; i<5; i++){
						Pair<String, Integer> r = resList.get(i);
						rd.ta.append(r.getKey().toString() + "\t"+(Integer.parseInt(r.getValue().toString()))+"\n");
					}
				}
				rd.setVisible(true);
			} catch (IOException | ClassNotFoundException ioException) {
				JOptionPane.showMessageDialog(null ,ioException.getMessage());
				System.exit(1);
			}

		});

		m.add(nB);
		m.add(oB);
		m.add(sB);
		m.add(rB);
		m.add(eB);
		mB.add(m);
		setJMenuBar(mB);
	}

	public class OpenBox extends JDialog{
		private JLabel notify;
		private JTextField id;
		private JButton confirm;
		private JPanel topPanel;
		private JPanel centerPanel;
		private JPanel downPanel;
		public OpenBox() {
			notify = new JLabel("Please enter the Archive Number: ");
			id = new JTextField(5);
			confirm = new JButton("OK");
			topPanel = new JPanel();
			centerPanel = new JPanel();
			downPanel = new JPanel();
			//Can only enter number in textfield
			id.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
					int keyChar=e.getKeyChar();
					if (keyChar>=KeyEvent.VK_0 && keyChar<=KeyEvent.VK_9) {
					} else {
						e.consume();
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {

				}

				@Override
				public void keyReleased(KeyEvent e) {

				}
			});
			confirm.addActionListener(e->{
				//JOptionPane.showMessageDialog(this, "Id Confirm");
				this.dispose();
			});

			topPanel.add(notify);
			centerPanel.add(id);
			downPanel.add(confirm);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(centerPanel, BorderLayout.CENTER);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(300, 100);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(null);
		}

	}

	public class SaveWinnerBox extends JDialog{
		private JLabel notify;
		private JTextField name;
		private JButton confirm;
		private JPanel topPanel;
		private JPanel centerPanel;
		private JPanel downPanel;
		public SaveWinnerBox() {
			notify = new JLabel("Please enter the name of the winner: ");
			name = new JTextField(10);
			confirm = new JButton("OK");
			topPanel = new JPanel();
			centerPanel = new JPanel();
			downPanel = new JPanel();
			confirm.addActionListener(e->{
				this.dispose();
			});

			topPanel.add(notify);
			centerPanel.add(name);
			downPanel.add(confirm);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(centerPanel, BorderLayout.CENTER);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(255, 100);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(null);
		}
	}

	public class topBox extends JDialog{
		private JLabel notify;
		private JTextArea ta;
		private JButton ok;
		private JPanel topPanel;
		private JPanel centerPanel;
		private JPanel downPanel;
		public topBox() {
			notify = new JLabel("Top 5");
			notify.setFont(new java.awt.Font("Arial", Font.BOLD, 20));
			ok = new JButton("Ok");
			ta = new JTextArea(8, 15);
			ta.setEditable(false);
			topPanel = new JPanel();
			centerPanel = new JPanel();
			downPanel = new JPanel();

			ok.addActionListener(e->{
				Client.this.mtT.interrupt();
				this.dispose();
			});

			topPanel.add(notify);
			centerPanel.add(ta);
			downPanel.add(ok);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(centerPanel, BorderLayout.CENTER);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(230, 200);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(null);
		}

	}

	public boolean isFirstClick() {
		return firstClickJudge;
	}

	public void setFirstClick(boolean firstClick) {
		this.firstClickJudge = firstClick;
	}

	public String getGameState() {
		return gameJudge;
	}

	public void setGameState(String gameState) {
		this.gameJudge = gameState;
	}

	public int getRowNum() {
		return row;
	}

	public int getColNum() {
		return col;
	}

	public int getMineNum() {
		return mineSum;
	}

	public int getRestMine(){
		return mineRemained;
	}

	public static byte[] takeByte(Object o){
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(o);
			objectOutputStream.flush();
			byte[] buffer = byteArrayOutputStream.toByteArray();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ArrayList<Object> readByte(byte[] data) {
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			ArrayList<Object> status = (ArrayList<Object>) objectInputStream.readObject();
			return status;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<PaintMine> getBombList() {
		return paintMineList;
	}

	private void initState() {
		for (int i = picWidth; i < this.getWidth() - 2 * picWidth; i += picWidth) {
			for (int j = picWidth; j < this.getHeight() - 6 * picWidth; j += picHeight) {
				row = row > i / picWidth ? row : i / picWidth;
				col = col > j / picWidth ? col : j / picWidth;
				PaintMine paintMine = new PaintMine(i, j, 13, this);
				paintMineList.add(paintMine);
			}
		}
	}

	private void initState(ArrayList<PaintMine> paintMineList) {
		for (int i = picWidth; i < this.getWidth() - 2 * picWidth; i += picWidth) {
			for (int j = picWidth; j < this.getHeight() - 6 * picWidth; j += picHeight) {
				row = row > i / picWidth ? row : i / picWidth;
				col = col > j / picWidth ? col : j / picWidth;
				this.paintMineList = paintMineList;
			}
		}
	}

	public class paintMinePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g) {
			super.paintComponent(g);
			mineRemained = mineSum;
			otherSum =0;
			for (PaintMine paintMine : paintMineList) {
				paintMine.customPaint(g);
				if(paintMine.getCoverNum()==11 && mineRemained > 0)
					mineRemained--;
				if(paintMine.getCoverNum()>=0&& paintMine.getCoverNum()<=8)
					otherSum++;
			}

			if (gameJudge.equals("lose")) {
				for (PaintMine paintMine : paintMineList) {
					if (paintMine.getUnCoverNum() == 9) {
						if (paintMine.getCoverNum() == 11){
							paintMine.setCoverNum(11);
						}else {
							paintMine.setCoverNum(paintMine.getUnCoverNum());
							Font font = new Font("Arial", Font.BOLD, 45);
							g.setFont(font);
							g.setColor(Color.red);
							g.drawString("Game Over", 12, this.getHeight() / 2);
						}
					}
					if (paintMine.getCoverNum() == 11 && paintMine.getUnCoverNum() != 9){
						paintMine.setCoverNum(12);
					}
				}
			}

			drawTime(g);

			if(!gameJudge.equals("lose")&& otherSum + mineSum == col * row)
			{
				gameJudge ="win";
				Font font = new Font("Arial", Font.BOLD, 50);
				g.setFont(font);
				g.setColor(Color.blue);
				g.drawString("Game Win", 12, this.getHeight() / 2);
				mtT.exit = true;
				Client.this.winJudge = true;
			}
		}

		private void drawTime(Graphics g) {
			Font font = new Font("Arial", Font.BOLD, 15);
			g.setFont(font);
			g.setColor(Color.magenta);
			g.drawString(String.valueOf(mineRemained), 5, this.getHeight());
			g.drawString("Time Remaining: " + sumTime,56,12);
		}
	}

	public class maintainThread extends Thread {
		private volatile boolean exit = false;
		private volatile boolean sleepFlag = false;
		public boolean getExit()
		{
			return this.exit;
		}
		public void setExit(boolean exit){
			this.exit = exit;
		}
		public void run() {
			while (!exit) {
				repaint();
				if (gameJudge.equals("start")) {
					if(!firstClickJudge){
						timeDown +=100;
						if(timeDown ==1000){
							timeDown =0;
							sumTime--;
							if (sumTime == 0){
								setGameState("lose");
								repaint();
							}
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (sleepFlag){
					try {
						Thread.sleep(8000000);
					} catch (InterruptedException e) {
						sleepFlag = false;
					}
				}
				if (winJudge){
					SaveWinnerBox sm = new SaveWinnerBox();
					sm.setVisible(true);
					try {
						socket.sendUrgentData(0xFF);
						ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
						ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
						ArrayList<Object> tempForSend = new ArrayList<>();
						if (sm.name.getText().trim().equals("")){
							tempForSend.add("Default");
						}
						else{
							tempForSend.add(sm.name.getText());
						}
						tempForSend.add(Client.this.sumTime);
						toServer.writeObject(tempForSend);
						toServer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	public class customMouseListener extends MouseAdapter {
		private Client C;
		private int col;
		private int row;
		private boolean isFirstClick;
		private ArrayList<PaintMine> paintMineList = new ArrayList<PaintMine>();
		boolean[] bb;
		public customMouseListener() {
			super();
			// TODO Auto-generated constructor stub
		}

		public customMouseListener(Client C) {
			super();
			this.C = C;
			col = C.getColNum();
			row = C.getRowNum();
			bb = new boolean[col * row];
			paintMineList = C.getBombList();
			this.isFirstClick= C.isFirstClick();
		}


		public void mouseReleased(MouseEvent e) {
			if (C.getGameState().equals("lose")) {
				return;
			}
			int x = e.getX();
			int y = e.getY();
			Rectangle rec = new Rectangle(x, y, 1, 1);
			if (e.getButton() == MouseEvent.BUTTON1) {
				for (PaintMine paintMine : paintMineList) {
					if (paintMine.getCoverNum() != 11){
						if (rec.intersects(paintMine.getRectangle())) {
							if (paintMine.getUnCoverNum() == 9) {
								C.setGameState("lose");
							} else {
								if (paintMine.getUnCoverNum() == 0) {
									autoPaint(paintMineList.indexOf(paintMine));
								}
								paintMine.setCoverNum(paintMine.getUnCoverNum());
							}

						}
					}
				}
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				for (PaintMine paintMine : paintMineList) {
					if (rec.intersects(paintMine.getRectangle())) {
						if(paintMine.getCoverNum()!= paintMine.getUnCoverNum()){
							if(C.getRestMine() > 0){
								if(paintMine.getCoverNum()==13){
									paintMine.setCoverNum(11);
								}
								else if(paintMine.getCoverNum()==11){
									paintMine.setCoverNum(13);
								}
							}
							else{
								if(paintMine.getCoverNum() == 11){
									paintMine.setCoverNum(13);
								}
							}

						}
					}
				}
			}
		}

		private void autoPaint(int index) {
			if (bb[index])
				return;
			bb[index] = true;
			boolean eU = false, eD = false;
			if ((index + 1) % (col) != 0)
				eU = true;
			if (index % (col) != 0)
				eD = true;
			if (borderJudge(index - 1) && eD) {
				PaintMine paintMine = paintMineList.get(index - 1);
				setBb(paintMine, index - 1);
			}

			if (borderJudge(index + 1) && eU) {
				PaintMine paintMine = paintMineList.get(index + 1);
				setBb(paintMine, index + 1);
			}

			if (borderJudge(index - col)) {
				PaintMine paintMine = paintMineList.get(index - col);
				setBb(paintMine, index - col);
			}

			if (borderJudge(index + col)) {
				PaintMine paintMine = paintMineList.get(index + col);
				setBb(paintMine, index + col);
			}

			if (borderJudge(index - col + 1) && eU) {
				PaintMine paintMine = paintMineList.get(index - col + 1);
				setBb(paintMine, index - col + 1);
			}

			if (borderJudge(index - col - 1) && eD) {
				PaintMine paintMine = paintMineList.get(index - col - 1);
				setBb(paintMine, index - col - 1);
			}

			if (borderJudge(index + col + 1) && eU) {
				PaintMine paintMine = paintMineList.get(index + col + 1);
				setBb(paintMine, index + col + 1);
			}

			if (borderJudge(index + col - 1) && eD) {
				PaintMine paintMine = paintMineList.get(index + col - 1);
				setBb(paintMine, index + col - 1);
			}

		}

		//判断边界
		private boolean borderJudge(int i) {
			if (i >= 0 && i < paintMineList.size())
				return true;
			return false;
		}

		//显示某位置
		public void setBb(PaintMine paintMine, int index) {
			if (paintMine.getCoverNum() == paintMine.getUnCoverNum() && paintMine.getCoverNum() != 0)
				return;
			if (paintMine.getUnCoverNum() >= 0 && paintMine.getUnCoverNum() <= 8 && paintMine.getUnCoverNum() != 9) {
				paintMine.setCoverNum(paintMine.getUnCoverNum());
				if (paintMine.getCoverNum() == 0)
					autoPaint(index);
			} else {
				autoPaint(index);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (C.getGameState().equals("lose")) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
				if (isFirstClick) {
					isFirstClick = false;
					C.setFirstClick(false);
					initBomb(e);
					checkBomb();
				}
			}
		}

		private void checkBomb() {

			for (PaintMine paintMine : paintMineList) {
				int x = paintMineList.indexOf(paintMine);

				boolean eU = false, eD = false;
				if ((x + 1) % (col) != 0)
					eU = true;
				if (x % (col) != 0)
					eD = true;
				if (paintMine.getUnCoverNum() != 9) {
					if (judgeNow(x - 1) && eD)
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x + 1) && eU)
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x - col))
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x + col))
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x - col + 1) && eU)
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x - col - 1) && eD)
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x + col + 1) && eU)
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
					if (judgeNow(x + col - 1) && eD)
						paintMine.setUnCoverNum(paintMine.getUnCoverNum() + 1);
				}
			}
		}

		//判断某位置是否是地雷
		private boolean judgeNow(int x) {
			if (x >= 0 && x < paintMineList.size()) {
				if (paintMineList.get(x).getUnCoverNum() == 9)
					return true;
			}
			return false;
		}

		private void initBomb(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			Rectangle rec = new Rectangle(x, y, 1, 1);
			PaintMine paintMineTemp =new PaintMine();
			int what=0;
			for (PaintMine paintMine : paintMineList) {
				if(rec.intersects(paintMine.getRectangle())){
					what= paintMine.getUnCoverNum();
					paintMineTemp = paintMine;
					paintMine.setUnCoverNum(9);
					break;
				}
			}
			Random r = new Random();
			for (int i = 0; i < C.getMineNum(); i++) {
				while (true) {
					int index = r.nextInt(paintMineList.size());
					if (paintMineList.get(index).getUnCoverNum() != 9) {
						paintMineList.get(index).setUnCoverNum(9);
						break;
					}
				}
			}
			paintMineTemp.setUnCoverNum(what);
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.setVisible(true);
	}
}
