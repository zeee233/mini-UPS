from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.hashers import check_password

from .models import *

# Create your views here.
def sign_in(request):
    if(request.method=="POST"):
        email = request.POST.get("user_email")
        password = request.POST.get("user_password")
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            user = None

        print(user)
        if user is not None and check_password(password, user.password):
            login(request, user)

            return render(request, 'ride_request.html', {'user': user})
        else:
            print("////")
            return render(request, 'sign_in.html', {'error': 'Email does not exist or password is incorrect!'})
    elif request.method=="GET":
        return render(request, 'sign_in.html')

def sign_up(request):
    if(request.method=="POST"):
        name = request.POST["user_name"]
        email = request.POST["user_email"]
        password = request.POST['user_password']
        password2 = request.POST['user_password2']
        if password == password2:
            # Check if email already exists
            if User.objects.filter(email=email).exists():
                return render(request, 'sign_up.html', {'error': 'Email already in use'})
            else:
                # Create the user
                user = User(username=name, email=email)
                user.set_password(password)
                user.save()

                return redirect('/sign_in')
        else:
            return render(request, 'sign_up.html', {'error': 'Passwords do not match'})
    elif(request.method=="GET"):
        return render(request, 'sign_up.html')

def main_page(request):
    return render(request, 'main_page.html')
