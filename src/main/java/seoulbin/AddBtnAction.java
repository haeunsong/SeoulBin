package seoulbin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

 // 위도와 경도(DB에 전달), 주소(화면 상단 주소 표시에 사용)를 지도측에서 받아와야함.
class AddBtnAction extends JFrame {
	private JLabel instructionLabel, imageLabel, addressLabel, trashTypeLabel, agreeLabel;
    private JPanel checkBoxPanel, radioButtonPanel;
    private JCheckBox checkBox1, checkBox2;
    private JRadioButton radioButton2_1,radioButton2_2;
    private ButtonGroup radioButtonGroup ;
    private JButton chooseImageButton, confirmButton;
    private JFileChooser fileChooser;
    private ImageIcon imageIcon, changeIcon;
    private File selectedImageFile;

    // 임의로 받은 주소 (이 부분은 실제 지도에서 받은 값을 대체해야 함)
    private String receivedAddress;
    private double lat;
    private double lng;
    
 // 선택된 체크박스 값 처리 함수
    private int getCheckBoxValue(JCheckBox checkBox1, JCheckBox checkBox2) {
        if (checkBox1.isSelected() && !checkBox2.isSelected()) {
            return 1; // "재활용" 체크됨
        } else if (!checkBox1.isSelected() && checkBox2.isSelected()) {
            return 0; // "일반" 체크됨
        } else if(checkBox1.isSelected() && checkBox2.isSelected()) {
        	return 2; // 둘다 체크됨.
        }else {
        	return 3; // 없을때.
        }
    }
 // 외부에서 주소를 전달받을 수 있도록 하는 메서드
    public AddBtnAction(double lat, double lng,String address) {
    	this.lat=lat;
    	this.lng=lng;
    	this.receivedAddress=address;
        // 서브 프레임 설정
        setTitle("추가");
        
        // 새 창의 기본 설정
        setSize(750, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 레이아웃 설정
        setLayout(new GridBagLayout());
        GridBagConstraints addFrame = new GridBagConstraints();
        // 여백 추가 (top, left, bottom, right)
        addFrame.insets = new Insets(10, 10, 10, 10);

        // "쓰레기통이 없으면 신고해주세요" 문구
        instructionLabel = new JLabel("쓰레기통을 추가합니다.");
        addFrame.gridx = 0;
        addFrame.gridy = 0;
        addFrame.gridwidth = 2;
        addFrame.fill = GridBagConstraints.NONE;
        add(instructionLabel, addFrame);
        
        // "주소"를 표시하는 JLabel (지도에서 받은 주소를 출력)
        addressLabel = new JLabel("주소: " + receivedAddress);
        addressLabel.setPreferredSize(new Dimension(600, 30));
        //addressLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        addFrame.gridx = 0;
        addFrame.gridy = 1;
        addFrame.gridwidth = 2;
        addFrame.fill = GridBagConstraints.HORIZONTAL;
        add(addressLabel, addFrame);
        
        // "쓰레기통 종류" 문구 추가
        trashTypeLabel = new JLabel("쓰레기통 종류 ");
        addFrame.gridx = 0;
        addFrame.gridy = 2;
        addFrame.gridwidth = 1;
        addFrame.gridheight = 1;
        addFrame.fill = GridBagConstraints.NONE;
        addFrame.anchor = GridBagConstraints.WEST; // 문구는 오른쪽 정렬
        //gbc.insets = new Insets(5, 5, 5, 5); // 여백을 조금 줄임
        
        // 체크박스 1
        checkBox1 = new JCheckBox("재활용");
        checkBox2 = new JCheckBox("일반");
        
        checkBoxPanel = new JPanel();
        checkBoxPanel.add(trashTypeLabel);
        checkBoxPanel.add(checkBox1);
        checkBoxPanel.add(checkBox2);
        
        addFrame.gridx = 0;
        addFrame.gridy = 2;
        addFrame.gridwidth = 2;
        addFrame.anchor = GridBagConstraints.WEST; // 오른쪽 정렬
        //gbc.insets = new Insets(5, 5, 5, 5); // 여백을 더 줄임
        add(checkBoxPanel, addFrame);
        
        // 이미지 표시 공간
        imageLabel = new JLabel("이미지가 여기에 표시됩니다.", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(600, 400));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addFrame.gridx = 0;
        addFrame.gridy = 3;
        addFrame.gridwidth = 1;
        addFrame.anchor = GridBagConstraints.CENTER;
        add(imageLabel, addFrame);

        // 이미지 선택 버튼
        chooseImageButton = new JButton("이미지 선택");
        chooseImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 이미지 파일 선택 다이얼로그
                fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "png", "gif"));
                int result = fileChooser.showOpenDialog(AddBtnAction.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = fileChooser.getSelectedFile();
                    imageIcon = new ImageIcon(selectedImageFile.getAbsolutePath());
                 // 이미지 크기를 레이블에 맞게 조정
                    Image image = imageIcon.getImage();
                    Image scaledImage = image.getScaledInstance(imageLabel.getWidth(),imageLabel.getHeight(), Image.SCALE_SMOOTH);
                    changeIcon=new ImageIcon(scaledImage);
                    
                    imageLabel.setIcon(changeIcon);
                    imageLabel.setText(""); // 기본 텍스트 제거
                }
            }
        });
        addFrame.gridx = 0;
        addFrame.gridy = 4;
        addFrame.gridwidth = 2;
        addFrame.fill = GridBagConstraints.HORIZONTAL; // 버튼이 가로로 길어짐
        addFrame.anchor = GridBagConstraints.CENTER;
        add(chooseImageButton, addFrame);

        // 문구
        agreeLabel = new JLabel("쓰레기통 등록하시겠습니까? ");
        addFrame.gridx = 0;
        addFrame.gridy = 5;
        addFrame.gridwidth = 1;
        addFrame.gridheight = 1;
        addFrame.fill = GridBagConstraints.NONE;
        addFrame.anchor = GridBagConstraints.WEST; // 문구는 오른쪽 정렬

        // 라디오 버튼 2개
        radioButton2_1 = new JRadioButton("동의");
        radioButton2_2 = new JRadioButton("비동의");

        // 라디오 버튼 그룹으로 묶기
        radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(radioButton2_1);
        radioButtonGroup.add(radioButton2_2);

        radioButtonPanel = new JPanel();
        radioButtonPanel.add(agreeLabel);
        radioButtonPanel.add(radioButton2_1);
        radioButtonPanel.add(radioButton2_2);

        addFrame.gridx = 0;
        addFrame.gridy = 5;
        addFrame.gridwidth = 2;
        addFrame.fill = GridBagConstraints.NONE;
        addFrame.anchor = GridBagConstraints.CENTER; // 왼쪽 정렬
        add(radioButtonPanel, addFrame);


     // 확인 버튼
        confirmButton = new JButton("확인");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	// 주소, 체크박스 상태, 선택된 이미지 정보 가져오기
                
                boolean isCheckBox1Selected = checkBox1.isSelected();
                boolean isCheckBox2Selected = checkBox2.isSelected();
                boolean isRadioButton2_1Selected = radioButton2_1.isSelected();
                
                Icon imageIcon = imageLabel.getIcon(); // 이미지가 선택되었는지 확인
                // 주소입력은 없어서 스킵
                // 체크박스1 확인
                if(!isCheckBox1Selected&& !isCheckBox2Selected) {
                	JOptionPane.showMessageDialog(AddBtnAction.this, "옵션을 체크해주십시오.", "경고", JOptionPane.WARNING_MESSAGE);
                	checkBox1.requestFocus(); // 체크박스 중 첫 번째에 포커스 이동
                	return ;// 경고 후 처리 종료
                }
                // 이미지 확인
                if (imageIcon == null) {
                    JOptionPane.showMessageDialog(AddBtnAction.this, "이미지를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                    chooseImageButton.requestFocus(); // 이미지 선택 버튼에 포커스 이동
                    return;
                }
                // 동의 확인
                if(!isRadioButton2_1Selected) {
                	JOptionPane.showMessageDialog(AddBtnAction.this, "동의해주십시오.", "경고", JOptionPane.WARNING_MESSAGE);
                	radioButton2_1.requestFocus(); // 체크박스 중 첫 번째에 포커스 이동
                	return ;// 경고 후 처리 종료
                }
                
                // 선택된 옵션에 따라 메시지 생성
                String optionText = "";
                switch (getCheckBoxValue(checkBox1,checkBox2)) {
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
                
                String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "default_image_path";
                // 데베에 전달: 주소, 체크박스 체크여부 2개, 이미지 경로 
                if(getCheckBoxValue(checkBox1,checkBox2)==0||getCheckBoxValue(checkBox1,checkBox2)==1||getCheckBoxValue(checkBox1,checkBox2)==2) {
                	saveAddBin(getCheckBoxValue(checkBox1,checkBox2), receivedAddress, imagePath);
                }
                System.out.println("데이터 베이스에 전달"); // 확인용
                
                String message = "주소: " + receivedAddress + "\n" +
                        		"쓰레기통 종류: " + optionText + "\n" +  // 선택된 옵션을 출력
                        		"사진 : 첨부됨.";
                

                // 메시지 표시
                JOptionPane.showMessageDialog(AddBtnAction.this, message, "입력된 정보", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        // 버튼 크기 키우기
        confirmButton.setPreferredSize(new Dimension(115, 35)); // 확인 버튼 크기
        
        addFrame.gridx = 0;
        addFrame.gridy = 6;
        addFrame.gridwidth = 2;
        addFrame.fill = GridBagConstraints.NONE;
        addFrame.anchor = GridBagConstraints.CENTER; // 중앙 정렬
        add(confirmButton, addFrame);

        setVisible(true);
    }
}