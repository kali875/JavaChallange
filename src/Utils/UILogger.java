package Utils;

import challenge.game.event.actioneffect.ActionEffectType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class UILogger {
    public static String log_data = "";
    private boolean full_heart = true;

    public static void log_string(String data) {
        log_data = log_data + data + "\n";
    }

    public static void log_int(int data) {
        log_data = log_data + data + "\n";
    }

    public static void log_float(float data) {
        log_data = log_data + data + "\n";
    }
    public static void log_long(long data) {
        log_data = log_data + data + "\n";
    }

    public static void log_string_arraylist(ArrayList<String> data) {
        for (String d : data) {
            log_data = log_data + d + "\n";
        }
    }

    public static void log_actionEffectType_arraylist(List<ActionEffectType> data) {
        for (ActionEffectType d : data) {
            log_data += d + "\n";
        }
    }

    public void log(JTextArea textArea, JLabel label) {
        if (log_data != "") {
            textArea.append(log_data);
            log_data = "";
        }
        if (full_heart) {
            label.setText("Heartbeat - \u2764");
            full_heart = false;
        } else {
            label.setText("Heartbeat - \u2661");
            full_heart = true;
        }
    }
}