package seoulbin.review;

import javax.swing.*;
import mapdata.Utils;

public class ReviewButton extends JButton {
    int review = 0;

    public ReviewButton(String title) {
        super(title);
    }

    public void loadReview(int bin_index) {
        review = (int)Math.round(Utils.selectBinReview(bin_index)); // 반올림
    }
    
    public int getReview() { return review; }
}
