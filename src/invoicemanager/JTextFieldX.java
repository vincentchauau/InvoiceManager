package invoicemanager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JTextFieldX extends JTextField implements KeyListener,
        DocumentListener {

    public String[] possibilities;
    public int currentGuess;
    public Color incompleteColor;
    public boolean areGuessing;
    public boolean caseSensitive;

    public JTextFieldX(String[] poss) {
        super.setColumns(10);
        super.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        possibilities = poss;
        incompleteColor = Color.BLUE;
        currentGuess = -1;
        areGuessing = false;
        caseSensitive = false;
        addKeyListener(this);
        getDocument().addDocumentListener(this);
    }

    public void setIncompleteColor(Color incompleteColor) {
        incompleteColor = incompleteColor;
    }

    private String getCurrentGuess() {
        if (currentGuess != -1) {
            return possibilities[currentGuess];
        }
        return getText();
    }

    public void setCaseSensitive(boolean sensitive) {
        caseSensitive = sensitive;
    }

    private void findCurrentGuess() {
        String entered = getText();
        if (!caseSensitive) {
            entered = entered.toLowerCase();
        }

        for (int i = 0; i < possibilities.length; i++) {
            currentGuess = -1;
            String possibility = possibilities[i];
            if (!caseSensitive) {
                possibility = possibility.toLowerCase();
            }
            if (possibility.startsWith(entered)) {
                currentGuess = i;
                break;
            }
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        areGuessing = false;
        currentGuess = -1;
    }

    @Override
    public void paintComponent(Graphics g) {
        String guess = getCurrentGuess();
        String drawGuess = guess;
        super.paintComponent(g);
        String entered = getText();
        Rectangle2D enteredBounds = g.getFontMetrics().getStringBounds(entered, g);
        if (!(caseSensitive)) {
            entered = entered.toLowerCase();
            guess = guess.toLowerCase();
        }
        if (!(guess.startsWith(entered))) {
            areGuessing = false;
        }
        if (entered != null && !(entered.equals(""))
                && areGuessing) {
            String subGuess = drawGuess.substring(entered.length(), drawGuess.length());
            Rectangle2D subGuessBounds = g.getFontMetrics().getStringBounds(drawGuess, g);
            int centeredY = ((getHeight() / 2) + (int) (subGuessBounds.getHeight() / 2));
            g.setColor(incompleteColor);
            g.drawString(subGuess, (int) (enteredBounds.getWidth()) + 2, centeredY - 2);
        }
    }

    public void keyTyped(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_TAB) {
            if (this.areGuessing) {
                this.setText(getCurrentGuess());
                this.areGuessing = false;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (this.areGuessing) {
                this.setText(getText());
                this.areGuessing = false;
                e.consume();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        String temp = getText();

        if (temp.length() == 1) {
            areGuessing = true;
        }

        if (areGuessing) {
            findCurrentGuess();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        String temp = getText();
        if (!(areGuessing)) {
            areGuessing = true;
        }
        if (temp.length() == 0) {
            areGuessing = false;
        } else if (areGuessing) {
            findCurrentGuess();
        }
    }

    public void changedUpdate(DocumentEvent e) {
    }

}
