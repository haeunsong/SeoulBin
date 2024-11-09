package seoulbin;
import java.awt.EventQueue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {

	private JFrame frame;
	private JPanel left_menu_bar;
	private boolean isExpanded = false; 
	private JLabel label1;
    private JButton button1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}); 
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		left_menu_bar = new JPanel();
		left_menu_bar.setBackground(Color.LIGHT_GRAY);
		left_menu_bar.setBounds(0, 0, 88, 600);
		frame.getContentPane().add(left_menu_bar);
		left_menu_bar.setLayout(null);

		// 메뉴바 아이콘 넣기
		JLabel iconLabel = new JLabel();
		iconLabel.setBounds(15, 10, 50, 50); // JLabel 크기 설정

		// 아이콘 이미지 불러와서 50x50 크기로 조정
		ImageIcon icon = new ImageIcon(getClass().getResource("/menubar.png"));
		Image scaledImage = icon.getImage().getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH);
		iconLabel.setIcon(new ImageIcon(scaledImage));

		left_menu_bar.add(iconLabel);
		
		// 패널 내부의 라벨 및 버튼 초기화
        label1 = new JLabel("확장된 상태에서 보이는 라벨");
        label1.setBounds(10, 70, 150, 30);
        label1.setVisible(false); // 초기에는 숨김
        left_menu_bar.add(label1);

        button1 = new JButton("확장된 상태에서 보이는 버튼");
        button1.setBounds(10, 110, 150, 30);
        button1.setVisible(false); // 초기에는 숨김
        left_menu_bar.add(button1);

		// 아이콘에 마우스 클릭 이벤트 추가
		iconLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				togglePanelSize();
			}
		});

	}
	// 패널 크기 토글 메서드
	private void togglePanelSize() {
		if (left_menu_bar == null) {
            System.out.println("panel_1 is null!");
            return; // panel_1이 null인 경우 실행하지 않음
        }
		if (isExpanded) {
			left_menu_bar.setBounds(0, 0, 88, 600); // 축소 크기
            label1.setVisible(false); // 축소 시 내부 컴포넌트 숨김
            button1.setVisible(false);
        } else {
        	left_menu_bar.setBounds(0, 0, 200, 600); // 확장 크기
            label1.setVisible(true); // 확장 시 내부 컴포넌트 표시
            button1.setVisible(true);
        }
        isExpanded = !isExpanded;
        frame.revalidate();
        frame.repaint();
	}
}
