from django.db import models
from django.conf import settings
from django.contrib.auth.models import User

# Create your models here.
class Mail(models.Model):
    sender = models.ForeignKey(User, on_delete=models.CASCADE, related_name='sender')
    receiver = models.ForeignKey(User, on_delete=models.CASCADE, related_name='receiver')
    date = models.DateTimeField(auto_now_add=True)
    message = models.CharField(max_length=20)

