
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class ScreenToDisplay {

    private final JButton leftButton, rightButton, leftSubmit, rightSubmit;
    public final ArrayList<JTextField> leftFields, rightFields;
    private final AtomicBoolean leftSideStatus, rightSideStatus;
    private final double leftItemHeightPercentage;
    private final double rightItemHeightPercentage;
    public final AtomicBoolean hasEvent;
    public final ArrayList<String> eventResult;

    public ScreenToDisplay(
            String leftButtonName,
            String rightButtonName,
            ArrayList<JTextField> leftFieldsz,
            ArrayList<JTextField> rightFieldsz,
            JFrame ownerFrame) {
        int frameWidth = ownerFrame.getWidth(),
                frameHeight = ownerFrame.getHeight();
        double currentHeight;
        hasEvent = new AtomicBoolean(false);
        eventResult = new ArrayList<>();

        //leftItemHeightPercentage = (0.8 - 0.05 * (leftFieldsz.size() + 2.5)) / (2 + leftFieldsz.size());
        leftItemHeightPercentage = 0.08;

        this.leftFields = leftFieldsz;

        currentHeight = 0.2;

        // Se initializeaza apasarea butonului cu false.
        leftSideStatus = new AtomicBoolean(true);

        // Se initializeaza butonul.
        leftButton = new JButton(leftButtonName);
        leftButton.setForeground(Color.WHITE);
        leftButton.setBackground(Color.BLACK);
        leftButton.setBounds(
                (int) (0.1 * frameWidth),
                (int) (currentHeight * frameHeight),
                (int) (0.35 * frameWidth),
                (int) (0.7 * frameHeight));
        leftButton.setVisible(false);
        ownerFrame.add(leftButton);

        // Se seteaza pozitia butonului de submit.
        leftSubmit = new JButton("Submit");
        leftSubmit.setForeground(Color.WHITE);
        leftSubmit.setBackground(Color.BLACK);
        leftSubmit.setBounds(
                (int) (0.1 * frameWidth),
                (int) (currentHeight * frameHeight),
                (int) (0.35 * frameWidth),
                (int) (leftItemHeightPercentage * frameHeight));
        leftSubmit.setVisible(false);
        leftSubmit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (!hasEvent.get()) {
                    //eventResult.add(leftButton.getName());
                    eventResult.add(leftButton.getText());
                    System.out.println(leftButton.getText() + " ");
                    for (JTextField textField : leftFields) {
                        if (textField.isEditable()) {
                            eventResult.add(textField.getText());
                        }
                    }
                    hasEvent.set(true);

                    //System.out.println("60 aici");
                }
            }
        });
        ownerFrame.add(leftSubmit);

        leftButton.addActionListener(new ButtonActionListener(
                leftFields, leftSideStatus, leftSubmit));

        // Se seteaza urmatoare inaltime la care se poate pune un item pe
        // ecran.
        currentHeight += leftItemHeightPercentage + 0.05;

        // Se seteaza pozitiile field-urilor.
        for (JTextField textField : leftFields) {
            textField.setBounds(
                    (int) (0.1 * frameWidth),
                    (int) (currentHeight * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (leftItemHeightPercentage * frameHeight));
            textField.setVisible(false);
            ownerFrame.add(textField);

            currentHeight += leftItemHeightPercentage + 0.05;
        }

        //rightItemHeightPercentage = (0.8 - 0.05 * (rightFieldsz.size() + 2.5)) / (2 + rightFieldsz.size());
        rightItemHeightPercentage = 0.08;
        this.rightFields = rightFieldsz;

        currentHeight = 0.2;

        // Se initializeaza apasarea butonului cu false.
        rightSideStatus = new AtomicBoolean(true);

        // Se seteaza pozitia butonului de submit.
        rightSubmit = new JButton("Submit");
        rightSubmit.setForeground(Color.WHITE);
        rightSubmit.setBackground(Color.BLACK);
        rightSubmit.setBounds(
                (int) (0.55 * frameWidth),
                (int) (currentHeight * frameHeight),
                (int) (0.35 * frameWidth),
                (int) (rightItemHeightPercentage * frameHeight));
        rightSubmit.setVisible(false);
        rightSubmit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (!hasEvent.get()) {
                    eventResult.add(rightButton.getText());
                    for (JTextField textField : rightFields) {
                        if (textField.isEditable()) {
                            eventResult.add(textField.getText());
                        }
                    }
                    hasEvent.set(true);
                }
            }
        });
        ownerFrame.add(rightSubmit);

        // Se initializeaza butonul.
        rightButton = new JButton(rightButtonName);
        rightButton.setForeground(Color.WHITE);
        rightButton.setBackground(Color.BLACK);
        rightButton.setBounds(
                (int) (0.55 * frameWidth),
                (int) (currentHeight * frameHeight),
                (int) (0.35 * frameWidth),
                (int) (0.7 * frameHeight));
        rightButton.addActionListener(new ButtonActionListener(
                rightFields, rightSideStatus, rightSubmit));
        rightButton.setVisible(false);
        ownerFrame.add(rightButton);

        // Se seteaza urmatoare inaltime la care se poate pune un item pe
        // ecran.
        currentHeight += rightItemHeightPercentage + 0.05;

        // Se seteaza pozitiile field-urilor.
        for (JTextField textField : rightFields) {
            textField.setBounds(
                    (int) (0.55 * frameWidth),
                    (int) (currentHeight * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (rightItemHeightPercentage * frameHeight));
            textField.setVisible(false);
            ownerFrame.add(textField);

            currentHeight += rightItemHeightPercentage + 0.05;
        }

    }

    public void makeVisible() {
        this.leftButton.setVisible(true);
        this.rightButton.setVisible(true);
        this.leftSideStatus.set(true);
        this.rightSideStatus.set(true);
    }

    public void makeNotVisible() {
        this.leftButton.setVisible(false);
        this.rightButton.setVisible(false);
        if (!this.leftSideStatus.get()) {
            this.leftSideStatus.set(true);

            this.leftSubmit.setVisible(false);
            for (JTextField textField : leftFields) {
                textField.setVisible(false);
            }
        }
        if (!this.rightSideStatus.get()) {
            this.rightSideStatus.set(true);

            this.rightSubmit.setVisible(false);
            for (JTextField textField : rightFields) {
                textField.setVisible(false);
            }
        }
    }

    public void updateItems(JFrame ownerFrame) {
        int frameWidth = ownerFrame.getWidth(),
                frameHeight = ownerFrame.getHeight();
        if (leftSideStatus.get()) {
            leftButton.setBounds(
                    (int) (0.1 * frameWidth),
                    (int) (0.2 * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (0.1 * frameHeight));
        } else {
            double currentHeight = 0.2;
            leftButton.setBounds(
                    (int) (0.1 * frameWidth),
                    (int) (0.2 * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (leftItemHeightPercentage * frameHeight));

            currentHeight += leftItemHeightPercentage + 0.05;

            // Se seteaza pozitiile field-urilor.
            for (JTextField textField : leftFields) {
                textField.setBounds(
                        (int) (0.1 * frameWidth),
                        (int) (currentHeight * frameHeight),
                        (int) (0.35 * frameWidth),
                        (int) (leftItemHeightPercentage * frameHeight));

                currentHeight += leftItemHeightPercentage + 0.05;
            }

            leftSubmit.setBounds(
                    (int) (0.1 * frameWidth),
                    (int) (currentHeight * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (leftItemHeightPercentage * frameHeight));
        }
        if (rightSideStatus.get()) {
            rightButton.setBounds(
                    (int) (0.55 * frameWidth),
                    (int) (0.2 * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (0.1 * frameHeight));
        } else {
            double currentHeight = 0.2;
            rightButton.setBounds(
                    (int) (0.55 * frameWidth),
                    (int) (0.2 * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (rightItemHeightPercentage * frameHeight));

            currentHeight += rightItemHeightPercentage + 0.05;

            // Se seteaza pozitiile field-urilor.
            for (JTextField textField : rightFields) {
                textField.setBounds(
                        (int) (0.55 * frameWidth),
                        (int) (currentHeight * frameHeight),
                        (int) (0.35 * frameWidth),
                        (int) (rightItemHeightPercentage * frameHeight));

                currentHeight += rightItemHeightPercentage + 0.05;
            }

            rightSubmit.setBounds(
                    (int) (0.55 * frameWidth),
                    (int) (currentHeight * frameHeight),
                    (int) (0.35 * frameWidth),
                    (int) (rightItemHeightPercentage * frameHeight));
        }
    }
}
