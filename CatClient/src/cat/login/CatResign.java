package cat.login;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
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

import cat.client.CatChatroom;
import cat.function.CatBean;
import cat.login.CatLogin.ClientLoginThread;
import cat.util.CatUtil;
import cat.login.CatLogin;

public class CatResign extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	private JLabel lblNewLabel;
	private Socket client;
	final JButton btnNewButton_1;

	public CatResign(final Socket client) {
		this.client = client;
		setTitle("Registered cat chat room\n");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(350, 250, 450, 300);
		contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(new ImageIcon(
						"images\\\u6CE8\u518C\u754C\u9762.jpg").getImage(), 0,
						0, getWidth(), getHeight(), null);
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(150, 42, 104, 21);
		textField.setOpaque(false);
		contentPane.add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setEchoChar('*');
		passwordField.setOpaque(false);
		passwordField.setBounds(190, 98, 104, 21);
		contentPane.add(passwordField);

		passwordField_1 = new JPasswordField();
		passwordField_1.setBounds(192, 152, 104, 21);
		passwordField_1.setOpaque(false);
		contentPane.add(passwordField_1);

		// 注册按钮
		btnNewButton_1 = new JButton();
		btnNewButton_1.setIcon(new ImageIcon("images\\注册1.jpg"));
		btnNewButton_1.setBounds(320, 198, 80, 40);
		getRootPane().setDefaultButton(btnNewButton_1);
		contentPane.add(btnNewButton_1);

		// 返回按钮
		final JButton btnNewButton = new JButton("");
		btnNewButton.setIcon(new ImageIcon("images\\返回.jpg"));
		btnNewButton.setBounds(230, 198, 80, 40);
		contentPane.add(btnNewButton);

		// 提示信息
		lblNewLabel = new JLabel();
		lblNewLabel.setBounds(55, 218, 185, 20);
		lblNewLabel.setForeground(Color.red);
		contentPane.add(lblNewLabel);

		// 返回按钮监听
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setEnabled(false);
				// 返回登陆界面
				CatLogin frame = new CatLogin();
				frame.setVisible(true);
				setVisible(false);
			}
		});

		// 注册按钮监听
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					lblNewLabel.setText("");
					String u_name = textField.getText();
					String u_pwd = new String(passwordField.getPassword());
					String u_pwd_ag = new String(passwordField_1.getPassword());
					if (u_name.length() != 0) {
						if (u_pwd.equals(u_pwd_ag)) {
							if (u_pwd.length() != 0) {

								// 发送到服务端
								CatLogin.client = new Socket("localhost", 8523);
								CatLogin.oos = new ObjectOutputStream(client
										.getOutputStream());

								CatBean bean = new CatBean();
								bean.setType(6);// 6为注册
								bean.setName(textField.getText());
								bean.setPassword(u_pwd);
								HashSet set = new HashSet();
								set.add(textField.getText());
								bean.setClients(set);
								bean.setTimer(CatUtil.getTimer());
								CatLogin.oos.writeObject(bean);
								System.out.println("client");
								CatLogin.oos.flush();
								// 启动客户接收线程
								new ClientLoginThread().start();

							} else {
								lblNewLabel.setText("密码为空！");
							}
						} else {
							lblNewLabel.setText("密码不一致！");
						}
					} else {
						lblNewLabel.setText("用户名不能为空！");
					}
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					JOptionPane
							.showMessageDialog(
									contentPane,
									"The connection with the server is interrupted, please login again",
									"Error Message", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					JOptionPane
							.showMessageDialog(
									contentPane,
									"The connection with the server is interrupted, please login again",
									"Error Message", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
	}

	public static ObjectInputStream ois;
	public static ObjectOutputStream oos;

	class ClientLoginThread extends Thread {

		public void run() {
			try {
				// 不停的从服务器接收信息直至收到
				boolean snow = true;
				while (snow) {
					ois = new ObjectInputStream(client.getInputStream());
					final CatBean bean = (CatBean) ois.readObject();
					switch (bean.getType()) {
					case 6: {
						// TODO 判断是否注册成功
						System.out.println("snow::" + bean.getInfo());
						if (bean.getInfo().equals("用户名已存在")) {
							lblNewLabel.setText("用户名已存在!");
						} else if (bean.getInfo().equals("注册成功")) {
							btnNewButton_1.setEnabled(false);
							// 返回登陆界面
							CatLogin frame = new CatLogin();
							frame.setVisible(true);
							setVisible(false);
							System.out.println("get6Register");
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
