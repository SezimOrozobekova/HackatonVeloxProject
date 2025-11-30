
from django.urls import path, include, re_path
from django.views.generic import TemplateView
from django.contrib import admin
from django.urls import path
from app.views import ActivateUserView

urlpatterns = [
    path('auth/', include('djoser.urls')),
    path('auth/', include('djoser.urls.jwt')),
    path('admin/', admin.site.urls),
    path('api/', include('app.urls')),
    path('activate/<uidb64>/<token>/', ActivateUserView.as_view(), name='activate'),
]

