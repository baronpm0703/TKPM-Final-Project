package com.psvm.client.views.components.friend;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

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
        if (!blocked){
            blockItem = new JMenuItem("🚫 Chặn người này");
            blockItem.setForeground(Color.red);
            blockItem.setFont(new Font(null,Font.PLAIN,16));
            popupMenu.add(blockItem);
        }
        else{
            blockItem = new JMenuItem("🚫 Bỏ chặn người này");
            blockItem.setForeground(Color.red);
            blockItem.setFont(new Font(null,Font.PLAIN,16));
            popupMenu.add(blockItem);
        }
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
                JOptionPane.showMessageDialog(null,
                        "Bạn đã tạo 1 nhóm với người này, vui lòng kiểm tra bên 'Nhóm'!");
            }
        });

        blockItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blocked){
                    int response = JOptionPane.showConfirmDialog(null,
                            "Bạn có chắc muốn chặn người này?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                        JOptionPane.showMessageDialog(null, "Chặn...");
                    }
                }
                else{
                    int response = JOptionPane.showConfirmDialog(null,
                            "Bạn có chắc muốn bỏ chặn người này?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                        JOptionPane.showMessageDialog(null, "Bỏ chặn...");
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
                    JOptionPane.showMessageDialog(null, "Huỷ kết bạn...");
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
                    JOptionPane.showMessageDialog(null, "Báo cáo Spam...");
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
