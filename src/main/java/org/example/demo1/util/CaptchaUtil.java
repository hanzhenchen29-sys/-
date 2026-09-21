package org.example.demo1.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

//验证码的工具类
public class CaptchaUtil {
    private static final int HEIGHT = 40;
    private static final int WIDTH = 120;
    private static final char[] CHARS = {'2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    private static final int CODE_LEN = 4;
    private static final Random RANDOM = new Random();
    public static String generate(OutputStream out) throws IOException {
        // 创建画布
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景浅灰色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制干扰线
        for (int i = 0; i < 10; i++) {
            g.setColor(getRandColor(160, 200));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制噪点
        for (int i = 0; i < 80; i++) {
            g.setColor(getRandColor(120, 200));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.drawRect(x, y, 1, 1);
        }

        // 生成验证码字符
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LEN; i++) {
            char c = CHARS[RANDOM.nextInt(CHARS.length)];
            code.append(c);
            // 字体随机大小、颜色、旋转
            g.setColor(new Color(20 + RANDOM.nextInt(110), 20 + RANDOM.nextInt(110), 20 + RANDOM.nextInt(110)));
            Font font = new Font("Arial", Font.BOLD, 28);
            g.setFont(font);
            double theta = RANDOM.nextDouble() * Math.PI / 6 - Math.PI / 12;
            g.rotate(theta, 25 + i * 22, 28);
            g.drawString(String.valueOf(c), 25 + i * 22, 28);
            g.rotate(-theta, 25 + i * 22, 28);
        }

        g.dispose();
        //出图
        ImageIO.write(image, "jpg", out);
        return code.toString();
    }

    //颜色随机
    private static Color getRandColor(int fc, int bc) {
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + RANDOM.nextInt(bc - fc);
        int g = fc + RANDOM.nextInt(bc - fc);
        int b = fc + RANDOM.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
