import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class PasswordManagerFrame extends JFrame {
    private JTextField siteField, userField, passField;
    private JTextArea display;

    public PasswordManagerFrame() {
        setTitle("Password Manager");
        setSize(540, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel siteLabel = new JLabel("Site:");
        siteLabel.setBounds(30, 20, 80, 25);
        add(siteLabel);
        siteField = new JTextField();
        siteField.setBounds(100, 20, 380, 25);
        add(siteField);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30, 55, 80, 25);
        add(userLabel);
        userField = new JTextField();
        userField.setBounds(100, 55, 380, 25);
        add(userField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 90, 80, 25);
        add(passLabel);
        passField = new JTextField();
        passField.setBounds(100, 90, 380, 25);
        add(passField);

        JButton addBtn = new JButton("Add Entry");
        addBtn.setBounds(30, 135, 110, 30);
        add(addBtn);

        JButton viewBtn = new JButton("View All");
        viewBtn.setBounds(150, 135, 110, 30);
        add(viewBtn);

        JButton updateBtn = new JButton("Update Entry");
        updateBtn.setBounds(270, 135, 130, 30);
        add(updateBtn);

        JButton deleteBtn = new JButton("Delete Entry");
        deleteBtn.setBounds(410, 135, 110, 30);
        add(deleteBtn);

        display = new JTextArea();
        display.setEditable(false);
        JScrollPane scroll = new JScrollPane(display);
        scroll.setBounds(30, 180, 490, 300);
        add(scroll);

        addBtn.addActionListener(e -> saveEntry());
        viewBtn.addActionListener(e -> loadEntries());
        updateBtn.addActionListener(e -> updateEntry());
        deleteBtn.addActionListener(e -> deleteEntry());

        setVisible(true);
    }

    private void saveEntry() {
        try {
            String site = siteField.getText().trim();
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                display.setText("Error: Site, Username, and Password cannot be empty.");
                return;
            }

            String encryptedPass = AESUtil.encrypt(pass);

            BufferedWriter bw = new BufferedWriter(new FileWriter("passwords.dat", true));
            bw.write(site + "," + user + "," + encryptedPass);
            bw.newLine();
            bw.close();

            siteField.setText("");
            userField.setText("");
            passField.setText("");
            display.setText("Entry added successfully.");
        } catch (Exception ex) {
            display.setText("Error: " + ex.getMessage());
        }
    }

    private void loadEntries() {
        display.setText("");
        File file = new File("passwords.dat");

        if (!file.exists() || file.length() == 0) {
            display.setText("No entries to display.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean hasData = false;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    String site = parts[0];
                    String user = parts[1];
                    String decryptedPass = AESUtil.decrypt(parts[2]);
                    display.append("Site: " + site + "\nUser: " + user + "\nPass: " + decryptedPass + "\n\n");
                    hasData = true;
                }
            }
            if (!hasData) {
                display.setText("No valid entries found.");
            }
        } catch (Exception ex) {
            display.setText("Error reading file: " + ex.getMessage());
        }
    }

    private void updateEntry() {
        String targetSite = siteField.getText().trim();
        String targetUser = userField.getText().trim();
        String newPass = passField.getText().trim();

        if (targetSite.isEmpty() || targetUser.isEmpty() || newPass.isEmpty()) {
            display.setText("Error: Site, Username, and Password cannot be empty.");
            return;
        }

        try {
            File file = new File("passwords.dat");
            BufferedReader br = new BufferedReader(new FileReader(file));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            boolean updated = false;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3 && parts[0].equals(targetSite) && parts[1].equals(targetUser)) {
                    String encNewPass = AESUtil.encrypt(newPass);
                    lines.add(targetSite + "," + targetUser + "," + encNewPass);
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
            br.close();

            if (updated) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                for (String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
                bw.close();
                display.setText("Entry updated for: " + targetSite);
            } else {
                display.setText("No matching entry found to update.");
            }

        } catch (Exception ex) {
            display.setText("Error updating: " + ex.getMessage());
        }
    }

    private void deleteEntry() {
        String targetSite = siteField.getText().trim();
        String targetUser = userField.getText().trim();

        if (targetSite.isEmpty() || targetUser.isEmpty()) {
            display.setText("Error: Site and Username must be filled to delete.");
            return;
        }

        try {
            File file = new File("passwords.dat");
            if (!file.exists()) {
                display.setText("No data file found.");
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            boolean deleted = false;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3 && parts[0].equals(targetSite) && parts[1].equals(targetUser)) {
                    deleted = true;
                    continue;
                }
                lines.add(line);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
            bw.close();

            if (deleted) {
                display.setText("Entry deleted for: " + targetSite);
            } else {
                display.setText("No matching entry found to delete.");
            }

        } catch (Exception ex) {
            display.setText("Error deleting: " + ex.getMessage());
        }
    }
}
