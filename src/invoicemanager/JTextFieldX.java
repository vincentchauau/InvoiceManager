package invoicemanager;

//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.KeyboardFocusManager;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.geom.Rectangle2D;
//import java.util.Collections;
//import javax.swing.JComboBox;
//import javax.swing.JTextField;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;

//public class JTextFieldX extends JTextField implements KeyListener,
//        DocumentListener {
//
//    public String[] values;
//    public String guess = "";
//
//    public JTextFieldX(String[] values) {
//        super.setColumns(10);
//        super.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
//        this.values = values;
//        addKeyListener(this);
//        getDocument().addDocumentListener(this);
//    }
//
//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Rectangle2D enteredBounds = g.getFontMetrics().getStringBounds(getText(), g);
//        if (!guess.equals("") && !getText().equals("")) {
//            g.setColor(Color.BLUE);
//            g.drawString(guess, (int) (enteredBounds.getWidth()) + 2, (int) enteredBounds.getHeight());
//        }
//    }
//    @Override
//    public void keyPressed(KeyEvent e) {
//        switch (e.getKeyCode()) {
//            case KeyEvent.VK_TAB:
//                transferFocus();
//                setText(getText() + guessText(getText()));
//                break;
//            case KeyEvent.VK_ENTER:
//                setText(getText());
//                break;
//            default:
//                break;
//        }
//    }
//
//    public String guessText(String currentText) {
//        for (int i = 0; i < values.length; ++i) {
//            if (values[i].startsWith(currentText)) {
//                return values[i].substring(currentText.length());
//            } else {
//            }
//        }
//        return "";
//    }
//
//    @Override
//    public void keyTyped(KeyEvent e) {
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//    }
//
//    @Override
//    public void insertUpdate(DocumentEvent e) {
//        if (!getText().equals("")) {
//            guess = guessText(getText());
//            System.out.println("Text:" + getText());
//            System.out.println("Guess:" + guess);
//        } else {
//        }
//    }
//
//    @Override
//    public void removeUpdate(DocumentEvent e) {
//        if (!getText().equals("")) {
//            guess = guessText(getText());
//            System.out.println("Text:" + getText());
//            System.out.println("Guess:" + guess);
//        } else {
//        }
//    }
//
//    @Override
//    public void changedUpdate(DocumentEvent e) {
//    }
//}
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class JTextFieldX extends JTextField {

    private JComboBox cbInput = new JComboBox() {
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, 0);
        }
    };
    private DefaultComboBoxModel cbInputModel = new DefaultComboBoxModel();
    private String[] _items;
    public JTextFieldX(String[] items) {
        _items = items;
        setLayout(new BorderLayout());
        add(cbInput, BorderLayout.SOUTH);
//        addHierarchyListener(new HierarchyListener() {
//            @Override
//            public void hierarchyChanged(HierarchyEvent e) {
//                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)!=0 && isShowing()) 
//                {
//                    requestFocusInWindow();
//                }
//            }});
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Default action
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if (cbInput.getSelectedItem() != null)
                    {
                        setText(cbInput.getSelectedItem().toString());
                        cbInput.setPopupVisible(false);
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_TAB)
                {
                }
                else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    e.setSource(cbInput);
                    cbInput.dispatchEvent(e);
                }
                else{}
            }
            private void autoCompleteData() {
                String input = getText();
                cbInputModel.removeAllElements();
                if (!input.isEmpty()) {
                    for (String item : _items) {
                        if (item.toLowerCase().startsWith(input.toLowerCase())) {
                            cbInputModel.addElement(item);
                        }
                    }
                }
                cbInput.setModel(cbInputModel);
                cbInput.setMaximumRowCount(cbInputModel.getSize());
                cbInput.setPopupVisible(cbInput.getItemCount() > 0);
            }

            public boolean isPrintableKeyChar(char c) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                return (!Character.isISOControl(c))
                        && c != KeyEvent.CHAR_UNDEFINED
                        && block != null
                        && block != Character.UnicodeBlock.SPECIALS;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (isPrintableKeyChar(e.getKeyChar())|| e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                {
                   autoCompleteData();
                }
                else{}
            }
        });
    }

}
