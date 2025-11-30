# Velox – Smart Planner App

Velox — это проект, который состоит из двух частей:

- **Backend** — Django + Python
- **Frontend** — Android приложение на Kotlin (Jetpack Compose)

Репозиторий имеет структуру:


---

## 🚀 Backend (Python + Django)

### 📌 Технологии
- Python 3.10+
- Django REST Framework
- JWT Authentication
- PostgreSQL / SQLite (в зависимости от настроек)
- Docker (опционально)

### 🔧 Установка и запуск backend

#### 1. Перейдите в директорию backend:

```bash
cd Velox/backend
``` 

2. Создайте виртуальное окружение:
```bash
python -m venv venv
```
3. Активируйте venv:

Windows:
```bash
venv\Scripts\activate
```

4. Установите зависимости:
```bash
pip install -r requirements.txt
```
5. Выполните миграции БД:
```bash
python manage.py migrate
```
6. Запустите сервер:
```bash
python manage.py runserver
```

Backend будет доступен по адресу:

http://127.0.0.1:8000/


# 📱 Frontend (Kotlin + Android)
📌 Технологии

Kotlin

Jetpack Compose

MVVM

OkHttp

Navigation Compose

▶️ Как запустить Android-приложение

Откройте папку:

Velox/frontend/


в Android Studio

Дождитесь загрузки Gradle

Выберите виртуальное устройство или подключите телефон

Нажмите Run ▶️

После этого приложение запустится на Androi
