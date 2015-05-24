package cat.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;

import cat.function.CatBean;
import cat.util.CatUtil;

public class AddFriend extends JDialog {

	private JPanel contentPane;
	private static AbstractListModel listmodel;
	private static Vector onlines;
	private static JList list;
	private JTextField textField;
	public static ObjectInputStream ois;
	public static ObjectOutputStream oos;

	public AddFriend(final Socket client, final String name,
			final AbstractListModel listmodel) {
		setTitle("Add Friend\n");
		setBounds(350, 250, 450, 300);
		textField = new JTextField(10);
		textField.setBounds(220,10,200,20);
		getContentPane().add(textField);
		contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(new ImageIcon(
						"images\\\u767B\u9646\u754C\u9762.jpg").getImage(), 0,
						0, getWidth(), getHeight(), null);
			}
		};
		// 在线客户列表
		list = new JList(listmodel);
		list.setCellRenderer(new CellRenderer());
		list.setModel(listmodel);
		list.setOpaque(false);
		Border etch = BorderFactory.createEtchedBorder();
		list.setBorder(BorderFactory.createTitledBorder(etch, "在线用户<双击添加>:", 
				TitledBorder.LEADING, TitledBorder.TOP,
				new Font("sdf", Font.BOLD, 20), Color.green));

		JScrollPane scrollPane_2 = new JScrollPane(list);
		scrollPane_2.setBounds(430, 10, 245, 375);
		scrollPane_2.setOpaque(false);
		scrollPane_2.getViewport().setOpaque(false);
		getContentPane().add(scrollPane_2);
		
		
		//JTextField 监听
		textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				String s = textField.getText().trim();
				setList(s);
			}
			private void setList(String s) {
				// TODO Auto-generated method stub
				// 在线客户列表
				Vector myFriend = new Vector();
				AbstractListModel friendListModel;
				for (int i = 0; i < listmodel.getSize(); i++) {
					if (listmodel.getElementAt(i).toString().contains(s)) {
						myFriend.add(listmodel.getElementAt(i));
						
					}
				}
				friendListModel = new UUListModel(myFriend);
				list.setModel(friendListModel);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				String s = textField.getText().trim();
				setList(s);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				String s = textField.getText().trim();//trim()方法用于去掉你可能误输入的空格号
				setList(s);
			}
		}
		 );
		//双击提示加好友
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				List to = list.getSelectedValuesList();
				//判断双击
				if (e.getClickCount() == 2) {

					if (to.toString().contains(name + "(我)")) {
						JOptionPane.showMessageDialog(getContentPane(),
								"不能添加自己");
						return;
					} else {
						int addFriend = JOptionPane.showConfirmDialog(
								getContentPane(), "是否添加" + to.toString()
										+ "为好友", "提示",
								JOptionPane.YES_NO_OPTION);
						//确认要加好友  发送到服务器
						if (addFriend == 0) {
							try {
								oos = new ObjectOutputStream(client
										.getOutputStream());
								
								// 记录上线客户的信息在catbean中，并发送给服务器
								CatBean bean = new CatBean();
								bean.setType(7);//注册
								bean.setName(name);
								bean.setTimer(CatUtil.getTimer());
								HashSet<String> set = new HashSet<String>();
								// 客户昵称
								set.addAll(to);
								bean.setClients(set);
								bean.setInfo("addFriend");
								oos.writeObject(bean);
								oos.flush();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

						}
					}
				}
			}
		});

	}

}
