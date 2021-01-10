package Azterketa2021.control.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainUIKud implements Initializable {

    @FXML
    private TableView<?> taula;

    ObservableList<?> emaitza= FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ResultSet resultSet= DBKudeatzaile.getInstantzia().execSQL("select * from captchas");

        try {
            datuakSartu(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private void datuakSartu(ResultSet resultSet) throws SQLException {

        while (resultSet.next()){

        }
        taula.setItems(emaitza);
    }
}
