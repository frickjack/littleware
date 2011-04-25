/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.view;

import com.google.common.collect.ImmutableMap;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import littleware.base.event.LittleBean;

/**
 * Properties editor component
 */
public class JPropEditor extends JPanel {

    private static final Logger log = Logger.getLogger(JPropEditor.class.getName());
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    {
        setName("JPropEditor.panel");
    }

    private static class PropTableModel extends AbstractTableModel {

        private final Map<String, String> props;
        private final List<String> sortedKeys;
        private final JPropEditor parent;

        public Map<String, String> getProperties() {
            return ImmutableMap.copyOf(props);
        }

        public PropTableModel(Map<String, String> props, JPropEditor parent) {
            this.props = new HashMap<String, String>(props);
            sortedKeys = new ArrayList<String>(props.keySet());
            Collections.sort(sortedKeys);
            this.parent = parent;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Key";
                case 1:
                    return "Value";
                default:
                    return "--" + col;
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return props.size();
        }

        @Override
        public String getValueAt(int row, int col) {
            final String key = sortedKeys.get(row);
            if (col == 0) {
                return key;
            } else {
                return props.get(key);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        @Override
        public void setValueAt(Object obj, int row, int col) {
            log.log(Level.FINE, "Model update ...." + col + ": " + obj);
            if (col != 1) {
                return;
            }
            final String key = sortedKeys.get(row);
            final String old = props.get(key);
            final String value = (String) obj;
            props.put(key, value);
            parent.firePropertyChange(key, old, value);
        }
    }
    // -------------------------------
    private JTable jtable = new JTable();

    {
        this.add(new JScrollPane(jtable));
        jtable.setDefaultRenderer(String.class,
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus, int row, int column) {

                        return super.getTableCellRendererComponent(table, color, (column != 0) && isSelected,
                                (column != 0) && hasFocus, row, column);
                    }
                });
    }

    /**
     * Get read-only view of current editor state
     */
    public Map<String, String> getProperties() {
        final TableModel model = jtable.getModel();
        if (model instanceof PropTableModel) {
            return ImmutableMap.copyOf(((PropTableModel) model).getProperties());
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Re-initialize editor
     */
    public void setProperties(Map<String, String> value) {
        jtable.setModel(new PropTableModel(value, this));
    }
}
