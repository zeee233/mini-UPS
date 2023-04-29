from django.urls import path

from . import views

urlpatterns = [
    path('', views.main_page, name='main_page'),#same for home
    path('sign_in/', views.sign_in, name='sign_in'),
    path('sign_up/', views.sign_up, name='sign_up'),
    path('search_packages/', views.search_packages, name='search_packages')
]
