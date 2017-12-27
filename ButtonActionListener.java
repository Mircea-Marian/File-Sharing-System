
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JTextField;

public class ButtonActionListener implements ActionListener {

    private final ArrayList<JTextField> fields;
    private final AtomicBoolean status;
    private final JButton submit;

    public ButtonActionListener(ArrayList<JTextField> fields, AtomicBoolean status, JButton submit) {
        this.fields = fields;
        this.status = status;
        this.submit = submit;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if (status.get()) {
            status.set(false);

            for (JTextField textField : fields) {
                textField.setVisible(true);

                if (textField.isEditable()) {
                    textField.setText("");
                }
            }

            submit.setVisible(true);
        } else {
            status.set(true);

            for (JTextField textField : fields) {
                textField.setVisible(false);
            }

            submit.setVisible(false);
        }
    }

}
