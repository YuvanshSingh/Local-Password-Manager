import javax.swing.*;
import java.awt.*;
import java.io.*;

public class LoginFrame extends JFrame {
    private JPasswordField passwordField;
    private JLabel status;

    public LoginFrame() {
        setTitle("Login - Password Manager");
        setSize(480, 230);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null); // Center window

        JLabel label = new JLabel("Enter Master Password:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBounds(140, 20, 200, 25);
        add(label);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 50, 270, 30);
        add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(30, 100, 120, 30);
        add(loginBtn);

        JButton resetBtn = new JButton("Reset Master");
        resetBtn.setBounds(170, 100, 130, 30);
        add(resetBtn);

        JButton changeBtn = new JButton("Change Master");
        changeBtn.setBounds(310, 100, 130, 30);
        add(changeBtn);

        status = new JLabel("");
        status.setBounds(30, 150, 400, 20);
        status.setForeground(Color.RED);
        add(status);

        loginBtn.addActionListener(e -> handleLogin());
        resetBtn.addActionListener(e -> resetMaster());
        changeBtn.addActionListener(e -> changeMasterPassword());

        setVisible(true);
    }

    private void handleLogin() {
        String enteredPass = new String(passwordField.getPassword()).trim();
        try {
            File masterFile = new File("master.pass");
            File dataFile = new File("passwords.dat");

            if (!masterFile.exists() && dataFile.exists()) {
                status.setText("Security error: Master password missing!");
                return;
            }

            if (!masterFile.exists()) {
                String hash = PasswordUtils.hash(enteredPass);
                FileWriter fw = new FileWriter(masterFile);
                fw.write(hash);
                fw.flush();
                fw.close();
                AESUtil.setMasterPassword(enteredPass);
                status.setForeground(new Color(0, 128, 0));
                status.setText("Master password set.");
                openManager();
            } else {
                BufferedReader br = new BufferedReader(new FileReader(masterFile));
                String storedHash = br.readLine();
                br.close();
                if (PasswordUtils.hash(enteredPass).equals(storedHash)) {
                    AESUtil.setMasterPassword(enteredPass);
                    openManager();
                } else {
                    status.setText("Incorrect password.");
                }
            }
        } catch (Exception ex) {
            status.setText("Error: " + ex.getMessage());
        }
    }

    private void resetMaster() {
        File masterFile = new File("master.pass");
        File dataFile = new File("passwords.dat");

        if (dataFile.exists()) {
            status.setText("Reset blocked: Password data exists.");
            return;
        }

        if (masterFile.exists()) {
            masterFile.delete();
            status.setForeground(Color.BLUE);
            status.setText("Master password reset. Please set a new one.");
        } else {
            status.setText("No master password set yet.");
        }
    }

    private void changeMasterPassword() {
        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();

        Object[] fields = {
            "Current Password:", oldPass,
            "New Password:", newPass,
            "Confirm New Password:", confirmPass
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Change Master Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String oldPwd = new String(oldPass.getPassword());
                String newPwd = new String(newPass.getPassword());
                String confirmPwd = new String(confirmPass.getPassword());

                BufferedReader br = new BufferedReader(new FileReader("master.pass"));
                String storedHash = br.readLine();
                br.close();

                if (!PasswordUtils.hash(oldPwd).equals(storedHash)) {
                    status.setText("Old password incorrect.");
                    return;
                }

                if (!newPwd.equals(confirmPwd)) {
                    status.setText("New passwords do not match.");
                    return;
                }

                AESUtil.setMasterPassword(oldPwd);
                BufferedReader reader = new BufferedReader(new FileReader("passwords.dat"));
                StringBuilder updatedData = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", 3);
                    if (parts.length == 3) {
                        String decrypted = AESUtil.decrypt(parts[2]);
                        AESUtil.setMasterPassword(newPwd);
                        String reEncrypted = AESUtil.encrypt(decrypted);
                        updatedData.append(parts[0]).append(",").append(parts[1]).append(",").append(reEncrypted).append("\n");
                        AESUtil.setMasterPassword(oldPwd);  // switch back
                    }
                }
                reader.close();

                AESUtil.setMasterPassword(newPwd);
                BufferedWriter writer = new BufferedWriter(new FileWriter("passwords.dat"));
                writer.write(updatedData.toString());
                writer.close();

                BufferedWriter hashWriter = new BufferedWriter(new FileWriter("master.pass"));
                hashWriter.write(PasswordUtils.hash(newPwd));
                hashWriter.close();

                status.setForeground(new Color(0, 128, 0));
                status.setText("Master password changed.");
            } catch (Exception ex) {
                status.setText("Error changing password: " + ex.getMessage());
            }
        }
    }

    private void openManager() {
        dispose();
        new PasswordManagerFrame();
    }
}
