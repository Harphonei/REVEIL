# ขั้นที่ 1: ให้เกมมีหน้าต่าง

เป้าหมายคือเปิดหน้าต่างได้ ปิดแล้วโปรแกรมจบ และเห็นชื่อเกม
เรายังไม่ใส่ระบบเคลื่อนไหว เพื่อเรียนรู้ความสัมพันธ์ของสามคลาสก่อน

## ทำไมมี 3 คลาส

class คือแบบกำหนดข้อมูลและพฤติกรรม ส่วน object คือสิ่งที่สร้างขึ้นจากแบบนั้น
GameApplication เริ่มโปรแกรม; GameWindow เป็นหน้าต่าง; GamePanel เป็นพื้นที่วาด
เมื่อเปลี่ยนภาพในอนาคต เราแก้ส่วนวาดโดยไม่ต้องเขียนจุดเริ่มโปรแกรมใหม่

## GameApplication.java

- package finalgame: จัดสามคลาสให้อยู่กลุ่มเดียวกัน ไม่ใช้ชื่อ package ว่า final เพราะเป็นคำสงวน
- import javax.swing.SwingUtilities: ทำให้เรียกชื่อ SwingUtilities แบบสั้นได้
- public class GameApplication: ประกาศคลาสสาธารณะ ชื่อตรงกับไฟล์
- public static void main(String[] args): จุดเริ่มของโปรแกรม; static ทำให้เรียกได้โดยไม่สร้าง GameApplication; void คือไม่คืนค่า; args รับข้อความจากตอนรัน แต่ยังไม่ได้ใช้
- SwingUtilities.invokeLater(...): ฝากงานสร้างหน้าต่างให้เธรดจัดการ GUI ของ Swing หรือ EDT
- () -> { ... }: lambda สำหรับงานที่ไม่มีพารามิเตอร์ โดยคำสั่งในวงเล็บปีกกาจะถูกรันบน EDT
- GameWindow window = new GameWindow(): สร้าง object แล้วเก็บ reference ในตัวแปร local ชื่อ window
- window.setVisible(true): แสดงหน้าต่างที่สร้างไว้; true คือให้แสดง
- }); ปิดบล็อก lambda ปิดวงเล็บเรียก invokeLater แล้วจบคำสั่ง
- วงเล็บปีกกาที่เหลือปิด main และปิด class ตามลำดับ

## GameWindow.java

- import JFrame: ใช้คลาสหน้าต่างสำเร็จรูปของ Swing
- extends JFrame: GameWindow สืบทอดความสามารถของหน้าต่าง จึงเรียก setVisible และคำสั่งหน้าต่างอื่นได้
- private final GamePanel gamePanel: field ของหน้าต่าง เก็บ reference ไปยังพื้นที่วาด; private จำกัดการเข้าถึงโดยตรง; final ห้ามเปลี่ยน reference หลังตั้งค่า ไม่ได้ทำให้ตัว object เปลี่ยนแปลงไม่ได้
- public GameWindow(): constructor ชื่อเดียวกับคลาสและไม่มีชนิดค่าคืน ถูกเรียกเมื่อ new GameWindow()
- super("FINAL"): เรียก constructor ของ JFrame เพื่อตั้งชื่อบนแถบหน้าต่าง
- gamePanel = new GamePanel(): สร้างพื้นที่วาดที่หน้าต่างนี้เป็นเจ้าของ
- setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE): ปิดโปรแกรมเมื่อกด X
- setResizable(false): ยังไม่ให้เปลี่ยนขนาด เพื่อลดเรื่องการคำนวณพิกัดในช่วงแรก
- setContentPane(gamePanel): ใส่พื้นที่วาดลงในหน้าต่าง
- pack(): ให้หน้าต่างคำนวณขนาดจาก preferred size ของพื้นที่ด้านใน รวมขอบหน้าต่างด้วย
- setLocationRelativeTo(null): จัดหน้าต่างไว้กลางจอ

## GamePanel.java

imports คือเครื่องมือ: Color สี, Dimension ขนาด, Font แบบตัวอักษร, Graphics เครื่องมือวาด, JPanel พื้นที่ GUI

| ชื่อ | ชนิด/ขอบเขต | หน้าที่และค่า |
| --- | --- | --- |
| VIEW_WIDTH | int, static final field | ความกว้างพื้นที่เกม 960 พิกเซล เป็นค่าทดลองที่เราเลือกเอง |
| VIEW_HEIGHT | int, static final field | ความสูง 640 พิกเซล เป็นค่าทดลองที่เราเลือกเอง |
| g | Graphics, parameter | Swing ส่งเครื่องมือวาดมาให้ทุกครั้งที่ต้องวาดภาพ |

- extends JPanel: ทำให้คลาสนี้เป็นพื้นที่ GUI ที่ใส่ใน JFrame ได้
- static ของขนาดหมายถึงค่าระดับคลาส; final หมายถึงกำหนดครั้งเดียว
- GamePanel(): constructor ทำงานเมื่อหน้าต่างสั่ง new GamePanel()
- new Dimension(VIEW_WIDTH, VIEW_HEIGHT): สร้างข้อมูลขนาดจาก 960 และ 640
- setPreferredSize(...): บอกขนาดที่ต้องการให้ pack() ของหน้าต่างนำไปใช้
- setBackground(Color.BLACK): ตั้งสีพื้นหลัง
- @Override: ให้ compiler ตรวจว่าเราเขียนเมธอดทับเมธอดที่สืบทอดมาจริง
- protected void paintComponent(Graphics g): เมธอดวาดภาพของ JPanel; protected เป็นระดับการเข้าถึง; void ไม่คืนค่า
- super.paintComponent(g): ให้ JPanel วาดพื้นหลังตามปกติก่อนเราเพิ่มข้อความ
- g.setColor(Color.WHITE): เลือกสีสำหรับคำสั่งวาดถัดไป
- new Font(Font.MONOSPACED, Font.BOLD, 48): สร้างแบบอักษรความกว้างคงที่ ตัวหนา ขนาด 48
- g.setFont(...): เลือกแบบอักษรนั้นให้เครื่องมือวาด
- g.drawString("FINAL", 60, 100): วาดข้อความที่ x=60 และเส้นฐานตัวอักษร y=100; จุด (0,0) อยู่มุมซ้ายบน x เพิ่มไปทางขวา y เพิ่มลงล่าง
- อีกสองบรรทัดเปลี่ยนขนาดเป็น 18 แบบธรรมดา แล้ววาดข้อความรองที่ (60,145)

## ไล่ลำดับจริง

1. main ฝากงานไปยัง EDT
2. EDT เริ่ม new GameWindow()
3. constructor GameWindow สร้าง GamePanel; constructor GamePanel ตั้งขนาดและสี
4. กลับมาที่ GameWindow ใส่ panel แล้วจัดขนาด/ตำแหน่งหน้าต่าง
5. สร้างเสร็จ reference ถูกเก็บใน window แล้วสั่งแสดง
6. Swing เรียก paintComponent เมื่อถึงเวลาวาดหน้าจอ; ไม่ต้องเรียกเมธอดนี้เอง

OOP ที่เห็นตอนนี้: constructor, inheritance, overriding และ composition ของหน้าต่างกับ panel
ยังไม่อ้างว่าขั้นนี้ครอบคลุม OOP ครบหกหัวข้อหรือมี game-loop thread แล้ว

## ตรวจบนเครื่องอีฟ

- Run แล้วข้อความทั้งสองปรากฏในหน้าต่าง
- ย่อหน้าต่างแล้วเปิดกลับ ภาพยังอยู่
- กด X แล้วโปรแกรมหยุด
- ถ้า NetBeans แจ้ง error ส่งข้อความ error หรือภาพมาได้
