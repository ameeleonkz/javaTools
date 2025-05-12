# OTP Service

Сервис для генерации и валидации OTP (One-Time Password) кодов с поддержкой различных каналов доставки.

## Требования

- Java 17 или выше
- PostgreSQL 17 или выше
- Maven

## Установка и запуск

1. Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE otp_service;
```

2. Настройте параметры подключения к базе данных:
   - Скопируйте `src/main/resources/database.properties.example` в `src/main/resources/database.properties`
   - Заполните параметры подключения к базе данных

3. Настройте сервисы уведомлений:
   - Скопируйте `src/main/resources/notification.properties.example` в `src/main/resources/notification.properties`
   - Заполните параметры для каждого сервиса уведомлений:
     - Email (SMTP): хост, порт, имя пользователя и пароль
     - SMS (SMPP): адрес отправителя
     - Telegram: токен бота и ID чата
     - Файл: путь для сохранения OTP кодов
4. Запустите скрипт начальной настройки базы данных
```bash
psql -U a1111 -d otp_service -f src/main/resources/init.sql 
```

5. Соберите проект:
```bash
mvn clean package
```

6. Запустите приложение:
```bash
java -jar target/otp-service-1.0-SNAPSHOT.jar
```

## API Endpoints

### Аутентификация

- `POST /api/auth/register` - Регистрация нового пользователя
  ```json
  {
    "username": "user",
    "password": "password",
    "role": "USER"
  }
  ```

- `POST /api/auth/login` - Вход в систему
  ```json
  {
    "username": "user",
    "password": "password"
  }
  ```

### OTP операции

- `POST /api/otp/generate` - Генерация OTP кода
  ```json
  {
    "recipient": "user@example.com"
  }
  ```

- `POST /api/otp/validate` - Валидация OTP кода
  ```json
  {
    "code": "123456"
  }
  ```

### Административные операции

- `GET /api/admin/users` - Получение списка пользователей
- `POST /api/admin/users/delete` - Удаление пользователя
  ```json
  {
    "userId": 1
  }
  ```
- `GET /api/admin/otp/config` - Получение конфигурации OTP
- `PUT /api/admin/otp/config` - Обновление конфигурации OTP
  ```json
  {
    "codeLength": 6,
    "expirationTimeMinutes": 5
  }
  ```

## Каналы доставки OTP

Сервис поддерживает следующие каналы доставки OTP кодов:

1. Email (SMTP)
2. SMS (SMPP)
3. Telegram
4. Файл

## Безопасность

- Пароли хранятся в зашифрованном виде (SHA-256)
- Аутентификация осуществляется с помощью JWT токенов
- OTP коды имеют ограниченный срок действия
- Поддерживается только один администратор в системе
- Конфиденциальные данные хранятся в конфигурационных файлах, которые не включаются в репозиторий

## Логирование

Логи сохраняются в директории `logs/` и консоли. Используется библиотека Logback.









При запросе на генерацию OTP кода сначала будет пытаться отправить его через SMS сервис
SMS сервис всегда будет возвращать успешный ответ (моковая реализация)
Если SMS сервис не сработает (что маловероятно), система попробует другие каналы
Ответ 200 OK будет возвращен, если хотя бы один канал доставки сработал успешно