package seoulbin.review;

import mapdata.Utils;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ReviewDialog extends JDialog {
    JPanel panel;
    int bin_index;
    ReviewButton reviewButton;
    int review;
    public ReviewDialog(JFrame frame, ReviewButton reviewButton,int bin_index) {
        super(frame, "리뷰 추가", true);
        this.bin_index = bin_index;
        this.reviewButton = reviewButton;
        this.review = reviewButton.getReview();

        addWindowListener(new WindowAdapter() { // 닫기 버튼 시에도 리뷰 버튼  text초기화
            @Override
            public void windowClosing(WindowEvent e) {
                reviewButton.resetReview();
            }
        });

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        JLabel[] stars = new JLabel[5];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = createStarLabel(i, stars); //별을 표시할 레이블 생성
            panel.add(stars[i]);
        }
        
        add(panel);
        
        setLocationRelativeTo(frame);
        setSize(300, 100);
        setResizable(false);
        
    	setFocusableWindowState(false);
        setVisible(true);
    }

    private JLabel createStarLabel(int index, JLabel[] stars) {
        String str = "";
        if (review >= index+1) str = "★";
        else str = "☆";

        JLabel label = new JLabel(str);
        label.setFont(new Font("Malgun gothic", Font.BOLD, 40));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 커서  모양 바꾸기
        
        SwingUtilities.invokeLater(() -> {
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
                    Utils.addBinReview(bin_index, index + 1); //1부터 시작이라서 (1추가) // 별 인덱스는 0
//                    reviewButton.loadReview(bin_index);
                    reviewButton.resetReview();
                    dispose();
                }
            });
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
