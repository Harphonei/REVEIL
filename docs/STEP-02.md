# ขั้นที่ 2: Player และ WASD

## อัปเดตโปรเจกต์เดิม

ปิดหน้าต่างเกมก่อน จาก ZIP ใหม่คัดลอก src และ docs ไปทับใน FINAL เดิม พร้อม README.md
ไม่ต้องแทนที่ .git ของโปรเจกต์ที่ใช้อยู่ จึงเก็บ Git ของอีฟไว้ได้
ZIP นี้เป็นชุดเต็มสำหรับเปิดแยกได้ด้วย แต่ควรใช้โฟลเดอร์ทำงานเดิมต่อเนื่อง

## งานที่ต้องทำ

จำว่าปุ่มใดกดอยู่ > คำนวณทิศทาง > เปลี่ยนพิกัด Player > วาดภาพใหม่
แยก Player เพื่อให้ตำแหน่งและกฎการเดินอยู่ในคลาสตัวละคร ไม่กระจายอยู่ในหน้าจอทั้งหมด

| ตัวแปร | ชนิด/ขอบเขต | หน้าที่ |
| --- | --- | --- |
| x, y | double, private field ของ Player | พิกัดมุมซ้ายบน เก็บทศนิยมให้การเดินนุ่มนวล |
| size | int, final field | ความกว้างและสูงของสี่เหลี่ยม 24 พิกเซล |
| speed | double, final field | ความเร็ว 220 พิกเซลต่อวินาที เป็นค่าทดลอง |
| startX, startY | double, constructor parameter | ตำแหน่งเริ่มต้นที่ GamePanel ส่งมา |
| player | Player, private final field ของ panel | reference ไปยังตัวละครของหน้าจอ |
| pressed | boolean[], field ของ panel | จำปุ่ม 0=W, 1=A, 2=S, 3=D ค่าเริ่มต้นทั้งหมด false |
| timer | javax.swing.Timer, field ของ panel | เรียก updateGame โดยขอช่วงประมาณ 16 มิลลิวินาที |
| previousTime | long, field ของ panel | เวลาของการอัปเดตรอบก่อน หน่วยนาโนวินาที |
| directionX, directionY | int, local variable | ทิศแต่ละแกน -1, 0 หรือ 1 |
| seconds | double, parameter/local | เวลาที่ผ่านไปในรอบนี้ หน่วยวินาที |

## Player.java อ่านทีละส่วน

package/import ทำหน้าที่เดิม โดย Graphics สำหรับวาดและ Color สำหรับสี
private จำกัดการเข้าถึงพิกัดโดยตรง ให้เปลี่ยนผ่าน move ซึ่งตรวจขอบจอด้วย
final ของ size/speed คือกำหนดแล้วไม่เปลี่ยน

Constructor Player(double startX, double startY) รับค่าที่ส่งมาจาก new Player(468,308)
คำสั่ง x=startX และ y=startY เก็บ 468 กับ 308 ลง field ของ object ตัวนี้
เมื่อขนาดจอ 960x640 และตัวละคร 24x24 ค่าดังกล่าวมาจาก (960-24)/2 กับ (640-24)/2

move รับทิศ เวลา และขนาดพื้นที่ เมธอด void ไม่คืนค่า แต่เปลี่ยน field x/y
Math.hypot(directionX,directionY) หาความยาวเวกเตอร์ทิศ เช่น (1,0) ได้ 1 และ (1,1) ได้ประมาณ 1.414
if(distance>0) ป้องกันหารด้วยศูนย์ตอนยืนเฉย
directionX/distance และ directionY/distance ทำให้ความเร็วเดินเฉียงเท่ากับเดินตรง
คูณ speed*seconds เพื่อเปลี่ยนความเร็วต่อวินาทีเป็นระยะในรอบนี้
x+=... คือ x=x+...; y+=... ใช้หลักเดียวกัน
Math.min(x,areaWidth-size) จำกัดขอบขวา แล้ว Math.max(0,...) จำกัดขอบซ้าย
แกน y ใช้หลักเดียวกันเพื่อจำกัดขอบบน/ล่าง

draw(Graphics g) เลือกสีขาว แล้ว fillRect วาดสี่เหลี่ยมที่พิกัด x,y ขนาด size,size
(int)x แปลงพิกัดทศนิยมเป็นจำนวนเต็มสำหรับวาด โดยค่า field เดิมยังเป็น double

## GamePanel: รับปุ่ม

imports ใหม่: ActionEvent ข้อมูลเหตุการณ์, AbstractAction งานเมื่อเกิดเหตุการณ์,
JComponent ค่าขอบเขตการรับปุ่ม, KeyStroke ชื่อปุ่ม, Timer งานตามช่วงเวลา

new boolean[4] สร้างช่องจำปุ่มสี่ช่อง ไม่ใช่จำนวนพิกัด
constructor สร้าง player แล้วเรียก bindKey ทีละปุ่ม โดย index ที่กำหนดตรงกับตาราง
new Timer(16,event -> updateGame()) สร้างตัวเรียกงาน ไม่ได้เริ่มมันทันที
event คือ parameter เหตุการณ์ของ timer แต่รอบนี้ไม่ได้ใช้ข้อมูลในนั้น

bindKey(String key,int index) รับชื่อปุ่มและช่องใน array ที่จะเปลี่ยน
pressAction="press-"+key และ releaseAction="release-"+key สร้างชื่อแยก เช่น press-W/release-W
getInputMap(...).put(KeyStroke...,ชื่อ action) ผูกการกด/ปล่อยเข้ากับชื่อการกระทำ
WHEN_IN_FOCUSED_WINDOW หมายถึงรับเมื่อหน้าต่างนี้กำลังถูกใช้งาน
getActionMap().put(ชื่อ,new AbstractAction(){...}) ผูกชื่อนั้นกับ object การกระทำ
รูปแบบ new AbstractAction(){...} คือ anonymous subclass ที่เขียน actionPerformed ทับเมธอดเดิม
เมื่อกด W จะตั้ง pressed[0]=true; เมื่อปล่อย W จะตั้ง false
เราไม่เดินใน event โดยตรง เพราะการกดค้างให้ event ซ้ำขึ้นกับการตั้งค่าคีย์บอร์ด

## GamePanel: เปลี่ยนตำแหน่ง

System.nanoTime() อ่านนาฬิกาสำหรับวัดระยะเวลา ไม่ใช่เวลาปฏิทิน
(now-previousTime)/1_000_000_000.0 แปลงเวลาที่ผ่านไปเป็นวินาที
ขีดล่างในตัวเลขช่วยให้อ่านง่าย ไม่มีผลต่อค่า
Math.min(...,0.05) จำกัดเวลาคำนวณหนึ่งรอบไม่ให้เกิน 0.05 วินาที ป้องกันเดินกระโดดเมื่อโปรแกรมค้างช่วงสั้น
previousTime=now เก็บเวลารอบนี้สำหรับคำนวณรอบหน้า
direction เริ่ม 0 ทุกครั้ง แล้วลด/เพิ่มตามปุ่ม; -- ลดหนึ่ง และ ++ เพิ่มหนึ่ง
W ลด y, S เพิ่ม y, A ลด x, D เพิ่ม x เพราะพิกัดเริ่มมุมซ้ายบน
player.move(...,getWidth(),getHeight()) ส่งทิศและขนาดพื้นที่จริงให้ตัวละคร
repaint() ขอให้ Swing วาดใหม่ ไม่เรียก paintComponent โดยตรง
paintComponent วาดข้อความตามเดิมแล้วเรียก player.draw(g)

## เริ่ม/หยุดและสลับหน้าต่าง

addNotify ถูก Swing เรียกเมื่อ panel เชื่อมเข้ากับระบบหน้าต่าง: เรียก super ก่อน แล้วตั้งเวลาและเริ่ม timer
removeNotify หยุด timer เมื่อ panel ถูกถอดออก แล้วให้ superclass ทำงานต่อ
clearInput วน i=0 ถึง pressed.length-1 ตั้งทุกช่อง false และเริ่มนับเวลาใหม่
GameWindow เพิ่ม WindowAdapter ซึ่ง override windowLostFocus เพื่อเรียก clearInput
เมื่อ Alt+Tab ขณะกดเดิน เกมจึงไม่จำปุ่มค้างหากไม่ได้รับเหตุการณ์ปล่อยปุ่ม

Timer นี้อัปเดตบน EDT เดียวกับ event และการวาด จึงยังไม่สร้างเธรด game loop แยก
เรื่อง Thread ตามเป้าหมายโปรเจกต์จะพิจารณาเพิ่มเมื่อจัดระบบอัปเดตเต็มรูปแบบ

## Trace หนึ่งรอบ (ตัวอย่างเมื่อเวลารอบนี้เท่ากับ 0.016 วินาที)

| กด | directionX | directionY | ระยะ x | ระยะ y |
| --- | --- | --- | --- | --- |
| ไม่กด | 0 | 0 | 0 | 0 |
| D | 1 | 0 | +3.52 | 0 |
| W | 0 | -1 | 0 | -3.52 |
| W+D | 1 | -1 | ประมาณ +2.49 | ประมาณ -2.49 |
| W+S | 0 | 0 | 0 | 0 |

ถ้าเริ่ม (468,308) แล้วกด D หนึ่งรอบตัวอย่าง จะเป็น (471.52,308)
เวลา 16ms เป็นคำขอของ timer ไม่ได้รับประกันว่าทุกรอบจะตรงเป๊ะ จึงวัดเวลาจริง

## ตรวจเมื่อรัน

WASD เดินครบสี่ทิศ; กดสองปุ่มเดินเฉียง; ปล่อยแล้วหยุด; เดินไม่พ้นจอ
ลองกด D แล้ว Alt+Tab กลับมาควรหยุด ไม่เดินเอง
ขั้นนี้ตรวจเพียงขอบจอ ยังไม่มีสิ่งกีดขวางหรือการชนกำแพงในแผนที่
