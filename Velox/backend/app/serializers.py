from rest_framework import serializers
from .models import Task, Category, UserAccount
from django.utils import timezone
from djoser.serializers import UserCreateSerializer
from django.contrib.auth import get_user_model

User = get_user_model()

class UserCreateSerializer(UserCreateSerializer):
    class Meta(UserCreateSerializer.Meta):
        model = User
        fields = ('id', 'email', 'name', 'password')

class UserTimeSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['name', 'wake_up_time', 'sleep_time']



class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ['id', 'name']


class TaskSerializer(serializers.ModelSerializer):
    
    category = serializers.PrimaryKeyRelatedField(
        queryset=Category.objects.all(), required=False, allow_null=True
    )

    class Meta:
        model = Task
        fields = '__all__'
        read_only_fields = ['user']




    def create(self, validated_data):
        category = validated_data.pop('category', None)
        user = self.context['request'].user

        # Проверяем, если у категории есть поле user и оно не совпадает с текущим пользователем
        if category and hasattr(category, 'user') and category.user != user:
            raise serializers.ValidationError("Category does not belong to the user")

        validated_data['category'] = category
        validated_data['user'] = user
        return Task.objects.create(**validated_data)

    def update(self, instance, validated_data):
        category = validated_data.pop('category', None)
        user = self.context['request'].user

        if category and hasattr(category, 'user') and category.user != user:
            raise serializers.ValidationError("Category does not belong to the user")

        if category is not None:
            instance.category = category

        for attr, value in validated_data.items():
            setattr(instance, attr, value)

        instance.save()
        return instance


class UserTimeSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserAccount
        fields = ['wake_up_time', 'sleep_time']