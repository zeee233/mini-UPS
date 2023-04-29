from django.db import models
from django.contrib.auth.models import User

# Create your models here.

class PackageD(models.Model):
    package_id = models.BigIntegerField()
    warehouse_id = models.IntegerField()
    warehouse_x = models.IntegerField()
    warehouse_y = models.IntegerField()
    destination_x = models.IntegerField()
    destination_y = models.IntegerField()
    ups_id = models.CharField(max_length=255, null=True, blank=True)
    status = models.CharField(max_length=255)
    truck_id = models.IntegerField(null=True, blank=True)
    detail = models.TextField(null=True, blank=True)
    class Meta:
        managed = False
        db_table = 'package'
        app_label = 'ups'

class UGoDeliverD(models.Model):
    truck_id = models.IntegerField(null=False)
    seq_num = models.BigIntegerField(null=False)
    class Meta:
        managed = False
        db_table = 'u_go_deliver'
        app_label = 'ups'

class UDeliveryLocationD(models.Model):
    package_id = models.BigIntegerField(null=False)
    x = models.IntegerField(null=False)
    y = models.IntegerField(null=False)
    u_go_deliver = models.ForeignKey(UGoDeliverD, on_delete=models.CASCADE, related_name='packages')
    class Meta:
        managed = False
        db_table = 'u_delivery_location'
        app_label = 'ups'