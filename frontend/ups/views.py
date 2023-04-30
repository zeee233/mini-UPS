from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.hashers import check_password
from django.db import transaction
from django.db.models import Max

from .models import *

# Create your views here.
def sign_in(request):
    if(request.method=="POST"):
        email = request.POST.get("user_email")
        password = request.POST.get("user_password")
        try:
            temp_user = User.objects.get(email=email)
            user = authenticate(request, username=temp_user.username, password=password)
        except User.DoesNotExist:
            user = None
        
        print(user)
        if user is not None and check_password(password, user.password):
            backend = user.backend

            login(request, user, backend=backend)

            return render(request, 'search_packages.html', {'user': user})
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
    return render(request, 'search_packages.html')

def search_packages(request):
    return render(request, 'search_packages.html')


def search_package_id(request):
    records = None
    if request.method=="POST":
        packageID = request.POST["packageid"]
        records = PackageD.objects.filter(package_id=packageID)
        print(records)
        return render(request, 'search_packages.html', {'records': records})
    return render(request, 'search_packages.html', {'records': records})


def package_info(request):
    if request.method == "GET":
        packageid = request.GET.get('package_id')
        print("packageid: ",packageid)
        package = PackageD.objects.get(package_id=packageid)

    if request.method == "POST":
        destination_x = int(request.POST["destination_x"])
        destination_y = int(request.POST["destination_y"])

        packageid = request.POST["package_id"]
        package = PackageD.objects.get(package_id=packageid)
        if package.status == "delivering":
            with transaction.atomic():
                try:
                    u_delivery_location = UDeliveryLocationD.objects.select_for_update().get(package_id=packageid)

                    # Update the existing UDeliveryLocationD record
                    u_delivery_location.x = destination_x
                    u_delivery_location.y = destination_y
                    u_delivery_location.save()

                except UDeliveryLocationD.DoesNotExist:
                    pass

                # Find the max seq_num in the database and lock the SeqNumD table
                max_seq_num_entry = SeqNumD.objects.select_for_update().aggregate(Max('seq_num'))
                max_seq_num = max_seq_num_entry['seq_num__max']

                # Increment seq_num by 1
                if max_seq_num is not None:
                    new_seq_num = max_seq_num + 1
                else:
                    new_seq_num = 1

                # Save the new seq_num in the SeqNumD table
                seq_num_entry = SeqNumD(seq_num=new_seq_num)
                seq_num_entry.save()

                # Create new UGoDeliverD and UDeliveryLocationD records
                u_go_deliver = UGoDeliverD(truck_id=package.truck_id, seq_num=new_seq_num)
                u_go_deliver.save()

                u_delivery_location = UDeliveryLocationD(package_id=packageid, x=destination_x, y=destination_y, u_go_deliver=u_go_deliver)
                u_delivery_location.save()

                package.destination_x = destination_x
                package.destination_y = destination_y
                package.save()
        # Redirect to the same page to avoid resubmitting the form
        return render(request, 'package_info.html', {'package': package})
    
    return render(request, 'package_info.html', {'package': package})

def account_info(request):
    user = User.objects.get(email=request.user.email)

    return render(request, 'account_info.html', {'user': user})


def user_packages(request):
    user = User.objects.get(email=request.user.email)
    name = user.username
    records = PackageD.objects.filter(ups_id=name)

    return render(request, 'user_packages.html', {'records': records})

def sign_out(request):
    logout(request)
    return redirect('/')