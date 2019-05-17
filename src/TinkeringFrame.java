import com.sun.javafx.binding.StringFormatter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TinkeringFrame extends JFrame {

    public static final int fractions = 10;

    private static final NumberFormat formatter = new DecimalFormat("#.0");

    private GraphWindow viewer;

    public void registerViewer(GraphWindow window){
        viewer = window;
    }

    public TinkeringFrame(IGrapher grapher) {
        super("Tinkering with graph");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                if (viewer != null){
                    viewer.terminate();
                }
            }
        });

        Border margins = BorderFactory.createEmptyBorder(10,10,10,10);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JLabel title = new JLabel(grapher.name());
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setAlignmentY(CENTER_ALIGNMENT);
        title.setBorder(margins);
        add(title);
        JPanel sliders = new JPanel();
        sliders.setLayout(new GridLayout(grapher.argc(), 1));
        JScrollPane scroll = new JScrollPane(sliders);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll);

        for (ArgInfo arg : grapher.getArgs()) {
            JPanel cell = new JPanel();
            cell.setLayout(new GridLayout(2, 1));
            cell.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            JLabel label = new JLabel(arg.name + ": " + formatter.format(arg.value));
            JSlider slider = new JSlider(0, (int) (arg.range()*fractions), (int) (arg.normalized()*fractions));
            slider.addChangeListener( e -> {
                arg.value = slider.getValue()/(float)fractions + arg.min;
                label.setText(arg.name + ": " + formatter.format(arg.value));
            });
            cell.add(label);
            cell.add(slider);
            sliders.add(cell);
        }
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(2, 1));
        cell.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        JLabel label = new JLabel( "Resolution: " + formatter.format(grapher.getResolution()));
        JSlider slider = new JSlider(100, grapher.getResolution(), 10000);
        slider.addChangeListener( e -> {
            grapher.setResolution(slider.getValue());
            label.setText("Resolution: " + formatter.format(grapher.getResolution()));
        });
        cell.add(label);
        cell.add(slider);
        cell.setBorder(margins);


        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Capture");
        saveButton.addActionListener( e -> {
            viewer.requestImage(img -> {
                File outfile = new File(System.getProperty("user.home") + "/Desktop/capture.jpg");
                try {
                    outfile.createNewFile();
                    ImageIO.write(img, "jpg", outfile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        });
        buttonContainer.add(cell);
        buttonContainer.add(saveButton);
        add(buttonContainer);

        pack();
    }
}
