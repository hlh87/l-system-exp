/** Author: Helen Lily Hu
 * Date: 7/2/19 - 7/7/19*/

package system;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GUI extends Application {

	/** Handles L-system operations */
	LSystemHandler handler;

	/** Launches the application */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// get screen dimensions to create scene
		Rectangle2D screenSize= Screen.getPrimary().getVisualBounds();
		double smolDim= Math.min(screenSize.getHeight(), screenSize.getWidth());

		// initialize assets
		Canvas canvas= new Canvas(smolDim, smolDim);
		GraphicsContext gc= canvas.getGraphicsContext2D();
		handler= new LSystemHandler(gc);

		// white out canvas
		clearRect(gc, smolDim);

		// create layout manager
		Pane root= new Pane();
		root.getChildren().add(canvas);

		HBox toolbar= initToolBar(canvas, gc, smolDim);
		root.getChildren().add(toolbar);

		Scene scene= new Scene(root, smolDim, smolDim, Color.WHITE);
		primaryStage.setTitle("Playing With L-Systems");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

		// set up event handlers
		initEventHandlers(canvas);
	}

	/** Sets event handlers <br>
	 * Helper for start */
	private void initEventHandlers(Canvas canvas) {
		canvas.setOnMousePressed(input -> {
			handler.startSystem(input);
		});

		canvas.setOnMouseReleased(input -> {
			handler.erase();
		});
	}

	/** Sets up and returns tool bar <br>
	 * Helper for start */
	private HBox initToolBar(Canvas canvas, GraphicsContext gc, double canvasSide) {
		HBox toolbar= new HBox();
		toolbar.setAlignment(Pos.CENTER);
		toolbar.setSpacing(10);

		// add L-system selector
		ComboBox<String> sysSelector= initLSystemSelector();
		toolbar.getChildren().add(sysSelector);

		// add color picker
		ColorPicker cPick= initColorPicker(canvas);
		toolbar.getChildren().add(cPick);

		// add stroke size label and selector
		Text sizeLabel= new Text("   Size ");
		toolbar.getChildren().add(sizeLabel);
		ComboBox<Integer> sizeSelector= initSizeSelector();
		toolbar.getChildren().add(sizeSelector);

		// add clear button
		Button clear= initClearButton(gc, canvasSide);
		toolbar.getChildren().add(clear);

		return toolbar;

	}

	/** Sets up and returns L-system selector <br>
	 * Helper for initToolBar */
	private ComboBox<String> initLSystemSelector() {
		ComboBox<String> box= new ComboBox<>();
		box.getItems().addAll("Original Lindenmayer", "Barnsley Fern-ish",
			"Fractal Plant", "Lichtenberg Figure", "Cracked Earth", "Porpita porpita");
		box.setEditable(false);
		box.setValue("Original Lindenmayer");

		box.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				switch (box.getValue()) {
				case "Original Lindenmayer":
					handler.setSystem('A');
					break;
				case "Barnsley Fern-ish":
					handler.setSystem('B');
					break;
				case "Fractal Plant":
					handler.setSystem('F');
					break;
				case "Lichtenberg Figure":
					handler.setSystem('L');
					break;
				case "Cracked Earth":
					handler.setSystem('C');
					break;
				case "Porpita porpita":
					handler.setSystem('P');
					break;
				}

			}

		});

		return box;
	}

	/** Sets up and returns color picker <br>
	 * Helper for initToolBar */
	private ColorPicker initColorPicker(Canvas canvas) {
		ColorPicker cPick= new ColorPicker(Color.BLACK);

		cPick.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				handler.setColor(cPick.getValue());
			}

		});

		return cPick;
	}

	/** Sets up and returns size selector <br>
	 * Helper for initToolBar */
	private ComboBox<Integer> initSizeSelector() {
		ComboBox<Integer> box= new ComboBox<>();
		box.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		box.setEditable(false);
		box.setValue(5);

		box.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				switch (box.getValue()) {
				case 1:
					handler.setSize(1);
					break;
				case 2:
					handler.setSize(2);
					break;
				case 3:
					handler.setSize(3);
					break;
				case 4:
					handler.setSize(4);
					break;
				case 5:
					handler.setSize(5);
					break;
				case 6:
					handler.setSize(6);
					break;
				case 7:
					handler.setSize(7);
					break;
				case 8:
					handler.setSize(8);
					break;
				case 9:
					handler.setSize(9);
					break;
				case 10:
					handler.setSize(10);
					break;
				}

			}

		});

		return box;
	}

	/** Sets up and returns clear button for square canvas of side length l<br>
	 * Helper for toolbar */
	private Button initClearButton(GraphicsContext gc, double l) {
		Button clear= new Button("Clear");

		clear.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				clearRect(gc, l);
			}

		});

		return clear;
	}

	/** Clears square w/side length l with white in upper left corner of canvas */
	private void clearRect(GraphicsContext gc, double l) {
		synchronized (gc) {
			gc.setFill(Color.WHITE);
			gc.fillRect(0, 0, l, l);
		}
	}

}
