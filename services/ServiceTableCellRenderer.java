package services;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class ServiceTableCellRenderer extends DefaultTableCellRenderer {
    private static final Color LIGHT_GREEN = new Color(144, 238, 144);
    private static final Color LIGHT_RED = new Color(255, 182, 193);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String status = (String) table.getValueAt(row, 2);

        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        } else {
            if (status.equalsIgnoreCase("RUNNING")) {
                label.setBackground(Color.white);
            } else if (status.equalsIgnoreCase("STOPPED")) {
                label.setBackground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
            }
            label.setForeground(table.getForeground());
        }

        return label;
    }
}