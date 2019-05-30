package GameClasses;

import java.awt.*;

public class ScreenText extends Component {

    private float x;
    private float y;
    private int textSize;
    private String text;
    private Color col;

    public ScreenText(float x, float y, String text, int textSize, Color col){
        this.x = x;
        this.y = y;
        this.text = text;
        this.textSize = textSize;
        this.col = col;
    }

    public void paint(Graphics g) {

        g.setColor(col);
        Graphics2D g2d = (Graphics2D) g;
        Font font = new Font("Serif", Font.PLAIN, textSize);
        g2d.setFont(font);
        g2d.drawString(text, x-(g.getFontMetrics().stringWidth(text)/2), y);

    }

}

