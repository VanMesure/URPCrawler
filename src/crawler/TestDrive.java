package crawler;

import java.util.Scanner;

public class TestDrive {
	
	
	public static void main(String[] args) {
		int userName = 201601000;
		int passwd = 201601000;
		int i = 0;
		for(i = 0; i <= 200; i ++) {
			Crawler c = new Crawler();
			c.setUser(Integer.toString(userName + i), Integer.toString(passwd + i));
			c.getHeadShot();
			System.out.println(userName + i);
		}
	}
	
	
	private static void getch() {
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
		
	}
}
