package de.oberien.adbremotecontrol.adb;

import de.oberien.adbremotecontrol.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AdbScreencap extends AdbExecOut {
    private BufferedImage screenshot;
    private boolean useBase64;

    public AdbScreencap(boolean useBase64) {
        super("screencap " + (Config.imageFormat.equals("jpg") ? "-j" : "-p") + (useBase64 ? " | base64 -w 0" : ""));
        this.useBase64 = useBase64;
    }

    public BufferedImage getScreenshot() {
        if (screenshot == null)
            try {
                byte[] img = readToEnd();

                if (this.useBase64) {
                    // convert to string to handle UTF-16 correctly on Windows
                    String correctEncoding = new String(img);
                    correctEncoding = correctEncoding.trim();
                    img = correctEncoding.getBytes(StandardCharsets.ISO_8859_1);
                    Base64.getMimeDecoder().decode(img, img);
                } else {
                    img = tryFixODOA(img);
                    writeByte("device_dump.png", img);
                }
                ByteArrayInputStream bis = new ByteArrayInputStream(img);
                screenshot = ImageIO.read(bis);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                screenshot = null;
            }
        return screenshot;
    }

    static byte[] tryFixODOA(byte[] src) {
        ByteArrayOutputStream bos = null;
        if (src.length > 16) {
            // checing for .PNG header
            if (src[0] == -119 && src[1] == 80 && src[2] == 78 && src[3] == 71) {
                if (src[5] == 0x0D && src[6] == 0x0A) {
                    // broken OD OA, it must be 0A at src[5]
                    // http://www.libpng.org/pub/png/spec/1.2/PNG-Rationale.html#R.PNG-file-signature
                    bos = new ByteArrayOutputStream();
                    for (int i = 0; i < src.length - 1; ++i)
                        if (!(src[i] == 0x0D && src[i + 1] == 0x0A))
                            bos.write(src[i]);
                }
            }
        }
        return (bos != null) ? bos.toByteArray() : src;
    }

    // Method which write the bytes into a file
    static void writeByte(String filename, byte[] bytes) {
        try {

            // Initialize a pointer
            // in file using OutputStream
            OutputStream os = new FileOutputStream(filename);

            // Starts writing the bytes in it
            os.write(bytes);
            // Close the file
            os.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}
