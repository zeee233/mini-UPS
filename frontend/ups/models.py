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
        db_table = 'package'