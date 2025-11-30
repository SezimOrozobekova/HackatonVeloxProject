from pathlib import Path
import os
from datetime import timedelta
from .keys import GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, OPENAI_API_KEY, GOOGLE_REDIRECT_URI, SECRET_KEY
from .keys import EMAIL_BACKEND, EMAIL_HOST, EMAIL_HOST_PASSWORD, EMAIL_HOST_USER, EMAIL_PORT, EMAIL_USE_TLS


BASE_DIR = Path(__file__).resolve().parent.parent


EMAIL_BACKEND = EMAIL_BACKEND
EMAIL_HOST = EMAIL_HOST
EMAIL_HOST_PASSWORD = EMAIL_HOST_PASSWORD
EMAIL_HOST_USER = EMAIL_HOST_USER
EMAIL_PORT = EMAIL_PORT
EMAIL_USE_TLS = EMAIL_USE_TLS

SECRET_KEY = SECRET_KEY

GOOGLE_CLIENT_ID = GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET = GOOGLE_CLIENT_SECRET
GOOGLE_REDIRECT_URI = GOOGLE_REDIRECT_URI

GOOGLE_OAUTH_SCOPES = ['openid', 'email', 'profile', 'https://www.googleapis.com/auth/calendar']
GOOGLE_SCOPES = ['https://www.googleapis.com/auth/calendar']

OPENAI_API_KEY = OPENAI_API_KEY

DEBUG = True

ALLOWED_HOSTS = []


INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'rest_framework',
    'djoser',
    'app',
    'social_django',
    'rest_framework_simplejwt',
    'django_extensions',
    'rest_framework_simplejwt.token_blacklist'
]


MIDDLEWARE = [
    'social_django.middleware.SocialAuthExceptionMiddleware',
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'project.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [os.path.join(BASE_DIR, 'build')],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
                'social_django.context_processors.backends',
                'social_django.context_processors.login_redirect',
            ],
        },
    },
]

WSGI_APPLICATION = 'project.wsgi.application'


DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': 'auth_system',
        'USER': 'postgres',
        'PASSWORD': '1234',
        'HOST': 'localhost',
        'PORT': '5432',
    }
}




EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
EMAIL_HOST = 'smtp.gmail.com'
EMAIL_PORT = 587
EMAIL_USE_TLS = True
EMAIL_HOST_USER = 'kozhamuratovaaizhan@gmail.com'
EMAIL_HOST_PASSWORD = 'ukcncbgoyddcpssu'




AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_TZ = True


STATIC_URL = 'static/'

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': (
        'rest_framework_simplejwt.authentication.JWTAuthentication',
    ),
    'DEFAULT_RENDERER_CLASSES': (
        'rest_framework.renderers.JSONRenderer',
        'rest_framework.renderers.BrowsableAPIRenderer',  # Для красивой HTML-страницы в браузере
    ),
}

AUTHENTICATION_BACKENDS = (
  'social_core.backends.google.GoogleOAuth2',
  'django.contrib.auth.backends.ModelBackend'
)

SIMPLE_JWT = {
   'AUTH_HEADER_TYPES': ('JWT',),
   'ACCESS_TOKEN_LIFETIME': timedelta(minutes=60),
   'REFRESH_TOKEN_LIFETIME': timedelta(days=1),
   'AUTH_TOKEN_CLASSES': (
     'rest_framework_simplejwt.tokens.AccessToken',
   )
}

DJOSER = {
  'LOGIN_FIELD': 'email',
  'USER_CREATE_PASSWORD_RETYPE': True,
  'USERNAME_CHANGED_EMAIL_CONFIRMATION': True,
  'PASSWORD_CHANGED_EMAIL_CONFIRMATION': True,
  'SEND_CONFIRMATION_EMAIL': True,
  'SET_USERNAME_RETYPE': True,
  'SET_PASSWORD_RETYPE': True,
  'PASSWORD_RESET_CONFIRM_URL': 'password-reset/{uid}/{token}',
  'USERNAME_RESET_CONFIRM_URL': 'email/{uidb64}/{token}',
  'ACTIVATION_URL': 'activate/{uid}/{token}',
  'SEND_ACTIVATION_EMAIL': True,
  'SERIALIZERS': {
    'user_create': 'app.serializers.UserCreateSerializer',
    'user': 'app.serializers.UserCreateSerializer',
    'current_user': 'app.serializers.UserCreateSerializer',
    'user_delete': 'djoser.serializers.UserDeleteSerializer',
  }
}

AUTH_USER_MODEL = 'app.UserAccount'

ALLOWED_HOSTS = ['localhost', '127.0.0.1', '192.168.185.202', '*']
