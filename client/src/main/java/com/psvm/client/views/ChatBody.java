package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;

public class ChatBody extends JPanel {
    private GroupLayout layout;
    private GroupLayout.ParallelGroup leftParallel;
    private GroupLayout.ParallelGroup rightParallel;
    private GroupLayout.SequentialGroup sequential;

    public ChatBody() {
        initUI();
        addLeft("heheheheh");
        addLeft("hehehehehehehehehe");
        addRight("hahahahahaahahaahahahahahaha");
        addRight("hahhahahahahaha");
        addLeft("hehehehehehehehehheehehehe");
        addRight("hahhahahahahahahahahahahahahahaaha");
    }

    private void initUI() {
        setPreferredSize(new Dimension(840, 764));
        this.layout = new GroupLayout(this);
        this.layout.setAutoCreateGaps(true);
        this.layout.setAutoCreateContainerGaps(true);

        this.leftParallel = this.layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        this.rightParallel = this.layout.createParallelGroup(GroupLayout.Alignment.TRAILING); // Align from right to left
        this.sequential = this.layout.createSequentialGroup();

        this.layout.setHorizontalGroup(
                this.layout.createSequentialGroup()
                        .addGroup(this.leftParallel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                        .addGroup(this.rightParallel)
        );

        this.layout.setVerticalGroup(
                this.sequential
        );

        setLayout(this.layout);
    }

    private void addLeft(String msg) {
        JLabel label = new JLabel(msg);
        leftParallel.addComponent(label);
        sequential.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(label));
    }

    private void addRight(String msg) {
        JLabel label = new JLabel(msg);
        rightParallel.addComponent(label);
        sequential.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(label));
    }
}
