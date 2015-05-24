package cat.client;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cat.function.CatBean;
import cat.login.CatResign;
import cat.util.AES;
import cat.util.CatUtil;

class CellRenderer extends JLabel implements ListCellRenderer {
	CellRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));// 加入宽度为5的空白边框
		if (value != null) {
			setText(value.toString());
			setIcon(new ImageIcon("images//1.jpg"));
		}
		if (isSelected) {
			setBackground(new Color(255, 255, 153));// 设置背景色
			setForeground(Color.black);
		} else {
			// 设置选取与取消选取的前景与背景颜色.
			setBackground(Color.white); // 设置背景色
			setForeground(Color.black);
		}
		setEnabled(list.isEnabled());
		setFont(new Font("sdf", Font.ROMAN_BASELINE, 13));
		setOpaque(true);
		return this;
	}
}

class UUListModel extends AbstractListModel {

	private Vector vs;

	public UUListModel(Vector vs) {
		this.vs = vs;
	}

	@Override
	public Object getElementAt(int index) {

		return vs.get(index);
	}

	@Override
	public int getSize() {

		return vs.size();
	}

}

public class CatChatroom extends JFrame {

	private static final long serialVersionUID = 6129126482250125466L;

	private static JPanel contentPane;
	private static Socket clientSocket;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static String name;
	private static JTextArea textArea;
	private static AbstractListModel listmodel,friendListModel;
	private static JList list;
	private static String filePath;
	private static JLabel lblNewLabel;
	private static JProgressBar progressBar;
	private static Vector onlines,myFriend;
	private static boolean isSendFile = false;
	private static boolean isReceiveFile = false;

	// 声音
	private static File file, file2;
	private static URL cb, cb2;
	private static AudioClip aau, aau2;
	private JButton jbAddFriend;

	/**
	 * Create the frame.
	 */

	public CatChatroom(String u_name, Socket client) {
		// 赋值
		name = u_name;
		clientSocket = client;
		onlines = new Vector();
		myFriend = new Vector();

		SwingUtilities.updateComponentTreeUI(this);

		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e1) {

			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		// 框的设置
		setTitle(name);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(200, 100, 688, 510);
		contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(new ImageIcon("images\\聊天室1.jpg").getImage(), 0, 0,
						getWidth(), getHeight(), null);
			}

		};
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// 聊天信息显示区域
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 10, 410, 300);
		getContentPane().add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);// 激活自动换行功能
		textArea.setWrapStyleWord(true);// 激活断行不断字功能
		textArea.setFont(new Font("sdf", Font.BOLD, 13));
		scrollPane.setViewportView(textArea);// 设置jscroll的显示页面

		// 打字区域
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 347, 411, 97);
		getContentPane().add(scrollPane_1);

		final JTextArea textArea_1 = new JTextArea();
		textArea_1.setLineWrap(true);// 激活自动换行功能
		textArea_1.setWrapStyleWord(true);// 激活断行不断字功能
		scrollPane_1.setViewportView(textArea_1);// 设置jscroll的显示页面

		// 关闭按钮
		final JButton btnClose = new JButton("\u5173\u95ED");
		btnClose.setBounds(214, 448, 60, 30);
		getContentPane().add(btnClose);

		// 发送按钮
		JButton btnSendMsg = new JButton("\u53D1\u9001");
		btnSendMsg.setBounds(313, 448, 60, 30);
		getRootPane().setDefaultButton(btnSendMsg);
		getContentPane().add(btnSendMsg);

		// 添加好友
		JButton btnAddFriend = new JButton("\u6dfb\u52a0\u597d\u53cb");
		btnAddFriend.setBounds(603, 448, 80, 30);
		getRootPane().setDefaultButton(btnAddFriend);
		getContentPane().add(btnAddFriend);
		// TODO 改为好友列表

		//在线用户列表
		listmodel = new UUListModel(onlines);
		// 好友列表
		friendListModel = new UUListModel(myFriend);
		list = new JList(friendListModel);
		list.setCellRenderer(new CellRenderer());
		list.setOpaque(false);
		Border etch = BorderFactory.createEtchedBorder();
		list.setBorder(BorderFactory.createTitledBorder(etch, "<" + u_name
				+ ">" + "好友:", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("sdf", Font.BOLD, 20), Color.green));

		JScrollPane scrollPane_2 = new JScrollPane(list);
		scrollPane_2.setBounds(430, 10, 245, 375);
		scrollPane_2.setOpaque(false);
		scrollPane_2.getViewport().setOpaque(false);
		getContentPane().add(scrollPane_2);

		// 文件传输栏
		progressBar = new JProgressBar();
		progressBar.setBounds(430, 390, 245, 15);
		progressBar.setMinimum(1);
		progressBar.setMaximum(100);
		getContentPane().add(progressBar);

		// 文件传输提示
		lblNewLabel = new JLabel("\u6587\u4EF6\u4F20\u9001\u4FE1\u606F\u680F:");
		lblNewLabel.setFont(new Font("SimSun", Font.PLAIN, 12));
		lblNewLabel.setBackground(Color.WHITE);
		lblNewLabel.setBounds(430, 410, 245, 15);
		getContentPane().add(lblNewLabel);

		try {
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			// 记录上线客户的信息在catbean中，并发送给服务器
			CatBean bean = new CatBean();
			bean.setType(0);
			bean.setName(name);
			bean.setTimer(CatUtil.getTimer());
			oos.writeObject(bean);
			oos.flush();

			// 消息提示声音
			file = new File("sounds\\呃欧.wav");
			cb = file.toURL();
			aau = Applet.newAudioClip(cb);
			// 上线提示声音
			file2 = new File("sounds\\叮.wav");
			cb2 = file2.toURL();
			aau2 = Applet.newAudioClip(cb2);

			// 启动客户接收线程
			new ClientInputThread().start();

		} catch (IOException e) {

			e.printStackTrace();
		}

		// 发送按钮
		btnSendMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String infotext = textArea_1.getText();
				List to = list.getSelectedValuesList();

				if (to.size() != 1) {
					JOptionPane.showMessageDialog(getContentPane(), "请选择聊天对象,聊天对象只能为一个");
					return;
				}
				if (to.toString().contains(name + "(我)")) {
					JOptionPane.showMessageDialog(getContentPane(), "不能向自己发送信息");
					return;
				}
				if (infotext.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "不能发送空信息");
					return;
				}
				if (info!=null) {//若上一条没发送成功，则不发送过去：//TODO 改成可以丢弃 或者重新发送
					JOptionPane.showMessageDialog(getContentPane(), "上一条信息未发送成功");
					return;
				}
				CatBean clientBean = new CatBean();
				clientBean.setType(1);
				clientBean.setName(name);
				String time = CatUtil.getTimer();
				clientBean.setTimer(time);
				String encrypt = AES.encrypt(infotext, AES.password+name+to.iterator().next());//加密   密钥为设定密钥+发起聊天方用户名
				clientBean.setInfo(encrypt);
				HashSet set = new HashSet();
				set.addAll(to);
				clientBean.setClients(set);

				// 自己发的内容也要现实在自己的屏幕上面
				textArea.append(time + " 我对" + to + "说:\r\n" + infotext + "\r\n");

				sendMessage(clientBean);
				textArea_1.setText(null);
				textArea_1.requestFocus();
			}
		});

		// 关闭按钮
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isSendFile || isReceiveFile) {
					JOptionPane.showMessageDialog(contentPane,
							"正在传输文件中，您不能离开...", "Error Message",
							JOptionPane.ERROR_MESSAGE);
				} else {
					btnClose.setEnabled(false);
					CatBean clientBean = new CatBean();
					clientBean.setType(-1);
					clientBean.setName(name);
					clientBean.setTimer(CatUtil.getTimer());
					sendMessage(clientBean);
				}
			}
		});
		
		//添加好友按钮 
		btnAddFriend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				AddFriend frame = new AddFriend(clientSocket, name, listmodel);
				frame.setVisible(true);// 显示在线用户列表
			}
		});
		// 离开
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (isSendFile || isReceiveFile) {
					JOptionPane.showMessageDialog(contentPane,
							"正在传输文件中，您不能离开...", "Error Message",
							JOptionPane.ERROR_MESSAGE);
				} else {
					int result = JOptionPane.showConfirmDialog(
							getContentPane(), "您确定要离开聊天室");
					if (result == 0) {
						CatBean clientBean = new CatBean();
						clientBean.setType(-1);
						clientBean.setName(name);
						clientBean.setTimer(CatUtil.getTimer());
						sendMessage(clientBean);
					}
				}
			}
		});

		// 列表监听
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				List to = list.getSelectedValuesList();

				if (e.getClickCount() == 2) {

					if (to.toString().contains(name + "(我)")) {
						JOptionPane.showMessageDialog(getContentPane(),
								"不能向自己发送文件");
						return;
					}

					// 双击打开文件文件选择框
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("选择文件框"); // 标题哦...
					chooser.showDialog(getContentPane(), "选择"); // 这是按钮的名字..

					// 判定是否选择了文件
					if (chooser.getSelectedFile() != null) {
						// 获取路径
						filePath = chooser.getSelectedFile().getPath();
						File file = new File(filePath);
						// 文件为空
						if (file.length() == 0) {
							JOptionPane.showMessageDialog(getContentPane(),
									filePath + "文件为空,不允许发送.");
							return;
						}

						CatBean clientBean = new CatBean();
						clientBean.setType(2);// 请求发送文件
						clientBean.setSize(new Long(file.length()).intValue());
						clientBean.setName(name);
						clientBean.setTimer(CatUtil.getTimer());
						clientBean.setFileName(file.getName()); // 记录文件的名称
						clientBean.setInfo("请求发送文件");

						// 判断要发送给谁
						HashSet<String> set = new HashSet<String>();
						set.addAll(list.getSelectedValuesList());
						clientBean.setClients(set);
						sendMessage(clientBean);
					}
				}
			}
		});

	}

	String info = null;//判断是否发送成功
	class ClientInputThread extends Thread {

		@Override
		public void run() {
			try {
				// 不停的从服务器接收信息
				while (true) {
					ois = new ObjectInputStream(clientSocket.getInputStream());
					final CatBean bean = (CatBean) ois.readObject();
					switch (bean.getType()) {
					case 0: {
						// 更新在线名单列表,无需打印出来
						onlines.clear();
						HashSet<String> clients = bean.getClients();
						Iterator<String> it = clients.iterator();
						while (it.hasNext()) {
							String ele = it.next();
							if (name.equals(ele)) {
								onlines.add(ele + "(我)");
							} else {
								onlines.add(ele);
							}
						}

						listmodel = new UUListModel(onlines);
						break;
					}
					case 100: {//接收在线好友信息
						myFriend.clear();
						HashSet<String> clients = bean.getClients();
						Iterator<String> it = clients.iterator();
						while (it.hasNext()) {
							String ele = it.next();
							if (name.equals(ele)) {
								myFriend.add(ele + "(我)");
							} else {
								myFriend.add(ele);
							}
						}

						friendListModel = new UUListModel(myFriend);
						list.setModel(friendListModel);
						aau2.play();
						textArea.append(bean.getInfo() + "\r\n");
						textArea.selectAll();
						break;
					}
					case 101: {//接收好友上线信息
						myFriend.clear();
						HashSet<String> clients = bean.getClients();
						Iterator<String> it = clients.iterator();
						while (it.hasNext()) {
							String ele = it.next();
							if (name.equals(ele)) {
								myFriend.add(ele + "(我)");
							} else {
								myFriend.add(ele);
							}
						}

						friendListModel = new UUListModel(myFriend);
						list.setModel(friendListModel);
						aau2.play();
						textArea.append(bean.getInfo() + "\r\n");
						textArea.selectAll();
						break;
					}
					case -1: {

						return;
					}
					case 1: {// 聊天
						if (name.equals(bean.getName())) {
							JOptionPane.showConfirmDialog(getContentPane(), "他还不是你的好友！不能发起聊天");
						}else{
							if (bean.getName().equals(name)) {
								info = bean.getTimer() + "  我"
										+ " 对 " + bean.getClients() + "说:\r\n";
							}else {
								info = bean.getTimer() + "  " + bean.getName()
										+ " 对  我" + "说:\r\n";
							}
							String decrypt = AES.decrypt(bean.getInfo(), AES.password+bean.getName()+name);//解密，密钥为初始密钥+发起方用户名
							info += decrypt;
							aau.play();
							textArea.append(info + "\r\n");
							textArea.selectAll();
							info = null;
							//返回给源用户 聊天接收成功
							CatBean beanBack = new CatBean();
							beanBack.setType(110);
							beanBack.setName(name);
							beanBack.setTimer(CatUtil.getTimer());//接收成功时的时间
							HashSet<String> set = new HashSet<String>();
							// 客户的昵称
							set.add(bean.getName());
							beanBack.setClients(set);
							oos = new ObjectOutputStream(clientSocket.getOutputStream());
							oos.writeObject(beanBack);
							oos.flush();
						}
						break;
					}
					case 110:{//聊天内容发送成功的回执
						aau.play();
						textArea.append(bean.getTimer()+",对方已收到" + "\r\n\r\n");
						textArea.selectAll();
						info=null;
						break;
					}
					case 2: {
						// 由于等待目标客户确认是否接收文件是个阻塞状态，所以这里用线程处理
						if (name.equals(bean.getName())) {
							JOptionPane.showConfirmDialog(getContentPane(), "他还不是你的好友！不能发送文件");
						}else{
						new Thread() {
							public void run() {
								// 显示是否接收文件对话框
								int result = JOptionPane.showConfirmDialog(
										getContentPane(), bean.getInfo());
								switch (result) {
								case 0: { // 接收文件
									JFileChooser chooser = new JFileChooser();
									chooser.setDialogTitle("保存文件框"); // 标题哦...
									// 默认文件名称还有放在当前目录下
									chooser.setSelectedFile(new File(bean
											.getFileName()));
									chooser.showDialog(getContentPane(), "保存"); // 这是按钮的名字..
									// 保存路径
									String saveFilePath = chooser
											.getSelectedFile().toString();

									// 创建客户CatBean
									CatBean clientBean = new CatBean();
									clientBean.setType(3);
									clientBean.setName(name); // 接收文件的客户名字
									clientBean.setTimer(CatUtil.getTimer());
									clientBean.setFileName(saveFilePath);
									clientBean.setInfo("确定接收文件");

									// 判断要发送给谁
									HashSet<String> set = new HashSet<String>();
									set.add(bean.getName());
									clientBean.setClients(set); // 文件来源
									clientBean.setTo(bean.getClients());// 给这些客户发送文件

									// 创建新的tcp socket 接收数据, 这是额外增加的功能, 大家请留意...
									try {
										ServerSocket ss = new ServerSocket(0); // 0可以获取空闲的端口号

										clientBean.setIp(clientSocket
												.getInetAddress()
												.getHostAddress());
										clientBean.setPort(ss.getLocalPort());
										sendMessage(clientBean); // 先通过服务器告诉发送方,
																	// 你可以直接发送文件到我这里了...

										isReceiveFile = true;
										// 等待文件来源的客户，输送文件....目标客户从网络上读取文件，并写在本地上
										Socket sk = ss.accept();
										textArea.append(CatUtil.getTimer()
												+ "  " + bean.getFileName()
												+ "文件保存中.\r\n");
										DataInputStream dis = new DataInputStream( // 从网络上读取文件
												new BufferedInputStream(
														sk.getInputStream()));
										DataOutputStream dos = new DataOutputStream( // 写在本地上
												new BufferedOutputStream(
														new FileOutputStream(
																saveFilePath)));

										int count = 0;
										int num = bean.getSize() / 100;
										int index = 0;
										while (count < bean.getSize()) {
											int t = dis.read();
											dos.write(t);
											count++;

											if (num > 0) {
												if (count % num == 0
														&& index < 100) {
													progressBar
															.setValue(++index);
												}
												lblNewLabel.setText("下载进度:"
														+ count + "/"
														+ bean.getSize()
														+ "  整体" + index + "%");
											} else {
												lblNewLabel
														.setText("下载进度:"
																+ count
																+ "/"
																+ bean.getSize()
																+ "  整体:"
																+ new Double(
																		new Double(
																				count)
																				.doubleValue()
																				/ new Double(
																						bean.getSize())
																						.doubleValue()
																				* 100)
																		.intValue()
																+ "%");
												if (count == bean.getSize()) {
													progressBar.setValue(100);
												}
											}

										}

										// 给文件来源客户发条提示，文件保存完毕
										PrintWriter out = new PrintWriter(
												sk.getOutputStream(), true);
										out.println(CatUtil.getTimer() + " 发送给"
												+ name + "的文件["
												+ bean.getFileName() + "]"
												+ "文件保存完毕.\r\n");
										out.flush();
										dos.flush();
										dos.close();
										out.close();
										dis.close();
										sk.close();
										ss.close();
										textArea.append(CatUtil.getTimer()
												+ "  " + bean.getFileName()
												+ "文件保存完毕.存放位置为:"
												+ saveFilePath + "\r\n");
										isReceiveFile = false;
									} catch (Exception e) {

										e.printStackTrace();
									}

									break;
								}
								default: {
									CatBean clientBean = new CatBean();
									clientBean.setType(4);
									clientBean.setName(name); // 接收文件的客户名字
									clientBean.setTimer(CatUtil.getTimer());
									clientBean.setFileName(bean.getFileName());
									clientBean.setInfo(CatUtil.getTimer()
											+ "  " + name + "取消接收文件["
											+ bean.getFileName() + "]");

									// 判断要发送给谁
									HashSet<String> set = new HashSet<String>();
									set.add(bean.getName());
									clientBean.setClients(set); // 文件来源
									clientBean.setTo(bean.getClients());// 给这些客户发送文件

									sendMessage(clientBean);

									break;

								}
								}
							};
						}.start();
						}
						break;
					}
					case 3: { // 目标客户愿意接收文件，源客户开始读取本地文件并发送到网络上
						textArea.append(bean.getTimer() + "  " + bean.getName()
								+ "确定接收文件" + ",文件传送中..\r\n");
						new Thread() {
							public void run() {

								try {
									isSendFile = true;
									// 创建要接收文件的客户套接字
									Socket s = new Socket(bean.getIp(),
											bean.getPort());
									DataInputStream dis = new DataInputStream(
											new FileInputStream(filePath)); // 本地读取该客户刚才选中的文件
									DataOutputStream dos = new DataOutputStream(
											new BufferedOutputStream(
													s.getOutputStream())); // 网络写出文件

									int size = dis.available();

									int count = 0; // 读取次数
									int num = size / 100;
									int index = 0;
									while (count < size) {

										int t = dis.read();
										dos.write(t);
										count++; // 每次只读取一个字节

										if (num > 0) {
											if (count % num == 0 && index < 100) {
												progressBar.setValue(++index);

											}
											lblNewLabel.setText("上传进度:" + count
													+ "/" + size + "  整体"
													+ index + "%");
										} else {
											lblNewLabel
													.setText("上传进度:"
															+ count
															+ "/"
															+ size
															+ "  整体:"
															+ new Double(
																	new Double(
																			count)
																			.doubleValue()
																			/ new Double(
																					size)
																					.doubleValue()
																			* 100)
																	.intValue()
															+ "%");
											if (count == size) {
												progressBar.setValue(100);
											}
										}
									}
									dos.flush();
									dis.close();
									// 读取目标客户的提示保存完毕的信息...
									BufferedReader br = new BufferedReader(
											new InputStreamReader(
													s.getInputStream()));
									textArea.append(br.readLine() + "\r\n");
									isSendFile = false;
									br.close();
									s.close();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							};
						}.start();
						break;
					}
					case 4: {//中途取消接收文件
						textArea.append(bean.getInfo() + "\r\n");
						break;
					}
					case 5: {//登陆
						break;
					}
					case 6: {//注册
						break;
					}
					case 7: {
						if (bean.getInfo().equals("addFriend")) {//收到别人发起的好友请求
							int addFriend = JOptionPane.showConfirmDialog(
									getContentPane(),"用户<"+bean.getName()+">想要加您为好友，是否接受？", "提示",
									JOptionPane.YES_NO_OPTION);
							System.out.println("addFriend:"+addFriend);
							if (addFriend==0) {//点击了是,添加好友并告知服务器
								//TODO 显示在右上角其为好友
								
								myFriend.add(bean.getName());
								friendListModel = new UUListModel(myFriend);
								list.setModel(friendListModel);
								// 记录上线客户的信息在catbean中，并发送给服务器
								CatBean beanBack = new CatBean();
								beanBack.setType(7);//注册
								beanBack.setName(name);
								beanBack.setTimer(CatUtil.getTimer());
								beanBack.setInfo("addFriendBack");
								HashSet<String> set = new HashSet<String>();
								// 客户昵称
								set.add(bean.getName());
								beanBack.setClients(set);
								oos = new ObjectOutputStream(clientSocket.getOutputStream());
								oos.writeObject(beanBack);
								oos.flush();

							}
						}else if (bean.getInfo().equals("addFriendBack")) {//收到别人同意添加好友的请求
							//TODO 显示在右上角其为好友
							myFriend.add(bean.getName());
							friendListModel = new UUListModel(myFriend);
							list.setModel(friendListModel);
						}
						
						break;
					}
					default: {
						break;
					}
					}

				}
			} catch (IOException e) {

				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} finally {
				if (clientSocket != null) {
					try {
						clientSocket.close();
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		}
	}

	private void sendMessage(CatBean clientBean) {
		try {
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(clientBean);
			oos.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
