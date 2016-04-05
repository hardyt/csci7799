################################################################################
# settings.py
# Greg Bayer <greg@gbayer.com>.
################################################################################

#############################################
# Calculate LOCALHOST flag
#############################################
import os, socket
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

LOCALHOST = False
try:
    if socket.gethostname().find('local') != -1:
        LOCALHOST = True
    else:
        LOCALHOST = False
except:
    if ('Development' in os.environ['SERVER_SOFTWARE']):
        LOCALHOST = True
    else:
        LOCALHOST = False


GCM_CONFIG = {'gcm_api_key': 'AIzaSyAUfv7q8YdmzQU-5uQalaJJuTQPH9tVKRQ',
#              'delete_bad_token_callback_func': 'EXAMPLE_MANAGE_TOKENS_MODULE.delete_bad_gcm_token',
#              'update_token_callback_func': 'EXAMPLE_MANAGE_TOKENS_MODULE.update_gcm_token',
              }