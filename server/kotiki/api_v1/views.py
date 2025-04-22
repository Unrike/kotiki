from ninja import NinjaAPI, Schema, errors
from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from django.db import IntegrityError
from typing import Optional
from django.db.models import Q

from .models import Mail


api = NinjaAPI()

class LoginSchema(Schema):
    login: str
    password: str

@api.post('/login')
def login(request, data: LoginSchema):
    user = authenticate(username = data.login, password = data.password)
    if user is None:
        raise errors.HttpError(401, "Unauthorized")
    return 200, "ok"


@api.post('/register')
def register(request, data: LoginSchema):
    try:
        User.objects.create(username= data.login, password = data.password)
    except IntegrityError:
        raise errors.HttpError(403, "User already exists")
    return 200, "ok"


class SendSchema(Schema):
    sender: str
    receiver: str
    message: str

@api.post('/send')
def send(request, data: SendSchema):
    try:
        sender = User.objects.get(username = data.sender)
        receiver = User.objects.get(username = data.receiver)
    except User.DoesNotExist:
        raise errors.HttpError(400, "User not exists")
    Mail.objects.create(sender = sender, receiver = receiver, message = data.message)
    return 200, 'ok'



@api.get('/receive/{user}')
def receive(request, user: str):
    try:
        user = User.objects.get(username = user)
    except User.DoesNotExist:
        raise errors.HttpError(401, "Unknown user")
    messages = Mail.objects.filter(Q(sender = user) | Q(receiver = user)) 
    ret = []
    for m in messages:
        ret.append({'sender': m.sender.username,
                    'receiver': m.receiver.username,
                    'date': m.date,
                    'message':m.message})
    return ret

@api.get('/users/{user}')
def get_users(request, user: str):
    users = User.objects.exclude(username=user)
    res = []
    for u in users:
        res.append(u.username)
    return res