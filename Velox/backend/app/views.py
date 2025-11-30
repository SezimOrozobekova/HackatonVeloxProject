from django.contrib.auth.tokens import default_token_generator
from django.shortcuts import render
from django.utils.http import urlsafe_base64_decode
from django.views import View
from django.http import JsonResponse, HttpResponseBadRequest, HttpResponse
from django.conf import settings
from django.utils import timezone

from rest_framework import viewsets, filters, status
from rest_framework.permissions import IsAuthenticated
from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework.views import APIView
from rest_framework.response import Response

from google_auth_oauthlib.flow import Flow
from google.oauth2.credentials import Credentials
from google.auth.transport.requests import Request
from googleapiclient.discovery import build

import requests
import json
import datetime
import re

from openai import OpenAI

from .models import Task, GoogleCredentials, UserAccount, Category
from .serializers import CategorySerializer, UserTimeSerializer, TaskSerializer, User

#for activation
from django.contrib.auth import get_user_model
from django.utils.http import urlsafe_base64_decode
from django.contrib.auth.tokens import default_token_generator
from django.http import HttpResponse, HttpResponseBadRequest
from django.views import View

#code for activation
User = get_user_model()

class ActivateUserView(View):
    def get(self, request, uidb64, token):
        try:
            uid = int(urlsafe_base64_decode(uidb64).decode())
            user = User.objects.get(pk=uid)
        except (TypeError, ValueError, OverflowError, User.DoesNotExist):
            user = None

        if user is not None and default_token_generator.check_token(user, token):
            if not user.is_active:
                user.is_active = True
                user.save()
                return HttpResponse("✅ Account activated!")
            else:
                return HttpResponse("ℹ️ Account was activated already.")
        else:
            return HttpResponseBadRequest("❌ Url is old.")

class CategoryViewSet(viewsets.ModelViewSet):  
    queryset = Category.objects.all()
    serializer_class = CategorySerializer
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTAuthentication]

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    def get_queryset(self):
        return Category.objects.filter(user=self.request.user)


class UpdateUserTimeView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        serializer = UserTimeSerializer(request.user, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response({'message': 'Time updated successfully'})
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    

class TaskViewSet(viewsets.ModelViewSet):
  serializer_class = TaskSerializer
  authentication_classes = [JWTAuthentication]
  permission_classes = [IsAuthenticated]


  filter_backends = [filters.SearchFilter, filters.OrderingFilter]
  search_fields = ['title', 'category', 'notes']
  ordering_fields = ['date', 'time_start', 'created_at']

  def get_queryset(self):
        return Task.objects.filter(user=self.request.user)


  def perform_create(self, serializer):
      task = serializer.save(user=self.request.user)

      try:
          creds_data = GoogleCredentials.objects.get(user=self.request.user)
      except GoogleCredentials.DoesNotExist:
          print("❌ Нет Google токенов для пользователя.")
          return

      creds = Credentials(
          token=creds_data.access_token,
          refresh_token=creds_data.refresh_token,
          token_uri=creds_data.token_uri,
          client_id=creds_data.client_id,
          client_secret=creds_data.client_secret,
          scopes=creds_data.scopes.split(',')
      )

      if creds.expired and creds.refresh_token:
          try:
              creds.refresh(Request())
              creds_data.access_token = creds.token
              creds_data.token_expiry = creds.token_expiry or creds.expiry
              creds_data.save()
          except Exception as e:
              print("❌ Error:", str(e))
              raise

      try:
          create_event_in_google_calendar(task, creds.token)
          print(f"✅ Task is created {task.title}")
      except Exception as e:
          print(f"❌ Error while creaing task: {str(e)}")
          raise 


from datetime import datetime, timedelta
from .models import Category

class OpenAITextProcessView(APIView):
    authentication_classes = [JWTAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request):
        now = datetime.now()
        current_date = now.strftime("%Y-%m-%d")
        default_start = (now + timedelta(hours=1)).strftime("%H:%M:%S")
        default_end = (now + timedelta(hours=2)).strftime("%H:%M:%S")

        categories = Category.objects.filter(user=request.user)
        category_mapping = {cat.name.lower(): cat.id for cat in categories}
        allowed_categories_str = ", ".join(f'"{name}"' for name in category_mapping.keys())

        user_text = request.data.get('text')
        if not user_text:
            return Response({"error": "No text provided"}, status=status.HTTP_400_BAD_REQUEST)

        prompt = f"""
Extract task data in JSON format with fields:
title, category, date (YYYY-MM-DD), time_start (HH:MM:SS), time_end (HH:MM:SS), reminder (true/false), location, notes, frequency

Frequency must be one of: none, daily, weekly, monthly, yearly.

Text:
{user_text}

Respond with ONLY a valid JSON object without any markdown code blocks or additional text.
If frequency cannot be determined confidently, use "none".

Today's day of week, date and time is {now.strftime('%A')}, {now}.

Rules for the "title":
- Title must be a short but meaningful summary of the task (1–6 words).
- It MUST capture the main action + its target or purpose.
- Include important context if it changes the meaning (e.g., "Запись к стоматологу", "Оплатить аренду", "Созвон с командой").
- Do NOT include unnecessary long descriptions, but keep essential information that defines the task.


Only use one of the following category names:
[{allowed_categories_str}]

Do not invent new categories. Use names exactly as listed.

Rules for the "notes":
- Include all specific details like names, places, people, or extra context.
- E.g., if the input is "Dinner with Mother and Alessia", notes should be "Dinner with Mother and Alessia".

If no time mentioned:
- Set date to current date ({current_date}),
- Set time_start to one hour from now ({default_start}),
- Set time_end to one hour after time_start.

IF TEXT INPUT IN RUSSIAN TASK SHOULD BE IN RUSSIAN

"""

        client = OpenAI(api_key=settings.OPENAI_API_KEY)

        try:
            completion = client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[{"role": "user", "content": prompt}],
                temperature=0,
                max_tokens=300,
            )
            response_text = completion.choices[0].message.content.strip()
            cleaned_text = re.sub(r"```jsons*|s*```", "", response_text).strip()

            try:
                parsed_data = json.loads(cleaned_text)
            except json.JSONDecodeError:
                return Response({"error": "Invalid JSON from OpenAI", "raw_response": response_text}, status=500)

            # 🛡️ Обработка категории
            category_name = parsed_data.get("category", "").strip().lower()
            if not category_name or category_name not in category_mapping:
                if "other" in category_mapping:
                    parsed_data["category"] = category_mapping["other"]
                else:
                    return Response({"error": "Category not recognized and 'Other' category not found."}, status=400)
            else:
                parsed_data["category"] = category_mapping[category_name]

            # Проверка frequency
            allowed_frequencies = {choice[0] for choice in Task.FREQUENCY_CHOICES}
            frequency = parsed_data.get("frequency", "").strip().lower()
            if frequency not in allowed_frequencies:
                parsed_data["frequency"] = "none"
            else:
                parsed_data["frequency"] = frequency

            # ⏰ Обработка даты и времени
            date = parsed_data.get("date", "").strip()
            time_start = parsed_data.get("time_start", "").strip()
            time_end = parsed_data.get("time_end", "").strip()

            if not date:
                parsed_data["date"] = current_date
            if not time_start:
                parsed_data["time_start"] = default_start
            if not time_end:
                parsed_data["time_end"] = default_end

            return Response(parsed_data)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)




class GoogleOAuthInitView(APIView):
    def get(self, request):
        flow = Flow.from_client_config(
            {
                "web": {
                    "client_id": settings.GOOGLE_CLIENT_ID,
                    "client_secret": settings.GOOGLE_CLIENT_SECRET,
                    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                    "token_uri": "https://oauth2.googleapis.com/token",
                    "redirect_uris": [settings.GOOGLE_REDIRECT_URI],
                }
            },
            scopes=settings.GOOGLE_OAUTH_SCOPES,
        )
        flow.redirect_uri = settings.GOOGLE_REDIRECT_URI

        authorization_url, state = flow.authorization_url(
            access_type='offline',
            include_granted_scopes='true',
            prompt='consent',
        )

        request.session['google_oauth_state'] = state
        return Response({'auth_url': authorization_url})



class GoogleAuthAndroidCallbackView(View):
    def post(self, request):
        try:
            body = json.loads(request.body)
            code = body.get('code')
            if not code:
                return HttpResponseBadRequest("Missing 'code'")
        except Exception:
            return HttpResponseBadRequest("Invalid JSON")
        
        token_url = 'https://oauth2.googleapis.com/token'
        data = {
            'code': code,
            'client_id': settings.GOOGLE_CLIENT_ID,
            'client_secret': settings.GOOGLE_CLIENT_SECRET,
            'redirect_uri': '',  # Для Android обычно пусто
            'grant_type': 'authorization_code',
        }
        response = requests.post(token_url, data=data)
        if response.status_code != 200:
            return HttpResponseBadRequest(response.text)

        tokens = response.json()
        # Здесь сохраняете токены в БД или возвращаете клиенту
        return JsonResponse(tokens)



class SaveGoogleTokensView(APIView):
    authentication_classes = [JWTAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        data = request.data

        access_token = data.get('access_token')
        refresh_token = data.get('refresh_token')
        expires_in = data.get('expires_in')
        token_uri = 'https://oauth2.googleapis.com/token'
        client_id = settings.GOOGLE_CLIENT_ID
        client_secret = settings.GOOGLE_CLIENT_SECRET
        scopes = 'openid,email,profile,https://www.googleapis.com/auth/calendar'
        expiry_date = timezone.now() + datetime.timedelta(seconds=expires_in)

        if not access_token or not refresh_token:
            return Response({'error': 'Tokens are required'}, status=status.HTTP_400_BAD_REQUEST)

        GoogleCredentials.objects.update_or_create(
            user=user,
            defaults={
                'access_token': access_token,
                'refresh_token': refresh_token,
                'token_expiry': expiry_date,
                'token_uri': token_uri,
                'client_id': client_id,
                'client_secret': client_secret,
                'scopes': scopes,
            }
        )

        return Response({'message': 'Google tokens saved successfully'})



User = get_user_model()

