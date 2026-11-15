# 🚀 TG AutoResponder (Android APK + Windows EXE)

Универсальный автоответчик для **Telegram**:
- 📱 **Android**: мобильное приложение на Kotlin + Jetpack Compose (отвечает на входящие личные сообщения через перехват уведомлений).
- 💻 **Windows**: десктопное приложение `.exe` с красивым интерфейсом (поддерживает текст, любые эмодзи и **абсолютно любые стикеры Telegram**!).

---

## 📥 Готовые файлы для скачивания

### 1. Windows (.exe)
- **Легковесная версия:** [`dist-windows-lite/TGAutoResponderDesktop.exe`](dist-windows-lite/TGAutoResponderDesktop.exe) (~94 КБ).
- **Standalone архив (все включено, не требует .NET):** [`dist-windows/TGAutoResponder-Windows-Standalone.zip`](dist-windows/TGAutoResponder-Windows-Standalone.zip).

**Как пользоваться на ПК:**
1. Запустите `TGAutoResponderDesktop.exe`.
2. Вставьте токен вашего бота (создается за 10 секунд в [@BotFather](https://t.me/BotFather) командой `/newbot`).
3. Введите текст ответа и/или ID стикера.
4. *(Опционально)* Если у вас **Telegram Business**, подключите вашего бота в **Настройки → Telegram Business → Чат-боты**, и бот будет автоматически отвечать на все ваши личные входящие сообщения!
5. Нажмите **▶ Запустить**.

### 2. Android (.apk)
- Исходный код находится в папке `app/`.
- Файл `app-debug.apk` автоматически собирается в облаке через [GitHub Actions](../../actions) и публикуется в разделе **[Releases](../../releases)** и **Artifacts**!

---

## 🛠 Сборка APK локально (Android Studio)
1. Откройте эту папку в **Android Studio**.
2. Дождитесь загрузки Gradle.
3. Выберите в меню **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. Готовый файл появится в `app/build/outputs/apk/debug/app-debug.apk`.

---

## ⚙️ Настройка на телефоне после установки
1. Запустите приложение на телефоне.
2. Нажмите кнопку **«Разрешить доступ»** и включите доступ к уведомлениям для **TG AutoResponder**.
3. Рекомендуется отключить оптимизацию батареи для приложения в настройках Android.
4. Введите текст автоответа и включите тумблер **«Автоответчик активен»**.
