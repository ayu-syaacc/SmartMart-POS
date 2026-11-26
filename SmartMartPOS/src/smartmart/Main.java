package smartmart;

import javax.swing.*;

/** Titik masuk aplikasi SmartMart POS. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
        UIManager.put("OptionPane.messageFont", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("Label.font", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("Button.font", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("RadioButton.font", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("CheckBox.font", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("ComboBox.font", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("Spinner.font", Ui.f(java.awt.Font.PLAIN, 13));
        UIManager.put("TextField.font", Ui.f(java.awt.Font.PLAIN, 13));
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
