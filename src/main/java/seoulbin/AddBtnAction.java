package seoulbin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

// 위도와 경도(DB에 전달), 주소(화면 상단 주소 표시에 사용)를 지도측에서 받아와야함.
class AddBtnAction extends JFrame {

	// 임의로 받은 주소 (이 부분은 실제 지도에서 받은 값을 대체해야 함)
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
        addFrame.insets = new Insets(10, 10, 10, 10);
        
        // 제일 위에 텍스트와 주소 표시
        JLabel titleLabel = new JLabel("쓰레기통을 추가합니다");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24)); // 폰트 크기를 크게 설정
        addFrame.gridx = 0;
        addFrame.gridy = 0;
        addFrame.gridwidth = 2;
        addFrame.anchor = GridBagConstraints.CENTER;
        frame.add(titleLabel, addFrame);

        // "안녕하세요" 텍스트 추가
        JLabel helloLabel = new JLabel("주소: "+receivedAddress);
        addFrame.gridx = 0;
        addFrame.gridy = 1; // "쓰레기통 등록" 바로 아래에 위치
        addFrame.gridwidth = 2;
        addFrame.anchor = GridBagConstraints.CENTER;
        frame.add(helloLabel, addFrame);

        JLabel cityLabel = new JLabel("도시(--구): ");
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
        addFrame.gridx = 0;
        addFrame.gridy = 3;
        frame.add(detailLabel, addFrame);

        JTextField detailField = new JTextField(15);
        addFrame.gridx = 1;
        frame.add(detailField, addFrame);

        // 체크박스 2개 (쓰레기통 종류)
        JCheckBox general = new JCheckBox("일반");
        addFrame.gridx = 0;
        addFrame.gridy = 4;
        frame.add(general, addFrame);

        JCheckBox recycle = new JCheckBox("재활용");
        addFrame.gridx = 1;
        frame.add(recycle, addFrame);

//        // 이미지 표시 영역과 이미지 선택 버튼
//        JLabel imageLabel = new JLabel("이미지: ");
//        addFrame.gridx = 0;
//        addFrame.gridy = 5;
//        frame.add(imageLabel, addFrame);

        // 이미지 표시용 JLabel (초기에는 텍스트만 표시)
        JLabel selectedImageLabel = new JLabel("선택된 이미지 없음");
        addFrame.gridx = 0;
        addFrame.gridwidth = 2;
        addFrame.gridy = 6;
        frame.add(selectedImageLabel, addFrame);

        // 이미지 선택 버튼
        JButton selectImageButton = new JButton("이미지 선택");
        addFrame.gridx = 1;
        addFrame.gridy = 7;
        frame.add(selectImageButton, addFrame);

        // 라디오 버튼 (동의 및 비동의)
        JLabel agreeLabel = new JLabel("쓰레기통을 등록하시겠습니까?");
        addFrame.gridx = 0;
        addFrame.gridy = 8;
        addFrame.gridwidth = 2;
        frame.add(agreeLabel, addFrame);

        JRadioButton agreeRadioButton = new JRadioButton("동의");
        JRadioButton disagreeRadioButton = new JRadioButton("비동의");
        ButtonGroup agreeGroup = new ButtonGroup();
        agreeGroup.add(agreeRadioButton);
        agreeGroup.add(disagreeRadioButton);

        addFrame.gridx = 0;
        addFrame.gridy = 9;
        frame.add(agreeRadioButton, addFrame);
        addFrame.gridx = 1;
        frame.add(disagreeRadioButton, addFrame);

        // 확인 버튼
        JButton submitButton = new JButton("추가");
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
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "gif"));
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                    // 선택한 이미지를 JLabel에 표시
                    ImageIcon imageIcon = new ImageIcon(imagePath);
                    Image image = imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH); // 이미지 크기 조절
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
                    JOptionPane.showMessageDialog(AddBtnAction.this, "도시를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                    cityField.requestFocus(); // 주소 텍스트 필드로 포커스 이동
                    return;
                }

                if (detailText.isEmpty()) {
                    JOptionPane.showMessageDialog(AddBtnAction.this, "상세 정보를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                    detailField.requestFocus(); // 상세 정보 텍스트 필드로 포커스 이동
                    return;
                }
                // 체크박스1 확인
                if(!isCheckBox1Selected&& !isCheckBox2Selected) {
                	JOptionPane.showMessageDialog(AddBtnAction.this, "옵션을 체크해주십시오.", "경고", JOptionPane.WARNING_MESSAGE);
                	general.requestFocus(); // 체크박스 중 첫 번째에 포커스 이동
                	return ;// 경고 후 처리 종료
                }
             // 이미지 확인
                if (selectedImageLabel.getIcon() == null) {
                    JOptionPane.showMessageDialog(AddBtnAction.this, "이미지를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // 동의 확인
                if(!isRadioButton2_1Selected) {
                	JOptionPane.showMessageDialog(AddBtnAction.this, "동의해주십시오.", "경고", JOptionPane.WARNING_MESSAGE);
                	agreeRadioButton.requestFocus(); // 체크박스 중 첫 번째에 포커스 이동
                	return ;// 경고 후 처리 종료
                }
                
                // 선택된 옵션에 따라 메시지 생성
                String optionText = "";
                switch (getCheckBoxValue(general,recycle)) {
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
                
                //String imagePath = ((ImageIcon) imageIcon).getDescription();
                
                System.out.println("데이터 베이스에 전달"); // 확인용
                
                String message = "주소: " + receivedAddress + "\n" +
                				"지역구: "+ cityText+"\n"+
                				"상세 정보: "+detailText+"\n"+
                        		"쓰레기통 종류: " + optionText + "\n" +  // 선택된 옵션을 출력
                        		"사진 : 첨부됨.";
                
                // 메시지 표시
                JOptionPane.showMessageDialog(AddBtnAction.this, message, "입력된 정보", JOptionPane.INFORMATION_MESSAGE);
                // 데베에 전달: 주소, 체크박스 체크여부 2개, 이미지 경로 
                //if(getCheckBoxValue(checkBox1,checkBox2)==0||getCheckBoxValue(checkBox1,checkBox2)==1||getCheckBoxValue(checkBox1,checkBox2)==2) {
                	//utils.addBinData(lat, lng, getCheckBoxValue(general, recycle), receivedAddress, detailText, cityText, imagePath);
                //}
            }
        });

        // 화면 표시
        frame.setVisible(true);
    }
}
