package seoulbin.review;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import mapdata.Utils;

public class ReviewButton extends JButton {
    int review = 0;

    public ReviewButton() {
        super();
        resetReview();
    }

    public void loadReview(int bin_index) {
        review = (int)Math.round(Utils.selectBinReview(bin_index)); // 반올림
        String str = "";
        System.out.println(review);
        if (review <= 5 && review >= 0) {
            for (int i=1; i<=5; ++i) {
                if (i<=review) str += "★";
                else str += "☆";
            }

            setText(str);
        }
    }

    public void resetReview() {
        setText("☆☆☆☆☆");
    }
}
