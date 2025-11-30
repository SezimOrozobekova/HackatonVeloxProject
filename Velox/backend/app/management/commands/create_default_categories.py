from django.core.management.base import BaseCommand
from app.models import UserAccount, Category

class Command(BaseCommand):
    help = "Create default categories for all users without categories"

    def handle(self, *args, **kwargs):
        default_categories = ["Work", "Study", "Personal", "Shopping", "Health", "Home", "Other"]

        users = UserAccount.objects.all()
        for user in users:
            existing_categories = Category.objects.filter(user=user).values_list('name', flat=True)
            missing_categories = [cat for cat in default_categories if cat not in existing_categories]

            for category_name in missing_categories:
                Category.objects.create(name=category_name, user=user)
                self.stdout.write(f"Created category '{category_name}' for user {user.email}")

        self.stdout.write("Default categories created for all users where needed.")
