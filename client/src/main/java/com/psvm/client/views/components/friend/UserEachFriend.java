package com.psvm.client.views.components.friend;

import com.psvm.client.controllers.*;
import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class UserEachFriend extends JPanel {
    // Data
    private String id;
    private String avatar;
    private String name;
    private String lastChat;
    private LocalDateTime lastTime;
    private String userStatus;
    private boolean blocked = false;
    private String lastChatStatus;

    // GUI components to display data
    private GridBagConstraints gbc;
    private JLabel avatarLabel;
    private JLabel friendName;
    private JLabel friendLastTime;
    private JLabel lastMessage;
    private JLabel statusMessage;

    UserEachFriend(String id, String avatar, String name, String lastChat, LocalDateTime lastTime, String lastChatStatus){
        this.id = id;
        this.avatar = avatar;
        this.name = name;
        this.lastChat = lastChat;
        this.lastTime = lastTime;
        this.lastChatStatus = lastChatStatus;
        this.setPreferredSize(new Dimension(super.getWidth(),70));
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.setBackground(Color.WHITE);
        initialize();
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)){
                    showPopupMenu(e.getX(),e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

        });
    }
    void initialize(){

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();

        // Component 1 (avatar, spanning 2 rows)//nhớ thay avatar dưới này
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 40, 40, userStatus);
        avatarLabel = new JLabel(avatarIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(avatarLabel, gbc);

        // Component 2 (button in the second column)
        friendName = new JLabel(name);
        Dimension friendNameDimension = new Dimension();
        friendNameDimension.setSize(160, 1);
        friendName.setPreferredSize(friendNameDimension);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1; // Reset grid height
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(friendName, gbc);

        // Component 3 (button in the third column)
        friendLastTime = new JLabel(formatLocalDateTime(lastTime));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0); // Reset insets
        this.add(friendLastTime, gbc);

        // Component 4 (button in the second row)
        lastMessage= new JLabel(lastChat);
        Dimension lastMessageDimension = new Dimension();
        lastMessageDimension.setSize(160, 1);
        lastMessage.setPreferredSize(lastMessageDimension);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(lastMessage, gbc);

        // Component 5 (button in the third row)
        statusMessage = createUserStatusDot(lastChatStatus);
        gbc.gridx = 2;
        gbc.gridy = 1;
        this.add(statusMessage, gbc);

        // Add vertical spacing
        this.add(Box.createVerticalStrut(10));
    }

    public String getConversationId() { return id; }
    public String getName() { return name; }

    public void setData(String name, String lastChat, LocalDateTime lastTime, String lastChatStatus) {
        this.name = name;
        this.lastChat = lastChat;
        this.lastTime = lastTime;
        this.lastChatStatus = lastChatStatus;

        friendName.setText(name);
        lastMessage.setText(lastChat);
        friendLastTime.setText(formatLocalDateTime(lastTime));
        this.remove(statusMessage);
        statusMessage = createUserStatusDot(lastChatStatus);
        this.add(statusMessage, gbc);
        revalidate();
    }

    private void showPopupMenu(int x, int y){
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(Color.WHITE);

        JMenuItem addGroupItem = new JMenuItem("👋 Tạo nhóm với người này");
        addGroupItem.setFont(new Font(null,Font.PLAIN,16));
        addGroupItem.setForeground(Color.blue);
        popupMenu.add(addGroupItem);
        JMenuItem blockItem;

        blockItem = new JMenuItem("🚫 Chặn/Bỏ chặn người này");
        blockItem.setForeground(Color.red);
        blockItem.setFont(new Font(null,Font.PLAIN,16));
        popupMenu.add(blockItem);

        JMenuItem unfriendItem = new JMenuItem("❌ Huỷ bạn bè");
        unfriendItem.setFont(new Font(null,Font.PLAIN,16));
        unfriendItem.setForeground(Color.blue);
        popupMenu.add(unfriendItem);

        JMenuItem spamItem = new JMenuItem("🤐 Báo cáo Spam");
        spamItem.setFont(new Font(null,Font.PLAIN,16));
        spamItem.setForeground(Color.red);
        popupMenu.add(spamItem);




        addGroupItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateNewGroupWithThread newGroup = new CreateNewGroupWithThread(id);
                newGroup.start();
                JOptionPane.showMessageDialog(null, "Tạo Group...");
                try {
                    newGroup.join();
                    if (newGroup.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) {
                        JOptionPane.showMessageDialog(null, "Tạo Group Thành Công");
                    } else JOptionPane.showMessageDialog(null, "Tạo Group Không Thành Công. Có Lỗi!");
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        blockItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Thực hiện hành động này?\n(nếu bạn đang chặn người này thì bạn sẽ bỏ chặn)",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    UnOrBlockUserButtonThread blockThread = new UnOrBlockUserButtonThread(id);
                    blockThread.start();
                    JOptionPane.showMessageDialog(null, "Đang Xác Thực...");
                    try {
                        blockThread.join();
                        if (blockThread.getResponseCode() == SocketResponse.RESPONSE_BLOCK_CODE_BLOCK) {
                            JOptionPane.showMessageDialog(null, "Chặn Thành Công");
                        } else JOptionPane.showMessageDialog(null, "Bỏ Chặn Thành Công. ");
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }
        });

        unfriendItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Bạn có chắc muốn huỷ kết bạn?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    UnFriendButtonThread unfriendThread = new UnFriendButtonThread(id);
                    unfriendThread.start();
                    JOptionPane.showMessageDialog(null, "Huỷ kết bạn...");
                    try {
                        unfriendThread.join();
                        JOptionPane.showMessageDialog(null, "Huỷ kết bạn thành công");

                    } catch (InterruptedException ex) {
                        JOptionPane.showMessageDialog(null, "Huỷ kết bạn thất bại");
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        spamItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Bạn có chắc muốn báo cáo Spam người này?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    ReportUserThread reportThread = new ReportUserThread(id);
                    reportThread.start();
                    JOptionPane.showMessageDialog(null, "Báo cáo Spam...");
                    try {
                        reportThread.join();
                        JOptionPane.showMessageDialog(null, "Báo cáo Spam thành công.");
                    } catch (InterruptedException ex) {
                        JOptionPane.showMessageDialog(null, "Báo cáo Spam thất bại.");
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        popupMenu.show(this, x, y);
    }

    private static ImageIcon createCircularAvatar(String imagePath, int width, int height, String userStatus) {
        try {
            //https://stackoverflow.com/questions/14731799/bufferedimage-into-circle-shape
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();

            Shape clip = new Ellipse2D.Float(0, 0, width, height);
            g2d.setClip(clip);
            g2d.drawImage(originalImage, 0, 0, width, height, null);

            if ("Online".equals(userStatus)) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(0, 0, width - 1, height - 1);
            }

            g2d.dispose();

            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String formatLocalDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
            // Same day
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return dateTime.format(formatter);
        } else {
            // Different day
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return dateTime.format(formatter);
        }
    }

    private static JLabel createUserStatusDot(String userStatus) {
        JLabel statusLabel = new JLabel();
        statusLabel.setPreferredSize(new Dimension(20, 20));

        ImageIcon icon = null;

        if ("Online".equals(userStatus)) {
            icon = new ImageIcon("client/src/main/resources/icon/chatWhenOnline.png");
        } else if ("Offline".equals(userStatus)) {
            icon = new ImageIcon("client/src/main/resources/icon/chatWhenOffline.png");
        }
        if (icon != null) {
            Image scaledImage = icon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            statusLabel.setIcon(scaledIcon);
        } else {
            statusLabel.setVisible(false);
        }
        return statusLabel;
    }
}

class UnOrBlockUserButtonThread extends Thread {

    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    private String conId;

    private int responseCode;

    public UnOrBlockUserButtonThread( String conId) {
        this.conId = conId;

        /* Multithreading + Socket */
        try {
            socket = new Socket(SOCKET_HOST, SOCKET_PORT);
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        super.run();
        SocketResponse final_response;
        GetConversationInfo request = new GetConversationInfo(socket, socketIn, socketOut,conId);
        SocketResponse response = request.talk();

        String userId = null;
        for (Map<String, Object> e : response.getData()){
            System.out.println(e.get("MemberId"));
            if (!e.get("MemberId").toString().equals(LocalData.getCurrentUsername())){
                userId = e.get("MemberId").toString();
            }
        }

        UnorBlockUserWithId final_request = new UnorBlockUserWithId(socket, socketIn, socketOut, userId);
        final_response = final_request.talk();
        //SendMessageRequest request = new SendMessageRequest(clientSocket, socketIn, socketOut, userId, content);
        System.out.println("response.getData()");


        responseCode = final_response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

class UnFriendButtonThread extends Thread {

    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    private String conId;

    private int responseCode;

    public UnFriendButtonThread( String conId) {
        this.conId = conId;

        /* Multithreading + Socket */
        try {
            socket = new Socket(SOCKET_HOST, SOCKET_PORT);
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        super.run();
        SocketResponse final_response;
        GetConversationInfo request = new GetConversationInfo(socket, socketIn, socketOut,conId);
        SocketResponse response = request.talk();

        String userId = null;
        for (Map<String, Object> e : response.getData()){
            System.out.println(e.get("MemberId"));
            if (!e.get("MemberId").toString().equals(LocalData.getCurrentUsername())){
                userId = e.get("MemberId").toString();
            }
        }
        RemoveFriend semi_final_request = new RemoveFriend(socket, socketIn, socketOut, userId);
        SocketResponse semi_final_res = semi_final_request.talk();
        DeleteConversation final_request = new DeleteConversation(socket, socketIn, socketOut,conId);
        final_response = final_request.talk();
        //SendMessageRequest request = new SendMessageRequest(clientSocket, socketIn, socketOut, userId, content);
        System.out.println("response.getData()");

        responseCode = final_response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

class ReportUserThread extends Thread {

    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    private String conId;

    private int responseCode;

    public ReportUserThread( String conId) {
        this.conId = conId;

        /* Multithreading + Socket */
        try {
            socket = new Socket(SOCKET_HOST, SOCKET_PORT);
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        super.run();
        SocketResponse final_response;
        GetConversationInfo request = new GetConversationInfo(socket, socketIn, socketOut,conId);
        SocketResponse response = request.talk();

        String userId = null;
        for (Map<String, Object> e : response.getData()){
            System.out.println(e.get("MemberId"));
            if (!e.get("MemberId").toString().equals(LocalData.getCurrentUsername())){
                userId = e.get("MemberId").toString();
            }
        }
        ReportUser final_request = new ReportUser(socket, socketIn, socketOut, userId);
        final_response = final_request.talk();
        //SendMessageRequest request = new SendMessageRequest(clientSocket, socketIn, socketOut, userId, content);
        System.out.println("response.getData()");

        responseCode = final_response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

class CreateNewGroupWithThread extends Thread {

    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    private String conId;

    private int responseCode;

    public CreateNewGroupWithThread( String conId) {
        this.conId = conId;

        /* Multithreading + Socket */
        try {
            socket = new Socket(SOCKET_HOST, SOCKET_PORT);
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        super.run();
        SocketResponse final_response;

        // Get friendId u participate in
        GetConversationInfo request = new GetConversationInfo(socket, socketIn, socketOut,conId);
        SocketResponse response = request.talk();

        // From selected conId get your friendId
        String userId = null;
        for (Map<String, Object> e : response.getData()){
            System.out.println(e.get("MemberId"));
            if (!e.get("MemberId").toString().equals(LocalData.getCurrentUsername())){
                userId = e.get("MemberId").toString();
            }
        } // Get friendId, this user is talking to

        /* Create new conversation */
        GetHighestConvId highestConvIdReq = new GetHighestConvId(socket, socketIn, socketOut);
        SocketResponse highestConvIdRes = highestConvIdReq.talk(); // Receive res

        // Res return a vector<Map<String,Object>>
        String newConId = incrementCVNumber((String) highestConvIdRes.getData().get(0).get("highestConId")); //Convert back to string
        String newConName = "Cuộc trò truyện " + newConId;

        // Create new conversation with new id, name, ...
        CreateNewConv conRequest = new CreateNewConv(socket, socketIn, socketOut, newConId, newConName, true);
        SocketResponse conResponse = conRequest.talk();


        ConvAddMemWhenUAreAdMin uAddMemToGroupReq = new ConvAddMemWhenUAreAdMin(socket, socketIn, socketOut, newConId, userId);
        SocketResponse uAddMemToGroupRes = uAddMemToGroupReq.talk();

        // For u as an admin first
        UpdateConvLog semi_final_request = new UpdateConvLog(socket, socketIn, socketOut, newConId, LocalData.getCurrentUsername(),0);
        SocketResponse semi_final_response = semi_final_request.talk();

        // Sleep for not duplicate the date between update convLog
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // For user u add to group
        UpdateConvLog final_request = new UpdateConvLog(socket, socketIn, socketOut, newConId, userId,2);
        final_response = final_request.talk();

        //SendMessageRequest request = new SendMessageRequest(clientSocket, socketIn, socketOut, userId, content);
        System.out.println("response.getData()");

        responseCode = final_response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }

    public static String incrementCVNumber(String input) {
        // Extract numeric part
        String numericPart = input.substring(2);

        // Convert to integer, increment, and format back
        int numericValue = Integer.parseInt(numericPart);
        numericValue++;

        // Format the incremented value back into the original format
        String formattedIncremented = String.format("CV%06d", numericValue);

        return formattedIncremented;
    }
}

