package com.Kanak.Connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMN = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static String discColor1 = "#24303E";
	private static String discColor2 = "#4CAA88";


	private static String PLAYER_ONE = "Player One";
	private static String PLAYER_TW0 = "Player Two";

	private  Disc[][] insertedDiscArray = new Disc[ROWS][COLUMN];

	private boolean isPlayerOneTurn = true;

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	public TextField playerOneTextField, playerTwoTextField;

	@FXML
	public Button setNamesButton;

	@FXML
	public ChoiceBox Colour;


	private boolean isAllowedToInsert = true;
	private static final String red = "Red-Orange";
	private static final String d = "Default";

	private void choice(){
		Colour.getItems().add(d);
		Colour.getItems().add(red);
		Colour.setValue(d);
		Colour.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->{
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Connect4");
			alert.setHeaderText("Want to reset the Game");
			alert.setContentText("You have changed the Colour, Do you want to reset the game to change the Colour");
			ButtonType yesBtn = new ButtonType("Yes, Reset");
			ButtonType noBtn = new ButtonType("No, Continue");
			alert.getButtonTypes().setAll(yesBtn,noBtn);

			if (newValue.equals(red)) {
				discColor1 = "#ff0000";
				discColor2 = "#ffa500";
				Platform.runLater(() -> {
					Optional<ButtonType> buttonClicked = alert.showAndWait();
					if (buttonClicked.isPresent() && buttonClicked.get() == yesBtn){
						resetGame();
						return;
					}else {
						discColor1 = "#24303E";
						discColor2 = "#4CAA88";
					}
				});
			}
			if (newValue.equals(d)){
				discColor1 = "#24303E";
				discColor2 = "#4CAA88";
				Platform.runLater(() -> {
					Optional<ButtonType> buttonClicked = alert.showAndWait();
					if (buttonClicked.isPresent() && buttonClicked.get() == yesBtn){
						resetGame();
						return;
					}else {
						discColor1 = "#ff0000";
						discColor2 = "#ffa500";
					}
				});
			}
		});

	}

	public void createPlayground(){

		Shape rectangleWithHoles = createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles, 0, 1);

		List<Rectangle> rectangleList = createClickableColumns();

		for (Rectangle rectangle: rectangleList) {
			rootGridPane.add(rectangle,0, 1);
		}

		setNamesButton.setOnAction(event -> {
			PLAYER_ONE = playerOneTextField.getText();
			PLAYER_TW0 = playerTwoTextField.getText();
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TW0);
		});
	}

	private Shape createGameStructuralGrid(){
		Shape rectangleWithHoles = new Rectangle((COLUMN + 1) * CIRCLE_DIAMETER,(ROWS + 1) * CIRCLE_DIAMETER);

		for (int row = 0; row < ROWS; row++){
			for (int col = 0; col < COLUMN; col++){
				Circle circle  = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER / 2);
				circle.setCenterY(CIRCLE_DIAMETER / 2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));
				circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));
				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}
		rectangleWithHoles.setFill(javafx.scene.paint.Color.WHITE);
		return rectangleWithHoles;

	}

	private List<Rectangle> createClickableColumns(){


		List<Rectangle> rectangleList = new ArrayList<>();
		for (int col = 0; col < COLUMN; col++){

			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(javafx.scene.paint.Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(javafx.scene.paint.Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(javafx.scene.paint.Color.TRANSPARENT));

			final int column = col;
			rectangle.setOnMouseClicked(event -> {

				if (isAllowedToInsert) {

					isAllowedToInsert = false;
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle);

		}
		return rectangleList;
	}

	private void insertDisc(Disc disc, int column){

		int row = ROWS - 1;
		while (row >= 0){
			if (getDiscIsPresent(row,column) == null)
				break;
			row--;
		}
		if (row < 0)
			return;

		insertedDiscArray[row][column] = disc;
		insertedDiscPane.getChildren().add(disc);
		disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.4), disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));
		translateTransition.setOnFinished(event -> {

			isAllowedToInsert = true;
			if (gameEnded(currentRow, column)){
				gameOver();
				return;
			}
			isPlayerOneTurn = !isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TW0);

		});
		translateTransition.play();
	}

	private boolean gameEnded(int row, int column){

		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3)
										.mapToObj(r -> new Point2D(r, column))
										.collect(Collectors.toList());
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
											.mapToObj(col -> new Point2D(row, col))
											.collect(Collectors.toList());

		Point2D startPoint1 = new Point2D(row - 3, column + 3);
		List<Point2D> diagonalsPoints = IntStream.rangeClosed(0, 6)
										.mapToObj(i -> startPoint1.add(i, -i))
										.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row - 3, column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint2.add(i, i))
				.collect(Collectors.toList());


		boolean isEnded = checkCombination(verticalPoints)
							|| checkCombination(horizontalPoints)
							|| checkCombination(diagonalsPoints)
							|| checkCombination(diagonal2Points);

		return isEnded;
	}

	private boolean checkCombination(List<Point2D> points) {

		int chain = 0;

		for (Point2D point: points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscIsPresent(rowIndexForArray, columnIndexForArray);
			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn){ // if last inserted disc belongs to the current player

				chain++;
				if(chain == 4){
					return true;
				}
			}else {
				chain = 0;
			}
		}
		return  false;
	}

	private Disc getDiscIsPresent(int row, int column){
		if (row >= ROWS || row < 0 || column >= COLUMN || column < 0)
			return null;

		return insertedDiscArray[row][column];
	}

	private void gameOver(){
		String winner = isPlayerOneTurn? PLAYER_ONE : PLAYER_TW0;

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is: " + winner);
		alert.setContentText("Want to play Again");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);

		Platform.runLater(() -> {

			Optional<ButtonType> btnClicked = alert.showAndWait();

			if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
				resetGame();
			}else {
				Platform.exit();
				System.exit(0);
			}
		});

	}
	public void resetGame() {

		insertedDiscPane.getChildren().clear();

		for (int row = 0; row < insertedDiscArray.length; row++) {

			for (int col = 0; col < insertedDiscArray[row].length; col++) {
				insertedDiscArray[row][col] = null;
			}
			isPlayerOneTurn = true;
			playerNameLabel.setText(PLAYER_ONE);

			//createPlayground();
		}
	}

	private static class Disc extends Circle{

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove){

			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER / 2);
			setFill(isPlayerOneMove? javafx.scene.paint.Color.valueOf(discColor1): javafx.scene.paint.Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER / 2);
			setCenterY(CIRCLE_DIAMETER / 2);

		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		choice();
	}
}
