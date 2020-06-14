package de.oberien.adbremotecontrol.adb;

import de.oberien.adbremotecontrol.thread.KeyboardInputThread;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class AdbDevice {
    final private KeyboardInputThread keyboardThread = new KeyboardInputThread();
    private boolean useBase64 = false;

    public AdbDevice() {
        keyboardThread.start();
    }

    private BufferedImage getScreenshot() {
        BufferedImage screenshot = null;
        System.out.printf("Get Screenshot, base64=%d\n", (useBase64) ? 1 : 0);
        try (AdbScreencap screencap = new AdbScreencap(this.useBase64)) {
            screenshot = screencap.getScreenshot();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return screenshot;
    }

    public BufferedImage screenshot() {

        // The screenshot can be null if the image isn't valid.
        // That can be the case on Windows when stdin / stdout are converted to UTF16
        // like in
        // <https://github.com/oberien/adb-remote-control/issues/12#issuecomment-632007509>.
        // In that case, we encode the image to base64.

        BufferedImage screenshot = getScreenshot();
        if (screenshot == null && !useBase64) {
            System.out.println("Switching to Base64-Mode");
            useBase64 = true;
            screenshot = getScreenshot();
        }

        return screenshot;
    }

    public void text(String text) {
        keyboardThread.addText(text);
    }

    public void type(AndroidKeyEvent key) {
        keyboardThread.addKey(key);
    }

    public void click(int x, int y) {
        try (AdbShell shell = new AdbShell()) {
            shell.executeAsync("input tap " + x + " " + y);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void swipe(int downX, int downY, int upX, int upY, long duration) {
        try (AdbShell shell = new AdbShell()) {
            shell.executeAsync("input swipe " + downX + " " + downY + " " + upX + " " + upY + " " + duration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void draganddrop(int downX, int downY, int upX, int upY, long duration) {
        try (AdbShell shell = new AdbShell()) {
            shell.executeAsync("input draganddrop " + downX + " " + downY + " " + upX + " " + upY + " " + duration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
