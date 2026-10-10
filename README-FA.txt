Fully Boot Launcher
===================

هدف:
بعد از بوت Android TV، حدود 15 ثانیه صبر می‌کند و سپس Fully Kiosk Browser را با این Activity اجرا می‌کند:
  de.ozerov.fully/.MainActivity

پکیج Helper:
  com.fully.bootlauncher

سازگاری هدف:
  Android TV 11 / API 30
  minSdk 23
  targetSdk 30

Build در Android Studio:
1) پوشه پروژه را در Android Studio باز کنید.
2) اگر SDK Platform 30 نصب نیست، اجازه دهید Android Studio آن را نصب کند.
3) Build > Build Bundle(s) / APK(s) > Build APK(s)
4) خروجی معمولاً در مسیر زیر ساخته می‌شود:
   app/build/outputs/apk/release/app-release.apk
   (برای تحویل نهایی می‌توانید نام آن را به Fully-Boot-Launcher.apk تغییر دهید.)

نصب با ADB:
  adb install -r Fully-Boot-Launcher.apk

تست دستی Helper:
  adb shell am start -n com.fully.bootlauncher/.MainActivity

تست سرویس (بدون ریبوت):
  adb shell am start-foreground-service -n com.fully.bootlauncher/.LaunchService

تست نهایی:
  adb reboot
  حدود 15 تا 30 ثانیه بعد از بالا آمدن سیستم صبر کنید.

نکات:
- Fully Kiosk Browser باید با پکیج de.ozerov.fully نصب باشد.
- Fully خودش نیز بهتر است Launch on Boot = ON داشته باشد.
- روی بعضی فریمورهای Android TV محدودیت Background Activity Launch وجود دارد. این Helper از Foreground Service استفاده می‌کند تا شانس اجرای موفق بیشتر شود.
- اگر فریمور همچنان شروع Activity از پس‌زمینه را مسدود کرد، باز کردن دستی Helper باید Fully را فوراً اجرا کند؛ در آن حالت محدودیت مربوط به خود فریمور TV است.
