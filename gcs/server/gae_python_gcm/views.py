################################################################################
# gae_python_gcm/views.py
# Greg Bayer <greg@gbayer.com>
################################################################################

import logging
from django.http import HttpResponse

from gae_python_gcm.gcm import GCMConnection, GCMMessage
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

# # Try sending a single queued message (called by task queue for every queued message)
# def send_request_handler(request):
#     try:
#         device_tokens = request.REQUEST.get('device_tokens')
#         notification = request.REQUEST.get('notification')
#         collapse_key = request.REQUEST.get('collapse_key')
#         delay_while_idle = request.REQUEST.get('delay_while_idle')
#         time_to_live = request.REQUEST.get('time_to_live')
#
#
#         if ',' in device_tokens:
#             device_tokens = device_tokens.split(',')
#         else:
#             device_tokens = [device_tokens]
#         message = GCMMessage(device_tokens, notification, collapse_key, delay_while_idle, time_to_live)
#
#         gcm_connection = GCMConnection()
#         gcm_connection._send_request(message)
#     except:
#         logging.exception('Error in send_request_handler')
#         logging.info('message: ' + repr(request))
#
#     return HttpResponse()

# Try sending a single queued message (called by task queue for every queued message)
class send_request_handler(webapp.RequestHandler):
    def post(self):
        try:
            device_tokens = self.request.get('device_tokens')
            notification = self.request.get('notification')
            collapse_key = self.request.get('collapse_key')
            delay_while_idle = self.request.get('delay_while_idle')
            time_to_live = self.request.get('time_to_live')


            if ',' in device_tokens:
                device_tokens = device_tokens.split(',')
            else:
                device_tokens = [device_tokens]
            message = GCMMessage(device_tokens, notification, collapse_key, delay_while_idle, time_to_live)

            gcm_connection = GCMConnection()
            gcm_connection._send_request(message)
        except:
            logging.exception('Error in send_request_handler')
            logging.info('message: ' + repr(self.request))

        return HttpResponse()


# Get debug stats
def debug_handler(request):
    try:
        gcm_connection = GCMConnection()
        output = gcm_connection.debug('stats')
        output = output.replace('\n', '<br>\n')
        return HttpResponse(output)
    except:
        logging.exception('Error in debug_handler')

    return HttpResponse()

application = webapp.WSGIApplication(
	[('/gae_python_gcm/send_request_handler', send_request_handler),
	], debug=True)

def main():
    run_wsgi_app(application)

if __name__ == '__main__':
    main()