package com.psvm.client.views;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DetailOfChat extends JPanel {
    //Cái này thì tốt nhất là mày truyền một class của người dùng vào cái Constructor để lấy thông tin
    //Mấy cái bên dưới t làm đại thôi, truyền sao mày thấy tiện là được

    DetailOfChat(){
        setPreferredSize(new Dimension(250,764));
        this.setBackground((Color.WHITE));
        renderFriend();
    }
    void removeComponent(){
        Component[] componentList = this.getComponents();
        for(Component c : componentList){
            //Find the components you want to remove
            this.remove(c);
        }
    }
    void renderFriend(){
        removeComponent();
        DetailOfAFriend detailOfAFriend = new DetailOfAFriend("test","Nguyễn Lâm Hải","kizark");
        this.add(detailOfAFriend);
        this.revalidate();
        this.repaint();
    }

    void renderGroup(){
        removeComponent();
        //call function here
        DetailOfAGroup detailOfAGroup = new DetailOfAGroup("test","Hehefdsfbsdfnsdfn");
        this.add(detailOfAGroup);
        this.revalidate();
        this.repaint();
    }
}

