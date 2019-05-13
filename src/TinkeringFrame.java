import com.sun.javafx.binding.StringFormatter;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TinkeringFrame extends JFrame {

    public static final int fractions = 10;
    private static final NumberFormat formatter = new DecimalFormat("#.0");
    IGrapher grapher;

    public TinkeringFrame(IGrapher grapher) {
        super("Tinkering with graph");
        grapher = grapher;

        JLabel title = new JLabel(grapher.name());
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setAlignmentY(CENTER_ALIGNMENT);
        add(title);
        JPanel sliders = new JPanel();
        sliders.setLayout(new GridLayout(grapher.argc(), 1));
        JScrollPane scroll = new JScrollPane(sliders);
        add(scroll);

        for (ArgInfo arg : grapher.getArgs()) {
            JPanel cell = new JPanel();
            cell.setLayout(new GridLayout(2, 1));
            cell.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            JLabel label = new JLabel(arg.name + ": " + formatter.format(arg.value));
            JSlider slider = new JSlider(0, (int) (arg.range()*fractions), (int) (arg.normalized()*fractions));
            slider.addChangeListener( e -> {
                arg.value = slider.getValue()/10f + arg.min;
                label.setText(arg.name + ": " + formatter.format(arg.value));
            });
            cell.add(label);
            cell.add(slider);
            sliders.add(cell);
        }

        pack();
    }

    public static void main(String[] args){
//        IGrapher grapher = new FlowerGrapher();
//        TinkeringFrame frame = new TinkeringFrame(grapher);
//        frame.setVisible(true);
        new GraphWindow(new FlowerGrapher()).run();
    }

}
