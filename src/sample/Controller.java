package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javafx.scene.media.Media;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML BorderPane borderPane;
    @FXML Button browseBtn;
    @FXML Button playBtn;
    @FXML Text fileText;
    @FXML NumberAxis yAxis;
    @FXML CategoryAxis xAxis;
    @FXML BarChart<String,Number> barChart;
    @FXML ColorPicker clPicker;
    @FXML Label clLabel;
    @FXML Button styleChoice;

    private final int BANDS = 156;

    private MediaPlayer mp = null;

    @Override
    public void initialize(URL location, ResourceBundle bundle){
        setUpAxes(true);
        clPicker.setValue(new Color(255.0/256.0, 105.0/256.0, 180.0/256.0,1));
        changeColor();
    } // init

    @FXML
    private void styleChoiceHandle(){
        if(styleChoice.getText().equals("Literal")){
            styleChoice.setText("Dynamic");
        } else {
            styleChoice.setText("Literal");
        }
        changeGraph();
    }

    @FXML
    private void changeGraph() {
        if(mp != null) {
            if (styleChoice.getText() == "Dynamic") {
                displayDynamicVisualizer();
                changeColor();
            } else {
                displayLiteralVisualizer();
                changeColor();
            }
        }
    }

    @FXML
    private void changeColor(){
        Color c = clPicker.getValue();
        String hex = String.format( "#%02X%02X%02X",
                (int)( c.getRed() * 255 ),
                (int)( c.getGreen() * 255 ),
                (int)( c.getBlue() * 255 ) );
        String hex2 = String.format( "#%02X%02X%02X",
                (int)(255 - (c.getRed() * 255)),
                (int)(255 - (c.getGreen() * 255)),
                (int)(255 - (c.getBlue() * 255 )));
        String hexFade = String.format( "#%02X%02X%02X",
                (int)((c.getRed() * 255)+(255-(c.getRed() * 255))/1.12),
                (int)((c.getGreen() * 255)+(255-(c.getGreen() * 255))/1.12),
                (int)((c.getBlue() * 255)+(255-(c.getBlue() * 255))/1.12));
        String hexFade2 = String.format( "#%02X%02X%02X",
                (int)((255 - (c.getRed() * 255))+(255-(255 - (c.getRed() * 255)))/1.12),
                (int)((255 -(c.getGreen() * 255))+(255-(255 - (c.getGreen() * 255)))/1.12),
                (int)((255 - (c.getBlue() * 255))+(255-(255 - (c.getBlue() * 255 )))/1.12));
        for(Node n:barChart.lookupAll(".default-color0.chart-bar")) {
            n.setStyle("-fx-background-color:linear-gradient(to top, " + hex + ", " + hexFade + ");");
        }
        for(Node n:barChart.lookupAll(".default-color1.chart-bar")) {
            n.setStyle("-fx-background-color:linear-gradient(to top, " + hex2 + ", " + hexFade2 + ");");
        }
    }

    @FXML
    private void showClPicker(){
        clPicker.show();
    }

    private void setUpMedia(File file){
        try {
            //BASIC MEDIA
            URL mediaUrl = file.toURI().toURL();
            String mediaStringUrl = mediaUrl.toExternalForm();
            Media media = new Media(mediaStringUrl);
            mp = new MediaPlayer(media);
            mp.setVolume(.1);
            mp.setAudioSpectrumThreshold(-100);
            mp.setAudioSpectrumNumBands(BANDS);

            changeGraph();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } // try catch

    } // setUpMedia


    private void setUpAxes(boolean literal){
        if(literal) {
            yAxis.setLowerBound(0);
        } else {
            yAxis.setLowerBound(-100);
        }
        yAxis.setUpperBound(100);
        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(10);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis,null,"dB"));
        yAxis.setMinorTickVisible(literal);
        yAxis.setTickMarkVisible(literal);
        yAxis.setTickLabelsVisible(literal);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
    }

    private void displayLiteralVisualizer(){
        setUpAxes(true);

        barChart.getData().clear();

        //POSITIVE SERIES
        XYChart.Series<String,Number> series1 = new XYChart.Series<> ();
        XYChart.Data[] series1Data = new XYChart.Data[64];
        for (int i=0; i<series1Data.length; i++) {
            series1Data[i] = new XYChart.Data<>(Integer.toString(i+1),0);
            series1.getData().add(series1Data[i]);
        }
        barChart.getData().add(series1);

        visualize(series1Data);
    } // displayVisualizer

    private void displayDynamicVisualizer(){
        setUpAxes(false);

        barChart.getData().clear();

        XYChart.Series<String,Number> series1 = new XYChart.Series<> ();
        XYChart.Data[] series1Data = new XYChart.Data[16];
        for (int i=0; i<series1Data.length; i++) {
            series1Data[i] = new XYChart.Data<>(Integer.toString(i+1),0);
            series1.getData().add(series1Data[i]);
        }
        barChart.getData().add(series1);

        XYChart.Series<String,Number> series2 = new XYChart.Series<> ();
        XYChart.Data[] series2Data = new XYChart.Data[16];
        for (int i=0; i<series2Data.length; i++) {
            series2Data[i] = new XYChart.Data<>(Integer.toString(i+1),0);
            series2.getData().add(series2Data[i]);
        }
        barChart.getData().add(series2);

        visualize(series1Data, series2Data);

    }

    //Logic for the Listener. magnitudes[] is an array of size equal to the number of bands
    // in the audio. 0db default is -60.
    private void visualize(XYChart.Data[] series1Data){

        float[] buf = new float[64];
            Arrays.fill(buf, mp.getAudioSpectrumThreshold());
            mp.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
            for(int i=0;i<63;i++) {
                double dif = (double)magnitudes[i] - (double)buf[i];
                buf[i] += dif/1.8;
                series1Data[i].setYValue((1 + buf[i] - mp.getAudioSpectrumThreshold()));
            } // for
        }); // lambda
    } // visualize

    private void visualize(XYChart.Data[] series1Data, XYChart.Data[] series2Data){
        float[] buf = new float[64];
        Arrays.fill(buf, mp.getAudioSpectrumThreshold());

        mp.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
            for(int i=0;i<16;i++) {
                double dif = (double)magnitudes[(int)(i*Math.random())] - (double)buf[i];
                buf[i] += dif/1.8;
                series1Data[i].setYValue((1 + buf[i] - mp.getAudioSpectrumThreshold()));
                series2Data[i].setYValue(-1*(1 + buf[i] - mp.getAudioSpectrumThreshold()));
            } // for
        }); // lambda
    } // visualize

    @FXML
    private void buttonHandle(){
        if(mp == null){
            return;
        } // if

        if(mp.getStatus() == MediaPlayer.Status.PLAYING){
            mp.pause();
            playBtn.setText("Play");
        } else {
            mp.play();
            changeColor();
            playBtn.setText("Pause");
        }
    } // toggleMusic

    private void stopMusic(){
        mp.stop();
        barChart.getData().clear();
        playBtn.setText("Play");
    }

    @FXML
    private void browseFiles(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a File:");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac")
        );
        File selectedFile = fc.showOpenDialog(browseBtn.getScene().getWindow());
        if(selectedFile != null){
            if(mp != null){
                stopMusic();
            }
            fileText.setText(selectedFile.toString());
            setUpMedia(selectedFile);
        }
    }
}
