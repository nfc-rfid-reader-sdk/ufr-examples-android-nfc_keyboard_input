package net.dlogic.ufr.keyboard;


import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import net.dlogic.ufr.lib.DlReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.dlogic.ufr.keyboard.MyInputMethodService.Tools.byteArr2Str;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    static Context context;
    static DlReader device;
    private KeyboardView keyboardView;
    private Keyboard keyboard;

    @Override
    public View onCreateInputView() {



        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.number_pad);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);





        return keyboardView;
    }

    @Override
    public void onPress(int i) {
    }

    @Override
    public void onRelease(int i) {

    }
    private boolean caps = false;
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            switch(primaryCode) {
                case Keyboard.KEYCODE_DELETE :
                    CharSequence selectedText = inputConnection.getSelectedText(0);

                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0);
                    } else {
                        inputConnection.commitText("", 1);
                    }
                case Keyboard.KEYCODE_SHIFT:
                    caps = !caps;
                    keyboard.setShifted(caps);
                    keyboardView.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));

                    break;

                default :

                        char code = (char) primaryCode;
                        if (Character.isLetter(code) && caps) {
                            code = Character.toUpperCase(code);
                        }
                        inputConnection.commitText(String.valueOf(code), 1);

            }
        }

    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    public void onStartInputView (EditorInfo info,
                                  boolean restarting)
    {
        context = this;
        try {

            device = DlReader.getInstance(context, R.xml.accessory_filter, R.xml.dev_desc_filter);

        } catch (Exception e) {

            // e.printStackTrace();
        }

        String cardID = "";
        byte[] data1;


        try {


            device.open();
        } catch (Exception e) {

        }


        if (device.readerStillConnected()) {


            DlReader.CardParams c_params = new DlReader.CardParams();

            try {

                cardID = byteArr2Str(device.getCardIdEx(c_params));

                    InputConnection inputConnection = getCurrentInputConnection();
                    inputConnection.commitText(cardID, 1);


            } catch (Exception e) {
                cardID = "";

            }



            // requestHideSelf(0);
        }


        Read();



    }

    public  void Read()
    {



        new Thread(new Task()).start();



    }
    String tempUID="";
    class Task implements Runnable {
        @Override
        public void run() {
            while(true) {
                String cardID = "";
                byte[] data1;


                try {


                    device.open();
                } catch (Exception e) {

                }


                if (device.readerStillConnected()) {


                    DlReader.CardParams c_params = new DlReader.CardParams();

                    try {

                        cardID = byteArr2Str(device.getCardIdEx(c_params));
                        if(tempUID.equals(cardID))
                        {

                        }
                        else {
                            InputConnection inputConnection = getCurrentInputConnection();
                            inputConnection.commitText(cardID, 1);
                        }
                        tempUID = cardID;
                    } catch (Exception e) {
                        cardID = "";
                        tempUID = "";
                    }



                    // requestHideSelf(0);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }



    static class Tools {

        public static boolean isNumeric(String s){
            if(TextUtils.isEmpty(s)){
                return false;
            }
            Pattern p = Pattern.compile("[-+]?[0-9]*");
            Matcher m = p.matcher(s);
            return m.matches();
        }

        public static String byteArr2Str(byte[] byteArray) {
            StringBuilder sBuilder = new StringBuilder(byteArray.length * 2);
            for(byte b: byteArray)
                sBuilder.append(String.format("%02x", b & 0xff));
            return sBuilder.toString();
        }
    }


}