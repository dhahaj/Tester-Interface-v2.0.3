package com.tester;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import static processing.core.PApplet.println;

/**
 *
 * @author dmh
 */
public final class MyJButton extends JButton implements ActionListener {

  private static final long serialVersionUID = -4110718147615504078L;

  GUIFrame parent;
  private static boolean shown = false;

  public MyJButton(GUIFrame parent, String test) {
    super(test);
    setMinimumSize(new Dimension(150, 60));
    setPreferredSize(new Dimension(160, 60));
    addActionListener(this);
  }

  public MyJButton(String test) {
    this(GUIFrame.frame, test);
  }

  public void setParent(GUIFrame frame) {
    parent = frame;
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setMinimumSize(Dimension minimumSize) {
    super.setMinimumSize(new Dimension(120, 80)); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setPreferredSize(Dimension preferredSize) {
    super.setPreferredSize(new Dimension(150, 50)); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    println(command);
    if (GUIFrame.getArduino() == null && !shown) {
      JOptionPane.showMessageDialog(null, "Comm Error");
      shown = true;
    } else if (GUIFrame.getArduino() != null) {
      int pin;
      if ("Test".equals(command)) {
        pin = 3;
      } else if ("Door".equals(command)) {
        pin = 6;
      } else if ("OKC".equals(command)) {
        pin = 8;
      } else if ("AC Power".equals(command)) {
        pin = 9;
      } else if ("Battery Power".equals(command)) {
        pin = 4;
      } else if ("Low Battery".equals(command)) {
        pin = 2;
      } else if ("Remote".equals(command)) {
        pin = 5;
      } else {
        return;
      }
      MyJButton jb = (MyJButton) e.getSource();
      
      GUIFrame.getArduino().toggle(pin);
    }
  }

}
