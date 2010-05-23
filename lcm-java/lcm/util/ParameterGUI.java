package lcm.util;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ParameterGUI
{
    HashMap<String, PValue> parammap = new HashMap<String, PValue>();
    JPanel  panel = new JPanel(new GridBagLayout());
    int     row = 0;
    GridBagConstraints gA, gB, gC, gD, gBC, gCD, gBCD, gABCD;
    ArrayList<ParameterListener> listeners = new ArrayList<ParameterListener>();
    boolean showvars;

    /** This makes a text field yellow until they've pressed return;
        it's a reminder that you need to hit return for the changes to
        your text field to apply.
    **/
    static void setupJTextField(JTextField jtf)
    {
        JTFListener l = new JTFListener(jtf);
        jtf.addActionListener(l);
        jtf.addCaretListener(l);
        jtf.addKeyListener(l);
    }

    static class JTFListener extends KeyAdapter implements ActionListener, CaretListener
    {
        JTextField jtf;

        JTFListener(JTextField jtf)
        {
            this.jtf=jtf;
        }

        public void keyPressed(KeyEvent e)
        {
            jtf.setBackground(Color.yellow);
        }

        public void actionPerformed(ActionEvent e)
        {
            jtf.setBackground(Color.white);
        }

        public void caretUpdate(CaretEvent e)
        {
        }
    }

    /** Parent interface of all value elements **/
    abstract class PValue
    {
        String name;  // name of element, e.g., "logthresh"
        String desc;  // description, e.g., "Logarithmic threshold"

        PValue(String name, String desc)
        {
            this.name = name;
            this.desc = desc;
        }

        abstract void setEnabled(boolean enabled);
    }

    /** parse the integer in string s, or if it fails, return 'default' **/
    static final int parseInteger(String s, int val)
    {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return val;
        }
    }

    /** parse the integer in string s, or if it fails, return 'default' **/
    static final double parseDouble(String s, double val)
    {
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return val;
        }
    }

    class ActionNotifier implements ActionListener
    {
        String name;

        public ActionNotifier(String name)
        {
            this.name = name;
        }

        public void actionPerformed(ActionEvent e)
        {
            notifyListeners(name);
        }
    }

    class BooleanValue extends PValue
    {
        JCheckBox  jcb;
        boolean value;

        BooleanValue(String name, String desc, boolean value)
        {
            super(name, desc);

            this.value = value;
        }

        JCheckBox getCheckBox()
        {
            if (jcb == null)
            {
                jcb = new JCheckBox(desc, value);
                jcb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setBooleanValue(jcb.isSelected());
                    }});

            }
            return jcb;
        }

        boolean getBooleanValue()
        {
            return value;
        }

        void setBooleanValue(boolean v)
        {
            if (v==value)
                return;

            value = v;
            notifyListeners(name);
        }

        void setEnabled(boolean v)
        {
            jcb.setEnabled(v);
        }
    }

    class IntegerValue extends PValue
    {
        JSlider    slider;
        JTextField textField;
        JLabel     label;
        int        min, max;
        int        value;

        IntegerValue(String name, String desc, int min, int max, int value)
        {
            super(name, desc);

            this.min = min;
            this.max = max;
            this.value = value;
        }

        JSlider getSlider()
        {
            if (slider == null)
            {
                slider = new JSlider(min, max, value);
                slider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        setIntegerValue(slider.getValue());
                    }});
            }
            return slider;
        }

        JLabel getLabel()
        {
            if (label == null)
                label = new JLabel(""+value);
            return label;
        }

        JTextField getTextField()
        {
            if (textField == null) {
                textField = new JTextField(""+value);
                setupJTextField(textField);
                textField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setIntegerValue(parseInteger(textField.getText(), value));
                    }});

            }
            return textField;
        }

        int getIntegerValue()
        {
            return value;
        }

        void setIntegerValue(int v)
        {
            if (v<min)
                v=min;
            if (v>max)
                v=max;
            if (v == value)
                return;

            value = v;
            if (slider != null)
                slider.setValue(v);
            if (textField != null)
                textField.setText(""+v);
            if (label != null)
                label.setText(""+v);

            notifyListeners(name);
        }

        void setMinMax(int min, int max)
        {
            this.min = min;
            this.max = max;
            if (slider != null)
            {
                slider.setMinimum(min);
                slider.setMaximum(max);
            }
        }

        void setEnabled(boolean v)
        {
            if (slider!=null)
                slider.setEnabled(v);
            if (textField!=null)
                textField.setEnabled(v);
        }
    }

    class DoubleValue extends PValue
    {
        JSlider    slider;
        JTextField textField;
        JLabel     label;
        double     min, max;
        double     value;
        int        ivalue; // scaled values used for slider
        static final int SLIDER_CLICKS = 100000;
        double     stepsize;
        String     svalue; // string representation of value.

        DoubleValue(String name, String desc, double min, double max, double value)
        {
            super(name, desc);

            this.min = min;
            this.max = max;
            this.value = value;

            updateSliderValue();
        }

        void updateSliderValue()
        {
            ivalue = (int) (SLIDER_CLICKS*(value-min)/(max-min));
            if (slider != null)
                slider.setValue(ivalue);
            stepsize = (max-min)/SLIDER_CLICKS;
        }

        JSlider getSlider()
        {
            if (slider == null)
            {
                slider = new JSlider(0, SLIDER_CLICKS, ivalue);
                slider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        setDoubleValue(((double)slider.getValue()+.5)/SLIDER_CLICKS*(max-min)+min);
                    }});
            }
            return slider;
        }

        JLabel getLabel()
        {
            if (label == null)
                label = new JLabel(""+value);
            return label;
        }

        JTextField getTextField()
        {
            if (textField == null) {
                textField = new JTextField(""+value);
                setupJTextField(textField);
                textField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setDoubleValue(parseDouble(textField.getText(), value));
                    }});
            }

            return textField;
        }

        double getDoubleValue()
        {
            return value;
        }

        void setDoubleValue(double v)
        {
            if (v<min)
                v=min;
            if (v>max)
                v=max;
            if (v == value)
                return;

            value = v;
            updateSliderValue();

            {
                int digits = (int) (-Math.log(stepsize)/Math.log(10))+1;
                if (digits<0)
                    digits=0;

                svalue=String.format("%."+digits+"f",value);
            }

            if (textField != null)
            {
                textField.setText(svalue);
            }
            if (label != null)
            {
                label.setText(svalue);
            }

            notifyListeners(name);
        }

        void setMinMax(double min, double max)
        {
            this.min = min;
            this.max = max;
            updateSliderValue();
        }

        void setEnabled(boolean v)
        {
            if (slider!=null)
                slider.setEnabled(v);
            if (textField!=null)
                textField.setEnabled(v);
        }
    }

    class StringValue extends PValue
    {
        JTextField textField;
        JComboBox  comboBox;

        String     value;
        String     values[];
        int        idx; // if values is set, idx is such that value=values[idx].

        public StringValue(String name, String desc, String values[], String value)
        {
            super(name, desc);
            this.value = value;
            this.values = values;
            updateIndex();
        }

        JTextField getTextField()
        {
            if (textField == null) {
                textField = new JTextField(value);
                setupJTextField(textField);
                textField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String v = textField.getText();
                        setStringValue(v);
                    }});
            }
            return textField;
        }

        JComboBox getComboBox()
        {
            assert(values != null);

            if (comboBox == null) {
                comboBox = new JComboBox(values);
                comboBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setStringValue(values[comboBox.getSelectedIndex()]);
                    }});
                comboBox.setSelectedIndex(idx);
            }

            return comboBox;
        }

        String getStringValue()
        {
            return value;
        }

        void updateIndex()
        {
            if (values!=null)
            {
                idx = -1;

                for (int i = 0; i < values.length; i++)
                {
                    if (value.equals(values[i]))
                        idx = i;
                }

                if (idx == -1)
                    System.out.println("Warning: illegal string value specified: "+value);
            }
        }

        void setStringValue(String v)
        {
            if (v.equals(value))
                return;

            value = v;
            updateIndex();

            if (textField != null)
                textField.setText(v);
            if (comboBox != null)
                comboBox.setSelectedIndex(idx);

            notifyListeners(name);
        }

        void setEnabled(boolean v)
        {
            if (textField!=null)
                textField.setEnabled(v);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    public ParameterGUI()
    {
        this(true);
    }

    // Our grid:
    //
    //    0                1                   2       3
    //  AAAAA  BBBBBBBBBBBBBBBBBBBBBBBBBBBB  CCCCCC  DDDDDD
    //    gA               gB                  gC      gD
    //
    //  gCD spans columns C and D.
    //  gBCD spans columsn B, C, and D. (etc)

    public ParameterGUI(boolean showvars)
    {
        this.showvars = showvars;

        gA = new GridBagConstraints();
        gA.gridx = 0;
        gA.weightx = 0.05;
        gA.fill=GridBagConstraints.HORIZONTAL;

        gB = new GridBagConstraints();
        gB.gridx = 1;
        gB.weightx = 1;
        gB.fill=GridBagConstraints.HORIZONTAL;

        gC = new GridBagConstraints();
        gC.gridx = 2;
        gC.weightx = .1;
        gC.fill=GridBagConstraints.HORIZONTAL;

        gD = new GridBagConstraints();
        gD.gridx = 3;
        gD.weightx = .05;
        //	gD.fill=GridBagConstraints.HORIZONTAL;
        gD.anchor = GridBagConstraints.CENTER;

        gBC = new GridBagConstraints();
        gBC.gridx = 1;
        gBC.gridwidth = 2;
        gBC.weightx = gB.weightx + gC.weightx;
        gBC.fill=GridBagConstraints.HORIZONTAL;

        gCD = new GridBagConstraints();
        gCD.gridx = 2;
        gCD.gridwidth = 2;
        gCD.weightx = gC.weightx + gD.weightx;
        gCD.fill=GridBagConstraints.HORIZONTAL;
        gCD.anchor = GridBagConstraints.EAST;

        gBCD = new GridBagConstraints();
        gBCD.gridx = 1;
        gBCD.gridwidth = 3;
        gBCD.weightx = gB.weightx + gC.weightx + gD.weightx;
        gBCD.fill=GridBagConstraints.HORIZONTAL;
        gBCD.anchor = GridBagConstraints.EAST;

        gABCD = new GridBagConstraints();
        gABCD.gridx = 0;
        gABCD.gridwidth = 4;
        gABCD.weightx = gA.weightx + gB.weightx + gC.weightx + gD.weightx;
        gABCD.fill=GridBagConstraints.HORIZONTAL;
    }

    protected void notifyListeners(String name)
    {
        for (ParameterListener pl : listeners)
            pl.parameterChanged(name);
    }

    public void addListener(ParameterListener l)
    {
        listeners.add(l);
    }

    public void removeListeners()
    {
        listeners.clear();
    }

    public void addInt(String name, String desc, int value)
    {
        addInt(name, desc, -Integer.MAX_VALUE, Integer.MAX_VALUE, value);
    }

    public void addInt(String name, String desc, int min, int max, int value)
    {
        IntegerValue val = new IntegerValue(name, desc, -Integer.MAX_VALUE, Integer.MAX_VALUE, value);
        parammap.put(name, val);

        gA.gridy = row;
        gBCD.gridy = row;
        panel.add(new JLabel(desc), gA);
        panel.add(val.getTextField(), gBCD);
        row++;
    }

    public void addIntSlider(String name, String desc, int min, int max, int value)
    {
        IntegerValue val = new IntegerValue(name, desc, min, max, value);
        parammap.put(name, val);

        gA.gridy = row;
        gBC.gridy = row;
        gD.gridy = row;

        panel.add(new JLabel(desc), gA);
        panel.add(val.getSlider(), gBC);
        panel.add(val.getLabel(), gD);
        row++;
    }

    public void addDouble(String name, String desc, double value)
    {
        addDouble(name, desc, -Double.MAX_VALUE, Double.MAX_VALUE, value);
    }

    public void addDouble(String name, String desc, double min, double max, double value)
    {
        DoubleValue val = new DoubleValue(name, desc, min, max, value);
        parammap.put(name, val);

        gA.gridy = row;
        gBCD.gridy = row;
        panel.add(new JLabel(desc), gA);
        panel.add(val.getTextField(), gBCD);
        row++;
    }

    public void addDoubleSlider(String name, String desc, double min, double max, double value)
    {
        DoubleValue val = new DoubleValue(name, desc, min, max, value);
        parammap.put(name, val);

        gA.gridy = row;
        gBC.gridy = row;
        gD.gridy = row;

        panel.add(new JLabel(desc), gA);
        panel.add(val.getSlider(), gBC);
        panel.add(val.getLabel(), gD);
        row++;
    }

    public void addString(String name, String desc, String value)
    {
        StringValue val = new StringValue(name, desc, null, value);
        parammap.put(name, val);

        gA.gridy = row;
        gBCD.gridy = row;

        panel.add(new JLabel(desc), gA);
        panel.add(val.getTextField(), gBCD);
        row++;
    }

    public void addChoice(String name, String desc, String values[], int value)
    {
        StringValue val = new StringValue(name, desc, values, values[value]);
        parammap.put(name, val);

        gA.gridy = row;
        gBCD.gridy = row;

        panel.add(new JLabel(desc), gA);
        panel.add(val.getComboBox(), gBCD);
        row++;
    }

    public void addBoolean(String name, String desc, boolean value)
    {
        BooleanValue val = new BooleanValue(name, desc, value);
        parammap.put(name, val);

        gA.gridy = row;
        gBCD.gridy = row;
        panel.add(new JLabel(desc), gA);
        panel.add(val.getCheckBox(), gBCD);
        row++;
    }

    public void addButton(String name, String desc)
    {
        addButtons(name, desc);
    }

    public void addButtons(Object... args)
    {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, args.length/2));

        for (int i = 0; i < args.length/2; i++)
	    {
            String name = (String) args[i*2];
            String desc = (String) args[i*2+1];

            JButton button = new JButton(desc);
            button.addActionListener(new ActionNotifier(name));

            p.add(button);
	    }

        gABCD.gridy = row;
        panel.add(p, gABCD);
        row++;
    }

    public void addCheckBoxes(Object... args)
    {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, args.length/3));

        for (int i = 0; i < args.length/3; i++)
	    {
            String name = (String) args[i*3+0];
            String desc = (String) args[i*3+1];
            boolean value = (Boolean) args[i*3+2];

            BooleanValue bv = new BooleanValue(name, desc, value);
            parammap.put(name, bv);

            p.add(bv.getCheckBox());
	    }

        gABCD.gridy = row;
        panel.add(p, gABCD);
        row++;
    }

    public double gd(String name)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof DoubleValue);

        return ((DoubleValue) p).getDoubleValue();
    }

    public void sd(String name, double v)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof DoubleValue);

        ((DoubleValue) p).setDoubleValue(v);
    }

    public int gi(String name)
    {
        PValue p = parammap.get(name);
        assert(p!=null);

        if (p instanceof StringValue)
            return ((StringValue) p).idx;

        assert(p instanceof IntegerValue);

        return ((IntegerValue) p).getIntegerValue();
    }

    public void si(String name, int v)
    {
        PValue p = parammap.get(name);
        assert(p!=null);

        if (p instanceof StringValue)
	    {
            StringValue sv = (StringValue) p;
            assert (v < sv.values.length);
            sv.setStringValue(sv.values[v]);
	    }
        else if (p instanceof IntegerValue)
	    {
            ((IntegerValue) p).setIntegerValue(v);
	    }
        else
	    {
            assert(false);
	    }

    }

    public void setMinMax(String name, int min, int max)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof IntegerValue);

        ((IntegerValue) p).setMinMax(min, max);
    }

    public void setMinMax(String name, double min, double max)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof DoubleValue);

        ((DoubleValue) p).setMinMax(min, max);
    }

    public String gs(String name)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof StringValue);

        return ((StringValue) p).getStringValue();
    }

    public void ss(String name, String v)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof StringValue);

        ((StringValue) p).setStringValue(v);
    }

    public boolean gb(String name)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof BooleanValue);

        return ((BooleanValue) p).getBooleanValue();
    }

    public void sb(String name, boolean v)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        assert(p instanceof BooleanValue);

        ((BooleanValue) p).setBooleanValue(v);
    }

    public void setEnabled(String name, boolean e)
    {
        PValue p = parammap.get(name);
        assert(p!=null);
        p.setEnabled(e);
    }

    public Container getPanel()
    {
        return panel;
    }

    public static void main(String[] args)
    {
        JFrame f = new JFrame("ParameterGUI Test");
        f.setLayout(new BorderLayout());
        f.setSize(400,400);

        ParameterGUI pg = new ParameterGUI();

        pg.addDouble("Double Value", "A double value", 3.1415926);
        pg.addInt("IntValue1", "An integer value", 45);
        pg.addIntSlider("IntValue2", "An integer value", 100, 1000, 999);
        pg.addDoubleSlider("DoubleSlider", "A sliding double", -1, 1, 0.5);
        pg.addDoubleSlider("DoubleSlider2", "Another sliding double",2500000, 3000000, 2500000);

        pg.addString("StringValue1", "A string value", "Hi");
        pg.addString("StringValue2", "A string value", "world");

        pg.addChoice("Combo", "A combo box", new String[] {"Choice 1", "Choice 2", "Choice 3"}, 1);

        pg.addBoolean("BoolVal1","A boolean value", true);
        pg.addBoolean("BoolVal2","A boolean value", false);

        pg.addButton("Button", "button one");
        pg.addButtons("Buttons", "button one",
                      "button2", "button two",
                      "button3", "button three");

        pg.addCheckBoxes("name1", "Checkbox 1", true,
                         "name2", "Checkbox 2", false);

        pg.addListener(new ParameterListener() {
            public void parameterChanged(String name)
            {
                System.out.println("Changed "+name);
            }
	    });
        f.add(pg.getPanel(), BorderLayout.CENTER);
        f.setVisible(true);
    }
}
