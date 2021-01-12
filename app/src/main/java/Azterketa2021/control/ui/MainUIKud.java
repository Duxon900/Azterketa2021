package Azterketa2021.control.ui;

import Azterketa2021.CheckSum;
import Azterketa2021.control.db.DBKudeatzaile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.apache.commons.codec.binary.Hex;
import org.sqlite.core.DB;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainUIKud implements Initializable {

    @FXML
    private TableView<CheckSum> taula;

    @FXML
    private TableColumn<?, ?> zutURL;

    @FXML
    private TableColumn<?, ?> zutMd5;

    @FXML
    private TableColumn<CheckSum, String> zutVersion;

    @FXML
    private TextField txtURL;


    @FXML
    private Label lblBadago;

    @FXML
    private Label lblEzDago;

    @FXML
    private Label lblSartu;

    ObservableList<CheckSum> emaitza= FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblBadago.setVisible(false);
        lblEzDago.setVisible(false);
        lblSartu.setVisible(false);


        zutMd5.setCellValueFactory(new PropertyValueFactory<>("md5"));
        zutVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
        zutURL.setCellValueFactory(new PropertyValueFactory<>("URL"));

        Callback<TableColumn<CheckSum,String>, TableCell<CheckSum,String>> defaultTextFactory= TextFieldTableCell.forTableColumn();
        zutVersion.setCellFactory(col -> {
            var cell=defaultTextFactory.call(col);

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    cell.setEditable(true);
                }
            });

            return cell;
        });

        zutVersion.setOnEditCommit(elem->{
            var unekoa=elem.getTableView().getItems().get(elem.getTablePosition().getRow());
            unekoa.setVersion(elem.getNewValue());

            String query="update checksums set version='"+unekoa.getVersion()+"' where md5='"+unekoa.getMd5()+"'";
            DBKudeatzaile.getInstantzia().execSQL(query);

            lblEzDago.setVisible(false);
            lblSartu.setVisible(true);
            lblBadago.setVisible(false);
        });


        taula.setEditable(true);
        taula.setItems(emaitza);
    }


    @FXML
    void onClickCheck(ActionEvent event) throws NoSuchAlgorithmException, IOException, SQLException {
        //https://www.hek.de/phpmyadmin
        String link=txtURL.getText()+"/README";

        try{
            URL url = new URL(link);
            InputStream is = url.openStream();
            MessageDigest md = MessageDigest.getInstance("MD5");
            String hashtext = getDigest(is, md, 2048);

            ResultSet resultSet=DBKudeatzaile.getInstantzia().execSQL("select * from checksums where md5='"+hashtext+"'");

            if (resultSet.next()){
                if(hashtext.equals(resultSet.getString("md5"))){
                    CheckSum checkSum=new CheckSum();
                    checkSum.setMd5(resultSet.getString("md5"));
                    checkSum.setURL(link);
                    checkSum.setVersion(resultSet.getString("version"));

                    emaitza.add(checkSum);

                    lblEzDago.setVisible(false);
                    lblSartu.setVisible(false);
                    lblBadago.setVisible(true);
                }
            }
            else {
                CheckSum checkSum=new CheckSum();
                checkSum.setMd5(hashtext);
                checkSum.setURL(link);
                emaitza.add(checkSum);

                String query="insert into checksums (idCMS,md5,path,version) values ('1','"+checkSum.getMd5()+"','README','-')";
                DBKudeatzaile.getInstantzia().execSQL(query);

                lblEzDago.setVisible(true);
                lblSartu.setVisible(false);
                lblBadago.setVisible(false);
            }

        }
        catch (FileNotFoundException | MalformedURLException e){
            lblEzDago.setVisible(true);
            lblSartu.setVisible(false);
            lblBadago.setVisible(false);
        }




    }

    public static String getDigest(InputStream is, MessageDigest md, int byteArraySize)
            throws NoSuchAlgorithmException, IOException {

        md.reset();
        byte[] bytes = new byte[byteArraySize];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            md.update(bytes, 0, numBytes);
        }
        byte[] digest = md.digest();

        String emaitza= new String(digest, StandardCharsets.UTF_8);
        System.out.println(emaitza);


        String result = new String(Hex.encodeHex(digest));
        System.out.println(result);

        return result;
    }


    @FXML
    void onClickClose(ActionEvent event) {
        //Aplikazioa ixten du
        System.exit(0);
    }

}
