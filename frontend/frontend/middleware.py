from django.conf import settings

class SiteConfigurationMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        if "apple" in request.path:
            settings.ACCOUNT_DEFAULT_HTTP_PROTOCOL = 'https'
        elif "google" in request.path:
            settings.ACCOUNT_DEFAULT_HTTP_PROTOCOL = 'http'
        
        response = self.get_response(request)
        return response
