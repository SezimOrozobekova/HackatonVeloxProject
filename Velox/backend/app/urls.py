from rest_framework.routers import DefaultRouter
from .views import TaskViewSet, CategoryViewSet, UpdateUserTimeView, OpenAITextProcessView, GoogleOAuthInitView, SaveGoogleTokensView, GoogleAuthAndroidCallbackView
from django.urls import path

router = DefaultRouter()
router.register(r'tasks', TaskViewSet, basename='task')
router.register(r'categories', CategoryViewSet, basename='category')

urlpatterns = router.urls + [
    path('process-text/', OpenAITextProcessView.as_view(), name='process-text'),
    path('google/token_save/', SaveGoogleTokensView.as_view(), name='google-token-save'),
    path('google/auth1/', GoogleOAuthInitView.as_view(), name='google-oauth-init'),
    # path('google/callback/', GoogleOAuthCallbackView.as_view(), name='google-oauth-callback'),
    path('google/auth/android/callback/', GoogleAuthAndroidCallbackView.as_view(), name='google-android-callback'),
    path('user/time/', UpdateUserTimeView.as_view(), name='update-user-time'),
]
