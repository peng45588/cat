package cat.login;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BEncoderStream;


import cat.client.CatChatroom;
import cat.function.CatBean;
import cat.function.ClientBean;
import cat.util.CatUtil;

public class CatLogin extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	public static HashMap<String, ClientBean> onlines;
	JButton btnLogin;
	static Socket client;
	public static ObjectInputStream ois;
	public static ObjectOutputStream oos;
	final JLabel lblNewLabel;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// 启动登陆界面
					CatLogin frame = new CatLogin();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CatLogin() {
		setTitle("Landing cat chat room\n");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(350, 250, 450, 300);
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
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(128, 153, 104, 21);
		textField.setOpaque(false);
		contentPane.add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setForeground(Color.BLACK);
		passwordField.setEchoChar('*');//输入密码 提示字符
		passwordField.setOpaque(false);
		passwordField.setBounds(128, 189, 104, 21);
		contentPane.add(passwordField);
		//登陆按钮
		btnLogin = new JButton();
		btnLogin.setIcon(new ImageIcon("images\\\u767B\u9646.jpg"));
		btnLogin.setBounds(246, 227, 50, 25);
		getRootPane().setDefaultButton(btnLogin);
		contentPane.add(btnLogin);
		//注册按钮
		final JButton btnRegister = new JButton();
		btnRegister.setIcon(new ImageIcon("images\\\u6CE8\u518C.jpg"));
		btnRegister.setBounds(317, 227, 50, 25);
		contentPane.add(btnRegister);

		// 提示信息
		lblNewLabel = new JLabel();
		lblNewLabel.setBounds(60, 220, 151, 21);
		lblNewLabel.setForeground(Color.red);
		getContentPane().add(lblNewLabel);
		try {
			client = new Socket("localhost", 8532);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			errorTip("The connection with the server is interrupted, please login again");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			errorTip("The connection with the server is interrupted, please login again");
		}
		// 监听登陆按钮
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 记录上线客户的信息在catbean中，并发送给服务器
				try {
					lblNewLabel.setText("");
					oos = new ObjectOutputStream(client.getOutputStream());
					CatBean bean = new CatBean();
					bean.setType(5);//5为登陆
					bean.setName(textField.getText());
					bean.setPassword(new String(passwordField.getPassword()));
					HashSet set = new HashSet();
					set.add(textField.getText());
					bean.setClients(set);
					bean.setTimer(CatUtil.getTimer());
					oos.writeObject(bean);
					oos.flush();
					// 启动客户接收线程
					new ClientLoginThread().start();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					errorTip("The connection with the server is interrupted, please login again");
				}
			}
		});

		//注册按钮监听
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnRegister.setEnabled(false);
				CatResign frame = new CatResign(client);
				frame.setVisible(true);// 显示注册界面
				setVisible(false);// 隐藏掉登陆界面
			}
		});
	}

	protected void errorTip(String str) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(contentPane, str, "Error Message",
				JOptionPane.ERROR_MESSAGE);
		textField.setText("");
		passwordField.setText("");
		textField.requestFocus();
	}
	

	
	class ClientLoginThread extends Thread {
		
		public void run() {
			try {
				// 不停的从服务器接收信息
				boolean snow = true;
				while (snow) {
					ois = new ObjectInputStream(client.getInputStream());
					final CatBean  bean = (CatBean) ois.readObject();
					switch (bean.getType()) {
					case 5:{
						if (bean.getInfo().equals("登陆成功")) {
							String u_name = textField.getText();
							btnLogin.setEnabled(false);
							CatChatroom frame = new CatChatroom(u_name,
									client);
							frame.setVisible(true);// 显示聊天界面
							setVisible(false);// 隐藏掉登陆界面
						}else if(bean.getInfo().equals("您输入的密码有误！")){
							lblNewLabel.setText("您输入的密码有误！");
						}else if (bean.getInfo().equals("该用户不存在")) {
							lblNewLabel.setText("该用户不存在！");
						}
						
						break;
					}
						

					}
					snow = false;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
	}
}