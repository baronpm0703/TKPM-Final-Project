//package com.psvm.client.views;
//
//import com.psvm.client.views.components.FriendList;
//import com.psvm.client.views.components.TestAddButton;
//import com.psvm.client.views.components.TestPane;
//import com.psvm.client.views.components.TestRemoveButton;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//import java.util.Random;
//import java.util.Vector;
//
//
//
//public class GUITest {
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				JFrame jFrame = new JFrame("Test application");
//				jFrame.setSize(1280, 720);
//				jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//				JPanel jPanel = new JPanel();
//				jPanel.setLayout(new GridLayout());
//				jPanel.add(new FriendList());
//				jPanel.add(new TestAddButton());
//				jPanel.add(new TestRemoveButton());
//				jPanel.add(new TestPane());
//				jFrame.add(jPanel);
//
//				jFrame.pack();
//				jFrame.setLocationRelativeTo(null);
//				jFrame.setVisible(true);
//			}
//		});
//	}
//}
