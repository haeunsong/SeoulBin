package seoulbin;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import mapdata.Utils;
import model.Model;


// 위도와 경도(DB에 전달), 주소(화면 상단 주소 표시에 사용)를 지도측에서 받아와야함.
class AddBtnAction extends JFrame {
	private String receivedAddress;
	private double lat;
	private double lng;
	private String imagePath;

	// 선택된 체크박스 값 처리 함수
	private int getCheckBoxValue(JCheckBox checkBox1, JCheckBox checkBox2) {
		if (checkBox1.isSelected() && !checkBox2.isSelected()) {
			return 0; // "일반 "체크됨
		} else if (!checkBox1.isSelected() && checkBox2.isSelected()) {
			return 1; // "재활용" 체크됨
		} else if (checkBox1.isSelected() && checkBox2.isSelected()) {
			return 2; // 둘다 체크됨.
		} else {
			return 3; // 없을때.
		}
	}

	// 위도와 경도(DB에 전달), 주소(화면 상단 주소 표시에 사용)를 지도측에서 받아와야함.
	// 'AddBtnAction' 클래스의 수정된 부분
	public AddBtnAction(double lat, double lng, String address) {
		this.lat = lat;
		this.lng = lng;
		this.receivedAddress = address;

		// JFrame 생성
		JFrame frame = new JFrame("추가");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(750, 800);

		// GridBagLayout 설정
		frame.setLayout(new GridBagLayout());
		GridBagConstraints addFrame = new GridBagConstraints();
		addFrame.insets = new Insets(10, 0, 10, 0);

		// 제일 위에 텍스트와 주소 표시
		JLabel titleLabel = new JLabel("쓰레기통을 추가합니다");
		titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24)); // 폰트 크기를 크게 설정
		addFrame.gridx = 0;
		addFrame.gridy = 0;
		addFrame.gridwidth = 2;
		addFrame.anchor = GridBagConstraints.CENTER;
		frame.add(titleLabel, addFrame);

		// "안녕하세요" 텍스트 추가
		JLabel addressLabel = new JLabel("주소: " + receivedAddress);
		addressLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 1; // "쓰레기통 등록" 바로 아래에 위치
		addFrame.gridwidth = 2;
		addFrame.anchor = GridBagConstraints.CENTER;
		frame.add(addressLabel, addFrame);

		JLabel cityLabel = new JLabel("지역구(--구): ");
		cityLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 2;
		addFrame.gridwidth = 1;
		addFrame.anchor = GridBagConstraints.WEST;
		frame.add(cityLabel, addFrame);

		JTextField cityField = new JTextField(15);
		addFrame.gridx = 1;
		frame.add(cityField, addFrame);

		// 입력 필드 2개
		JLabel detailLabel = new JLabel("상세 정보: ");
		detailLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 3;
		frame.add(detailLabel, addFrame);

		JTextField detailField = new JTextField(15);
		addFrame.gridx = 1;
		frame.add(detailField, addFrame);

		JLabel typeLabel = new JLabel("쓰레기통 종류: ");
		typeLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 4;
		frame.add(typeLabel, addFrame);

		// 체크박스 2개 (쓰레기통 종류)
		JCheckBox general = new JCheckBox("일반");
		general.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 1;
		addFrame.anchor = GridBagConstraints.WEST; // 왼쪽 정렬
		frame.add(general, addFrame);

		JCheckBox recycle = new JCheckBox("재활용");
		recycle.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 2;
		addFrame.anchor = GridBagConstraints.WEST; // 왼쪽 정렬
		frame.add(recycle, addFrame);

		// 이미지 표시용 JLabel (초기에는 텍스트만 표시)
		JLabel selectedImageLabel = new JLabel("선택된 이미지 없음");
		selectedImageLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridwidth = 2;
		addFrame.gridy = 6;
		addFrame.anchor = GridBagConstraints.CENTER; // 중앙 정렬
		frame.add(selectedImageLabel, addFrame);

		// 이미지 선택 버튼
		JButton selectImageButton = new JButton("이미지 선택");
		selectImageButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 7;
		addFrame.fill = GridBagConstraints.HORIZONTAL;
		addFrame.anchor = GridBagConstraints.CENTER; // 중앙 정렬
		frame.add(selectImageButton, addFrame);

		// 라디오 버튼 (동의 및 비동의)
		JLabel agreeLabel = new JLabel("쓰레기통을 등록하시겠습니까?");
		agreeLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 8;
		addFrame.gridwidth = 1;
		addFrame.anchor = GridBagConstraints.CENTER; // 중앙 정렬
		frame.add(agreeLabel, addFrame);

		JRadioButton agreeRadioButton = new JRadioButton("동의");
		JRadioButton disagreeRadioButton = new JRadioButton("비동의");
		agreeRadioButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		disagreeRadioButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		ButtonGroup agreeGroup = new ButtonGroup();
		agreeGroup.add(agreeRadioButton);
		agreeGroup.add(disagreeRadioButton);

		addFrame.gridx = 0;
		addFrame.gridy = 9;
		addFrame.insets = new Insets(2, 2, 2, 2);
		frame.add(agreeRadioButton, addFrame);
		addFrame.gridx = 1;
		addFrame.insets = new Insets(2, 2, 2, 2);
		frame.add(disagreeRadioButton, addFrame);

		// 확인 버튼
		JButton submitButton = new JButton("확인");
		submitButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
		addFrame.gridx = 0;
		addFrame.gridy = 10;
		addFrame.gridwidth = 2;
		addFrame.fill = GridBagConstraints.NONE;
		addFrame.anchor = GridBagConstraints.CENTER; // 중앙 정렬
		frame.add(submitButton, addFrame);

		// 이미지 선택 버튼 이벤트
		selectImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 이미지 선택 다이얼로그
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(
						new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "gif"));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					imagePath = fileChooser.getSelectedFile().getAbsolutePath();
					// 선택한 이미지를 JLabel에 표시
					ImageIcon imageIcon = new ImageIcon(imagePath);
					Image image = imageIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH); // 이미지 크기 조절
					selectedImageLabel.setIcon(new ImageIcon(image)); // JLabel에 이미지 설정
					selectedImageLabel.setText(""); // 텍스트 제거
				}
			}
		});

		// 확인 버튼 이벤트
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 주소, 체크박스 상태, 선택된 이미지 정보 가져오기

				boolean isCheckBox1Selected = general.isSelected();
				boolean isCheckBox2Selected = recycle.isSelected();
				boolean isRadioButton2_1Selected = agreeRadioButton.isSelected();

				// 텍스트 필드 값 확인
				String cityText = cityField.getText().trim();
				String detailText = detailField.getText().trim();

				// 주소입력은 없어서 스킵
				// 텍스트 필드 값 확인
				if (cityText.isEmpty()) {
					JOptionPane.showMessageDialog(AddBtnAction.this, "지역구를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
					cityField.requestFocus(); // 주소 텍스트 필드로 포커스 이동
					return;
				}

				if (detailText.isEmpty()) {
					JOptionPane.showMessageDialog(AddBtnAction.this, "상세 정보를 입력해주세요.", "경고",
							JOptionPane.WARNING_MESSAGE);
					detailField.requestFocus(); // 상세 정보 텍스트 필드로 포커스 이동
					return;
				}
				// 체크박스1 확인
				if (!isCheckBox1Selected && !isCheckBox2Selected) {
					JOptionPane.showMessageDialog(AddBtnAction.this, "옵션을 체크해주십시오.", "경고", JOptionPane.WARNING_MESSAGE);
					general.requestFocus(); // 체크박스 중 첫 번째에 포커스 이동
					return;// 경고 후 처리 종료
				}
				// 이미지 확인
				if (selectedImageLabel.getIcon() == null) {
					JOptionPane.showMessageDialog(AddBtnAction.this, "이미지를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
					return;
				}
				// 동의 확인
				if (!isRadioButton2_1Selected) {
					JOptionPane.showMessageDialog(AddBtnAction.this, "동의해주십시오.", "경고", JOptionPane.WARNING_MESSAGE);
					agreeRadioButton.requestFocus(); // 체크박스 중 첫 번째에 포커스 이동
					return;// 경고 후 처리 종료
				}

				// 선택된 옵션에 따라 메시지 생성
				String optionText = "";
				switch (getCheckBoxValue(general, recycle)) {
				case 0:
					optionText = "일반";
					break;
				case 1:
					optionText = "재활용";
					break;
				case 2:
					optionText = "일반, 재활용";
					break;
				}

				// 모델 호출 (isBin 메서드 사용)
				int binResult = Model.isBin(imagePath);

				if (binResult == 0) {
					// 쓰레기통이 아닌 경우 경고 메시지
					JOptionPane.showMessageDialog(AddBtnAction.this, "쓰레기통으로 인식되지 않았습니다.", "경고",JOptionPane.WARNING_MESSAGE);
					return; // 경고 메시지 후 종료
				}else if(binResult == 1) {
					JOptionPane.showMessageDialog(AddBtnAction.this,"쓰레기통으로 인식되었습니다.", "정보", JOptionPane.INFORMATION_MESSAGE);
				}

				String message = "주소: " + receivedAddress + "\n" + "지역구: " + cityText + "\n" + "상세 정보: " + detailText
						+ "\n" + "쓰레기통 종류: " + optionText + "\n" + // 선택된 옵션을 출력
						"사진 : 첨부됨.";

				// 정보 표시
				JOptionPane.showMessageDialog(AddBtnAction.this, message, "입력된 정보", JOptionPane.INFORMATION_MESSAGE);

				// 확인 후 데이터베이스에 전달
				int result = JOptionPane.showConfirmDialog(AddBtnAction.this, "정보를 데이터베이스에 저장하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					// 데베에 전달: 주소, 체크박스 체크여부 2개, 이미지 경로
					if (getCheckBoxValue(general, recycle) == 0 || getCheckBoxValue(general, recycle) == 1 || getCheckBoxValue(general, recycle) == 2) {
						if (getCheckBoxValue(general, recycle) == 2) {
							Utils.addBinData(lat, lng, 0, detailText, cityText, imagePath);
							Utils.addBinData(lat, lng, 1, detailText, cityText, imagePath);
							JOptionPane.showMessageDialog(AddBtnAction.this, "정보가 저장되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
							frame.dispose(); // 창을 닫음
						} else {
							System.out.println("쓰레기통 추가 정보가 정상적으로 데이터베이스에 전달되었습니다."); // 확인용
							Utils.addBinData(lat, lng, getCheckBoxValue(general, recycle), detailText, cityText,
									imagePath);
							JOptionPane.showMessageDialog(AddBtnAction.this, "정보가 저장되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
							frame.dispose(); // 창을 닫음
						}
					}
				} else {
					// 저장을 취소한 경우
					JOptionPane.showMessageDialog(AddBtnAction.this, "정보 저장이 취소되었습니다.", "취소", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		// 화면 표시
		frame.setVisible(true);
	}
}
