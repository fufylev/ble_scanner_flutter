# ble_scanner_flutter

### Это документация по Flutter
- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

---

Подключаем реальный андроид/ios телефон через кабель к компьютеру.

Можно и эмулятор но на нем не будет работать BLE

Выбираем телефон в поле `Devices`

---

## Android
В корне проекта запускаем команду:

```flutter pub get```

Далее запускаем команду:

```flutter run```



## iOS
Если нужно запустить iOS проект то в корне проекта:

```cd ios```

```pod install```

Если комп Mac OS на процессоре M1 и выше то:

```sudo arch -x86_64 gem install ffi```

```arch -x86_64 pod install --repo-update```

---

### Если нужно очистить проект:

```Flutter clean```