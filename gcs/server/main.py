#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import webapp2
import logging

from gae_python_gcm.gcm import GCMConnection, GCMMessage

class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.response.write('Welcome to Thomas M. Hardy .com!')

class send_request_handler(webapp2.RequestHandler):
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

            logging.info('message: ' + repr(message))

            gcm_connection = GCMConnection()
            gcm_connection._send_request(message)
        except:
            logging.exception('Error in send_request_handler')
            logging.info('message: ' + repr(self.request))

        return False

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/gae_python_gcm/send_request', send_request_handler),
], debug=True)

