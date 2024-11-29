package seoulbin.stamp;

import seoulbin.Main;
import seoulbin.utils.StampUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static seoulbin.utils.StampUtils.*;

public class Stamp extends JPanel {
    private static final int GRID_SIZE = 5; // 5x5 그리드
    private static final int MAX_PIECES_PER_DAY = 5; // 하루 최대 열 수 있는 조각 수
    private Main mainFrame;
    private JButton[][] stampButtons; // 스탬프 버튼 배열
    private JLabel textLabel;
    private BufferedImage fullImage;
    private BufferedImage scaledImage;
    private JLabel imgLabel;
    private int revealedCount = 0; // 드러난 조각 수
    private int currentImageId = 1; // 현재 이미지 ID
    private String imagePath; // 이미지 경로
    private JLayeredPane layeredPane;
    private JPanel buttonPanel;
    private int openedToday = 0; // 오늘 열린 조각 수

    public Stamp(Main mainFrame) throws IOException {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        initializeDailyProgress();

        // 상단 영역: 뒤로가기 버튼
        JPanel topPanel = new JPanel();
        JButton backButton = new JButton("뒤로가기");
        backButton.addActionListener(e -> mainFrame.showMainPage());
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 영역: 이미지 + 버튼
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(700, 700));
        buttonPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 0, 0));
        buttonPanel.setBounds(0, 0, 650, 650);
        buttonPanel.setOpaque(false);
        layeredPane.add(buttonPanel, Integer.valueOf(1));
        add(layeredPane, BorderLayout.WEST);

        // 문구 및 이전/다음 이미지 버튼
        JPanel textPanel = new JPanel(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setFont(new Font("Malgun gothic", Font.BOLD, 30));
        textPanel.add(textLabel, BorderLayout.CENTER);

        // 버튼을 수직으로 배치할 서브 패널
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5)); // 2x1 그리드, 버튼 사이 여백 추가
        JButton beforeImageButton = new JButton("이전 이미지 불러오기");
        beforeImageButton.addActionListener(e -> loadBeforeImage());
        buttonPanel.add(beforeImageButton);

        JButton nextImageButton = new JButton("다음 이미지 불러오기");
        nextImageButton.addActionListener(e -> loadNextImage());
        buttonPanel.add(nextImageButton);

        textPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(textPanel, BorderLayout.EAST);

        // 초기 이미지 및 버튼 로드
        loadImageAndButtons(currentImageId);
    }

    private void loadImageAndButtons(int imageId) {
        try {
            // 이미지 경로 가져오기
            imagePath = StampUtils.getImagePath(imageId);
            if (imagePath == null) {
                JOptionPane.showMessageDialog(this, "이미지를 찾을 수 없습니다.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 이미지 로드 및 크기 조정
            fullImage = ImageIO.read(new File(imagePath));
            if(fullImage == null) {
                System.out.println("fullImage 가 null 입니다.");
            }
            scaledImage = scaleImage(fullImage, 650, 650);

            // 이미지 레이블 업데이트
            if (imgLabel != null) {
                layeredPane.remove(imgLabel);
            }
            imgLabel = new JLabel(new ImageIcon(scaledImage));
            imgLabel.setBounds(0, 0, 650, 650);
            layeredPane.add(imgLabel, Integer.valueOf(0));

            // 버튼 초기화
            buttonPanel.removeAll();
            stampButtons = new JButton[GRID_SIZE][GRID_SIZE];
            List<Integer> openedPieces = StampUtils.getOpenedPieces(imageId);
            revealedCount = openedPieces.size();

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    JButton button = new JButton("?");
                    button.setOpaque(true);
                    button.setPreferredSize(new Dimension(scaledImage.getWidth() / GRID_SIZE, scaledImage.getHeight() / GRID_SIZE));
                    button.setFocusPainted(false);
                    button.setMargin(new Insets(0, 0, 0, 0));

                    int pieceId = i * GRID_SIZE + j;

                    // 이미 열린 조각 처리
                    if (openedPieces.contains(pieceId)) {
                        button.setVisible(false);
                    } else {
                        button.addActionListener(new StampClickListener(i, j));
                    }

                    stampButtons[i][j] = button;
                    buttonPanel.add(button);
                }
            }

            // 문구 갱신
            textLabel.setText("  현재 " + revealedCount + " 조각 완성!  ");

            // UI 갱신
            buttonPanel.revalidate();
            buttonPanel.repaint();
            layeredPane.revalidate();
            layeredPane.repaint();

        } catch (IOException e) {
//            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "더이상 불러올 이미지가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadNextImage() {
        int nextImageId = currentImageId + 1;
        if (StampUtils.getImagePath(nextImageId) != null) {
            currentImageId = nextImageId;
            loadImageAndButtons(currentImageId);
        } else {
            JOptionPane.showMessageDialog(this, "더 이상 불러올 이미지가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadBeforeImage() {
        int beforeImageId = currentImageId - 1;
        if (StampUtils.getImagePath(beforeImageId) != null) {
            currentImageId = beforeImageId;
            loadImageAndButtons(currentImageId);
        } else {
            JOptionPane.showMessageDialog(this, "더 이상 불러올 이미지가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 이미지를 700x700으로 크기 조정
    private BufferedImage scaleImage(BufferedImage image, int width, int height) {
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return bufferedImage;
    }

    // 클릭 이벤트 리스너
    private class StampClickListener implements ActionListener {
        private final int row;
        private final int col;

        public StampClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (!canOpenMorePieces()) {
                JOptionPane.showMessageDialog(Stamp.this, "하루에 최대 " + MAX_PIECES_PER_DAY + " 조각만 열 수 있습니다.", "제한", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JButton clickedButton = stampButtons[row][col];
            clickedButton.setVisible(false);
            revealedCount++;
            incrementOpenedCount();

            int pieceId = row * GRID_SIZE + col;
            StampUtils.updateProgress(currentImageId, pieceId, true);

            textLabel.setText("  현재 " + revealedCount + " 조각 완성!  ");


            if (revealedCount == GRID_SIZE * GRID_SIZE) {
                JOptionPane.showMessageDialog(Stamp.this, "축하합니다! 사진이 완성되었습니다!");
            }
        }
    }
}
