package gy2023;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.Serializable;

public class PaintMine implements Serializable {

	public static final Image bomb_0 = Toolkit.getDefaultToolkit().getImage("Resource/0.png");
	public static final Image bomb_1 = Toolkit.getDefaultToolkit().getImage("Resource/1.png");
	public static final Image bomb_2 = Toolkit.getDefaultToolkit().getImage("Resource/2.png");
	public static final Image bomb_3 = Toolkit.getDefaultToolkit().getImage("Resource/3.png");
	public static final Image bomb_4 = Toolkit.getDefaultToolkit().getImage("Resource/4.png");
	public static final Image bomb_5 = Toolkit.getDefaultToolkit().getImage("Resource/5.png");
	public static final Image bomb_6 = Toolkit.getDefaultToolkit().getImage("Resource/6.png");
	public static final Image bomb_7 = Toolkit.getDefaultToolkit().getImage("Resource/7.png");
	public static final Image bomb_8 = Toolkit.getDefaultToolkit().getImage("Resource/8.png");
	public static final Image bomb = Toolkit.getDefaultToolkit().getImage("Resource/9.png");
	public static final Image bombX = Toolkit.getDefaultToolkit().getImage("Resource/9.png");
	public static final Image block = Toolkit.getDefaultToolkit().getImage("Resource/10.png");
	public static final Image flag = Toolkit.getDefaultToolkit().getImage("Resource/11.png");
	public static final Image flagX = Toolkit.getDefaultToolkit().getImage("Resource/12.png");
	private int x_coordinate;
	private int y_coordinate;
	private int coverNum;
	private int unCoverNum = 0;
	private static final long serialVersionUID = 1L;
	private int cover = 15;
	private int unCover = 15;
	private transient Client C;

	public PaintMine() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PaintMine(int x_coordinate, int y_coordinate, int coverNum, Client C) {
		super();
		this.x_coordinate = x_coordinate;
		this.y_coordinate = y_coordinate;
		this.coverNum = coverNum;
		this.C = C;
	}

	public int getX_coordinate() {
		return x_coordinate;
	}

	public void setX_coordinate(int x_coordinate) {
		this.x_coordinate = x_coordinate;
	}

	public int getY_coordinate() {
		return y_coordinate;
	}

	public void setY_coordinate(int y_coordinate) {
		this.y_coordinate = y_coordinate;
	}

	public int getCoverNum() {
		return coverNum;
	}

	public void setCoverNum(int coverNum) {
		this.coverNum = coverNum;
	}

	public int getUnCoverNum() {
		return unCoverNum;
	}

	public void setUnCoverNum(int unCoverNum) {
		this.unCoverNum = unCoverNum;
	}

	public void customPaint(Graphics g) {
		switch (coverNum) {
		case 0:
			g.drawImage(bomb_0, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 1:
			g.drawImage(bomb_1, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 2:
			g.drawImage(bomb_2, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 3:
			g.drawImage(bomb_3, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 4:
			g.drawImage(bomb_4, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 5:
			g.drawImage(bomb_5, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 6:
			g.drawImage(bomb_6, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 7:
			g.drawImage(bomb_7, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 8:
			g.drawImage(bomb_8, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 9:
			g.drawImage(bomb, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 10:
			g.drawImage(bombX, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 11:
			g.drawImage(flag, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 12:
			g.drawImage(flagX, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		case 13:
			g.drawImage(block, x_coordinate, y_coordinate, cover, unCover, C);
			break;
		}
	}
	
	public Rectangle getRectangle() {
		return new Rectangle(x_coordinate, y_coordinate, cover, unCover);
	}
}
