//New working code
package services;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class ServiceManager {
    private JFrame frame;
    private JTable servicesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;
    private List<ServiceInfo> allServices;

    private List<String> configuredServices;
    
    private JToggleButton showSelectedButton;
    private JToggleButton showAllButton;
    private boolean showingAllServices = false;

    public ServiceManager(String services) {
        this.configuredServices = Arrays.asList(services.split(","));
        initializeUI();
        fetchAndDisplayConfiguredServices();
    }

    private void fetchAndDisplayConfiguredServices() {
        SwingWorker<List<ServiceInfo>, Void> worker = new SwingWorker<>() {
            protected List<ServiceInfo> doInBackground() {
                updateStatus("Fetching services...");
                return fetchServicesFromSystem();
            }

            @Override
            protected void done() {
                try {
                    allServices = get();
                    toggleView(false); // Show configured services by default
                    updateStatus("Services fetched successfully. Total services: " + allServices.size());
                } catch (Exception e) {
                    updateStatus("Error fetching services: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    private void initializeUI() {
        frame = new JFrame("PROLIM Service Manager");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255,255,255));
        headerPanel.setPreferredSize(new Dimension(1000, 85));

        URL imageUrl = getClass().getResource("/pngprolim.png");
        if (imageUrl == null) {
            throw new IllegalArgumentException("Resource not found: prolimlogopng.png");
        }
        
        ImageIcon logoIcon = new ImageIcon(imageUrl);
        Image logoImage = logoIcon.getImage();
        Image resizedLogo = logoImage.getScaledInstance(200, 100, Image.SCALE_SMOOTH);
        ImageIcon resizedLogoIcon = new ImageIcon(resizedLogo);

        JLabel titleLabel = new JLabel(resizedLogoIcon);
        titleLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        headerPanel.add(titleLabel, BorderLayout.WEST);
      
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setOpaque(false);
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setForeground(Color.BLACK);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        searchPanel.setVisible(false);

        // View Toggle Panel
        JPanel viewTogglePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        viewTogglePanel.setOpaque(false);
        viewTogglePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        showSelectedButton = createStyledToggleButton("CONFIGURED");
        showAllButton = createStyledToggleButton("ALL");
        showSelectedButton.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(showSelectedButton);
        buttonGroup.add(showAllButton);

        viewTogglePanel.add(showSelectedButton);
        viewTogglePanel.add(showAllButton);
        headerPanel.add(viewTogglePanel, BorderLayout.EAST);

        frame.add(headerPanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Services Table
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Service Name");
        tableModel.addColumn("Display Name");
        tableModel.addColumn("Status");

        servicesTable = new JTable(tableModel);
        servicesTable.setDefaultRenderer(Object.class, new ServiceTableCellRenderer());
        servicesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(servicesTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        showSelectedButton.addActionListener(e -> toggleView(false));
        showAllButton.addActionListener(e -> toggleView(true));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(createButton("Start Selected", new Color(0, 128, 0)));
        buttonPanel.add(createButton("Stop Selected", new Color(178, 34, 34)));
        buttonPanel.add(createButton("Start All", new Color(0, 128, 0)));
        buttonPanel.add(createButton("Stop All", new Color(178, 34, 34)));

        // Status Label
        statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Add search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterServices();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterServices();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterServices();
            }
        });
    }

    private JToggleButton createStyledToggleButton(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setPreferredSize(new Dimension(120, 30));
        button.setBackground(new Color(173, 216, 230)); // Light blue color
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2), // Cornflower blue border
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(135, 206, 250)); // Lighter blue on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(173, 216, 230)); // Back to original light blue
            }
        });

        return button;
    }
    private void toggleView(boolean showAll) {
        showingAllServices = showAll;
        if (showAll) {
            updateServicesTable(allServices);
        } else {
            List<ServiceInfo> filteredServices = allServices.stream()
                .filter(service -> configuredServices.contains(service.getName()))
                .collect(Collectors.toList());
            updateServicesTable(filteredServices);
        }
        filterServices(); // Apply current search filter
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            } else {
                System.err.println("Invalid argument format: " + arg);
            }
        }
        return map;
    }

    private static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }

    private static Map<String, String> configureApplication(String[] args) {
        Map<String, String> propertiesMap = parseArguments(args);
        if (propertiesMap.isEmpty()) {
            System.err.println("No valid arguments provided.");
            return null;
        }

        Properties properties = loadProperties(propertiesMap.get("configFile"));
        if (properties == null) {
            System.err.println("Failed to load properties from file: " + propertiesMap.get("configfile"));
            return null;
        }

        propertiesMap.putAll(propertiesToMap(properties));

        // Read services from the config file
        String services = properties.getProperty("services");
        if (services != null && !services.isEmpty()) {
            propertiesMap.put("services", services);
        } else {
            System.err.println("No services specified in the config file.");
        }

        return propertiesMap;
    }
    private static Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return map;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(e -> performAction(text));
        return button;
    }

    private void performAction(String action) {
        switch (action) {
            case "Start Selected":
                enableSelectedServices();
                break;
            case "Stop Selected":
                disableSelectedServices();
                break;
            case "Start All":
                enableAllDisplayedServices();
                break;
            case "Stop All":
            	disableAllDisplayedServices();
                break;
            case "Refresh":
                fetchAndDisplayServices();
                break;
            case "Select All":
                selectAllServices();
                break;
        }
    }

    private void fetchAndDisplayServices() {
        SwingWorker<List<ServiceInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceInfo> doInBackground() {
                updateStatus("Fetching services...");
                return fetchServicesFromSystem();
            }

            @Override
            protected void done() {
                try {
                    allServices = get();
                    updateServicesTable(allServices);
                    updateStatus("Services fetched successfully. Total services: " + allServices.size());
                    if (searchField.getText().trim().isEmpty()) {
                        // If search field is empty, show all services
                        updateServicesTable(allServices);
                    } else {
                        // If search field has text, filter and show matching services
                        filterServices();
                    }
                } catch (Exception e) {
                    updateStatus("Error fetching services: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private List<ServiceInfo> fetchServicesFromSystem() {
        List<ServiceInfo> serviceInfos = new ArrayList<>();
        String command = "sc query state= all";

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            ServiceInfo currentService = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SERVICE_NAME:")) {
                    if (currentService != null) {
                        serviceInfos.add(currentService);
                    }
                    String serviceName = line.substring("SERVICE_NAME:".length()).trim();
                    currentService = new ServiceInfo(serviceName, "", "");
                } else if (line.trim().startsWith("STATE")) {
                    if (currentService != null) {
                        String state = line.substring(line.lastIndexOf(":") + 1).trim().split(" ")[0];
                        currentService.setStatus(state);
                    }
                } else if (line.startsWith("DISPLAY_NAME:") && currentService != null) {
                    String displayName = line.substring("DISPLAY_NAME:".length()).trim();
                    currentService.setDisplayName(displayName);
                }
            }
            if (currentService != null) {
                serviceInfos.add(currentService);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();


 updateStatus("Error fetching services: " + e.getMessage());
        }

        return serviceInfos;
    }

    private void updateServicesTable(List<ServiceInfo> services) {
        tableModel.setRowCount(0);
        for (ServiceInfo service : services) {
            tableModel.addRow(new Object[]{service.getName(), service.getDisplayName(), service.getStatus()});
        }
    }
    private void filterServices() {
        String searchText = searchField.getText().toLowerCase().trim();
        List<ServiceInfo> filtered = allServices.stream()
                .filter(service -> showingAllServices || configuredServices.contains(service.getName()))
                .filter(service -> service.getDisplayName().toLowerCase().contains(searchText) ||
                        service.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        updateServicesTable(filtered);
        updateStatus("Filtered services: " + filtered.size());
    }

    private void enableSelectedServices() {
        int[] selectedRows = servicesTable.getSelectedRows();
        for (int row : selectedRows) {
            String serviceName = (String) servicesTable.getValueAt(row, 0);
            ServiceInfo serviceInfo = allServices.stream()
                    .filter(service -> service.getName().equals(serviceName))
                    .findFirst()
                    .orElse(null);
            if (serviceInfo != null) {
                enableService(serviceInfo);
            }
        }
    }

    private void disableSelectedServices() {
        int[] selectedRows = servicesTable.getSelectedRows();
        for (int row : selectedRows) {
            String serviceName = (String) servicesTable.getValueAt(row, 0);
            ServiceInfo serviceInfo = allServices.stream()
                    .filter(service -> service.getName().equals(serviceName))
                    .findFirst()
                    .orElse(null);
            if (serviceInfo != null) {
                disableService(serviceInfo);
            }
        }
    }

    private void enableAllDisplayedServices() {
        updateStatus("Enabling all displayed services...");
        List<ServiceInfo> displayedServices = getCurrentDisplayedServices();
        for (ServiceInfo serviceInfo : displayedServices) {
            enableService(serviceInfo);
        }
    }

    private void disableAllDisplayedServices() {
        updateStatus("Enabling all displayed services...");
        List<ServiceInfo> displayedServices = getCurrentDisplayedServices();
        for (ServiceInfo serviceInfo : displayedServices) {
            disableService(serviceInfo);
        }
    }

    private List<ServiceInfo> getCurrentDisplayedServices() {
        List<ServiceInfo> displayedServices = new ArrayList<>();
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String serviceName = (String) tableModel.getValueAt(i, 0);
            ServiceInfo serviceInfo = allServices.stream()
                    .filter(service -> service.getName().equals(serviceName))
                    .findFirst()
                    .orElse(null);
            if (serviceInfo != null) {
                displayedServices.add(serviceInfo);
            }
        }
        return displayedServices;
    }

    private void disableAllServices() {
        updateStatus("Disabling all services...");
        for (ServiceInfo serviceInfo : allServices) {
            disableService(serviceInfo);
        }
    }

    private void selectAllServices() {
        int rowCount = servicesTable.getRowCount();
        if (rowCount > 0) {
            servicesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            servicesTable.setRowSelectionInterval(0, rowCount - 1);
        }
    }

    private void enableService(ServiceInfo serviceInfo) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus("Enabling service: " + serviceInfo.getDisplayName());
                String command = "sc start \"" + serviceInfo.getName() + "\"";
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    serviceInfo.setStatus("RUNNING");
                    updateStatus("Service enabled successfully: " + serviceInfo.getDisplayName());
                } else {
                    updateStatus("Failed to enable service: " + serviceInfo.getDisplayName());
                }
                return null;
            }

            @Override
            protected void done() {
                updateServiceStatus(serviceInfo);
            }
        };
        worker.execute();
    }

    private void disableService(ServiceInfo serviceInfo) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus("Disabling service: " + serviceInfo.getDisplayName());
                String command = "sc stop \"" + serviceInfo.getName() + "\"";
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    serviceInfo.setStatus("STOPPED");
                    updateStatus("Service disabled successfully: " + serviceInfo.getDisplayName());
                } else {
                    updateStatus("Failed to disable service: " + serviceInfo.getDisplayName());
                }
                return null;
            }

            @Override
            protected void done() {
                updateServiceStatus(serviceInfo);
            }
        };
        worker.execute();
    }

    private void updateServiceStatus(ServiceInfo serviceInfo) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus("Updating status for service: " + serviceInfo.getDisplayName());
                String command = "sc query \"" + serviceInfo.getName() + "\"";
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("STATE")) {
                        String state = line.substring(line.lastIndexOf(":") + 1).trim().split(" ")[0];
                        serviceInfo.setStatus(state);
                        break;
                    }
                }
                process.waitFor();
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        if (tableModel.getValueAt(row, 0).equals(serviceInfo.getName())) {
                            tableModel.setValueAt(serviceInfo.getStatus(), row, 2);
                            break;
                        }
                    }
                    updateStatus("Status updated for service: " + serviceInfo.getDisplayName());
                });
            }
        };
        worker.execute();
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public static void main(String[] args) {
    	
    	 long startTime = System.currentTimeMillis();
        Map<String, String> propertiesMap = configureApplication(args);
        if (propertiesMap == null) {
            return; // Configuration failed
        }

        String services = propertiesMap.get("services");

        if (isAdministrator()) {
            SwingUtilities.invokeLater(() -> new ServiceManager(services));
        } else {
            JOptionPane.showMessageDialog(null, "Alert : Run the ServiceManager in Run as administrator.",
                    "Administrator Privileges Required", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Configuration time: " + executionTime + " milliseconds");    }
    private static boolean isAdministrator() {


        boolean isAdmin = false;
        String[] commands = {"cmd.exe", "/c", "net session >nul 2>&1"};
        try {
            Process process = Runtime.getRuntime().exec(commands);
            
            int exitCode = process.waitFor();
            isAdmin = (exitCode == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return isAdmin;
    }
}