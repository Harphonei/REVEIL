# FINAL — Step 01

ฐานเริ่มต้นสำหรับเกม Java Swing/Java2D ของอีฟ: หน้าต่างสีดำและข้อความ FINAL
ยังไม่มีระบบเดิน ต่อสู้ เมนู เซฟ หรือคำสั่ง Dev ในขั้นนี้

## เปิดโปรเจกต์

1. แตก ZIP แล้วเก็บโฟลเดอร์ FINAL ไว้ในที่ทำงานถาวรบนคอมพิวเตอร์
2. ใน NetBeans ใช้ File > Open Project เลือกโฟลเดอร์ FINAL ที่มี pom.xml
3. ใช้ JDK 17 ขึ้นไป รวมถึง JDK 25 ที่อีฟใช้อยู่ได้ โค้ดตั้งระดับภาษาไว้ที่ 17
4. Run Project เลือก finalgame.GameApplication หากโปรแกรมถาม Main Class
5. ควรเห็นหน้าต่าง FINAL พื้นหลังดำ ข้อความ FINAL และ A journey begins.

Maven เป็นตัวช่วยคอมไพล์และแพ็กไฟล์ ไม่ใช่ game engine และไม่มี dependency เกมภายนอก
ครั้งแรก NetBeans/Maven อาจต้องเชื่อมต่ออินเทอร์เน็ตเพื่อดาวน์โหลด build plugins
Clean and Build จะสร้าง target/FINAL.jar; บนเครื่องที่มี Java ใช้ java -jar target/FINAL.jar

## Git

ZIP รวม .git พร้อม commit เริ่มต้น ไม่มี remote และยังไม่ได้ push ไป GitHub
หลังแตก ZIP ใช้ git log --oneline และ git status เพื่อตรวจประวัติ
อย่า git init ซ้ำหาก .git ยังอยู่ ให้ใช้โฟลเดอร์นี้ต่อไป
ก่อน commit งานของตัวเอง ตรวจ git config user.name และ git config user.email
ค่าชื่อผู้เขียน commit ควรเป็นชื่อและอีเมล Git ของอีฟ
ไม่ต้องส่ง password หรือ token มาในแชต

วงจรทำงาน: แก้โค้ด > รันทดสอบ > git diff > git add ไฟล์ที่แก้ > git commit > git push
git push ใช้ได้เมื่อเชื่อม repository ปลายทางแล้วเท่านั้น

## ลำดับการเรียน

อ่าน docs/STEP-01.md และเปิดโค้ดทั้งสามไฟล์ประกอบ
ขั้นถัดไป: Player และการเดิน WASD ก่อนการยิง กระสุน และบอส
