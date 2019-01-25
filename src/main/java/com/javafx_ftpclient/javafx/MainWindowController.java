package com.javafx_ftpclient.javafx;

import com.javafx_ftpclient.ftp.FTPConnection;
import com.javafx_ftpclient.ftp.FTPOperation;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.StatusBar;

public class MainWindowController implements Initializable {

    private FTPConnection connection;

    @FXML
    private TableView localFileTableView, remoteFileTableView;

    @FXML
    private TextField localPathTextField, remotePathTextField, commandTextField;

    @FXML
    private TextArea consoleOut;

    @FXML
    private StatusBar statusBar;

    /**
     * Handling Enter pressed action on local path TextField
     *
     * @param event key event
     */
    @FXML
    public void onEnterPressedOnLocalPath(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String newPath = localPathTextField.getText();
            if (new File(newPath).exists()) {
                localFileTableView.setItems(this.getLocalListOfFile(newPath));
            } else {
                new MessageWindow().showInformation(
                        "Error",
                        "Invalid path value",
                        "Please, check path-field value",
                        Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Handling Enter pressed action on remote path TextField
     *
     * @param event key event
     */
    @FXML
    public void onEnterPressedOnRemotePath(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String newPath = remotePathTextField.getText();
            try {
                remoteFileTableView.setItems(this.getRestoreListOfFile(newPath));
            } catch (Exception ex) {
                new MessageWindow().showException(
                        "Exception",
                        "Exception has been occured",
                        "Please, read the exception stack",
                        ex);
            }
        }
    }

    /**
     * Handling event for double click on table's row for "local" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onLocalFileTableViewDobleClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                TableViewFile tableViewFile = null;
                TableViewFile path = null;
                try {
                    tableViewFile = (TableViewFile) localFileTableView
                            .getSelectionModel()
                            .getSelectedItem();
                    if (tableViewFile.getType().equals("dir")) {
                        localFileTableView
                                .getSelectionModel()
                                .selectFirst();
                        path = (TableViewFile) localFileTableView
                                .getSelectionModel()
                                .getSelectedItem();
                        localFileTableView
                                .getItems()
                                .clear();
                        if (!path.getName().endsWith(File.separator)) {
                            localFileTableView.setItems(
                                    this.getLocalListOfFile(
                                            path.getName() + File.separator + tableViewFile.getName()));
                            localPathTextField.setText(
                                    path.getName() + File.separator + tableViewFile.getName());
                        } else {
                            localFileTableView.setItems(
                                    this.getLocalListOfFile(path.getName() + tableViewFile.getName()));
                            localPathTextField.setText(
                                    path.getName() + tableViewFile.getName());
                        }
                    } else if (tableViewFile.getType().equals("...")) {
                        try {
                            if (new File(tableViewFile.getName()).getParentFile().exists()) {
                                localFileTableView.getItems().clear();
                                localFileTableView.setItems(
                                        this.getLocalListOfFile(
                                                new File(
                                                        tableViewFile.getName()).getParent()));
                                localPathTextField.setText(
                                        new File(
                                                tableViewFile.getName()).getParent());
                            }
                        } catch (Exception ex) {
                            new MessageWindow().showInformation(
                                    "Error",
                                    "Cannot access directory",
                                    "It's root path!",
                                    Alert.AlertType.INFORMATION);
                        }
                    }
                } catch (Exception ex) {
                    new MessageWindow().showInformation(
                            "Error",
                            "Cannot access directory",
                            "Error in accessing dir",
                            Alert.AlertType.INFORMATION);
                    localFileTableView.setItems(this.getLocalListOfFile(path.getName()));
                    localPathTextField.setText(path.getName());
                }
            }
        }
    }

    /**
     * Handling event for double click on table's row for "remote" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onRemoteFileTableViewDoubleClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                TableViewFile tableViewFile = null;
                TableViewFile path = null;
                try {
                    tableViewFile = (TableViewFile) remoteFileTableView
                            .getSelectionModel()
                            .getSelectedItem();
                    if (tableViewFile.getType().equals("dir")) {
                        remoteFileTableView.getSelectionModel().selectFirst();
                        path = (TableViewFile) remoteFileTableView
                                .getSelectionModel()
                                .getSelectedItem();
                        remoteFileTableView.getItems().clear();
                        if (!path.getName().endsWith("/")) {
                            remoteFileTableView.setItems(
                                    this.getRestoreListOfFile(
                                            path.getName() + "/" + tableViewFile.getName()));
                            remotePathTextField.setText(
                                    path.getName() + "/" + tableViewFile.getName());
                        } else {
                            remoteFileTableView.setItems(
                                    this.getRestoreListOfFile(
                                            path.getName() + tableViewFile.getName()));
                            remotePathTextField.setText(
                                    path.getName() + tableViewFile.getName());
                        }
                    } else if (tableViewFile.getType().equals("...")) {
                        try {
                            File dir = new File(tableViewFile.getName());
                            String parentPath = dir.getParent().replace(File.separator.toCharArray()[0], '/');
                            remoteFileTableView.getItems().clear();
                            remoteFileTableView.setItems(
                                    this.getRestoreListOfFile(parentPath));
                            remotePathTextField.setText(parentPath);
                        } catch (Exception ex) {
                            new MessageWindow().showInformation(
                                    "Error",
                                    "Cannot access directory",
                                    "Error in accessing dir",
                                    Alert.AlertType.INFORMATION);
                        }
                    }
                } catch (Exception ex) {
                    new MessageWindow().showInformation(
                            "Error",
                            "Cannot access directory",
                            "Error in accessing dir",
                            Alert.AlertType.INFORMATION);
                    localFileTableView.setItems(this.getLocalListOfFile(path.getName()));
                    localPathTextField.setText(path.getName());
                }
            }
        }
    }

    private ObservableList<TableViewFile> getLocalListOfFile(String path) {
        ObservableList<TableViewFile> fileList = FXCollections.observableArrayList();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        fileList.add(
                new TableViewFile(
                        new ImageView(ImageUtils.setImage("/images/home.png")),
                        folder.getAbsolutePath(),
                        "...",
                        "...",
                        "..."));
        for (File file : listOfFiles) {
            if (FilenameUtils.getExtension(file.getName()) != null
                    && FilenameUtils.getExtension(file.getName()).length() != 0) {
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                fileList.add(
                        new TableViewFile(
                                new ImageView(ImageUtils.setImage("/images/file.png")),
                                file.getName(),
                                FilenameUtils.getExtension(file.getName()),
                                Double.toString(file.length() / 1024),
                                dateFormat.format(new Date(file.lastModified()))));
            } else {
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                fileList.add(
                        new TableViewFile(
                                new ImageView(ImageUtils.setImage("/images/folder.png")),
                                file.getName(),
                                "dir",
                                "",
                                dateFormat.format(new Date(file.lastModified()))));
            }
        }
        return fileList;
    }

    private ObservableList<TableViewFile> getRestoreListOfFile(String path) {
        ObservableList<TableViewFile> fileList = FXCollections.observableArrayList();
        fileList.add(
                new TableViewFile(
                        new ImageView(ImageUtils.setImage("/images/home.png")),
                        path,
                        "...",
                        "...",
                        "..."));
        try {
            List<FTPFile> restoreList = new FTPOperation(
                    connection.getConnection()).getListOfFiles(path);
            for (FTPFile file : restoreList) {
                System.out.println(file.getName() + " " + file.getType());
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                if (file.getType() == 0) {
                    fileList.add(
                            new TableViewFile(
                                    new ImageView(ImageUtils.setImage("/images/file.png")),
                                    file.getName(),
                                    FilenameUtils.getExtension(file.getName()),
                                    Double.toString(file.getSize() / 1024),
                                    dateFormat.format(file.getTimestamp().getTime())));
                } else if (file.getType() == 1) {
                    fileList.add(
                            new TableViewFile(
                                    new ImageView(ImageUtils.setImage("/images/folder.png")),
                                    file.getName(),
                                    "dir",
                                    "",
                                    dateFormat.format(file.getTimestamp().getTime())));
                }
            }
        } catch (Exception ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
        return fileList;
    }

    /**
     * Handling action event on Connect button
     *
     * @param event ActionEvent
     */
    @FXML
    public void onConnectButtonClick(ActionEvent event) {
        consoleOut.setCache(false);
        ScrollPane sp = (ScrollPane) consoleOut.getChildrenUnmodifiable().get(0);
        sp.setCache(false);
        for (Node n : sp.getChildrenUnmodifiable()) {
            n.setCache(false);
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/LoginWindow.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            LoginWindowController controller = fxmlLoader.getController();
            //Console
            Console c = new Console(consoleOut);
            PrintStream ps = new PrintStream(c, true);
            System.setOut(ps);
            System.setErr(ps);
            //
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle("Connection form");
            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.showAndWait();
            connection = controller.getInitializedConnection();

            if (connection != null) {
                remoteFileTableView.setDisable(false);
                remotePathTextField.setDisable(false);
                remoteFileTableView.setItems(
                        this.getRestoreListOfFile(
                                new FTPOperation(
                                        connection.getConnection()).getCurrentDirPath()));
                remotePathTextField.setText(new FTPOperation(
                        connection.getConnection()).getCurrentDirPath());
                statusBar.setText("Currect connection info: "
                        + connection.getConnection().getPassiveHost()
                        + " ["
                        + connection.getConnection().getPassivePort()
                        + "]");
            }
        } catch (Exception ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
    }

    /**
     * Handling action event on "About" MenuItem click for open "About" window
     *
     * @param event ActionEvent
     */
    @FXML
    public void onMenuHelpAboutItemClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/AboutWindow.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle("About");
            stage.getIcons().add(
                    new Image(getClass()
                            .getResourceAsStream("/images/icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.show();
        } catch (IOException ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
    }

    /**
     * Handling event on drag detected rows for local file table for "local"
     * table
     *
     * @param event drag&drop event
     */
    @FXML
    public void onLocalFileTableViewDragDetected(MouseEvent event) {
        ObservableList<TableViewFile> fileList = localFileTableView
                .getSelectionModel()
                .getSelectedItems();
        localFileTableView.getSelectionModel().selectFirst();
        TableViewFile path = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (!fileList.isEmpty()) {
            Dragboard db = localFileTableView.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            List<File> files = new ArrayList<>();
            if (!path.getName().endsWith(File.separator)) {
                for (TableViewFile file : fileList) {
                    files.add(new File(path.getName() + File.separator + file.getName()));
                }
            } else {
                for (TableViewFile file : fileList) {
                    files.add(new File(path.getName() + file.getName()));
                }
            }
            content.putFiles(files);
            db.setContent(content);
            event.consume();
            localFileTableView.setItems(this.getLocalListOfFile(path.getName()));
        }
    }

    /**
     * Handling event on drag detected rows for local file table for "remote"
     * table
     *
     * @param event drag&drop event
     */
    @FXML
    public void onRemoteFileTableViewDragDetected(MouseEvent event) {
        ObservableList<TableViewFile> fileList = remoteFileTableView
                .getSelectionModel()
                .getSelectedItems();

        remoteFileTableView.getSelectionModel().selectFirst();
        TableViewFile path = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (!fileList.isEmpty()) {
            Dragboard db = remoteFileTableView.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            String temp = "";
            if (!path.getName().endsWith("/")) {
                for (TableViewFile file : fileList) {
                    temp += path.getName() + "/" + file.getName();
                    temp += "#!%delimiter%!#";
                }
            } else {
                for (TableViewFile file : fileList) {
                    temp += path.getName() + file.getName();
                    temp += "#!%delimiter%!#";
                }
            }
            content.putUrl(temp);
            db.setContent(content);
            event.consume();
            remoteFileTableView.setItems(getRestoreListOfFile(path.getName()));
        }
    }

    /**
     * Handling event on drag over rows for local file table for "local" table
     *
     * @param event drag&drop event
     */
    @FXML
    public void onLocalFileTableViewDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (event.getDragboard().hasUrl()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    /**
     * Handling event on drag over rows for local file table for "remote" table
     *
     * @param event drag&drop event
     */
    @FXML
    public void onRemoteFileTableViewDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    /**
     * Handling event on drag drop rows for local file table for "remote" table
     *
     * @param event drag&drop event
     */
    @FXML
    public void onRemoteFileTableViewDragDropped(DragEvent event) {
        remoteFileTableView.getSelectionModel().selectFirst();
        final TableViewFile path = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        Dragboard db = event.getDragboard();
        boolean success = false;
        try {
            if (event.getDragboard().hasFiles() && connection != null) {
                final List<File> files = db.getFiles();
                files.remove(0);//dummy but works (for removing home path link)
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                Thread.sleep(500);
                                new FTPOperation(connection.getConnection()).uploadFile(files, path.getName());
                                Thread.sleep(500);
                                return null;
                            }
                        };
                    }
                };
                FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/LoadingWindow.fxml"));
                Parent root = null;
                try {
                    root = (Parent) fxmlLoader.load();
                } catch (IOException ex) {
                    new MessageWindow().showException(
                            "Exception",
                            "Exception has been occured",
                            "Please, read the exception stack",
                            ex);
                }
                final Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.setTitle("Loading...");
                stage.getIcons().add(
                        new Image(getClass().getResourceAsStream("/images/icon.png")));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNDECORATED);
                service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        stage.show();
                    }
                });
                service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        stage.close();
                        Notifications.create()
                                .title("Success")
                                .text("File transfer ended successfully")
                                .showInformation();
                        remoteFileTableView.setItems(
                                getRestoreListOfFile(path.getName()));
                    }
                });
                service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        stage.close();
                        Notifications.create()
                                .title("Fail")
                                .text("File transfer ended unsuccessfully")
                                .showWarning();
                    }
                });
                service.start();
                success = true;
            }
        } catch (Exception ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
        event.setDropCompleted(success);
        event.consume();
        remoteFileTableView.setItems(getRestoreListOfFile(path.getName()));
    }

    /**
     * Handling event on drag drop rows for local file table for "local" table
     *
     * @param event drag&drop event
     */
    @FXML
    public void onLocalFileTableViewDragDropped(DragEvent event) {
        localFileTableView.getSelectionModel().selectFirst();
        final TableViewFile path = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        Dragboard db = event.getDragboard();
        boolean success = false;
        try {
            if (event.getDragboard().hasUrl()) {
                String url = db.getUrl();
                final List<String> arr = new LinkedList<>(Arrays.asList(url.split("#!%delimiter%!#")));
                arr.remove(0);
                if (!arr.isEmpty()) {
                    for (String s : arr) {
                        System.out.println("! " + s);
                    }
                    Service<Void> service = new Service<Void>() {
                        @Override
                        protected Task<Void> createTask() {
                            return new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Thread.sleep(500);
                                    new FTPOperation(
                                            connection.getConnection())
                                            .downloadFile(arr, path.getName());
                                    Thread.sleep(500);
                                    return null;
                                }
                            };
                        }
                    };
                    FXMLLoader fxmlLoader = new FXMLLoader(
                            getClass().getResource("/fxml/LoadingWindow.fxml"));
                    Parent root = null;
                    try {
                        root = (Parent) fxmlLoader.load();
                    } catch (IOException ex) {
                        new MessageWindow().showException(
                                "Exception",
                                "Exception has been occured",
                                "Please, read the exception stack",
                                ex);
                    }
                    final Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setResizable(false);
                    stage.setTitle("Loading...");
                    stage.getIcons().add(new Image(
                            getClass().getResourceAsStream("/images/icon.png")));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initStyle(StageStyle.UNDECORATED);
                    service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.show();
                        }
                    });
                    service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.close();
                            Notifications.create()
                                    .title("Success")
                                    .text("File transfer ended successfully")
                                    .showInformation();
                            localFileTableView.setItems(
                                    getLocalListOfFile(path.getName()));
                        }
                    });
                    service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.close();
                            Notifications.create()
                                    .title("Fail")
                                    .text("File transfer ended unsuccessfully")
                                    .showWarning();
                        }
                    });
                    service.start();
                }
                success = true;
            }
        } catch (Exception ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
        event.setDropCompleted(success);
        event.consume();
        localFileTableView.setItems(this.getLocalListOfFile(path.getName()));
    }

    /**
     * Handling mouse click event on Reload button
     *
     * @param event mouse click event
     */
    @FXML
    public void onReloadTableViewContentClick(MouseEvent event) {
        remoteFileTableView.getSelectionModel().selectFirst();
        TableViewFile restorePath = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (restorePath != null) {
            remoteFileTableView.setItems(getRestoreListOfFile(
                    restorePath.getName()));
        }
        localFileTableView.getSelectionModel().selectFirst();
        TableViewFile localPath = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (localPath != null) {
            localFileTableView.setItems(
                    this.getLocalListOfFile(localPath.getName()));
        }
    }

    /**
     * Handling mouse click event on "Connection info" window
     *
     * @param event mouse click event
     */
    @FXML
    public void onInfoButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/InfoWindow.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            InfoWindowController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle("Information about current connection");
            stage.getIcons().add(
                    new Image(
                            getClass().getResourceAsStream("/images/icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            if (connection != null) {
                controller.setConnection(connection);
            } else {
                new MessageWindow().showInformation(
                        "Warning",
                        "You are not connected to any FTP Server",
                        "Please, open 'Login Form' and input your data",
                        Alert.AlertType.WARNING);
            }
            stage.show();
        } catch (IOException ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
    }

    /**
     * Handling mouse click event on Send FTP command button
     *
     * @param event mouse click event
     */
    @FXML
    public void onSendButtonClick(MouseEvent event) {
        String command = commandTextField.getText();
        if (command != null && !command.isEmpty()) {
            try {
                new FTPOperation(connection.getConnection()).sendCommand(command);
            } catch (Exception ex) {
                new MessageWindow().showException(
                        "Exception",
                        "Exception has been occured",
                        "Please, read the exception stack",
                        ex);
            }
        } else {
            new MessageWindow().showInformation(
                    "Warning",
                    "Command error",
                    "Please, input FTP command",
                    Alert.AlertType.WARNING);
        }
    }

    /**
     * Handling mouse click event on disconnect button
     *
     * @param event mouse click event
     */
    @FXML
    public void onDisconnectButtonClick(ActionEvent event) {
        try {
            connection.disconnect();
            remoteFileTableView.getItems().clear();
            localFileTableView.getSelectionModel().selectFirst();
            TableViewFile path = (TableViewFile) localFileTableView
                    .getSelectionModel()
                    .getSelectedItem();
            localFileTableView.setItems(this.getLocalListOfFile(path.getName()));
            statusBar.setText("Connection is not established");
            remoteFileTableView.setDisable(true);
            remotePathTextField.setDisable(true);
        } catch (IOException ex) {
            new MessageWindow().showException(
                    "Exception",
                    "Exception has been occured",
                    "Please, read the exception stack",
                    ex);
        }
        new MessageWindow().showInformation(
                "Success",
                "Disconnect",
                "Successfully disconnected",
                Alert.AlertType.INFORMATION);
    }

    /**
     * Handling event on "command" text field keyboard key pressed
     *
     * @param event keyboard key press event
     */
    @FXML
    public void onCommandTextFieldEnterPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String command = commandTextField.getText();
            if (command != null && !command.isEmpty()) {
                try {
                    new FTPOperation(
                            connection.getConnection()).sendCommand(command);
                    commandTextField.setText("");
                } catch (Exception ex) {
                    new MessageWindow().showException(
                            "Exception",
                            "Exception has been occured",
                            "Please, read the exception stack",
                            ex);
                }
            } else {
                new MessageWindow().showInformation(
                        "Warning",
                        "Command error",
                        "Please, input FTP command",
                        Alert.AlertType.WARNING);
            }
        }
    }

    /**
     * Handling event on "close application" menu item click
     *
     * @param event mouse click event
     */
    @FXML
    public void onCloseWindowButtonClick(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Handling event on "delete" context menu item click for "remote" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onRemoteFileTableViewDeleteMenuItemClick(ActionEvent event) {
        ObservableList<TableViewFile> selected = remoteFileTableView
                .getSelectionModel()
                .getSelectedItems();
        remoteFileTableView.getSelectionModel().selectFirst();
        final TableViewFile restorePath = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        Optional<ButtonType> option = new MessageWindow().showConfirm(
                "Confirm",
                "Deleting file",
                "Are you really want to delete this file?");
        if (selected.isEmpty()) {
            new MessageWindow().showInformation(
                    "Error",
                    "Choose files",
                    "Please, choose at least one file to delete",
                    Alert.AlertType.ERROR);
        } else {
            if (option.get() == ButtonType.OK) {
                final List<String> list = new ArrayList<>();
                if (restorePath.getName().endsWith("/")) {
                    for (TableViewFile elem : selected) {
                        list.add(restorePath.getName() + elem.getName());
                    }
                } else {
                    for (TableViewFile elem : selected) {
                        list.add(restorePath.getName() + "/" + elem.getName());
                    }
                }
                try {
                    list.remove(0);//remove homepath
                    Service<Void> service = new Service<Void>() {
                        @Override
                        protected Task<Void> createTask() {
                            return new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Thread.sleep(500);
                                    new FTPOperation(
                                            connection.getConnection())
                                            .delete(list);
                                    Thread.sleep(500);
                                    return null;
                                }
                            };
                        }
                    };
                    FXMLLoader fxmlLoader = new FXMLLoader(
                            getClass().getResource("/fxml/LoadingWindow.fxml"));
                    Parent root = null;
                    try {
                        root = (Parent) fxmlLoader.load();
                    } catch (IOException ex) {
                        new MessageWindow().showException(
                                "Exception",
                                "Exception has been occured",
                                "Please, read the exception stack",
                                ex);
                    }
                    final Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setResizable(false);
                    stage.setTitle("Loading...");
                    stage.getIcons().add(new Image(
                            getClass().getResourceAsStream("/images/icon.png")));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initStyle(StageStyle.UNDECORATED);
                    service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.show();
                        }
                    });
                    service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.close();
                            Notifications.create()
                                    .title("Success")
                                    .text("Operation ended successfully")
                                    .showInformation();
                            remoteFileTableView.setItems(
                                    getRestoreListOfFile(restorePath.getName()));
                        }
                    });
                    service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.close();
                            Notifications.create()
                                    .title("Fail")
                                    .text("Operation ended unsuccessfully")
                                    .showWarning();
                        }
                    });
                    service.start();
                } catch (Exception ex) {
                    new MessageWindow().showException(
                            "Exception",
                            "Exception has been occured",
                            "Please, read the exception stack",
                            ex);
                }
            }
        }
        remoteFileTableView.setItems(getRestoreListOfFile(restorePath.getName()));
    }

    /**
     * Handling event on "delete" context menu item click for "local" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onLocalFileTableViewDeleteElementMenuItemClick(ActionEvent event) {
        ObservableList<TableViewFile> selected = localFileTableView
                .getSelectionModel()
                .getSelectedItems();
        localFileTableView.getSelectionModel().selectFirst();
        final TableViewFile localPath = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        Optional<ButtonType> option = new MessageWindow().showConfirm(
                "Confirm",
                "Deleting file",
                "Are you really want to delete this file?");
        if (selected.isEmpty()) {
            new MessageWindow().showInformation(
                    "Error",
                    "Choose files",
                    "Please, choose at least one file to delete",
                    Alert.AlertType.ERROR);
        } else {
            if (option.get() == ButtonType.OK) {
                final List<String> list = new ArrayList<>();
                if (localPath.getName().endsWith(File.separator)) {
                    for (TableViewFile elem : selected) {
                        list.add(localPath.getName() + elem.getName());
                    }
                } else {
                    for (TableViewFile elem : selected) {
                        list.add(localPath.getName() + File.separator + elem.getName());
                    }
                }
                try {
                    list.remove(0);//remove homepath
                    Service<Void> service = new Service<Void>() {
                        @Override
                        protected Task<Void> createTask() {
                            return new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Thread.sleep(500);
                                    for (String s : list) {
                                        new File(s).delete();
                                    }
                                    Thread.sleep(500);
                                    return null;
                                }
                            };
                        }
                    };
                    FXMLLoader fxmlLoader = new FXMLLoader(
                            getClass().getResource("/fxml/LoadingWindow.fxml"));
                    Parent root = null;
                    try {
                        root = (Parent) fxmlLoader.load();
                    } catch (IOException ex) {
                        new MessageWindow().showException(
                                "Exception",
                                "Exception has been occured",
                                "Please, read the exception stack",
                                ex);
                    }
                    final Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setResizable(false);
                    stage.setTitle("Loading...");
                    stage.getIcons().add(
                            new Image(
                                    getClass().getResourceAsStream("/images/icon.png")));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initStyle(StageStyle.UNDECORATED);
                    service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.show();
                        }
                    });
                    service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.close();
                            Notifications.create()
                                    .title("Success")
                                    .text("Operation ended successfully")
                                    .showInformation();
                            localFileTableView.setItems(getLocalListOfFile(localPath.getName()));
                        }
                    });
                    service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            stage.close();
                            Notifications.create()
                                    .title("Fail")
                                    .text("Operation ended unsuccessfully")
                                    .showWarning();
                        }
                    });
                    service.start();

                } catch (Exception ex) {
                    new MessageWindow().showException(
                            "Exception",
                            "Exception has been occured",
                            "Please, read the exception stack",
                            ex);
                }
            }
        }
        localFileTableView.setItems(this.getLocalListOfFile(localPath.getName()));
    }

    /**
     * Handling event on "add new folder" context menu item click for "remote"
     * table
     *
     * @param event mouse click event
     */
    @FXML
    public void onRemoteFileTableViewAddNewFolderMenuItemClick(ActionEvent event) {
        remoteFileTableView.getSelectionModel().selectFirst();
        final TableViewFile restorePath = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        String result = new MessageWindow().showTextInput(
                "Add new folder",
                "Please, input folder name");
        if (result != null) {
            if (!result.isEmpty()) {
                try {
                    if (restorePath.getName().endsWith("/")) {
                        new FTPOperation(connection.getConnection())
                                .createDirectory(restorePath.getName() + result);
                    } else {
                        new FTPOperation(connection.getConnection())
                                .createDirectory(restorePath.getName() + "/" + result);
                    }
                    remoteFileTableView.setItems(getRestoreListOfFile(restorePath.getName()));
                } catch (Exception ex) {
                    new MessageWindow().showException(
                            "Exception",
                            "Exception has been occured",
                            "Please, read the exception stack",
                            ex);
                }
            } else {
                new MessageWindow().showInformation(
                        "Error",
                        "Folder name is empty",
                        "Please, input folder name",
                        Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Handling event on "add new folder" context menu item click for "local"
     * table
     *
     * @param event mouse click event
     */
    @FXML
    public void onLocalFileTableViewAddNewFolderMenuItemClick(ActionEvent event) {
        localFileTableView.getSelectionModel().selectFirst();
        final TableViewFile localPath = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        String result = new MessageWindow().showTextInput(
                "Add new folder",
                "Please, input folder name");
        if (result != null) {
            if (!result.isEmpty()) {
                try {
                    if (localPath.getName().endsWith(File.separator)) {
                        File theDir = new File(localPath.getName() + result);
                        theDir.mkdir();
                    } else {
                        File theDir = new File(localPath.getName() + File.separator + result);
                        theDir.mkdir();
                    }
                } catch (Exception ex) {
                    new MessageWindow().showException(
                            "Exception",
                            "Exception has been occured",
                            "Please, read the exception stack",
                            ex);
                }
            } else {
                new MessageWindow().showInformation(
                        "Error",
                        "Folder name is empty",
                        "Please, input folder name",
                        Alert.AlertType.ERROR);
            }
            localFileTableView.setItems(getLocalListOfFile(localPath.getName()));
        }
    }

    /**
     * Handling event on "rename" context menu item click for "remote" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onRemoteFileTableViewRenameFolderMenuItemClick(ActionEvent event) {
        TableViewFile selected = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        remoteFileTableView.getSelectionModel().selectFirst();
        TableViewFile restorePath = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (selected.getName() != null) {
            String oldPath = null;
            if (restorePath.getName().endsWith("/")) {
                oldPath = restorePath.getName() + selected.getName();
            } else {
                oldPath = restorePath.getName() + "/" + selected.getName();
            }
            String newPath = new MessageWindow().showTextInput(
                    "Rename",
                    "Please, input new name",
                    oldPath);
            if (newPath != null) {
                if (!newPath.isEmpty() && !newPath.equals(oldPath)) {
                    try {
                        new FTPOperation(connection.getConnection()).rename(oldPath, newPath);
                    } catch (Exception ex) {
                        new MessageWindow().showException(
                                "Exception",
                                "Exception has been occured",
                                "Please, read the exception stack",
                                ex);
                    }
                } else {
                    new MessageWindow().showInformation(
                            "Error",
                            "Field empty or contains the same name",
                            "Please, input field",
                            Alert.AlertType.ERROR);
                }
            }
        }
        remoteFileTableView.setItems(getRestoreListOfFile(restorePath.getName()));
    }

    /**
     * Handling event on "rename" context menu item click for "local" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onLocalFileTableViewRenameFolderMenuItemClick(ActionEvent event) {
        TableViewFile selected = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        localFileTableView.getSelectionModel().selectFirst();
        TableViewFile localPath = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (selected.getName() != null) {
            String oldPath = null;
            if (localPath.getName().endsWith(File.separator)) {
                oldPath = localPath.getName() + selected.getName();
            } else {
                oldPath = localPath.getName() + File.separator + selected.getName();
            }
            String newPath = new MessageWindow().showTextInput(
                    "Rename",
                    "Please, input new name",
                    oldPath);
            if (newPath != null) {
                if (!newPath.isEmpty() && !newPath.equals(oldPath)) {
                    try {
                        File file = new File(oldPath);
                        file.renameTo(new File(newPath));
                    } catch (Exception ex) {
                        new MessageWindow().showException(
                                "Exception",
                                "Exception has been occured",
                                "Please, read the exception stack",
                                ex);
                    }
                } else {
                    new MessageWindow().showInformation(
                            "Error",
                            "Field empty",
                            "Please, input field",
                            Alert.AlertType.ERROR);
                }
            }
            localFileTableView.setItems(this.getLocalListOfFile(localPath.getName()));
        }
    }

    /**
     * Handling event on "copy" context menu item click for "remote" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onRemoteFileTableViewCopyMenuItemClick(ActionEvent event) {
        ObservableList<TableViewFile> selected = remoteFileTableView
                .getSelectionModel()
                .getSelectedItems();
        remoteFileTableView.getSelectionModel().selectFirst();
        final TableViewFile restorePath = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        localFileTableView.getSelectionModel().selectFirst();
        final TableViewFile localPath = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        String newPath = new MessageWindow().showTextInput(
                "Copy",
                "Are you sure you want copy files to this directory:",
                localPath.getName());

        if (selected != null) {

            if (selected.isEmpty()) {
                new MessageWindow().showInformation(
                        "Error",
                        "Choose files",
                        "Please, choose at least one file to copy",
                        Alert.AlertType.ERROR);
            } else {
                if (newPath != null) {
                    if (!newPath.equals("") && !newPath.isEmpty()) {
                        final List<String> list = new ArrayList<>();
                        if (restorePath.getName().endsWith("/")) {
                            for (TableViewFile elem : selected) {
                                list.add(restorePath.getName() + elem.getName());
                            }
                        } else {
                            for (TableViewFile elem : selected) {
                                list.add(restorePath.getName() + "/" + elem.getName());
                            }
                        }
                        String temp = null;
                        if (localPath.getName().endsWith(File.separator)) {
                            temp = localPath.getName();
                        } else {
                            temp = localPath.getName() + File.separator;
                        }
                        final String dirPath = temp;

                        try {
                            list.remove(0);
                            Service<Void> service = new Service<Void>() {
                                @Override
                                protected Task<Void> createTask() {
                                    return new Task<Void>() {
                                        @Override
                                        protected Void call() throws Exception {
                                            Thread.sleep(500);
                                            new FTPOperation(
                                                    connection.getConnection())
                                                    .downloadFile(list, dirPath);
                                            Thread.sleep(500);
                                            return null;
                                        }
                                    };
                                }
                            };
                            FXMLLoader fxmlLoader = new FXMLLoader(
                                    getClass().getResource("/fxml/LoadingWindow.fxml"));
                            Parent root = null;
                            try {
                                root = (Parent) fxmlLoader.load();
                            } catch (IOException ex) {
                                new MessageWindow().showException(
                                        "Exception",
                                        "Exception has been occured",
                                        "Please, read the exception stack",
                                        ex);
                            }
                            final Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.setResizable(false);
                            stage.setTitle("Loading...");
                            stage.getIcons().add(
                                    new Image(getClass().getResourceAsStream("/images/icon.png")));
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.initStyle(StageStyle.UNDECORATED);
                            service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent event) {
                                    stage.show();
                                }
                            });
                            service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent event) {
                                    stage.close();
                                    Notifications.create()
                                            .title("Success")
                                            .text("Operation ended successfully")
                                            .showInformation();
                                    remoteFileTableView.setItems(
                                            getRestoreListOfFile(restorePath.getName()));
                                    localFileTableView.setItems(
                                            getLocalListOfFile(localPath.getName()));
                                }
                            });
                            service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent event) {
                                    stage.close();
                                    Notifications.create()
                                            .title("Fail")
                                            .text("Operation ended unsuccessfully")
                                            .showWarning();
                                }
                            });
                            service.start();
                        } catch (Exception ex) {
                            new MessageWindow().showException(
                                    "Exception",
                                    "Exception has been occured",
                                    "Please, read the exception stack",
                                    ex);
                        }
                    }
                }
            }

        }
    }

    /**
     * Handling event on "copy" context menu item click for "local" table
     *
     * @param event mouse click event
     */
    @FXML
    public void onLocalFileTableViewCopyMenuItemClick(ActionEvent event) {
        ObservableList<TableViewFile> selected = localFileTableView
                .getSelectionModel()
                .getSelectedItems();
        localFileTableView.getSelectionModel().selectFirst();
        final TableViewFile localPath = (TableViewFile) localFileTableView
                .getSelectionModel()
                .getSelectedItem();
        remoteFileTableView.getSelectionModel().selectFirst();
        final TableViewFile restorePath = (TableViewFile) remoteFileTableView
                .getSelectionModel()
                .getSelectedItem();
        if (restorePath != null) {
            String newPath = new MessageWindow().showTextInput(
                    "Copy",
                    "Are you sure you want copy files to this directory:",
                    restorePath.getName());
            if (selected.isEmpty()) {
                new MessageWindow().showInformation(
                        "Error",
                        "Choose files",
                        "Please, choose at least one file to delete",
                        Alert.AlertType.ERROR);
            } else {
                if (newPath != null && !newPath.equals("") && !newPath.isEmpty()) {
                    final List<String> list = new ArrayList<>();
                    if (localPath.getName().endsWith(File.separator)) {
                        for (TableViewFile elem : selected) {
                            list.add(localPath.getName() + elem.getName());
                        }
                    } else {
                        for (TableViewFile elem : selected) {
                            list.add(localPath.getName() + File.separator + elem.getName());
                        }
                    }
                    String temp = null;
                    if (restorePath.getName().endsWith("/")) {
                        temp = restorePath.getName();
                    } else {
                        temp = restorePath.getName() + "/";
                    }
                    final String dirPath = temp;
                    try {
                        list.remove(0);//remove homepath
                        Service<Void> service = new Service<Void>() {
                            @Override
                            protected Task<Void> createTask() {
                                return new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        Thread.sleep(500);
                                        List<File> files = new ArrayList<>();
                                        for (String s : list) {
                                            files.add(new File(s));
                                        }
                                        new FTPOperation(connection.getConnection())
                                                .uploadFile(files, dirPath);
                                        Thread.sleep(500);
                                        return null;
                                    }
                                };
                            }
                        };
                        FXMLLoader fxmlLoader = new FXMLLoader(
                                getClass().getResource("/fxml/LoadingWindow.fxml"));
                        Parent root = null;
                        try {
                            root = (Parent) fxmlLoader.load();
                        } catch (IOException ex) {
                            new MessageWindow().showException(
                                    "Exception",
                                    "Exception has been occured",
                                    "Please, read the exception stack",
                                    ex);
                        }
                        final Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
                        stage.setTitle("Loading...");
                        stage.getIcons().add(new Image(
                                getClass().getResourceAsStream("/images/icon.png")));
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.initStyle(StageStyle.UNDECORATED);
                        service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent event) {
                                stage.show();
                            }
                        });
                        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent event) {
                                stage.close();
                                Notifications.create()
                                        .title("Success")
                                        .text("Operation ended successfully")
                                        .showInformation();
                                remoteFileTableView.setItems(
                                        getRestoreListOfFile(restorePath.getName()));
                            }
                        });
                        service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent event) {
                                stage.close();
                                Notifications.create()
                                        .title("Fail")
                                        .text("Operation ended unsuccessfully")
                                        .showWarning();
                            }
                        });
                        service.start();
                    } catch (Exception ex) {
                        new MessageWindow().showException(
                                "Exception",
                                "Exception has been occured",
                                "Please, read the exception stack",
                                ex);
                    }
                }
            }
            localFileTableView.setItems(this.getLocalListOfFile(localPath.getName()));
            remoteFileTableView.setItems(getRestoreListOfFile(restorePath.getName()));
        } else {
            new MessageWindow().showInformation(
                    "Error",
                    "Connection is not established",
                    "Please, create new connection",
                    Alert.AlertType.ERROR);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        localFileTableView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE);
        remoteFileTableView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE);
        String home = System.getProperty("user.home");
        localPathTextField.setText(home);
        localFileTableView.setItems(this.getLocalListOfFile(home));
        remoteFileTableView.setDisable(true);
        remotePathTextField.setDisable(true);
    }
}
