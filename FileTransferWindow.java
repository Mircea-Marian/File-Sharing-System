
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class FileTransferWindow extends Thread {

    private final JFrame frame;
    public final JFrame registeredUsersFrame;
    private final JTextField logInStatus, eventStatus;
    private final JButton goToLeft, goToRight;
    private ArrayList<ScreenToDisplay> screens, loggedInScreens;
    private char index;
    private ReentrantLock nextScreenLock;
    private final AtomicBoolean loggedInOrNot;
    public final AtomicBoolean normalUser;
    public final AtomicBoolean adminUser;
    private final AtomicBoolean userSet;
    private final AtomicBoolean adminSet;
    private ScreenToDisplay adminPanel;
    JTextArea textField;

    public JFrame getFrame() {
        return frame;
    }

    public JTextField getEvent() {
        return eventStatus;
    }

    class ImagePanel extends JComponent {

        private final Image image;
        JFrame frame;

        public ImagePanel(Image image, JFrame frame) {
            this.image = image;
            this.frame = frame;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, frame.getWidth(), frame.getHeight(), this);
        }
    }

    public FileTransferWindow() {
        loggedInOrNot = new AtomicBoolean(false);
        nextScreenLock = new ReentrantLock();
        loggedInScreens = new ArrayList<>();
        normalUser = new AtomicBoolean(false);
        adminUser = new AtomicBoolean(false);
        userSet = new AtomicBoolean(false);
        adminSet = new AtomicBoolean(false);
        adminPanel = null;

        registeredUsersFrame = new JFrame("RegisteredUsersInSystem");
        registeredUsersFrame.setSize(325, 500);
        registeredUsersFrame.setLayout(null);//using no layout managers  
        registeredUsersFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registeredUsersFrame.setLocation((getDefaultToolkit().getScreenSize().width - registeredUsersFrame.getWidth()) / 2
                - (getDefaultToolkit().getScreenSize().width / 2 - registeredUsersFrame.getWidth() / 2),
                (getDefaultToolkit().getScreenSize().height - registeredUsersFrame.getHeight()) / 2);

        textField = new JTextArea();
        textField.setEditable(false);
        Font fontr = new Font("Courier", Font.BOLD, 18);
        textField.setFont(fontr);
        textField.setForeground(Color.black);
        textField.setBackground(Color.cyan);
        textField.setBounds(
                0,
                0,
                (int) (registeredUsersFrame.getWidth()),
                (int) (registeredUsersFrame.getHeight()));
        registeredUsersFrame.getContentPane().add(textField);
        textField.setVisible(true);
        registeredUsersFrame.setVisible(true);

        frame = new JFrame("FileSharingClient");
        frame.setSize(450, 500);
        frame.setLayout(null);//using no layout managers  
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation((getDefaultToolkit().getScreenSize().width - frame.getWidth()) / 2,
                (getDefaultToolkit().getScreenSize().height - frame.getHeight()) / 2);

        try {
            BufferedImage myImage = ImageIO.read(new File("background.jpg"));

            ImagePanel ip = new ImagePanel(myImage, frame);

            frame.setContentPane(ip);

        } catch (IOException ex) {
            Logger.getLogger(FileTransferWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        logInStatus = new JTextField(new DefaultStyledDocument(), "Not signed in - Sign in or Sign up for further actions", MIN_PRIORITY);
        ((AbstractDocument) logInStatus.getDocument()).setDocumentFilter(new HighlightDocumentFilter(logInStatus));
        Font font = new Font("Courier", Font.BOLD, 18);
        logInStatus.setFont(font);
        logInStatus.setForeground(Color.red);
        logInStatus.setBackground(Color.CYAN);
        logInStatus.setEditable(false);
        logInStatus.setVisible(true);
        logInStatus.setBounds(
                (int) (0.1 * frame.getWidth()),
                0,
                (int) (0.5 * frame.getWidth()),
                (int) (0.08 * frame.getHeight()));
        frame.add(logInStatus);

        eventStatus = new JTextField(null, "No event occured", MIN_PRIORITY);
        Font font1 = new Font("Courier", Font.BOLD, 18);
        eventStatus.setFont(font1);
        eventStatus.setForeground(Color.GREEN);
        eventStatus.setBackground(Color.LIGHT_GRAY);
        eventStatus.setEditable(false);
        eventStatus.setVisible(true);
        eventStatus.setBounds(
                (int) (0.1 * frame.getWidth()),
                (int) (0.09 * frame.getHeight()),
                (int) (0.5 * frame.getWidth()),
                (int) (0.08 * frame.getHeight()));
        frame.add(eventStatus);

        JTextField field;
        ArrayList<JTextField> texts = new ArrayList<>();

        field = new JTextField("Username:", 20);
        field.setEditable(false);
        texts.add(field);

        field = new JTextField(20);
        texts.add(field);

        field = new JTextField("Password:", 20);
        field.setEditable(false);
        texts.add(field);

        field = new JTextField(20);
        texts.add(field);

        ArrayList<JTextField> textsRight = new ArrayList<>();

        field = new JTextField("Username:", 20);
        field.setEditable(false);
        textsRight.add(field);

        field = new JTextField(20);
        textsRight.add(field);

        field = new JTextField("Password:", 20);
        field.setEditable(false);
        textsRight.add(field);

        field = new JTextField(20);
        textsRight.add(field);

        screens = new ArrayList<>();
        screens.add(new ScreenToDisplay(
                "Sign in", "Sign up", texts, textsRight, frame));
        screens.get(0).makeVisible();

        textsRight = new ArrayList<>();
        field = new JTextField("Enter file/files name/names:", 20);
        field.setEditable(false);
        textsRight.add(field);

        field = new JTextField(30);
        textsRight.add(field);

        loggedInScreens.add(new ScreenToDisplay("Log out", "Upload", new ArrayList<>(), textsRight, frame));

        texts = new ArrayList<>();
        field = new JTextField("Enter file name:", 20);
        field.setEditable(false);
        texts.add(field);
        field = new JTextField(20);
        texts.add(field);
        field = new JTextField("Found file: uncertain", 20);
        field.setEditable(false);
        texts.add(field);

        textsRight = new ArrayList<>();
        field = new JTextField("Download state: unfinished", 20);
        field.setEditable(false);
        textsRight.add(field);

        loggedInScreens.add(new ScreenToDisplay("Find file", "Download", texts, textsRight, frame));

        texts = new ArrayList<>();
        field = new JTextField("Enter group name:", 20);
        field.setEditable(false);
        texts.add(field);
        field = new JTextField(20);
        texts.add(field);

        textsRight = new ArrayList<>();
        field = new JTextField("Delete group name:", 20);
        field.setEditable(false);
        textsRight.add(field);
        field = new JTextField(20);
        textsRight.add(field);
        loggedInScreens.add(new ScreenToDisplay("Create group", "Delete group", texts, textsRight, frame));

        texts = new ArrayList<>();
        field = new JTextField("Enter users:", 20);
        field.setEditable(false);
        texts.add(field);
        field = new JTextField(20);
        texts.add(field);
        field = new JTextField("Enter group:", 20);
        field.setEditable(false);
        texts.add(field);
        field = new JTextField(20);
        texts.add(field);

        textsRight = new ArrayList<>();
        field = new JTextField("Enter users:", 20);
        field.setEditable(false);
        textsRight.add(field);
        field = new JTextField(20);
        textsRight.add(field);
        field = new JTextField("Enter group:", 20);
        field.setEditable(false);
        textsRight.add(field);
        field = new JTextField(20);
        textsRight.add(field);
        loggedInScreens.add(new ScreenToDisplay("Add users", "Delete users", texts, textsRight, frame));

        texts = new ArrayList<>();
        field = new JTextField("Enter user:", 20);
        field.setEditable(false);
        texts.add(field);
        field = new JTextField(20);
        texts.add(field);

        loggedInScreens.add(new ScreenToDisplay("Delete user", "Log out", texts, new ArrayList<>(), frame));

        if (adminPanel == null) {
            adminPanel = loggedInScreens.get(loggedInScreens.size() - 1);
        }

        /*BufferedImage buttonIcon = null;
		try {
			buttonIcon = ImageIO.read(new File("butonLogIn.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        //gotToLeft =  new JButton(new ImageIcon(buttonIcon));
        goToLeft = new JButton("<");
        goToLeft.setFont(font);
        goToLeft.setBackground(Color.CYAN);
        goToLeft.setForeground(Color.red);
        goToLeft.setVisible(true);
        goToLeft.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                nextScreenLock.lock();
                try {
                    // TODO Auto-generated method stub
                    ArrayList<ScreenToDisplay> currentScreens = (loggedInOrNot.get())
                            ? loggedInScreens : screens;
                    currentScreens.get(index).makeNotVisible();
                    index = (char) ((index == 0) ? currentScreens.size() - 1 : index - 1);
                    currentScreens.get(index).makeVisible();
                } finally {
                    nextScreenLock.unlock();
                }
            }
        });
        frame.add(goToLeft);

        goToRight = new JButton(">");
        goToRight.setFont(font);
        goToRight.setBackground(Color.CYAN);
        goToRight.setForeground(Color.red);
        goToRight.setVisible(true);
        goToRight.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                nextScreenLock.lock();
                try {
                    // TODO Auto-generated method stub
                    ArrayList<ScreenToDisplay> currentScreens = (loggedInOrNot.get())
                            ? loggedInScreens : screens;
                    currentScreens.get(index).makeNotVisible();
                    index = (char) ((index == currentScreens.size() - 1) ? 0 : index + 1);
                    currentScreens.get(index).makeVisible();
                } finally {
                    nextScreenLock.unlock();
                }
            }
        });
        frame.add(goToRight);

        frame.setVisible(true);
    }

    @Override
    public void run() {

        while (this.frame.isVisible()) {

            nextScreenLock.lock();
            try {
                if (this.loggedInOrNot.get()) {
                    loggedInScreens.get(index).updateItems(this.frame);
                } else {
                    if (index > 0) {
                        index = 0;
                    }
                    screens.get(index).updateItems(this.frame);
                }
            } finally {
                nextScreenLock.unlock();
            }

            logInStatus.setBounds(
                    (int) (0.1 * frame.getWidth()),
                    0,
                    (int) (0.5 * frame.getWidth()),
                    (int) (0.08 * frame.getHeight()));

            eventStatus.setBounds(
                    (int) (0.1 * frame.getWidth()),
                    (int) (0.09 * frame.getHeight()),
                    (int) (0.5 * frame.getWidth()),
                    (int) (0.08 * frame.getHeight()));

            textField.setBounds(
                    0,
                    0,
                    (int) (registeredUsersFrame.getWidth()),
                    (int) (registeredUsersFrame.getHeight()));

            if (adminUser.get()) {
                if (!adminSet.get()) {
                    //System.out.println("ADMIN");
                    goToLeft.setVisible(false);
                    goToRight.setVisible(false);

                    if (adminPanel != null && loggedInScreens.size() < 0x5) {
                        loggedInScreens.add(adminPanel);
                    }

                    if (loggedInOrNot.get()) {
                        for (int i = 0; i < loggedInScreens.size(); i++) {
                            loggedInScreens.get(i).makeNotVisible();
                        }
                        // range 0->4
                        index = (char) (loggedInScreens.size() - 1);
                        //System.out.println("INDEX: " + (int) index);
                        loggedInScreens.get(index).makeVisible();

                        adminSet.set(true);
                    }
                }

            } else if (normalUser.get()) {

                if (!userSet.get()) {
                    if (loggedInOrNot.get()) {

                        goToLeft.setVisible(true);
                        goToRight.setVisible(true);

                        if (loggedInScreens.size() == 5) {
                            loggedInScreens.get(loggedInScreens.size() - 1).makeNotVisible();
                            loggedInScreens.remove(loggedInScreens.size() - 1);
                        }
                        //System.out.println("Size when Normal: " + loggedInScreens.size());

                        userSet.set(true);

                    }
                }
            }
            goToLeft.setBounds(
                    (int) (0.65 * frame.getWidth()),
                    0,
                    (int) (0.1 * frame.getWidth()),
                    (int) (0.15 * frame.getHeight()));
            goToRight.setBounds(
                    (int) (0.8 * frame.getWidth()),
                    0,
                    (int) (0.1 * frame.getWidth()),
                    (int) (0.15 * frame.getHeight()));
            try {
                Thread.currentThread().sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
            }
        }
    }

    public boolean getEventStatus() {
        boolean rez;
        nextScreenLock.lock();
        try {
            rez = (loggedInOrNot.get()) ? loggedInScreens.get(index).hasEvent.get()
                    : screens.get(index).hasEvent.get();
        } finally {
            nextScreenLock.unlock();
        }
        return rez;
    }

    public ArrayList<String> getEventData() {
        ArrayList<String> strArr;
        nextScreenLock.lock();
        try {
            strArr = (loggedInOrNot.get()) ? loggedInScreens.get(index).eventResult
                    : screens.get(index).eventResult;
        } finally {
            nextScreenLock.unlock();
        }

        return strArr;
    }

    public void resetEvent() {
        nextScreenLock.lock();
        try {
            if (loggedInOrNot.get()) {
                loggedInScreens.get(index).hasEvent.set(false);
                loggedInScreens.get(index).eventResult.clear();
            } else {
                screens.get(index).hasEvent.set(false);
                screens.get(index).eventResult.clear();
            }
        } finally {
            nextScreenLock.unlock();
        }
    }

    public class HighlightDocumentFilter extends DocumentFilter {

        private final DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(Color.YELLOW);
        private final JTextComponent field;
        private final SimpleAttributeSet background;

        public HighlightDocumentFilter(JTextComponent field) {
            this.field = field;
            background = new SimpleAttributeSet();
            StyleConstants.setBackground(background, Color.RED);
        }

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            super.insertString(fb, offset, text, attr);
        }

        @Override
        public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

            String[] textArr = text.split(" ");
            String match = textArr[textArr.length - 1];

            super.replace(fb, offset, length, text, attrs);

            if (match.equals("actions")) {
                return;
            }

            int startIndex = text.indexOf(match);
            if (startIndex >= 0) {

                String last = fb.getDocument().getText(startIndex, match.length()).trim();
                if (last.equalsIgnoreCase(match)) {

                    field.getHighlighter().addHighlight(startIndex, startIndex + match.length(), highlightPainter);
                }
            }
        }
    }

    public void setLoggedIn(String name, String type) throws BadLocationException {
        this.logInStatus.setText("Signed in as " + type + " " + name);
        this.eventStatus.setText("You signed in");
        this.loggedInOrNot.set(true);
        this.screens.get(this.index).makeNotVisible();
        this.index = 0;
        this.loggedInScreens.get(0).makeVisible();
    }

    public void setLoggedOut() {
        this.logInStatus.setText("Not signed in - Sign in or Sign up for further actions");
        this.eventStatus.setText("You logged out");
        this.loggedInOrNot.set(false);
        this.loggedInScreens.get(this.index).makeNotVisible();
        this.index = 0;
        this.adminSet.set(false);
        this.userSet.set(false);
        this.normalUser.set(false);
        this.adminUser.set(false);
        this.screens.get(0).makeVisible();
    }

    public void setFoundFileMessage(String name) {
        this.loggedInScreens.get(1).leftFields.get(2).setText("Found file: " + name);
    }

    public void setDownloadStatusMessage(String name) {
        this.loggedInScreens.get(1).rightFields.get(0).setText("Download state: " + name);
    }

}
