from django.db import models
from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.core.exceptions import ValidationError
from django.utils import timezone
from django.contrib.auth.models import Group, Permission
from datetime import timedelta
from django.conf import settings



class UserAccountManager(BaseUserManager):
    def create_user(self, email, name, password=None):
        if not email:
            raise ValueError('Users must have an email address')

        email = self.normalize_email(email)
        user = self.model(email=email, name=name)
        user.set_password(password)
        user.save()

        default_categories = [
            "Work", "Study", "Personal", "Shopping", "Health", "Home", "Other"
        ]

        for category_name in default_categories:
            Category.objects.create(name=category_name, user=user)

        return user

    def create_superuser(self, email, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)

        if extra_fields.get('is_staff') is not True:
            raise ValueError('Superuser must have is_staff=True.')
        if extra_fields.get('is_superuser') is not True:
            raise ValueError('Superuser must have is_superuser=True.')

        return self.create_user(email, password, **extra_fields)

class UserAccount(AbstractBaseUser, PermissionsMixin):
    email = models.EmailField(max_length=255, unique=True)
    name = models.CharField(max_length=255)
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)

    google_access_token = models.TextField(blank=True, null=True)
    google_refresh_token = models.TextField(blank=True, null=True)
    google_token_expires = models.DateTimeField(blank=True, null=True)
    
    wake_up_time = models.TimeField(null=True, blank=True)
    sleep_time = models.TimeField(null=True, blank=True)


    objects = UserAccountManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['name']

    def get_full_name(self):
        return self.name

    def get_short_name(self):
        return self.name

    def __str__(self):
        return self.email

    def is_google_token_valid(self):
        return self.google_token_expires and self.google_token_expires > timezone.now()



class Category(models.Model):
    user = models.ForeignKey(UserAccount, on_delete=models.CASCADE)
    name = models.CharField(max_length=100)

    class Meta:
        unique_together = ('user', 'name')





class Task(models.Model):
    FREQUENCY_CHOICES = [
        ('none', 'None'),
        ('daily', 'Every Day'),
        ('weekly', 'Every Week'),
        ('monthly', 'Every Month'),
        ('yearly', 'Every Year'),
    ]

    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='tasks')
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True, related_name='tasks')

    title = models.CharField(max_length=200)
    category = models.CharField(max_length=100, blank=True)
    date = models.DateField()
    time_start = models.TimeField(null=True, blank=True)
    time_end = models.TimeField(null=True, blank=True)
    reminder = models.BooleanField(default=False)
    location = models.CharField(max_length=250, blank=True)
    notes = models.TextField(blank=True)
    frequency = models.CharField(max_length=10, choices=FREQUENCY_CHOICES, default='none')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-date', 'time_start']

    def clean(self):
        if self.time_start and self.time_end and self.time_end <= self.time_start:
            raise ValidationError({'time_end': 'Время окончания должно быть позднее времени начала.'})

    def __str__(self):
        if self.time_start and self.time_end:
            return f"{self.title} @ {self.date} {self.time_start}-{self.time_end}"
        if self.time_start:
            return f"{self.title} @ {self.date} {self.time_start}"
        return f"{self.title} @ {self.date}"



class GoogleCredentials(models.Model):
    user = models.OneToOneField(UserAccount, on_delete=models.CASCADE)
    access_token = models.TextField()
    refresh_token = models.TextField(null=True, blank=True)
    token_expiry = models.DateTimeField(null=True, blank=True)
    token_uri = models.CharField(max_length=255, default='https://oauth2.googleapis.com/token')
    client_id = models.CharField(max_length=255)
    client_secret = models.CharField(max_length=255)
    scopes = models.TextField(default='https://www.googleapis.com/auth/calendar')

    def __str__(self):
        return f"Google Credentials for {self.user.email}"
    


