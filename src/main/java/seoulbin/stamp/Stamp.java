package seoulbin.stamp;

import seoulbin.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Stamp extends JPanel {
    private static final int GRID_SIZE = 5; // 5x5 그리드
    private Main mainFrame;
    private JButton[][] stampButtons; // 스탬프 버튼 배열
    private ImageIcon[][] imagePieces; // 이미지 조각 배열
    private int revealedCount = 0; // 드러난 조각 수

    public Stamp(Main mainFrame) throws IOException {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());

        // 상단 영역: 뒤로가기 버튼
        JPanel topPanel = new JPanel();
        JButton backButton = new JButton("뒤로가기");
        backButton.addActionListener(e -> mainFrame.showMainPage()); // 메인 페이지로 복귀
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 영역: 이미지 + 버튼 (JLayeredPane 사용)
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(700, 700));

        // 이미지 패널
        BufferedImage fullImage = ImageIO.read(new File("img/stamp1.png"));
        BufferedImage scaledImage = scaleImage(fullImage, 700, 700); // 이미지 크기 조정
        JLabel imgLabel = new JLabel(new ImageIcon(scaledImage));
        imgLabel.setBounds(0, 0, 700, 700); // 이미지 크기와 동일한 좌표 설정
        layeredPane.add(imgLabel, Integer.valueOf(0)); // 배경 레이어 (0번 레벨)

        // 버튼 그리드 패널
        JPanel buttonPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 0, 0));
        buttonPanel.setBounds(0, 0, 700, 700); // 이미지와 동일한 크기 설정
        buttonPanel.setOpaque(false); // 배경 투명 처리
        layeredPane.add(buttonPanel, Integer.valueOf(1)); // 버튼 레이어 (1번 레벨)

        // 스탬프 버튼 초기화
        stampButtons = new JButton[GRID_SIZE][GRID_SIZE];
        imagePieces = splitImage(scaledImage, GRID_SIZE); // 이미지 조각 생성
        int pieceWidth = scaledImage.getWidth() / GRID_SIZE;
        int pieceHeight = scaledImage.getHeight() / GRID_SIZE;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(pieceWidth, pieceHeight));
                button.setOpaque(true);
                button.setFocusPainted(false);
                button.setMargin(new Insets(0, 0, 0, 0)); // 버튼 내부 여백 제거
                //button.setBorder(BorderFactory.createEmptyBorder()); // 버튼 경계선 제거
                button.addActionListener(new StampClickListener(i, j));
                stampButtons[i][j] = button;
                buttonPanel.add(button); // 버튼을 버튼 패널에 추가
            }
        }

        add(layeredPane, BorderLayout.CENTER); // JLayeredPane 추가
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

    // 이미지를 GRID_SIZE x GRID_SIZE로 나누기
    private ImageIcon[][] splitImage(BufferedImage fullImage, int gridSize) {
        int pieceWidth = fullImage.getWidth() / gridSize;
        int pieceHeight = fullImage.getHeight() / gridSize;

        ImageIcon[][] pieces = new ImageIcon[gridSize][gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int x = j * pieceWidth;
                int y = i * pieceHeight;

                // 조각 추출
                BufferedImage piece = fullImage.getSubimage(x, y, pieceWidth, pieceHeight);

                // 조각을 ImageIcon으로 변환
                pieces[i][j] = new ImageIcon(piece);
            }
        }

        return pieces;
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
            // 버튼을 투명하게 설정하여 사라진 것처럼 보이게 처리
            JButton clickedButton = stampButtons[row][col];
            clickedButton.setVisible(false); // 버튼 숨기기
            revealedCount++;

            // 모든 조각이 드러나면 축하 메시지
            if (revealedCount == GRID_SIZE * GRID_SIZE) {
                JOptionPane.showMessageDialog(Stamp.this, "축하합니다! 사진이 완성되었습니다!");
            }
        }
    }

}
