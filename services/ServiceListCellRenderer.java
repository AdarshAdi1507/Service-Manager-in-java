package services;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class ServiceListCellRenderer extends DefaultListCellRenderer {
    private static final Color LIGHT_GREEN = new Color(144, 238, 144);
    private static final Color LIGHT_RED = new Color(255, 182, 193);

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        ServiceInfo serviceInfo = (ServiceInfo) value;

        String statusText = serviceInfo.getStatus();
        if ("Running".equals(statusText)) {
            label.setBackground(Color.darkGray);
        } else if ("Stopped".equals(statusText)) {
            label.setBackground(Color.black);
        } else {
            label.setBackground(Color.WHITE);
        }

        label.setText(serviceInfo.getDisplayName() + " - " + statusText);

        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setForeground(list.getForeground());
        }

        return label;
    }
}
