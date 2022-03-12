import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import java.util.logging.LogManager;


@ExtensionInfo(
        Title = "GBasketball",
        Description = "Bot for help u (Only official Habbo)",
        Version = "0.0.8",
        Author = "Julianty"
)

// This library was used: https://github.com/kwhat/jnativehook
public class GBasketball extends ExtensionForm implements NativeKeyListener, NativeMouseListener{
    public int xPod, yPod;

    public Button buttonStart;
    public TextField textPodId, textDelay, textHotKey;
    public CheckBox checkPodId;

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}    // ignore

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if(textHotKey.isFocused()){
            textHotKey.setText(NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode()));
            Platform.runLater(()->{
                buttonStart.requestFocus(); // Le da el foco al boton
            });
        }
        else if(!textHotKey.isFocused()){
            if(NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode()).equals(textHotKey.getText())){
                handleStart();
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}     // ignore

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {}    // ignore

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        try{                // or 100, which is better?
            Thread.sleep(128);  // Evita excepcion dado que Interceptar "MoveAvatar" tomo un poco de tiempo
        } catch(InterruptedException ignored) {}
        if(String.format("ON [%s]", textHotKey.getText()).equals(buttonStart.getText())){
            int MouseButton = nativeMouseEvent.getButton(); // value 1 for left click and 2 for right click

            try {
                Thread.sleep(Integer.parseInt(textDelay.getText())); // Delay para evitar parecer bot
            } catch (InterruptedException ignored1) {}

            try {
                if(MouseButton == 1){
                    sendToServer(new HPacket("MoveObject", HMessage.Direction.TOSERVER,
                            Integer.parseInt(textPodId.getText()), xPod, yPod, 0));
                }
                /* else if(MouseButton == 2){
                    sendToServer(new HPacket("MoveObject", HMessage.Direction.TOSERVER,
                            Integer.parseInt(textPodId.getText()), xPod, yPod, 0));
                    // I need it to move the pod but not move the user, maybe i will need implement other library
                    // for interpret the right click as left click, or maybe im wrong!, and then get coords and block moveavatar
                    // also i need to use a boolean flag to avoid bug
                }*/
            }catch (NumberFormatException ignored2){}
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {}   // ignore


    @Override // When you opens the extension
    protected void onShow() {
        LogManager.getLogManager().reset(); // https://stackoverflow.com/questions/30560212/how-to-remove-the-logging-data-from-jnativehook-library
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
    }

    @Override // When you close the extension
    protected void onHide() {
        buttonStart.setText(String.format("OFF [%s]", textHotKey.getText()));
        buttonStart.setStyle("-fx-text-fill: RED");
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException nativeHookException) {
            nativeHookException.printStackTrace();
        }
        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseListener(this);
    }

    @Override
    protected void initExtension(){
        textDelay.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Integer.parseInt(textDelay.getText());
            } catch (NumberFormatException e) {
                textDelay.setText(oldValue);
            }
        });

        textHotKey.textProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(()->{
            buttonStart.setText("OFF [" + textHotKey.getText() + "]");
            buttonStart.setStyle("-fx-text-fill: RED");
        }));

        intercept(HMessage.Direction.TOCLIENT, "NotificationDialog", hMessage -> {
            if(primaryStage.isShowing()){
                hMessage.setBlocked(true);  // Block the message: "You can't put the furni here"
                System.out.println("The annoying message has been blocked");
            }
        });

        intercept(HMessage.Direction.TOSERVER, "UseFurniture", hMessage -> {
            if(checkPodId.isSelected()){
                textPodId.setText(String.valueOf(hMessage.getPacket().readInteger()));  checkPodId.setSelected(false);
            }
        });

        intercept(HMessage.Direction.TOSERVER, "MoveAvatar", hMessage -> {
            if(String.format("ON [%s]", textHotKey.getText()).equals(buttonStart.getText())){
                xPod = hMessage.getPacket().readInteger();  yPod = hMessage.getPacket().readInteger();
            }
        });
    }

    public void handleStart()
    {
        if(String.format("OFF [%s]", textHotKey.getText()).equals(buttonStart.getText())){
            if(!"".equals(textPodId.getText())){
                Platform.runLater(()-> buttonStart.setText("ON [" + textHotKey.getText() + "]"));
                buttonStart.setStyle("-fx-text-fill: green");
            }
        }
        else{
            Platform.runLater(()-> buttonStart.setText("OFF [" + textHotKey.getText() + "]"));
            buttonStart.setStyle("-fx-text-fill: red");
        }
    }
}
