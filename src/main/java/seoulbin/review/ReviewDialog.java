package seoulbin.review;

import seoulbin.mapdata.Utils;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ReviewDialog extends JDialog {
    JPanel panel;
    int bin_index;
    ReviewButton reviewButton;
    int review;
    
    public ReviewDialog(JFrame frame, ReviewButton reviewButton,int bin_index) {
        super(frame, "별점 선택", true);
        this.bin_index = bin_index;
        this.reviewButton = reviewButton;
        this.review = reviewButton.getReview();
        
        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        JLabel[] stars = new JLabel[5];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = createStarLabel(i, stars); //별을 표시할 레이블 생성
            panel.add(stars[i]);
        }
        add(panel);
        setResizable(false);
        setLocationRelativeTo(frame);
        setSize(300, 100);

        requestFocusInWindow();
        setFocusable(true);
        setVisible(true);
    }

    private JLabel createStarLabel(int index, JLabel[] stars) {
        String str = "";
        if (review >= index+1) str = "★";
        else str = "☆";
        
        JLabel label = new JLabel(str);
        label.setFont(new Font("Malgun gothic", Font.BOLD, 40));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 커서  모양 바꾸기

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateStars(index, stars); // 별 그리기
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                updateStars(review-1, stars); // 리뷰는 1부터 시작이라서 >> - 1
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                // 리뷰가 있다면 리뷰를 업데이트하는 방식
                boolean hasReview = Utils.hasExistingReview(bin_index);
                if(hasReview) {
                   Utils.updateBinReview(bin_index, index+1);
                }else {
                    // 리뷰 새로 추가
                    Utils.addBinReview(bin_index, index + 1); //1부터 시작이라서 (1추가) // 별 인덱스는 0
                }
                dispose();
            }
        });

        return label;
    }

    private void updateStars(int clickedIndex, JLabel[] stars) {
        for (int i = 0; i < stars.length; i++) {
            if (i <= clickedIndex) stars[i].setText("★");
            else stars[i].setText("☆");
        }
    }
}
